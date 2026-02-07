package in.zeta.qa.utils.sftpManager;

public class SftpClientFactory {
    public static SftpClient createClient(String configName) {
        SftpConfig config = SftpConfigLoader.getConfig(configName);
        return SftpClient.getInstance(config);
    }
}
