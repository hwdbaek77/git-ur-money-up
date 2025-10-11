import java.io.File;
import java.nio.file.Files;

public class TreeTester {
    public static void main(String[] args) {
        Blob.ensureObjectsDir();

        File directory = new File("testDir/srx/docs");
        directory.mkdirs();
        File apples = new File("testDir/apples");
        apples.mkdir();

        File file = new File("testDir/apples/info.ini");
        File file2 = new File("testDir/srx/docs/README.txt");
        File file3 = new File("testDir/drive.txt");

        try {
            file.createNewFile();
            String contents = "informative information";
            Files.write(file.toPath(), contents.getBytes());

            file2.createNewFile();
            String contents2 = "PSYCH! Don't Read Me";
            Files.write(file2.toPath(), contents2.getBytes());

            file3.createNewFile();
            String contents3 = "drive";
            Files.write(file3.toPath(), contents3.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Blob.createDirectory("testDir");

        Tree.createTrees();
    }
}
