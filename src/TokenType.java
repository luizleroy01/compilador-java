package src;

public enum TokenType {
        PROGRAM, BEGIN, END, INT, FLOAT, CHAR,
        IF, THEN, ELSE, WHILE, DO, REPEAT, UNTIL,
        IN, OUT, IDENTIFIER, ASSIGN, // "="
        INTEGER_CONST, FLOAT_CONST, CHAR_CONST,
        PLUS, MINUS, OR, MULT, DIV, AND,
        EQ, GT, GE, LT, LE, NE,
        LPAREN, RPAREN, SEMICOLON, COMMA, COLON,
        STRING_LITERAL
}
