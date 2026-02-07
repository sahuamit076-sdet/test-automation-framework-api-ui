package in.zeta.qa.utils.fileUtils;

import in.zeta.qa.utils.misc.AllureLoggingUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class UnzipFile {

    public void unzipFile(String source,  String destination, String password){

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public void renameZipToTxt(String destination) {
        File folder = new File(destination);
        File file = Objects.requireNonNull(folder.listFiles())[0];

        File renamed = new File(folder, file.getName().replace(".zip", ".txt"));
        file.renameTo(renamed);
    }

    public void deleteFolderIfExists(String folder) throws IOException{
        Path folderPath = Paths.get(folder);

        if (Files.exists(folderPath)) {
            try (Stream<Path> walk = Files.walk(folderPath)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                AllureLoggingUtils.logsToAllureReport("Folder deleted: " + folderPath);
            }
        } else {
            AllureLoggingUtils.logsToAllureReport("Folder does not exist: " + folderPath);
        }
    }








}
