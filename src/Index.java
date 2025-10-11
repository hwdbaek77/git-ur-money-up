import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Index {
    public static String gitDir = "git";
    public static String indexPath = gitDir + "/index";
    public static String objectsDir = gitDir + "/objects";

    // Add a file to the index
    public static String add(File src) throws Exception {
        if (src != null && Files.readString(Paths.get(indexPath)).contains(src.getName()))
            return Sha1.ofFile(src);

        // Check if the file exists
        if (src == null || !src.isFile())
            throw new IllegalArgumentException("Index.add: source is null or not a file");

        if (Files.readString(new File(indexPath).toPath()).contains(Sha1.ofFile(src))) { // don't re-add files
            return Sha1.ofFile(src);
        }
        Blob.ensureObjectsDir();
        ensureIndexFile();

        // Create the blob and add it to the index
        String hash = Blob.create(src);

        // Create the index line
        String relPath;
        try {
            File base = new File(".").getCanonicalFile();
            File abs = src.getCanonicalFile();
            String candidate = base.toURI().relativize(abs.toURI()).getPath();
            if (candidate == null || candidate.isEmpty()) {
                // Fallback to original name if failed
                candidate = src.getPath();
            }
            // Normalize separators to forward slashes for consistency across OSes
            relPath = candidate.replace('\\', '/');
        } catch (IOException e) {
            // Fallback to oriagnl name if failed
            relPath = src.getPath().replace('\\', '/');
        }
        String line = hash + " " + relPath;

        // Write the line to the index file
        File idx = new File(indexPath);
        if (!idx.exists())
            ensureIndexFile();

        // Add a newline if needed
        boolean needsSep = newline(idx);
        try (FileOutputStream out = new FileOutputStream(idx, true)) {
            if (needsSep)
                out.write('\n');
            out.write(line.getBytes(StandardCharsets.UTF_8));
        }

        return hash;
    }

    // For some cases, we need a version of the function that cannot throw an error
    public static String safeAdd(File src) {
        try {
            return add(src);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Ensure that the index file exists
    public static void ensureIndexFile() throws Exception {
        File git = new File(gitDir);
        if (!git.exists() && !git.mkdirs())
            throw new RuntimeException("Failed to create git dir");
        File idx = new File(indexPath);
        if (!idx.exists()) {
            if (!idx.createNewFile())
                throw new RuntimeException("Failed to create index");
        }

        // Check if the index file is a file
        if (!idx.isFile())
            throw new RuntimeException("index path is not a file");
    }

    // Check if the index file needs a newline at the end
    public static boolean newline(File idx) throws Exception {
        // Check if the index file exists
        if (!idx.exists() || idx.length() == 0)
            return false;

        // Read the last byte of the index file
        try (RandomAccessFile raf = new RandomAccessFile(idx, "r")) {
            raf.seek(Math.max(0, raf.length() - 1));
            int last = raf.read();
            return last != '\n';
        }
    }

    // Needed for tester
    // Reset the index store for tests
    public static void resetGenerated() throws Exception {
        // Ensure that the objects directory exists
        Blob.ensureObjectsDir();
        File objs = new File(objectsDir);
        File[] kids = objs.listFiles();

        // Delete all blobs
        if (kids != null) {
            for (File k : kids) {
                if (k.isFile())
                    k.delete();
            }
        }

        // Delete the index file
        ensureIndexFile();
        try (FileOutputStream out = new FileOutputStream(new File(indexPath), false)) {
        }
    }
}
