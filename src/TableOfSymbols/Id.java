package src.TableOfSymbols;

import src.LexicalAnalyzer.Token;

public class Id {
    private Token token;
    private String type; // Tipos : int, float, char

    public Id(Token token, String type) {
        this.token = token;
        this.type = type;
    }

    public Token getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Id(" + token.getLexeme() + ", tipo=" + type + ")";
    }
}
