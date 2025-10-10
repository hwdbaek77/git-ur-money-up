import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Arrays;

public class Tree {
    // Creates a tree file with the given lines
    // The file will be in the objects folder and its name will be the SHA-1 hash of
    // its contents
    // Returns the SHA-1 hash of the file's contents (its name)
    public static String addTree(ArrayList<String> entries) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i);
            byte[] curBytes = entry.getBytes();
            for (byte curByte : curBytes) {
                bytes.add(curByte);
            }
            // We don't want our file to end with an extra newline
            if (i != entries.size() - 1) {
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
    public static String createTrees() {
        try {
            String mostRecent = "";
            Index.ensureIndexFile();
            String workingList = "";
            BufferedReader br = new BufferedReader(new FileReader("git/index"));
            while (br.ready()) {
                workingList += "blob " + br.readLine() + "\n";
            }
            br.close();

            HashMap<String, ArrayList<String>> trees = new HashMap<>();

            int lastNumberSlashes = -1;
            while (needToMakeTrees(workingList)) {
                int lineIndex = findMostNestedFile(workingList);
                String[] lines = workingList.split("\n");
                String line = lines[lineIndex];
                int numberSlashes = line.split("/").length - 1;
                ArrayList<String> linesArray = new ArrayList<>();
                for (String curLine : lines) {
                    linesArray.add(curLine);
                }
                if (numberSlashes != lastNumberSlashes) {
                    for (Entry<String, ArrayList<String>> entry : trees.entrySet()) {
                        String key = entry.getKey();
                        ArrayList<String> value = entry.getValue();
                        mostRecent = addTree(value);
                        linesArray.add("tree " + mostRecent + " " + key);
                    }
                    trees.clear();
                }

                String[] components = line.split(" ");
                String path = components[2];
                String fileType = components[0];
                String fileHash = components[1];
                int lastSlash = path.lastIndexOf("/");
                String folder = path.substring(0, lastSlash);
                String file = path.substring(lastSlash + 1);
                if (trees.containsKey(folder)) {
                    trees.get(folder).add(fileType + " " + fileHash + " " + file);
                } else {
                    trees.put(folder, new ArrayList<>());
                    trees.get(folder).add(fileType + " " + fileHash + " " + file);
                }

                workingList = "";
                for (int i = 0; i < linesArray.size(); i++) {
                    if (i != lineIndex) {
                        workingList += linesArray.get(i);
                    }
                    if (i != linesArray.size() - 1) {
                        workingList += "\n";
                    }
                }
                lastNumberSlashes = numberSlashes;
            }
            for (Entry<String, ArrayList<String>> entry : trees.entrySet()) {
                // String key = entry.getKey();
                ArrayList<String> value = entry.getValue();
                addTree(value);
            }
            ArrayList<String> rootFiles = new ArrayList<String>();
            for (File f : new File("./").listFiles()) {
                rootFiles.add("blob" + Sha1.ofFile(f) + f.getName());
            }
            addTree(rootFiles);

            trees.clear();
            return mostRecent;
        } catch (Exception e) {
            System.out.println("Failed to create trees!");
            e.printStackTrace();
            return "Something went wrong";
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
