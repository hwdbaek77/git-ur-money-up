import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.stream.Stream;
import java.util.zip.*;
import java.util.*;

public class Blob {
    public static boolean compress = false;
    public static String gitDir = "git";
    public static String objectsDir = gitDir + "/objects";
    public static String indexFile = gitDir + "/index";

    // Create a blob from a file
    public static String create(File src) throws Exception {
        // Check if the file exists
        if(src == null | !src.isFile()) throw new IllegalAccessException("Blob.create: source is null or isn't a file");

        // Create the blob
        try {
            ensureObjectsDir();

            String hash;
            if (!compress) {
                hash = Sha1.ofFile(src);
                File out = new File(objectsDir, hash);
                if(!out.exists()) Files.copy(src.toPath(), out.toPath());
            }
            // Compress the file and create a new blob
            else {
                byte[] raw = Files.readAllBytes(src.toPath());
                byte[] zipped = deflate(raw);
                hash = sha1OfBytes(zipped);
                File out = new File(objectsDir, hash);
                if(!out.exists()) Files.write(out.toPath(), zipped);
            }

            // Update index mapping: path -> hash
            updateIndex(src, hash);
            return hash;
        } catch (IOException e) {
            throw new RuntimeException("Blob.create: failed to copy file");
        }
    }

    // For some cases, we need a version of the function that cannot throw an error
    public static String safeCreateAndAdd(File src) {
        try {
            create(src);
            return Index.safeAdd(src);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a BLOB file without an actual file as input; just a byte array of data
    public static String create(byte[] data) {
        // Create the blob
        try {
            ensureObjectsDir();

            // Copy the file to the objects directory
            if (!compress) {
                String hash = sha1OfBytes(data);
                File out = new File(objectsDir, hash);
                if(!out.exists()) Files.write(out.toPath(), data);
                return hash;
            }

            // Compress the file and create a new blob
            else {
                byte[] zipped = deflate(data);
                String hash = sha1OfBytes(zipped);
                File out = new File(objectsDir, hash);
                if(!out.exists()) Files.write(out.toPath(), zipped);
                return hash;
            }
        } catch (IOException e) {
            throw new RuntimeException("Blob.create: failed to write to file");
        }
    }

    // Creates BLOBs for each file in the given directory and in all subdirectories
    // Also stages the files
    public static void createDirectory(String directory) {
        try {
            Stream<File> fileStream =  Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .map(Path::toFile);
            fileStream.forEach(Blob::safeCreateAndAdd);
            fileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if a blob exists
    public static boolean blobExists(String hash) {
        // Check if the hash is null or empty
        return hash != null && !hash.isEmpty() && new File(objectsDir, hash).isFile();
    }

    // Ensure that the objects directory exists
    public static void ensureObjectsDir() {
        File git = new File(gitDir);
        File objects = new File(objectsDir);
        if(!git.exists() && !git.mkdirs()) throw new RuntimeException("Failed to create objects directory");
        if(!objects.exists() && !objects.mkdirs()) throw new RuntimeException("Failed to create objects directory");
        if(!objects.isDirectory()) throw new RuntimeException("objects directory is not a directory");
    }

    // Check if all blobs exist
    public static boolean allBlobsExist(String... hashes) {
        if (hashes == null || hashes.length == 0) return false;
        for (String h : hashes) if (!blobExists(h)) return false;
        return true;
    }

    // Reset the blob store for tests
    public static void resetForTests(String... hashes) {
        // Ensure that the objects directory exists
        ensureObjectsDir();
        if (hashes == null) return;

        // Delete all blobs
        for (String h : hashes) {
            if (h == null || h.isEmpty()) continue;
            File f = new File(objectsDir, h);
            if (f.exists() && f.isFile()) {
                try { Files.deleteIfExists(f.toPath()); } catch (IOException ignored) {}
            }
        }
    }

    // Compress a byte array
    public static byte[] deflate(byte[] input) {
        // Compress the input
        Deflater d = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
        d.setInput(input);
        d.finish();

        // Get the compressed output
        byte[] buf = new byte[Math.max(1024, input.length)];
        int off = 0;

        // Compress until finished
        while (!d.finished()) {
            int n = d.deflate(buf, off, Math.max(1, buf.length - off));
            if (n == 0 && !d.finished()) {
                byte[] bigger = new byte[buf.length * 2];
                System.arraycopy(buf, 0, bigger, 0, off);
                buf = bigger;
            }
            // else if (n == 0 && d.finished()) { }
            else off += n;
        }

        // Return the compressed output
        byte[] out = new byte[off];
        System.arraycopy(buf, 0, out, 0, off);
        return out;
    }

    // Calculate the SHA-1 of a byte array
    public static String sha1OfBytes(byte[] bytes) {
        // Calculate the SHA-1 of the byte array
        try {
            // Create a SHA-1 MessageDigest instance and calculate the SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);

            // Convert the digest to a hex string
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", Byte.toUnsignedInt(b)));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-1 not available", e); // Should never happen
        }
    }

    // Ensure the index file exists and is a regular file
    public static void ensureIndexFile() {
        File idx = new File(indexFile);
        File parent = idx.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) throw new RuntimeException("Failed to create git directory");
        }
        if (idx.exists() && idx.isDirectory()) throw new RuntimeException("index path is a directory");
        if (!idx.exists()) {
            try {
                if (!idx.createNewFile()) throw new RuntimeException("Failed to create index file");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create index file", e);
            }
        }
    }

    // Read index into a path->hash map (order preserved)
    public static Map<String, String> readIndex() {
        ensureIndexFile();
        Map<String, String> map = new LinkedHashMap<>();
        Path p = Paths.get(indexFile);
        try {
            if (!Files.exists(p)) return map;
            for (String line : Files.readAllLines(p)) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int sep = line.lastIndexOf('\t');
                if (sep < 0) sep = line.lastIndexOf(' ');
                if (sep <= 0 || sep >= line.length() - 1) continue;
                String path = line.substring(0, sep);
                String hash = line.substring(sep + 1).trim();
                if (!hash.isEmpty()) map.put(path, hash);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read index", e);
        }
        return map;
    }

    // Write index map atomically
    public static void writeIndex(Map<String, String> map) {
        ensureIndexFile();
        Path idx = Paths.get(indexFile);
        Path tmp = idx.resolveSibling("index.tmp");
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            lines.add(e.getKey() + "\t" + e.getValue());
        }
        try {
            Files.write(tmp, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, idx, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, idx, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write index", e);
        }
    }

    // Update or add mapping from file path to blob hash
    public static void updateIndex(File src, String hash) { updateIndex(src == null ? null : src.getPath(), hash); }
    public static void updateIndex(String path, String hash) {
        if (path == null || path.isEmpty() || hash == null || hash.isEmpty()) return;
        Map<String, String> map = readIndex();
        map.put(path, hash);
        writeIndex(map);
    }

    // Lookup mapping
    public static String getIndexHash(String path) {
        if (path == null) return null;
        Map<String, String> map = readIndex();
        return map.get(path);
    }

    // Remove mapping
    public static void removeIndex(String path) {
        if (path == null) return;
        Map<String, String> map = readIndex();
        if (map.remove(path) != null) writeIndex(map);
    }
}
