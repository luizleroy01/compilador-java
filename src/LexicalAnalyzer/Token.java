package src.LexicalAnalyzer;

public class Token {
    private String type;
    private String lexeme;
    private int line;

    public Token(String type, String lexeme,int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public String getType(){
        return this.type;
    }

    public String getLexeme(){
        return this.lexeme;
    }

    public int getLine(){
        return this.line;
    }

    @Override
    public String toString() {
        return type + ": '" + lexeme + "'";
    }
}
