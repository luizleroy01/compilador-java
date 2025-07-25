package src.SemanticAnalyzer;

import src.LexicalAnalyzer.Token;
import src.TableOfSymbols.Env;
import src.TableOfSymbols.Id;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private Env currentEnv; // Escopo atual
    private final List<String> semanticErrors = new ArrayList<>();

    public SemanticAnalyzer() {
        this.currentEnv = new Env(null); // Ambiente global
    }

    public void enterScope() {
        currentEnv = new Env(currentEnv);
    }

    public void exitScope() {
        currentEnv = currentEnv.prev;
    }

    public void declareVariable(Token token, String type) {
        if (currentEnv.get(token) != null) {
            semanticErrors.add("Erro semântico na linha " + token.getLine() +
                    ": variável '" + token.getLexeme() + "' já declarada.");
        } else {
            currentEnv.put(token, new Id(token, type));
        }
    }

    public void useVariable(Token token) {
        if (currentEnv.get(token) == null) {
            semanticErrors.add("Erro semântico na linha " + token.getLine() +
                    ": variável '" + token.getLexeme() + "' não declarada.");
        }
    }

    public boolean hasErrors() {
        return !semanticErrors.isEmpty();
    }

    public List<String> getErrors() {
        return semanticErrors;
    }

    public void printSymbolTable() {
        System.out.println("\nTabela de Símbolos:");
        Env env = currentEnv;
        while (env != null) {
            env.getTable().forEach((token, id) -> System.out.println(id));
            env = env.prev;
        }
    }
}
