package in.zeta.qa.utils.db;

import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.db.teleport.TeleportUtil;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.SneakyThrows;

public class PostgresSqlUtil extends DatabaseBaseUtil {

    private static String port;
    private static final String DB_NAME;

    static {
        port = TeleportUtil.getPort();
        DB_NAME = PropertyFileReader.getPropertyValue("postgres.db.name");
    }

    private PostgresSqlUtil() {
        // Initialize pool on singleton creation
        try {
            getConnection().close(); // ensures pool is built, release connection immediately
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Postgres connection pool", e);
        }
    }

    public static PostgresSqlUtil getInstance() {
        return SingletonFactory.getInstance(PostgresSqlUtil.class);
    }


    /**
     * @return
     */
    @Override
    protected String jdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", "localhost", port, DB_NAME);
    }

    /**
     * @return
     */
    @Override
    @SneakyThrows
    protected String username() {
        return PropertyFileReader.getPropertyValue("teleport.user.name");
    }

    /**
     * @return
     */
    @Override
    protected String password() {
        return "";
    }

    /**
     * @return
     */
    @Override
    protected String driverClassName() {
        return CommonConstants.POSTGRES_DRIVER;
    }
}
