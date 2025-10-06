import java.io.File;
import java.nio.file.Files;

public class TreeTester {
    public static void main(String[] args) {
        Blob.ensureObjectsDir();

        File directory = new File("testDir/bloooob");
        directory.mkdir();

        File file = new File("testDir/bloooob/testFile.txt");
        File file2 = new File("testDir/dog.txt");
        try {
            file.createNewFile();
            String contents = "Hello";
            Files.write(file.toPath(), contents.getBytes());

            file2.createNewFile();
            String contents2 = "Byte";
            Files.write(file2.toPath(), contents2.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Blob.createDirectory("testDir");

        Tree.createTrees();
    }
}
