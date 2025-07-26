package src.SemanticAnalyzer;

import src.LexicalAnalyzer.Token;
import src.TableOfSymbols.Env;
import src.TableOfSymbols.Id;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final Env globalEnv; // Somente um escopo global
    private final List<String> semanticErrors = new ArrayList<>();

    public SemanticAnalyzer() {
        this.globalEnv = new Env(null);
    }

    public void declareVariable(Token token, String type) {
        if (globalEnv.get(token) != null) {
            semanticErrors.add("Erro semântico na linha " + token.getLine() +
                    ": variável '" + token.getLexeme() + "' já declarada.");
        } else {
            globalEnv.put(token, new Id(token, type));
        }
    }

    public void useVariable(Token token) {
        if (globalEnv.get(token) == null) {
            semanticErrors.add("Erro semântico na linha " + token.getLine() +
                    ": variável '" + token.getLexeme() + "' não declarada.");
        }
    }

    public String getVariableType(Token token) {
        Id id = globalEnv.get(token);
        if (id != null) return id.type;
        return "ERRO";
    }

    public void checkAssignmentType(Token var, String varType, String exprType) {
        if (varType.equals(exprType)) return;

        if (varType.equals("FLOAT") && exprType.equals("INT")) return;
        if (varType.equals("INT") && exprType.equals("CHAR")) return;

        semanticErrors.add("Erro semântico na linha " + var.getLine() +
                ": não é permitido atribuir um valor do tipo '" + exprType +
                "' para a variável do tipo '" + varType + "'");
    }

    public String resultingType(String t1, String t2, int line) {
        if (t1.equals(t2)) return t1;

        // INT + FLOAT → FLOAT
        if ((t1.equals("INT") && t2.equals("FLOAT")) || (t1.equals("FLOAT") && t2.equals("INT")))
            return "FLOAT";

        // CHAR + INT → INT
        if ((t1.equals("CHAR") && t2.equals("INT")) || (t1.equals("INT") && t2.equals("CHAR")))
            return "INT";

        // FLOAT + CHAR → ERRO
        if ((t1.equals("FLOAT") && t2.equals("CHAR")) || (t1.equals("CHAR") && t2.equals("FLOAT"))) {
            semanticErrors.add("Erro semântico na linha " + line +
                    ": tipos incompatíveis entre '" + t1 + "' e '" + t2 + "'");
            return "ERRO";
        }

        return "ERRO";
    }

    public boolean hasErrors() {
        return !semanticErrors.isEmpty();
    }

    public List<String> getErrors() {
        return semanticErrors;
    }

    public void printSymbolTable() {
        System.out.println("\nTabela de Símbolos:");
        globalEnv.getTable().forEach((token, id) -> System.out.println(id));
    }
}
