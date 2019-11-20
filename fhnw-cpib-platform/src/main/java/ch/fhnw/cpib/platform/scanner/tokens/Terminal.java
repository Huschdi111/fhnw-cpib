package ch.fhnw.cpib.platform.scanner.tokens;

public enum Terminal {

    // Identity
    IDENT("IDENT"),

    // Literal
    LITERAL("LITERAL"),

    // Operator
    MULTOPR("MULTOPR"),
    ADDOPR("ADDOPR"),
    RELOPR("RELOPR"),
    BOOLOPR("BOOLOPR"),

    // Type
    TYPE("TYPE"),

    // Symbols
    COLON(":"),
    COMMA(","),
    SEMICOLON(";"),
    BECOMES(":="),
    LPAREN("("),
    RPAREN(")"),

    // Modes
    FLOWMODE("FLOWMODE"),
    CHANGEMODE("CHANGEMODE"),
    MECHMODE("MECHMODE"),

    // Keywords
    PROGRAM("PROGRAM"),
    CALL("CALL"),
    CASE("CASE"),
    DEFAULT("DEFAULT"),
    IF("IF"),
    GUARDEDIF("GUARDEDIF"),
    ELSE("ELSE"),
    ELSEIF("ELSEIF"),
    ENDIF("ENDIF"),
    DEBUGIN("DEBUGIN"),
    DEBUGOUT("DEBUGOUT"),
    NOT("NOT"),
    FUN("FUN"),
    GLOBAL("GLOBAL"),
    LOCAL("LOCAL"),
    INIT("INIT"),
    PROC("PROC"),
    RETURNS("RETURNS"),
    SKIP("SKIP"),
    SWITCH("SWITCH"),
    THEN("THEN"),
    WHILE("WHILE"),
    DO("DO"),
    ENDWHILE("ENDWHILE"),
    ENDSWITCH("ENDSWITCH"),
    ENDPROGRAM("ENDPROGRAM"),
    ENDFUN("ENDFUN"),
    ENDPROC("ENDPROC"),
    SENTINEL("SENTINEL");

    private final String name;

    Terminal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
