package ch.fhnw.cpib.compiler.scanner;

import java.util.ArrayList;
import java.util.List;

public class TokenList {

    private final List<Token> tokens;

    private int counter;

    public TokenList() {
        this.tokens = new ArrayList<>();
        this.counter = 0;
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public Token nextToken() {
        if (tokens.size() > counter) {
            Token token = tokens.get(counter);
            counter++;
            return token;
        } else {
            return null;
        }
    }

    public void resetCounter() {
        counter = 0;
    }
}
