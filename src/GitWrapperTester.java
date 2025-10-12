public class GitWrapperTester {
    public static void main(String[] args) throws Exception {
        GitWrapper git = new GitWrapper();
        // git.init();

        // now we're going to add another file
        // git.add("newFileToAdd");

        // lastly we're going to commit these changes
        git.commit("Joe", "Commit Message (added newFileToAdd)");
    }
}
