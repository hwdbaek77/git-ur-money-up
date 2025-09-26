import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;

public class Sha1 {
    public static String ofString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ofFile(File file) throws IOException {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return ofString(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
