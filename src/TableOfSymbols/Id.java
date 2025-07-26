package src.TableOfSymbols;

import src.LexicalAnalyzer.Token;

public class Id {
    public String type;   // Tipo da variável (INT, FLOAT, CHAR)
    public String name;   // Nome da variável (lexema)

    public Id(Token token, String type) {
        this.name = token.getLexeme();
        this.type = type;
    }

    @Override
    public String toString() {
        return "Id(" + name + ", tipo=" + type + ")";
    }
}
