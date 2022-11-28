import java.io.File;

public class JackAnalyzer {
    public static void main(String[] args) {
        JackAnalyzer jackAnalyzer = new JackAnalyzer();
        int exitCode = jackAnalyzer.run(args);
        System.exit(exitCode);
    }

    private CompilationEngine compEngine;

    public int run(String[] args) {
        // check for argument length
        if (args.length != 1) {
            printUsage();
            return -1;
        }

        File fileOrDirectory = new File(args[0]);

        // analyze file or directory of files
        try {
            if (fileOrDirectory.isDirectory()) {
                analyzeFiles(fileOrDirectory);
            } else {
                analyzeFile(fileOrDirectory);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private void analyzeFile(File file) {
        String filename = file.getName();
        String lowerFilename = filename.toLowerCase();

        if (!lowerFilename.endsWith(".jack"))
            throw new IllegalArgumentException("Filename must end with .jack!");

        // compile file
        JackTokenizer jackTokenizer = new JackTokenizer(file);
        String outputFilename = filename.substring(0, 
                lowerFilename.lastIndexOf(".jack")) + ".xml";
        File outputFile = new File(outputFilename);
        
        compEngine = new CompilationEngine(jackTokenizer, outputFile);
        compEngine.compileClass();
    }

    private void analyzeFiles(File directory) {
        // iterate over directory non-recursively
        for (File f : directory.listFiles()) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".jack")) {
                analyzeFile(f);
            }
        }
    }

    private void printUsage() {
        System.err.println("Usage:\n" +
                           "  java JackAnalyzer (<filename>|<directory>)");
    }
}
