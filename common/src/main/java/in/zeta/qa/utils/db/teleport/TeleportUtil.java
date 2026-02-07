package in.zeta.qa.utils.db.teleport;

import in.zeta.qa.utils.fileUtils.FileWriteHelper;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;


public class TeleportUtil {
    /**
     * This class provides helper methods to interact with the Teleport tunnel and database.
     * It includes methods to start the tunnel, connect to the database, check if a database exists,
     * and query data from a specific table.
     */
    private static final Logger LOG = LogManager.getLogger(TeleportUtil.class);
    private static final String IDENTITY_FILE_PATH = "./src/main/resources/teleport/identity.tar";


    public static void writeIdentityFile() {
        if (PropertyFileReader.getPropertyValue("IDENTITY_TAR_CONTENT") == null) {
            LOG.error("IDENTITY_TAR_CONTENT property is not set in the properties file.");
            LOG.info("Using the default IDENTITY_TAR_CONTENT file : " + IDENTITY_FILE_PATH);
        } else {
            FileWriteHelper.writeToFile(PropertyFileReader.getPropertyValue("IDENTITY_TAR_CONTENT"), IDENTITY_FILE_PATH);
        }
    }

    public static String getPort() {
        try {
            writeIdentityFile();
        } catch (Exception e) {
            LOG.error("Failed to write identity file for Teleport tunnel", e);
            return null;
        }
        String port = TeleportTunnel.startTunnel();
        if (port == null) {
            LOG.info("Port not found. Exiting...");
            Assert.fail("Port not found. Please check the Teleport tunnel setup.");
            return null;
        }
        return port;
    }
}
