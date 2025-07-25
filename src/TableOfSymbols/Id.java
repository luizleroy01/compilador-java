package src.TableOfSymbols;

import src.LexicalAnalyzer.Token;

public class Id {
    private Token token;
    private String type; // Tipo (int, float, char)
    private int address; // Endereço de memória ou posição (se necessário)

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

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Id(" + token.getLexeme() + ", tipo=" + type + ")";
    }
}
