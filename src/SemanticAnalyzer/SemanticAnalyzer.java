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

    // Recupera o tipo da variável
    public String getVariableType(Token token) {
        Id id = currentEnv.get(token);
        if (id != null) {
            return id.getType();
        }
        return null;
    }

    public boolean areTypesCompatibleForAssignment(String varType, String exprType) {
        if (varType.equals(exprType)) return true; // mesmo tipo

        if (varType.equals("INT") && exprType.equals("CHAR")) return true; // CHAR → INT permitido (conversão para código ASCII)
        if (varType.equals("FLOAT") && exprType.equals("INT")) return true;

        // Não pode atribuir FLOAT → INT ou FLOAT → CHAR
        if (varType.equals("INT") && exprType.equals("FLOAT")) return false;
        if (varType.equals("CHAR") && !exprType.equals("CHAR")) return false;

        return false;
    }

    public String resultingType(String t1, String t2) {
        if (t1.equals(t2)) return t1;

        // INT + FLOAT → FLOAT
        if ((t1.equals("INT") && t2.equals("FLOAT")) || (t1.equals("FLOAT") && t2.equals("INT")))
            return "FLOAT";

        // CHAR + INT → INT
        if ((t1.equals("CHAR") && t2.equals("INT")) || (t1.equals("INT") && t2.equals("CHAR")))
            return "INT";

        // FLOAT + CHAR → ERRO
        if ((t1.equals("FLOAT") && t2.equals("CHAR")) || (t1.equals("CHAR") && t2.equals("FLOAT"))) {
            semanticErrors.add("Erro semântico: tipos incompatíveis entre '" + t1 + "' e '" + t2 + "'");
            return "ERRO";
        }

        return "ERRO";
    }

    // Para registrar erros diretamente do Parser
    public void reportError(String message) {
        semanticErrors.add(message);
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
