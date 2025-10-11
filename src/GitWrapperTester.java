public class GitWrapperTester {
    public static void main(String[] args) throws Exception {
        GitWrapper git = new GitWrapper();
        git.init();

        git.commit("JOE", "LOLLLLL");

        // git.add("newCommittedFile");
        // git.commit("JOE", "added newCommitedFile");

        // git.checkout("d21fc267e8db63038276c484a5dc1c6102d00ab1");
    }
}
