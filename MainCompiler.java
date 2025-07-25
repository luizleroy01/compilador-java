import src.LexicalAnalyzer.CodeAnalyzer;
import src.LexicalAnalyzer.Token;
import src.SyntacticAnalyzer.Parser;


import java.io.IOException;

public class MainCompiler {
    public static void main(String args[]) throws IOException {
        CodeAnalyzer codeAnalyzer = new CodeAnalyzer();
        String path = "src\\DataTests\\test4.txt";
        var tokens = codeAnalyzer.analyze(path);

        Parser parser = new Parser(tokens);
        try {
            parser.program();
            System.out.println("Análise sintática concluída com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro na análise sintática: " + e.getMessage());
        }

    }
}
