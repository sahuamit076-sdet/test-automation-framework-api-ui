package in.zeta.qa.utils.sftpManager;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SftpConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remoteDirectory;
}
