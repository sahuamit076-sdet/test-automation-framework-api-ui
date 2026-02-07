package in.zeta.qa.utils.sftpManager;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SftpClient {

    private static final Logger log = LogManager.getLogger(SftpClient.class);

    private static volatile SftpClient instance;
    private Session session;
    private ChannelSftp channelSftp;
    private final SftpConfig config;

    private SftpClient(SftpConfig config) {
        this.config = config;
    }

    public static SftpClient getInstance(SftpConfig config) {
        if (instance == null) {
            synchronized (SftpClient.class) {
                if (instance == null) {
                    instance = new SftpClient(config);
                }
            }
        }
        return instance;
    }

    public synchronized void connect() throws JSchException {
        if (session != null && session.isConnected()) {
            log.info("Already connected.");
            return;
        }

        JSch jsch = new JSch();
        session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
        session.setPassword(config.getPassword());

        Properties properties = new Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);

        log.info("Connecting to SFTP...");
        session.connect();
        log.info("Connected.");

        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
    }

    public synchronized void uploadFile(String localFilePath) throws Exception {
        if (channelSftp == null || !channelSftp.isConnected()) {
            throw new IllegalStateException("SFTP client is not connected.");
        }

        channelSftp.cd(config.getRemoteDirectory());

        File file = new File(localFilePath);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            channelSftp.put(inputStream, file.getName());
            log.info("File uploaded successfully: " + file.getName());
        }
    }

    public synchronized boolean isFileExists(String fileName, String folderPath) throws Exception {
        channelSftp.cd(folderPath);
        try {
            channelSftp.ls(fileName);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    public synchronized void disconnect() {
        if (channelSftp != null) {
            channelSftp.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        log.info("SFTP session closed.");
    }

    public synchronized void downloadFile(String localFilePath, String remoteFileName, String folderPath)  {
        String remoteFilePath = Paths.get(folderPath, remoteFileName).toString();
        File localFile = new File(localFilePath);
        try {
            channelSftp.get(remoteFilePath, localFile.getAbsolutePath());
            log.info("File downloaded successfully: {}", localFilePath);
        } catch (Exception e) {
            log.error("Failed to download file: {} from folder: {}", remoteFileName, folderPath, e);
        }
    }
}
