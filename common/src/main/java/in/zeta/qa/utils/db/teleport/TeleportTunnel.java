package in.zeta.qa.utils.db.teleport;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@author hashithas
@Slf4j
public class TeleportTunnel {

    private static final Logger LOG = LogManager.getLogger(TeleportTunnel.class);
    /**
     * Starts the Teleport tunnel by executing the teleportExecute.sh script.
     * This script is expected to output a line containing "localhost:<port>".
     * The method captures this output and extracts the port number.
     *
     * @return The port number as a String if found, otherwise null.
     */
    public static String startTunnel() {
        try {
            ProcessBuilder pb = new ProcessBuilder("./src/test/resources/teleport/teleportExecute.sh");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Pattern portPattern = Pattern.compile("localhost:(\\d+)");
            String line;

            while ((line = reader.readLine()) != null) {
                LOG.info("[tsh] " + line);
                Matcher matcher = portPattern.matcher(line);
                if (matcher.find()) {
                    String port = matcher.group(1);
                    log.info("Found port: " + port);
                    return port;
                }
            }

        } catch (IOException e) {
            log.info("Tunnel start failed");
            e.printStackTrace();
        }

        return null;
    }
}
