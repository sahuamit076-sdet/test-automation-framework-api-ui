package in.zeta.qa.utils.db;

import in.zeta.qa.constants.anotation.SqlQuery;
import in.zeta.qa.constants.anotation.Table;
import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.misc.AllureLoggingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DynamicSqlQueryHandler {


    private static final Logger LOG = LogManager.getLogger(DynamicSqlQueryHandler.class);

    private DynamicSqlQueryHandler()   {
        //Private Constructor
    }

    public static DynamicSqlQueryHandler getInstance() {
        return SingletonFactory.getInstance(DynamicSqlQueryHandler.class);
    }

    /**
     * Caller Method Should have @SqlQuery Annotation
     *
     * @param entityClass with @Table Annotation
     * @param args
     * @param <T>
     * @return
     */
    public <T> List<T> buildQueryAndExecute(Class<T> entityClass, Object... args) {
        try {
            //Build Query
            String query = buildQuery(entityClass, args);
            LOG.info("QUERY :: {}", query);
            AllureLoggingUtils.logsToAllureReport("Query :: " + query);
            //Execute Query
            return RedshiftUtil.getInstance().executeAndGetResult(entityClass, query);
        } catch (Exception e) {
            throw new RuntimeException("Error building and executing query for method: ", e);
        }
    }


    /**
     * Caller Method Should have @SqlQuery Annotation
     *
     * @param entityClass with @Table Annotation
     * @param args
     * @param <T>
     * @return SQL QUERY
     */
    public <T> String buildQuery(Class<T> entityClass, Object... args) {
        try {
            // Step 1: Get the calling method with @SqlQuery
            Method method = resolveSqlQueryMethod();
            String queryTemplate = method.getAnnotation(SqlQuery.class).value();
            //Step 2:Update query with table name
            String tableName = entityClass.getAnnotation(Table.class).value();
            queryTemplate = queryTemplate.replace("{TABLE}", tableName);
            // Step 3: Extract placeholders
            List<String> placeholders = getPlaceHolders(queryTemplate);
            //Step 4: Validate args
            validateArgs(placeholders, args);
            // Step 5: Replace other placeholders in order
            return updateQueryWithArgs(queryTemplate, placeholders, args);
        } catch (Exception e) {
            throw new RuntimeException("Error building and executing query for method: ", e);
        }
    }

//######################################################################################################################
//######################################################################################################################
//######################################################################################################################

    private Method resolveSqlQueryMethod() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            try {
                Class<?> callerClass = Class.forName(element.getClassName());

                // Skip the class where this method is defined to avoid resolving itself
                if (callerClass.equals(this.getClass())) continue;

                for (Method m : callerClass.getDeclaredMethods()) {
                    if (m.getName().equals(element.getMethodName()) && m.isAnnotationPresent(SqlQuery.class)) {
                        return m;
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // Skip invalid stack trace elements
            }
        }
        throw new RuntimeException("Calling method with @SqlQuery annotation not found in stack trace.");
    }

    private List<String> getPlaceHolders(String queryTemplate) {
        Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)}");
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(queryTemplate);
        List<String> placeholders = new ArrayList<>();
        while (matcher.find()) {
            placeholders.add(matcher.group(0)); // full token including {}
        }
        return placeholders;
    }

    private void validateArgs(List<String> placeholders, Object... args) {
        // Step 4: Validate argument count
        // First parameter is always Class<T>, skip it
        if (args.length != placeholders.size()) {
            throw new IllegalArgumentException("Mismatch between number of placeholders and method arguments. " +
                    "Expected: " + placeholders.size() + ", Provided: " + (args.length - 1));
        }
    }

    private String updateQueryWithArgs(String queryTemplate, List<String> placeholders, Object... args) {
        int argIndex = 0;
        for (String placeholder : placeholders) {
            Object value = args[argIndex++];
            String replacementValue = value instanceof Collection<?> ?
                    ((Collection<?>) value).stream().map(Object::toString).map(s -> "'" + s + "'")
                            .collect(Collectors.joining(",")) : value.toString();
            queryTemplate = queryTemplate.replace(placeholder, replacementValue);
        }
        return queryTemplate;
    }
}
