package src.SyntacticAnalyzer;

import src.LexicalAnalyzer.Token;
import src.SemanticAnalyzer.SemanticAnalyzer;

import java.util.ArrayList;
import java.util.List;

import src.TableOfSymbols.Env;
import src.TableOfSymbols.Id;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<String> foundErrors = new ArrayList<>();
    private final SemanticAnalyzer semantic; // <<< NOVO

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
        semantic.declareVariable(idToken, varType); // <<< AGORA COM SEMÂNTICO

        while (peek().getLexeme().equals(",")) {
            eat("SYMBOL");
            idToken = peek();
            eat("IDENTIFIER");
            semantic.declareVariable(idToken, varType);
        }
    }

    private void stmtList() {
       // semantic.enterScope(); // <<< NOVO ESCOPO
        stmt();
        while (peek().getLexeme().equals(";")) {
            eat("SYMBOL");
            stmt();
        }
      //  semantic.exitScope(); // <<< FIM DO ESCOPO
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
        } else if(peek().getType().equals("ELSE")) {
            eat("ELSE");
            if (peek().getType().equals("IF")) {
                ifStmt();
            } else {
                stmt();
            }
        }else if(peek().getType().equals("UNTIL")){
            condition();// stmtSuffix();
        } else {
            String error = "Erro sintático na linha " + peek().getLine() + ": comando inválido '" + lexeme + "'";
            foundErrors.add(error);
            throw new RuntimeException(error);
        }
    }

    private void assignStmt() {
        Token idToken = peek();
        semantic.useVariable(idToken); // <<< VERIFICA SE FOI DECLARADO

        eat("IDENTIFIER");
        eat("ASSIGN_SYMBOL");
        simpleExpr();
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

        semantic.enterScope(); // <<< AGORA SIM CRIA UM NOVO ESCOPO
        if (peek().getLexeme().matches("int|float|char")) {
            declList();
        }
        stmtList();
        semantic.exitScope(); // <<< FIM DO ESCOPO

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

    private void expression() {
        simpleExpr();
        if (peek().getType().equals("REL_OP")) {
            eat("REL_OP");
            simpleExpr();
        }
    }

    private void simpleExpr() {
        term();
        simpleExprLinha();
    }

    private void simpleExprLinha() {
        while (peek().getType().equals("ADD_OP")) {
            eat("ADD_OP");
            term();
        }
    }

    private void term() {
        factorA();
        termLinha();
    }

    private void termLinha() {
        while (peek().getType().equals("MUL_OP")) {
            eat("MUL_OP");
            factorA();
        }
    }

    private void factorA() {
        if (peek().getLexeme().equals("!")) {
            eat("SYMBOL"); // ou outro tipo que você definiu
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
            semantic.useVariable(peek()); // <<< VERIFICA ANTES DE USAR
            eat("IDENTIFIER");
        } else if (peek().getType().matches("INTEGER_CONST|FLOAT_CONST|CHAR_CONST")) {
            eat(peek().getType());
        } else if (peek().getType().equals("OPEN_PAR")) {
            eat("OPEN_PAR");
            expression();
            eat("CLOSE_PAR");
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
