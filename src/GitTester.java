import java.io.*;

public class GitTester {
    public static void main(String[] args) throws Exception {
        deleteGit();
        assertMissingAll();

        runGit();
        assertPresentAll();

        deleteGit();
        assertMissingAll();

        runGit();

        File index = new File("git/index");
        if(index.exists() && !index.delete()) throw new RuntimeException("Failed to delete index file");

        if(!isDir("git")) throw new RuntimeException( "Git directory not present" );
        if(!isDir("git/objects")) throw new RuntimeException( "Git objects directory not present" );
        if(isFile("git/index")) throw new RuntimeException( "Git index file present" );
        if(!isFile("git/HEAD")) throw new RuntimeException( "Git HEAD file present" );

        runGit();
        assertPresentAll();

        System.out.println("GitTester: All tests passed");
    }

    public static void runGit() throws Exception {
        Git.main(new String[0]);
    }

    public static void deleteGit() {
        deleteRecursive(new File("git"));
    }

    public static void deleteRecursive(File f) {
        if(!f.exists()) return;
        if(f.isDirectory()) {
            File[] kids = f.listFiles();
            if(kids != null) {
                for(File k : kids) deleteRecursive(k);
            }
        }

        if(!f.delete()) throw new RuntimeException("Failed to delete file " + f.getAbsolutePath());
    }

    public static boolean isDir(String path) {
        return new File(path).isDirectory();
    }

    public static boolean isFile(String path) {
        return new File(path).isFile();
    }

    public static void assertPresentAll() {
        if(!isDir("git"))  throw new RuntimeException("Git directory not present");
        if(!isDir("git/objects")) throw new RuntimeException("Git objects directory not present");
        if(!isFile("git/index")) throw new RuntimeException("Git index file not present");
        if(!isFile("git/HEAD")) throw new RuntimeException("Git HEAD file not present");
    }

    public static void assertMissingAll() {
        if(new File("git").exists()) throw new RuntimeException( "Git directory present" );
        if(new File("git/objects").exists()) throw new RuntimeException( "Git objects directory present" );
        if(new File("git/index").exists()) throw new RuntimeException( "Git index file present" );
        if(new File("git/HEAD").exists()) throw new RuntimeException( "Git HEAD file present" );
    }
}
