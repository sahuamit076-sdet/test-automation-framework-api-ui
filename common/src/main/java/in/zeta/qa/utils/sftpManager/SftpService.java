package in.zeta.qa.utils.sftpManager;

import com.jcraft.jsch.JSchException;
import in.zeta.qa.utils.misc.AllureLoggingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SftpService {
    private final SftpClient sftpClient;
    private static final Logger log = LogManager.getLogger(SftpService.class);

    public SftpService(SftpClient sftpClient) {
        this.sftpClient = sftpClient;
    }

    public void uploadFile(String localFilePath) throws IOException {
        try {
            sftpClient.connect();
            sftpClient.uploadFile(localFilePath);
        } catch (Exception e) {
            AllureLoggingUtils.logToAllureAndConsole(log, "Error uploading file: " + e.getMessage());
            throw new IOException("Error uploading file: " + e.getMessage());
        } finally {
            sftpClient.disconnect();
        }
    }

    public boolean isFileExists(String fileName, String folderName) throws Exception {
        sftpClient.connect();
        boolean status = sftpClient.isFileExists(fileName, folderName);
        sftpClient.disconnect();
        return status;
    }

    public String downloadFile(String localFilePath, String remoteFileName, String folderName) throws JSchException {
        sftpClient.connect();
        log.info("Downloading");
        sftpClient.downloadFile(localFilePath, remoteFileName, folderName);
        sftpClient.disconnect();
        return localFilePath;
    }
}
