package ch.fhnw.cpib.platform.scanner.states;

import ch.fhnw.cpib.platform.scanner.dictionary.Dictionary;
import ch.fhnw.cpib.platform.scanner.exception.ScannerException;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;

public abstract class State {

    private final TokenList tokenlist;

    private final Dictionary dictionary;

    private final StringBuilder stringbuilder;

    private int row;

    private int column;

    protected State(TokenList tokenList, Dictionary dictionary) {
        this.tokenlist = tokenList;
        this.dictionary = dictionary;
        this.stringbuilder = new StringBuilder();
        this.row = 1;
        this.column = 1;
    }

    protected State(State state) {
        this.tokenlist = state.tokenlist;
        this.dictionary = state.dictionary;
        this.stringbuilder = new StringBuilder();
        this.row = state.row;
        this.column = state.column;
    }

    protected State(State state, Character character) {
        this(state);
        addTokenElement(character);
    }

    public TokenList getTokenList() {
        return tokenlist;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    protected String getToken() {
        return stringbuilder.toString();
    }

    protected void addTokenElement(Character character) {
        stringbuilder.append(character);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public State handleNewline() throws ScannerException {
        throw new ScannerException("Unable to handle a newline");
    }

    public State handleLetter(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a " + character);
    }

    public State handleDigit(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a " + character);
    }

    public State handleSpace(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a space");
    }

    public State handleUnderScore(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle an " + character + " on " + row + ":" + column);
    }

    public State handleDash(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a " + character + " on " + row + ":" + column);
    }

    public State handleSlash(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a " + character + " on " + row + ":" + column);
    }

    public State handleBackSlash(Character character) throws ScannerException{
        throw new ScannerException("Unable to handle a " + character + " on " + row + ":" + column);
    }
    public State handleOperator(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle a " + character + " on " + row + ":" + column);
    }

    public State handleUnknown(Character character) throws ScannerException {
        throw new ScannerException("Unable to handle the unknown character " + character + " on " + row + ":" + column);
    }
}
