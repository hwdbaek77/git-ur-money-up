public class GitWrapperTester {
    public static void main(String[] args) throws Exception {
        GitWrapper git = new GitWrapper();
        git.init();

        git.commit("JOE", "LOLLLLL");
    }
}
