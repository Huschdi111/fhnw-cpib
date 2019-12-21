package ch.fhnw.cpib.platform.parser;

import ch.fhnw.cpib.platform.checker.Symbol;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.parser.exception.ParserException;
import ch.fhnw.cpib.platform.scanner.tokens.Terminal;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public class ParserTest {
    public static void main(String[] args) throws ParserException {
        Parser parser = new Parser();
        TokenList tokenList = new TokenList();
        tokenList.addToken(new Tokens.Token(Terminal.PROGRAM));
        tokenList.addToken(new Tokens.IdentifierToken("Assoc",Terminal.IDENT));
        tokenList.addToken(new Tokens.Token(Terminal.LPAREN));
        tokenList.addToken(new Tokens.Token(Terminal.RPAREN));
        tokenList.addToken(new Tokens.Token(Terminal.GLOBAL));
        tokenList.addToken(new Tokens.IdentifierToken("x", Terminal.IDENT));
        tokenList.addToken(new Tokens.Token(Terminal.COLON));
        tokenList.addToken(new Tokens.TypeToken(Terminal.TYPE, Tokens.TypeToken.Type.INT));
        tokenList.addToken(new Tokens.Token(Terminal.DO));
        tokenList.addToken(new Tokens.Token(Terminal.GUARDEDIF));
        tokenList.addToken(new Tokens.Token(Terminal.GUARDOPR));
        tokenList.addToken(new Tokens.IdentifierToken("x", Terminal.IDENT));
        tokenList.addToken(new Tokens.RelOprToken(Terminal.RELOPR,Tokens.RelOprToken.RelOpr.EQ));
        tokenList.addToken(new Tokens.LiteralToken("1",Terminal.LITERAL));
        tokenList.addToken(new Tokens.Token(Terminal.ARROWOPR));
        tokenList.addToken(new Tokens.IdentifierToken("x", Terminal.IDENT));
        tokenList.addToken(new Tokens.Token(Terminal.BECOMES));
        tokenList.addToken(new Tokens.LiteralToken("2",Terminal.LITERAL));
        tokenList.addToken(new Tokens.Token(Terminal.GUARDEDENDIF));
        tokenList.addToken(new Tokens.Token(Terminal.ENDPROGRAM));
        tokenList.addToken(new Tokens.Token(Terminal.SENTINEL));
        ConcreteTree.Program prog = parser.parseTokenList(tokenList);
        AbstractTree.Program absProg =  prog.toAbstract();

    }
}
