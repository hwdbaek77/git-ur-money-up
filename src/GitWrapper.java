import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class GitWrapper {
    private String currentRootTreeHash = "";

    /**
     * Initializes a new Git repository.
     * This method creates the necessary directory structure
     * and initial files (index, HEAD) required for a Git repository.
     */
    public void init() throws Exception {
        Git.initRepo();
        addFileRecursive(new File("./"));
        currentRootTreeHash = Tree.createTrees();
        commit("Git", "Initial Commit");
    };

    // adds the given file AND adds all subfiles
    public void addFileRecursive(File f) throws Exception {
        if (f.equals(new File("./git")) || f.equals(new File("./src")) || f.equals(new File("./.git"))
                || f.equals(new File("./README.md"))) // check: don't add
                                                      // dumb stuff
            return;
        if (!f.isDirectory()) { // base case: if its just a single file then just add it
            add(f.getPath());
            return;
        }

        if (f.listFiles() == null || f.listFiles().length == 0) // check: listfiles might be null for an empty dir,
                                                                // don't add it
            return;
        for (File sub : f.listFiles()) { // add all subfiles of any directory
            addFileRecursive(sub);
        }
    }

    /**
     * Stages a file for the next commit.
     * This method adds a file to the index file.
     * If the file does not exist, it throws an IOException.
     * If the file is a directory, it throws an IOException.
     * If the file is already in the index, it does nothing.
     * If the file is successfully staged, it creates a blob for the file.
     * 
     * @param filePath The path to the file to be staged.
     */
    public void add(String filePath) throws Exception {
        Blob.create(new File(filePath));
        Index.add(new File(filePath));
    };

    /**
     * Creates a commit with the given author and message.
     * It should capture the current state of the repository by building trees based
     * on the index file,
     * writing the tree to the objects directory,
     * writing the commit to the objects directory,
     * updating the HEAD file,
     * and returning the commit hash.
     * 
     * The commit should be formatted as follows:
     * tree: <tree_sha>
     * parent: <parent_sha>
     * author: <author>
     * date: <date>
     * summary: <summary>
     *
     * @param author  The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     */
    public String commit(String author, String message) throws Exception {
        addFileRecursive(new File("./"));
        Tree.createTrees();

        currentRootTreeHash = Tree.createTrees();
        String commit = "tree: " + currentRootTreeHash + "\n" +
                "parent: " + Files.readString(Paths.get("git/HEAD")) + "\n" +
                "author: " + author + "\n" +
                "date: " + LocalDate.now() + "\n" +
                "summary: " + message;

        Files.writeString(Paths.get("git/HEAD"), Sha1.ofString(commit));
        Files.writeString(Paths.get("git/objects/" + Sha1.ofString(commit)), commit);

        return Sha1.ofString(commit);
    };

    /**
     * EXTRA CREDIT:
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    public void checkout(String commitHash) {
        // to-do: implement functionality here

    };
}