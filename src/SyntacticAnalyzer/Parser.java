package src.SyntacticAnalyzer;

import src.LexicalAnalyzer.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<String> foundErrors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
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
            System.out.println(error);
            throw new RuntimeException(error);
        }
    }

    public void program() {
        eat("PROGRAM");
        declList();
        eat("BEGIN");
        stmtList();
        eat("END");
    }

    private void declList() {
        while (peek().getType().matches("INT|FLOAT|CHAR")) {
            decl();
        }
    }

    private void decl() {
        eat(peek().getType()); // INT, FLOAT ou CHAR
        eat("SYMBOL");
        identList();
        eat("SYMBOL");
    }

    private void identList() {
        eat("IDENTIFIER");
        while (peek().getLexeme().equals(",")) {
            eat("SYMBOL");
            eat("IDENTIFIER");
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
            stmt();
        } else if (peek().getType().equals("IF")) {
            ifStmt();
            stmt();
        } else if (peek().getType().equals("WHILE")) {
            whileStmt();
        } else if (peek().getType().equals("REPEAT")) {
            repeatStmt();
        } else if (peek().getType().equals("IN")) {
            readStmt();
            stmt();
        } else if (peek().getType().equals("OUT")) {
            writeStmt();
            stmt();
        } else if (peek().getType().equals("ELSE")) {
            eat("ELSE");
            if (peek().getType().equals("IF")) {
                ifStmt();
            } else {
                stmtList();
            }
        } else if (peek().getType().equals("UNTIL")) {
            return;
        } else if (peek().getType().equals("END")) {
            //eat("END");
        } else {
            String error = "Erro sintático na linha " + peek().getLine() + ": comando inválido '" + lexeme + "'";
            System.out.println(error);
            throw new RuntimeException(error);
        }
    }

    private void assignStmt() {
        eat("IDENTIFIER");
        eat("ASSIGN_SYMBOL");
        simpleExpr();
        eat("SYMBOL");
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

        if (peek().getType().matches("INT|FLOAT|CHAR")) {
            declList();
        }

        stmtList();

        if (peek().getType().equals("ELSE")) {
            eat("ELSE");

            if (peek().getType().equals("IF")) {
                ifStmt();
            } else {
                if (peek().getType().matches("INT|FLOAT|CHAR")) {
                    declList();
                }
                stmtList();
            }
        }

        eat("END");
    }

    private void repeatStmt() {
        eat("REPEAT");

        if (peek().getType().matches("INT|FLOAT|CHAR")) {
            declList();
        }

        stmtList();
        stmtSuffix(); // until condition (sem ponto e vírgula após)
    }

    private void stmtSuffix() {
        eat("UNTIL");

        if (peek().getType().equals("OPEN_PAR")) {
            eat("OPEN_PAR");
            condition();
            eat("CLOSE_PAR");
        } else {
            condition();
        }
    }

    private void whileStmt() {
        stmtPrefix();

        if (peek().getType().matches("INT|FLOAT|CHAR")) {
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
        eat("IDENTIFIER");
        eat("CLOSE_PAR");
        eat("SYMBOL");
    }

    private void writeStmt() {
        eat("OUT");
        eat("OPEN_PAR");
        writable();
        eat("CLOSE_PAR");
        eat("SYMBOL");
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

    private void expression() {
        simpleExpr();
        if (peek().getType().equals("REL_OP")) {
            eat("REL_OP");
            simpleExpr();
        }
    }

    private void simpleExpr() {
        term();
        while (peek().getType().equals("ADD_OP")) {
            eat("ADD_OP");
            term();
        }
    }

    private void term() {
        factorA();
        while (peek().getType().equals("MUL_OP")) {
            eat("MUL_OP");
            factorA();
        }
    }

    private void factorA() {
        if (peek().getLexeme().equals("!")) {
            eat("SYMBOL");
            factor();
        } else if (peek().getLexeme().equals("-")) {
            eat("ADD_OP");
            factor();
        } else {
            factor();
        }
    }

    private void factor() {
        if (peek().getType().equals("IDENTIFIER")) {
            eat("IDENTIFIER");
        } else if (peek().getType().matches("INTEGER_CONST|FLOAT_CONST|CHAR_CONST")) {
            eat(peek().getType());
        } else if (peek().getType().equals("OPEN_PAR")) {
            eat("OPEN_PAR");
            expression();
            eat("CLOSE_PAR");
        } else {
            String error = "Erro sintático na linha " + peek().getLine() + ": fator inválido '" + peek().getLexeme() + "'";
            System.out.println(error);
            throw new RuntimeException(error);
        }
    }
}
