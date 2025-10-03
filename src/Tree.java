import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

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
            String workingList = "";
            BufferedReader br = new BufferedReader(new FileReader("git/index"));
            while (br.ready()) {
                workingList += "blob " + br.readLine();
            }
            br.close();

            HashMap<String, ArrayList<String>> trees = new HashMap<>();

            while (needToMakeTrees(workingList)) {
                int lineIndex = findMostNestedFile(workingList);
                String[] lines = workingList.split("\n");
                String line = lines[lineIndex];
                String path = line.split(" ")[2];
                int lastSlash = path.lastIndexOf("/");
                String folder = path.substring(0, lastSlash);
                String file = path.substring(lastSlash + 1);
                if (trees.containsKey(folder)) {
                    trees.get(folder).add(file);
                } else {
                    trees.put(folder, new ArrayList<>());
                    trees.get(folder).add(file);
                    // TODO: add tree to working list
                }

                workingList = "";
                for (int i = 0; i < lines.length; i++) {
                    if (i != lineIndex) {
                        workingList += lines[i];
                    }
                    if (i != lines.length - 1) {
                        workingList += "\n";
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to create trees!");
            e.printStackTrace();
        }
    }

    public static boolean needToMakeTrees(String workingList) {
        return workingList.contains("/");
    }

    public static int findMostNestedFile(String workingList) {
        String[] lines = workingList.split("\n");
        int mostSlashes = -1;
        int lineWithMostSlashes = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int numSlashes = line.split("/").length - 1;
            if (numSlashes > mostSlashes) {
                lineWithMostSlashes = i;
                mostSlashes = numSlashes;
            }
        }
        return lineWithMostSlashes;
    }
}
