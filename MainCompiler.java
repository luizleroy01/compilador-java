import src.LexicalAnalyzer.CodeAnalyzer;

import java.io.IOException;

public class MainCompiler {
    public static void main(String args[]) throws IOException {
        CodeAnalyzer codeAnalyzer = new CodeAnalyzer();
        String path = "src\\DataTests\\test5.txt";
        codeAnalyzer.analyze(path);

    }
}
