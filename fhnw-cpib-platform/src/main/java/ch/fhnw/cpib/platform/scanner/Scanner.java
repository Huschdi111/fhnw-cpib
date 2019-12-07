package ch.fhnw.cpib.platform.scanner;

import ch.fhnw.cpib.platform.scanner.dictionary.Dictionary;
import ch.fhnw.cpib.platform.scanner.exception.ScannerException;
import ch.fhnw.cpib.platform.scanner.states.InitialState;
import ch.fhnw.cpib.platform.scanner.states.State;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;

public class Scanner {
    //TODO Guardedif in scanner tätschen
    private final Dictionary dictionary;

    public Scanner() {
        this.dictionary = new Dictionary();
    }

    public TokenList scanString(String content) throws ScannerException {
        return createTokenList(sanitizeContent(content));
    }

    private String sanitizeContent(String content) throws ScannerException {
        content = content.replace("\r\n", "\n");
        content = content.replace("\t", " ");
        return content;
    }

    private TokenList createTokenList(String content) throws ScannerException {
        TokenList tokenlist = new TokenList();
        State state = new InitialState(tokenlist, dictionary);
        for (int i = 0; i < content.length(); i++) {
            char element = content.charAt(i);
            //System.out.println("[" + i + "]: " + element);
            if (element == '\n') {
                state = state.handleNewline();
                state.setRow(state.getRow() + 1);
                state.setColumn(0);
            } else if (Character.isLetter(element)) {
                state = state.handleLetter(element);
            } else if (Character.isDigit(element)) {
                state = state.handleDigit(element);
            } else if (Character.isSpaceChar(element)) {
                state = state.handleSpace(element);
            } else if (element == '\'') {
                state = state.handleDash(element);
            } else if (element == '/') {
                state = state.handleSlash(element);
            }else if (element == ' ') {
                state = state.handleSpace(element);
            } else if (element == '_') {
                state = state.handleUnderScore(element);
            }else if (String.valueOf(element).matches("[(),;:=*+-/<>&?!|]")) {
                state = state.handleOperator(element);
            } else {
                state = state.handleUnknown(element);
            }
            state.setColumn(state.getColumn() + 1);
        }
        // Add final sentinel token
        tokenlist.addToken(dictionary.lookupSentinel(state));
        return tokenlist;
    }
}
