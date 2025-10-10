import java.io.*;

public class Git {
    public static void main(String[] args) {
        initRepo(); // initialize the repo that we're in
    }

    public static void initRepo() {
        // Root "git" directory"
        File gitDir = new File("git");
        File objectsDir = new File(gitDir, "objects");
        File indexFile = new File(gitDir, "index");
        File headFile = new File(gitDir, "HEAD");

        // Check if the repository already exists
        if (existsDir(gitDir) && existsDir(objectsDir) && existsFile(indexFile) && existsFile(headFile)) {
            System.out.println("Git Repository Already Exists");
            return;
        }

        // Create the repository
        boolean madeGitDir = mkdir(gitDir);
        boolean madeObjectsDir = mkdir(objectsDir);
        boolean madeIndex = touch(indexFile);
        boolean head = touch(headFile);

        // Check if the repository was created successfully
        boolean flag = madeGitDir && madeObjectsDir && madeIndex && head;
        if (flag)
            System.out.println("Git Repository Created Successfully");
        else
            System.out.println("Git Repository Creation Failed");
    }

    // Method to check if a file or directory exists
    public static boolean existsDir(File dir) {
        return dir.exists() && dir.isDirectory();
    }

    // Method to check if a file exists
    public static boolean existsFile(File file) {
        return file.exists() && file.isFile();
    }

    // Method to create a directory if it doesn't exist
    public static boolean mkdir(File dir) {
        if (existsDir(dir))
            return true;
        if (dir.exists() && !dir.isDirectory())
            return false;
        return dir.mkdirs();
    }

    // Method to create a file if it doesn't exist'
    public static boolean touch(File file) {
        if (existsFile(file))
            return true;
        if (file.exists() && !file.isFile())
            return false; // File exists but not a file
        try {
            // Create the parent directory if it doesn't exist
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs())
                    return false;
            }
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }
}