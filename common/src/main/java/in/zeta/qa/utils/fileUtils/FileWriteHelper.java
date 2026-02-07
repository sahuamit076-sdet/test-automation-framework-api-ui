package in.zeta.qa.utils.fileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriteHelper {

    public static void writeToFile(String content, String filePath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(content);
            writer.close();
        } catch (IOException ex) {
            System.out.print("Invalid File Path - "+filePath);
        }
    }
}
