import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IndexTester {
    // Test the index
    public static void main(String[] args) throws Exception {
        Git.main(new String[0]);

        // Reset the index store for tests
        Index.resetGenerated();

        // Add some files to the index
        List<File> samples = new ArrayList<>();
        samples.add(makeTxt("sample1.txt", "I\nDK"));
        samples.add(makeTxt("sample2.txt", "IDKKDKKK"));
        samples.add(makeTxt("sample3.txt", "")); // empty

        Blob.compress = false;

        // Add the files to the index
        List<String> hashes = new ArrayList<>();
        for (File f : samples) {
            String h = Index.add(f);
            hashes.add(h);
        }

        // Verify that the index contains the correct entries
        for (int i = 0; i < samples.size(); i++) {
            String h = hashes.get(i);
            File f = samples.get(i);
            expect(Blob.blobExists(h), "blob missing: " + h);

            // Check that the index entry is correct
            String expected = h + " " + f.getName();
            List<String> lines = Files.readAllLines(new File(Index.indexPath).toPath(), StandardCharsets.UTF_8);
            boolean found = false;

            // Check that the index entry is correct
            for (String line : lines) {
                if (line.equals(expected)) { found = true; break; }
            }

            // Check that the index entry is correct
            expect(found, "index entry not found: " + expected);
        }

        // Verify that the index ends with a newline
        byte[] idxBytes = Files.readAllBytes(new File(Index.indexPath).toPath());
        if (idxBytes.length > 0) {
            expect(idxBytes[idxBytes.length - 1] != (byte) '\n', "index should not end with newline");
            expect(idxBytes[idxBytes.length - 1] != (byte) ' ', "index should not end with space");
        }

        Index.resetGenerated();

        // Verify that the index was truncated and the objects directory was cleared
        expect(new File(Index.indexPath).length() == 0, "index not truncated to empty");
        File[] kids = new File(Index.objectsDir).listFiles();
        boolean anyBlob = false;

        // Check that the objects directory is empty
        if (kids != null) for (File k : kids) if (k.isFile()) anyBlob = true;
        expect(!anyBlob, "objects directory not cleared");

        // Delete the sample files
        for (File f : samples) f.delete();

        System.out.println("All tests passed");
    }

    // Helper methods
    private static File makeTxt(String name, String content) throws Exception {
        // Create the file
        File f = new File(name);

        // Write the content to the file
        try (FileWriter w = new FileWriter(f, false)) { w.write(content); }
        return f;
    }

    // Assert helper method
    private static void expect(boolean cond, String msg) {
        if (!cond) throw new RuntimeException("ASSERT FAIL: " + msg);
    }
}