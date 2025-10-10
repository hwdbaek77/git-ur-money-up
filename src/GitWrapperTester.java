public class GitWrapperTester {
    public static void main(String[] args) throws Exception {
        GitWrapper git = new GitWrapper();
        git.init();

        git.add("ASDF");
        git.add("jnco");

        // git.commit("asdf", "asdf");
    }
}
