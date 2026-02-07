package in.zeta.qa.utils.db;

import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RedshiftUtil extends DatabaseBaseUtil {
    private RedshiftUtil() {
    }

    public static RedshiftUtil getInstance() {
        return SingletonFactory.getInstance(RedshiftUtil.class);
    }

    @Override
    @SneakyThrows
    protected String jdbcUrl() {
        String url = PropertyFileReader.getPropertyValue("redshift.base.url");
        String port = PropertyFileReader.getPropertyValue("redshift.port");
        String dbName =PropertyFileReader.getPropertyValue("redshift.schema.name");
        String jdbc_url = String.format("jdbc:postgresql://%s:%s/%s", url, port, dbName);
        log.info("JDBC URL: {}", jdbc_url);
        return jdbc_url;
    }

    @Override
    @SneakyThrows
    protected String username() {
        return PropertyFileReader.getPropertyValue("redshift.user.name");
    }

    @Override
    @SneakyThrows
    protected String password() {
        return PropertyFileReader.getPropertyValue("redshift.user.password");
    }

    /**
     * @return
     */
    @Override
    protected String driverClassName() {
        return CommonConstants.POSTGRES_DRIVER;
    }

}
