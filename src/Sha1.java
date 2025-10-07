import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;

public class Sha1 {
    // Compute the SHA-1 hash of a string
    public static String ofString(String s) {
        // Create a SHA-1 MessageDigest instance and calculate the SHA-1
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Compute the SHA-1 hash of a file
    public static String ofFile(File file) throws IOException {
        try { // Read the file into a byte array
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return ofString(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Convert a byte array to a hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        // Convert the byte array to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
