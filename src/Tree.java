import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Tree {
    // Creates a tree file with the given lines
    // The file will be in the objects folder and its name will be the SHA-1 hash of its contents
    // Returns the SHA-1 hash of the file's contents (its name)
    public static String addTree(String[] entries) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            byte[] curBytes = entry.getBytes();
            for (byte curByte : curBytes) {
                bytes.add(curByte);
            }
            // We don't want our file to end with an extra newline
            if (i != entries.length - 1) {
                Byte newline = '\n';
                bytes.add(newline);
            }
        }
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = bytes.get(i);
        }
        return Blob.create(byteArray);
    }

    // Scans the index file for directories and creates tree files for all of them
    public static void createTrees() {
        try {
            Index.ensureIndexFile();
            BufferedReader br = new BufferedReader(new FileReader("git/index"));
            while (br.ready()) {
                String line = br.readLine();
                String path = line.split(" ")[1];
                String[] folders = path.split("/");
                if (folders.length > 1) {
                    
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Failed to create trees!");
            e.printStackTrace();
        }
    }
}
