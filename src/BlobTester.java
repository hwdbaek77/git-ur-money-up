import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BlobTester {
    public static void main(String[] args) throws Exception {
        // Make sure the objects directory exists
        Blob.ensureObjectsDir();

        // Create a blob from a file
        File src = new File("tmp_blob_src.txt");
        String content = "hello\nmr\ntheis";
        try (FileWriter w = new FileWriter(src)) { w.write(content); }
        byte[] originalBytes = Files.readAllBytes(src.toPath());

        // Create a blob from the file and check its contents
        Blob.compress = false;
        String h1 = Blob.create(src);
        expect(Blob.blobExists(h1), "blobExists(h1) should be true");
        expect(Blob.allBlobsExist(h1), "allBlobsExist(h1) should be true");

        // Check that the blob was created correctly
        File blob1 = new File(Blob.objectsDir, h1);
        byte[] stored1 = Files.readAllBytes(blob1.toPath());
        expect(Arrays.equals(stored1, originalBytes), "stored (no-compress) should equal original bytes");

        // Reset the blob store and check that the blobs were deleted
        Blob.resetForTests(h1);
        expect(!Blob.blobExists(h1), "blob h1 should be deleted after reset");

        // Create a blob from the file and check its contents again
        Blob.compress = true;
        String h2 = Blob.create(src);
        expect(Blob.blobExists(h2), "blobExists(h2) should be true");

        // Check that the blob was created correctly
        File blob2 = new File(Blob.objectsDir, h2);
        byte[] stored2 = Files.readAllBytes(blob2.toPath());
        expect(!Arrays.equals(stored2, originalBytes), "stored (compress) should NOT equal original bytes");
        String recomputed = Blob.sha1OfBytes(stored2);
        expect(h2.equals(recomputed), "hash should equal SHA-1 of stored (compressed) bytes");

        // Check that all blobs exist
        expect(Blob.allBlobsExist(h2), "allBlobsExist(h2) should be true");

        // Reset the blob store and check that the blobs were deleted again
        Blob.resetForTests(h2);
        expect(!Blob.blobExists(h2), "blob h2 should be deleted after reset");
        src.delete();

        System.out.println("BlobTester: All tests passed ✅");
    }

    public static void expect(boolean cond, String msg) {
        if (!cond) throw new RuntimeException("ASSERT FAIL: " + msg);
    }
}
