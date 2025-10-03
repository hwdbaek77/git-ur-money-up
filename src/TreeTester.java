import java.io.File;
import java.nio.file.Files;

public class TreeTester {
    public static void main(String[] args) {
        Blob.ensureObjectsDir();

        File directory = new File("testDir");
        directory.mkdir();

        File file = new File("testDir/testFile.txt");
        try {
            file.createNewFile();
            String contents = "Hello";
            Files.write(file.toPath(), contents.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Blob.createDirectory("testDir");

        Tree.addTree(new String[]
        {
            "blob bhfuerhgviurehgu dog.txt"
        });
    }
}
