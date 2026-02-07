package in.zeta.qa.utils.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.zeta.qa.constants.anotation.Column;
import in.zeta.qa.utils.misc.JsonHelper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public abstract class DatabaseBaseUtil {

    private static final Logger LOG = LogManager.getLogger(DatabaseBaseUtil.class);
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 5000; // 5 seconds

    // ===================================================================================
    // CONNECTION POOL MANAGEMENT
    // ===================================================================================
    protected abstract String jdbcUrl();

    protected abstract String username();

    protected abstract String password();

    protected abstract String driverClassName();


    private volatile HikariDataSource dataSource;

    // Initialize the connection pool with retry logic
    protected HikariConfig buildConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl());
        config.setUsername(username());
        config.setPassword(password());
        config.setDriverClassName(driverClassName());
        config.setMaximumPoolSize(15); // depends on max parallel threads
        config.setMinimumIdle(5);
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(3600000); // 1 hour
        config.setConnectionTimeout(60000);      // 60 sec
        config.setInitializationFailTimeout(-1);// to avoid fail-fast during startup
        config.setLeakDetectionThreshold(10_000); // logs unreturned connections
        config.setConnectionTestQuery("SELECT 1");  // Optional: validate connections before use
        return config;
    }

    private synchronized void initDataSourceWithRetry() throws SQLException {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                }
                dataSource = new HikariDataSource(buildConfig());
                LOG.info("Initialized HikariCP pool for DB: {}", dataSource.getJdbcUrl());
                return; // Success
            } catch (Exception e) {
                attempt++;
                LOG.error("Failed to initialize HikariCP pool (attempt {}/{}). Retrying in {} ms...",
                        attempt, MAX_RETRIES, RETRY_DELAY_MS, e);

                if (attempt >= MAX_RETRIES) {
                    LOG.error("Exceeded max retries to connect to DB. Failing.");
                    throw new SQLException("Could not initialize HikariCP pool", e);
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while retrying DB connection", ie);
                }
            }
        }
    }

    protected Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) { // double-checked locking
                    initDataSourceWithRetry(); // initialize once
                }
            }
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            Assert.fail("Failed to get connection after retry: " + ex.getMessage());
        }
        return null;
    }

    // ✅ Graceful shutdown
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOG.info("Closed HikariCP pool: {}", dataSource.getJdbcUrl());
        }
    }

    // ===================================================================================
    // QUERY EXECUTION
    // ===================================================================================

    protected JSONArray executeDbQuery(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return convertResultSetToJsonArray(rs);
        }
    }

    @SneakyThrows
    public JSONArray getDbData(String query) {
        LOG.info("Executing query: {}", query);
        return executeDbQuery(query);
    }

    public <T> List<T> executeQuery(String query, Class<T> entityClass) throws Exception {
        JSONArray queryResult = getDbData(query);
        return new JsonHelper().getObjectsFromString(queryResult.toString(), entityClass);
    }

    public <T> List<T> executeAndGetResult(Class<T> entityClass, String query) throws Exception {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return convertResultSetToPojo(entityClass, rs);
        }
    }

    // ===================================================================================
    // RESULTSET CONVERSION
    // ===================================================================================

    public static JSONArray convertResultSetToJsonArray(ResultSet resultSet) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            JSONObject jsonObject = new JSONObject();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = Optional.ofNullable(resultSet.getObject(i)).orElse("");
                jsonObject.put(columnName, value);
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public <T> List<T> convertResultSetToPojo(Class<T> entityClass, ResultSet result) throws Exception {
        List<T> allRows = new ArrayList<>();
        while (result.next()) {
            T row = entityClass.getDeclaredConstructor().newInstance();
            List<Field> fields = getAllFields(entityClass);

            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    String colName = field.getAnnotation(Column.class).value()[0];
                    try {
                        Object colValue = convertValue(result.getObject(colName), field.getType());
                        field.setAccessible(true);
                        field.set(row, colValue);
                    } catch (SQLException ignored) {
                        LOG.warn("Column '{}' not found in ResultSet for field '{}'", colName, field.getName());
                    }
                }
            }
            allRows.add(row);
        }
        return allRows;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(List.of(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private Object convertValue(Object value, Class<?> type) {
        if (value == null) return null;
        return switch (type.getSimpleName()) {
            case "Long" -> ((Number) value).longValue();
            case "Integer" -> ((Number) value).intValue();
            case "Double" -> ((Number) value).doubleValue();
            case "BigDecimal" -> (value instanceof BigDecimal bd) ? bd : new BigDecimal(value.toString());
            case "Boolean" -> (value instanceof Boolean b) ? b :
                    Set.of("true", "t", "1").contains(value.toString().trim().toLowerCase());
            case "LocalDate" -> (value instanceof Date d) ? d.toLocalDate() :
                    LocalDate.parse(value.toString());
            case "LocalDateTime" -> (value instanceof Timestamp ts) ? ts.toLocalDateTime() :
                    LocalDateTime.parse(value.toString());
            case "UUID" -> UUID.fromString(value.toString());
            default -> value.toString();
        };
    }
}
