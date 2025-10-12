# JOES UPDATE

1. What bugs did you discover, and which ones did you fix?
    Chase's tree code sometimes added an extra newline when recreating trees, I think. Totally possible that it was my fault for how I used the method. I fixed that.
2. What functionality was missing? Did you implement any missing functionality?
    Chase never implemented the create root tree functionality. So I implemented* it.
    *I used ai** to HELP me understand how to implement it but it didn't write the code for me.
    **no other ai was used on this project at all. 

# Git

### What is this

This is a java program that initializes a git-like repository in a folder named git/. It creates:

- git/
- git/objects/
- git/index
- git/HEAD

### To run

From your project root (where `Git.java` lives):

- java Git.java
- java Git

### Expected output

- First time (creates anything missing): Git Repository Created Successfully
- Second time (already exists): Git Repository Already Exists
- If something fails to create: Git Repository Creation Failed

### Methods

- public static void main(String[] args): Creates the repository
- public static boolean mkdir(File dir): Creates a directory if it doesn't exist
- public static boolean touch(File file): Creates a file if it doesn't exist
- public static boolean existsDir(File dir): Checks if a directory exists
- public static boolean existsFile(File file): Checks if a file exists

### Challenges

- Needed to ensure `git/` and `git/objects/` are directories and `git/index` and `git/HEAD` are files. If something else existed with the same name, creation had to fail cleanly instead of overwriting.
- The program should run multiple times without breaking anything. This required an early check (`existsDir` / `existsFile`) so it only creates missing pieces and simply reports when everything is already set up.
- The `touch` method had to create parent directories before files to avoid `IOException` when a parent didn’t exist.
- If any step failed (permissions, conflicts), the code needed to clearly print a failure message so it’s obvious why initialization didn’t complete.

## Git Tester

### What is this

This is a java program that tests the functionality of the `Git` class. It creates a git repository, then deletes it, then creates it again. It then checks that all the files and directories are present.

### To run

From your project root (where `GitTester.java` lives):
- javac Git.java GitTester.java
- java GitTester

### Expected output

GitTester: All tests passed

### Methods
- public static void runGit(): Creates the repository
- public static void deleteGit(): Deletes the repository
- public static void deleteRecursive(File f): Deletes a file or directory recursively
- public static boolean isDir(String path): Checks if a directory exists
- public static boolean isFile(String path): Checks if a file exists
- public static void assertPresentAll(): Checks that all the files and directories are present
- public static void assertMissingAll(): Checks that all the files and directories are missing

### Methodology
- deleteGit() removes any existing git/ directory and assertMissingAll() verifies nothing remains.
- runGit() calls Git.main() to create the structure. assertPresentAll() confirms all parts exist, and a second run ensures nothing breaks if executed twice.
- Deletes only git/index, confirms other items remain, reruns runGit() to ensure the missing file is restored.

### Edge cases
- If the program fails to create a file or directory, it throws an exception.

## Sha1

### What is this

This is a java program that calculates the SHA-1 hash of a string.

### How it works

- It uses the MessageDigest class to calculate the hash.
- It converts the hash to a hexadecimal string.
- It uses the String.format() method to add leading zeros to the hash.
- It returns the hexadecimal string.
- It throws an exception if the hash cannot be calculated.
- It throws an exception if the hash cannot be converted to a hexadecimal string.
- It throws an exception if the MessageDigest class is not available.
- It throws an exception if the SHA-1 algorithm is not available.
- It throws an exception if the file cannot be read.

### Challenges

- The program isn't yet secure. It doesn't check the hash of the file contents.
- It always uses UTF-8 encoding. It should use the encoding of the file being hashed.

## Tree

### What is this

Tree.java is capable of taking in an index file and generating all associated tree files, proceeding to hash their contents and place them in the object directory. A tree file contains a list of all the BLOBs and other trees in a given directory (directories are, in the end, just trees).

### How it works

- It iterates over the index file and generates a "working list" in memory, prefixing all files with the word "blob"
- It finds the line in the working list with the most slashes (i.e. the most nested file that has been staged)
- It adds that file to the tree file for its parent folder, which is stored in memory for now
- It removes that file from the working list
- Once all files at a given level of "nestedness" have been dealt with, we generate all tree files in memory and add them to the working list with the prefix "tree"
- Rinse and repeat until the working list is empty

### Challenges

- All these trees are really just floating in space, with nothing referencing them...