import src.LexicalAnalyzer.CodeAnalyzer;
import src.SyntacticAnalyzer.Parser;
import src.SemanticAnalyzer.SemanticAnalyzer;

import java.io.IOException;

public class MainCompiler {
    public static void main(String args[]) throws IOException {
        CodeAnalyzer codeAnalyzer = new CodeAnalyzer();
        String path = "C:\\Users\\Usuario\\IdeaProjects\\compilador-java\\src\\DataTests\\testY.txt";
        var tokens = codeAnalyzer.analyze(path);

        SemanticAnalyzer semantic = new SemanticAnalyzer();
        Parser parser = new Parser(tokens, semantic);

        try {
            parser.program();

            if (!parser.getSyntacticErrors().isEmpty()) {
                System.out.println("\nErros Sintáticos:");
                parser.getSyntacticErrors().forEach(System.out::println);
            }

            if (semantic.hasErrors()) {
                System.out.println("\nErros Semânticos:");
                semantic.getErrors().forEach(System.out::println);
            } else {
                System.out.println("\nAnálise concluída com sucesso!");
            }

            semantic.printSymbolTable();

        } catch (Exception e) {
            System.err.println("Erro durante a análise: " + e.getMessage());
        }
    }
}
