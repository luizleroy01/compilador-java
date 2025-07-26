package src.SyntacticAnalyzer;

import src.LexicalAnalyzer.Token;
import src.SemanticAnalyzer.SemanticAnalyzer;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<String> foundErrors = new ArrayList<>();
    private final SemanticAnalyzer semantic;

    public Parser(List<Token> tokens, SemanticAnalyzer semantic) {
        this.tokens = tokens;
        this.semantic = semantic;
    }

    private Token peek() {
        return current < tokens.size() ? tokens.get(current) : new Token("EOF", "", -1);
    }

    private Token advance() {
        return current < tokens.size() ? tokens.get(current++) : new Token("EOF", "", -1);
    }

    private void eat(String expectedType) {
        if (peek().getType().equals(expectedType) || peek().getLexeme().equals(expectedType)) {
            advance();
        } else {
            String error = "Erro sintático na linha " + peek().getLine()
                    + ": esperado '" + expectedType + "', encontrado '" + peek().getLexeme() + "'";
            foundErrors.add(error);
            throw new RuntimeException(error);
        }
    }

    public void program() {
        eat("PROGRAM");
        declList();
        eat("BEGIN");
        stmtList();
        eat("END");

        if (!foundErrors.isEmpty()) {
            System.out.println("Erros sintáticos encontrados:");
            foundErrors.forEach(System.out::println);
        }
    }

    private void declList() {
        while (peek().getType().matches("INT|FLOAT|CHAR")) {
            decl();
        }
    }

    private void decl() {
        String varType = peek().getType();
        eat(varType);
        eat("SYMBOL");
        identList(varType);
        eat("SYMBOL");
    }

    private void identList(String varType) {
        Token idToken = peek();
        eat("IDENTIFIER");
        semantic.declareVariable(idToken, varType);

        while (peek().getLexeme().equals(",")) {
            eat("SYMBOL");
            idToken = peek();
            eat("IDENTIFIER");
            semantic.declareVariable(idToken, varType);
        }
    }

    private void stmtList() {
        stmt();
        while (peek().getLexeme().equals(";")) {
            eat("SYMBOL");
            stmt();
        }
    }

    private void stmt() {
        String lexeme = peek().getLexeme();

        if (peek().getType().equals("IDENTIFIER")) {
            assignStmt();
        } else if (peek().getType().equals("IF")) {
            ifStmt();
        } else if (peek().getType().equals("WHILE")) {
            whileStmt();
        } else if (peek().getType().equals("REPEAT")) {
            repeatStmt();
        } else if (peek().getType().equals("IN")) {
            readStmt();
        } else if (peek().getType().equals("OUT")) {
            writeStmt();
        } else if (peek().getType().equals("ELSE")) {
            eat("ELSE");
            if (peek().getType().equals("IF")) {
                ifStmt();
            } else {
                stmt();
            }
        } else if (peek().getType().equals("UNTIL")) {
            condition();
        } else {
            String error = "Erro sintático na linha " + peek().getLine() +
                    ": comando inválido '" + lexeme + "'";
            foundErrors.add(error);
            throw new RuntimeException(error);
        }
    }

    private void assignStmt() {
        Token idToken = peek();
        semantic.useVariable(idToken);
        String varType = semantic.getVariableType(idToken);

        eat("IDENTIFIER");
        eat("ASSIGN_SYMBOL");
        String exprType = simpleExpr();

        semantic.checkAssignmentType(idToken, varType, exprType);
    }

    private void ifStmt() {
        eat("IF");
        if (peek().getType().equals("OPEN_PAR")) {
            eat("OPEN_PAR");
            condition();
            eat("CLOSE_PAR");
        } else {
            condition();
        }
        eat("THEN");

        if (peek().getLexeme().matches("int|float|char")) {
            declList();
        }
        stmtList();

        if (peek().getType().equals("ELSE")) {
            eat("ELSE");
            if (peek().getType().equals("IF")) {
                ifStmt();
            } else {
                if (peek().getLexeme().matches("int|float|char")) {
                    declList();
                }
                stmtList();
            }
        }
        eat("END");
    }

    private void repeatStmt() {
        eat("REPEAT");
        if (peek().getLexeme().matches("int|float|char")) {
            declList();
        }
        stmtList();
        stmtSuffix();
    }

    private void stmtSuffix() {
        eat("UNTIL");
        condition();
    }

    private void whileStmt() {
        stmtPrefix();
        if (peek().getLexeme().matches("int|float|char")) {
            declList();
        }
        stmtList();
        eat("END");
    }

    private void stmtPrefix() {
        eat("WHILE");
        condition();
        eat("DO");
    }

    private void readStmt() {
        eat("IN");
        eat("OPEN_PAR");
        Token idToken = peek();
        semantic.useVariable(idToken);
        eat("IDENTIFIER");
        eat("CLOSE_PAR");
    }

    private void writeStmt() {
        eat("OUT");
        eat("OPEN_PAR");
        writable();
        eat("CLOSE_PAR");
    }

    private void writable() {
        if (peek().getType().equals("LITERAL")) {
            eat("LITERAL");
        } else {
            simpleExpr();
        }
    }

    private void condition() {
        expression();
    }

    private String expression() {
        String t1 = simpleExpr();
        if (peek().getType().equals("REL_OP")) {
            eat("REL_OP");
            String t2 = simpleExpr();
            semantic.resultingType(t1, t2, peek().getLine());
            return "BOOL";
        }
        return t1;
    }

    private String simpleExpr() {
        String t1 = term();
        while (peek().getType().equals("ADD_OP")) {
            eat("ADD_OP");
            String t2 = term();
            t1 = semantic.resultingType(t1, t2, peek().getLine());
        }
        return t1;
    }

    private String term() {
        String t1 = factorA();
        while (peek().getType().equals("MUL_OP")) {
            eat("MUL_OP");
            String t2 = factorA();
            t1 = semantic.resultingType(t1, t2, peek().getLine());
        }
        return t1;
    }

    private String factorA() {
        if (peek().getLexeme().equals("!")) {
            eat("SYMBOL");
            return factor();
        } else if (peek().getLexeme().equals("-")) {
            eat("ADD_OP");
            return factor();
        } else {
            return factor();
        }
    }

    private String factor() {
        if (peek().getType().equals("IDENTIFIER")) {
            Token t = peek();
            semantic.useVariable(t);
            eat("IDENTIFIER");
            return semantic.getVariableType(t);
        } else if (peek().getType().equals("INTEGER_CONST")) {
            eat("INTEGER_CONST");
            return "INT";
        } else if (peek().getType().equals("FLOAT_CONST")) {
            eat("FLOAT_CONST");
            return "FLOAT";
        } else if (peek().getType().equals("CHAR_CONST")) {
            eat("CHAR_CONST");
            return "CHAR";
        } else if (peek().getType().equals("OPEN_PAR")) {
            eat("OPEN_PAR");
            String type = expression();
            eat("CLOSE_PAR");
            return type;
        } else {
            String error = "Erro sintático na linha " + peek().getLine() +
                    ": fator inválido '" + peek().getLexeme() + "'";
            foundErrors.add(error);
            throw new RuntimeException(error);
        }
    }

    public List<String> getSyntacticErrors() {
        return foundErrors;
    }
}
