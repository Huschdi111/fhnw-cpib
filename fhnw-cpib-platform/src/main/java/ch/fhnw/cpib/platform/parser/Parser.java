package ch.fhnw.cpib.platform.parser;

import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.parser.concretetree.Context;
import ch.fhnw.cpib.platform.parser.exception.ParserException;
import ch.fhnw.cpib.platform.scanner.tokens.Terminal;
import ch.fhnw.cpib.platform.scanner.tokens.TokenList;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public class Parser {

    public Parser() {
    }

    public ConcreteTree.Program parseTokenList(TokenList tokenlist) throws ParserException {
        // Get the first token to start with
        tokenlist.resetCounter();
        Tokens.Token token = tokenlist.nextToken();

        // Initialize the context
        Context context = new Context(tokenlist);
        context.setToken(token);
        context.setTerminal(token.getTerminal());

        // Parse the beginning
        ConcreteTree.Program program = parseProgram(context);
        consumeTerminal(context, Terminal.SENTINEL);
        return program;
    }

    private Tokens.Token consumeTerminal(Context context, Terminal expectedterminal) throws ParserException {
        if (context.getTerminal() == expectedterminal) {
            Tokens.Token consumedtoken = context.getToken();
            if (context.getTerminal() != Terminal.SENTINEL) {
                Tokens.Token token = context.getTokenList().nextToken();
                context.setToken(token);
                context.setTerminal(token.getTerminal());
            }
            return consumedtoken;
        } else {
            throw new ParserException("Parser expected the terminal " + expectedterminal + ", but found the terminal " + context.getTerminal());
        }
    }

    private ConcreteTree.Program parseProgram(Context context) throws ParserException {
        switch (context.getTerminal()) {
            case PROGRAM:
                consumeTerminal(context, Terminal.PROGRAM);
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.ProgParamList progparamlist = parseProgParamList(context, 1);
                ConcreteTree.OptCpsDecl optcpsdecl = parseOptCpsDecl(context, 1);
                consumeTerminal(context, Terminal.DO);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, 1);
                consumeTerminal(context, Terminal.ENDPROGRAM);
                return new ConcreteTree.Program(identifier, progparamlist, optcpsdecl, cpscmd, 0);
            default:
                throw new ParserException("Invalid terminal in program(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Decl parseDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case CHANGEMODE:
            case IDENT:
                ConcreteTree.StoDecl stodecl = parseStoDecl(context, idendation + 1);
                return new ConcreteTree.DeclSto(stodecl, idendation);
            case FUN:
                ConcreteTree.FunDecl fundecl = parseFunDecl(context, idendation + 1);
                return new ConcreteTree.DeclFun(fundecl, idendation);
            case PROC:
                ConcreteTree.ProcDecl procdecl = parseProcDecl(context, idendation + 1);
                return new ConcreteTree.DeclProc(procdecl, idendation);
            default:
                throw new ParserException("Invalid terminal in decl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.StoDecl parseStoDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case IDENT:
                ConcreteTree.TypedIdent typedident1 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.StoDeclIdent(typedident1, idendation);
            case CHANGEMODE:
                Tokens.ChangeModeToken changemode1 = (Tokens.ChangeModeToken) consumeTerminal(context, Terminal.CHANGEMODE);
                ConcreteTree.TypedIdent typedident2 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.StoDeclChangemode(changemode1, typedident2, idendation);
            default:
                throw new ParserException("Invalid terminal in stoDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.FunDecl parseFunDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case FUN:
                consumeTerminal(context, Terminal.FUN);
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.ParamList paramlist = parseParamList(context, idendation + 1);
                consumeTerminal(context, Terminal.RETURNS);
                ConcreteTree.StoDecl stodecl = parseStoDecl(context, idendation + 1);
                ConcreteTree.OptGlobImps optglobimps = parseOptGlobImps(context, idendation + 1);
                ConcreteTree.OptCpsStoDecl optcpsstodecl = parseOptCpsStoDecl(context, idendation + 1);
                consumeTerminal(context, Terminal.DO);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                consumeTerminal(context, Terminal.ENDFUN);
                return new ConcreteTree.FunDecl(identifier, paramlist, stodecl, optglobimps, optcpsstodecl, cpscmd, idendation);
            default:
                throw new ParserException("Invalid terminal in funDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ProcDecl parseProcDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case PROC:
                consumeTerminal(context, Terminal.PROC);
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.ParamList paramlist = parseParamList(context, idendation + 1);
                ConcreteTree.OptGlobImps optglobimps = parseOptGlobImps(context, idendation + 1);
                ConcreteTree.OptCpsStoDecl optcpsstodecl = parseOptCpsStoDecl(context, idendation + 1);
                consumeTerminal(context, Terminal.DO);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                consumeTerminal(context, Terminal.ENDPROC);
                return new ConcreteTree.ProcDecl(identifier, paramlist, optglobimps, optcpsstodecl, cpscmd, idendation);
            default:
                throw new ParserException("Invalid terminal in procDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());

        }
    }

    private ConcreteTree.OptGlobImps parseOptGlobImps(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case GLOBAL:
                consumeTerminal(context, Terminal.GLOBAL);
                ConcreteTree.GlobImps globimps = parseGlobImps(context, idendation + 1);
                return new ConcreteTree.OptGlobImpsGlobal(globimps, idendation);
            case DO:
            case LOCAL:
                return new ConcreteTree.OptGlobImpsEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optGlobImps(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.GlobImps parseGlobImps(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case FLOWMODE:
            case IDENT:
            case CHANGEMODE:
                ConcreteTree.GlobImp globimp = parseGlobImp(context, idendation + 1);
                ConcreteTree.RepGlobImps repglobimps = parseRepGlobImps(context, idendation + 1);
                return new ConcreteTree.GlobImps(globimp, repglobimps, idendation);
            default:
                throw new ParserException("Invalid terminal in globImps(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepGlobImps parseRepGlobImps(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case COMMA:
                consumeTerminal(context, Terminal.COMMA);
                ConcreteTree.GlobImp globimp = parseGlobImp(context, idendation + 1);
                ConcreteTree.RepGlobImps repglobimps = parseRepGlobImps(context, idendation + 1);
                return new ConcreteTree.RepGlobImpsComma(globimp, repglobimps, idendation);
            case DO:
            case LOCAL:
                return new ConcreteTree.RepGlobImpsEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repGlobImps(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptChangemode parseOptChangemode(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case CHANGEMODE:
                Tokens.ChangeModeToken changemode = (Tokens.ChangeModeToken) consumeTerminal(context, Terminal.CHANGEMODE);
                return new ConcreteTree.OptChangemodeChangemode(changemode, idendation);
            case IDENT:
                return new ConcreteTree.OptChangemodeEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optChangemode(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptMechmode parseOptMechmode(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case MECHMODE:
                Tokens.MechModeToken mechmode = (Tokens.MechModeToken) consumeTerminal(context, Terminal.MECHMODE);
                return new ConcreteTree.OptMechmodeMechmode(mechmode, idendation);
            case IDENT:
            case CHANGEMODE:
                return new ConcreteTree.OptMechmodeEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optMechmode(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.GlobImp parseGlobImp(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case IDENT:
            case CHANGEMODE:
                ConcreteTree.OptChangemode optchangemode1 = parseOptChangemode(context, idendation + 1);
                Tokens.IdentifierToken identifier1 = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                return new ConcreteTree.GlobImpExpression(optchangemode1, identifier1, idendation);
            case FLOWMODE:
                Tokens.FlowModeToken flowmode1 = (Tokens.FlowModeToken) consumeTerminal(context, Terminal.FLOWMODE);
                ConcreteTree.OptChangemode optchangemode2 = parseOptChangemode(context, idendation + 1);
                Tokens.IdentifierToken identifier2 = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                return new ConcreteTree.GlobImpFlowmode(flowmode1, optchangemode2, identifier2, idendation);
            default:
                throw new ParserException("Invalid terminal in globImp(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptCpsDecl parseOptCpsDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case GLOBAL:
                consumeTerminal(context, Terminal.GLOBAL);
                ConcreteTree.CpsDecl cpsecl = parseCpsDecl(context, idendation + 1);
                return new ConcreteTree.OptCpsDeclGlobal(cpsecl, idendation);
            case DO:
                return new ConcreteTree.OptCpsDeclEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optCpsDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.CpsDecl parseCpsDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case PROC:
            case FUN:
            case CHANGEMODE:
            case IDENT:
                ConcreteTree.Decl decl = parseDecl(context, idendation + 1);
                ConcreteTree.RepCpsDecl repcpsdecl = parseRepCpsDecl(context, idendation + 1);
                return new ConcreteTree.CpsDecl(decl, repcpsdecl, idendation);
            default:
                throw new ParserException("Invalid terminal in cpsDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepCpsDecl parseRepCpsDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case SEMICOLON:
                consumeTerminal(context, Terminal.SEMICOLON);
                ConcreteTree.Decl decl = parseDecl(context, idendation + 1);
                ConcreteTree.RepCpsDecl repcpsdecl = parseRepCpsDecl(context, idendation + 1);
                return new ConcreteTree.RepCpsDeclSemicolon(decl, repcpsdecl, idendation);
            case DO:
                return new ConcreteTree.RepCpsDeclEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repCpsDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptCpsStoDecl parseOptCpsStoDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LOCAL:
                consumeTerminal(context, Terminal.LOCAL);
                ConcreteTree.CpsStoDecl cpsstodecl = parseCpsStoDecl(context, idendation + 1);
                return new ConcreteTree.OptCpsStoDeclLocal(cpsstodecl, idendation);
            case DO:
                return new ConcreteTree.OptCpsStoDeclEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optCpsStoDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.CpsStoDecl parseCpsStoDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case CHANGEMODE:
            case IDENT:
                ConcreteTree.StoDecl stodecl = parseStoDecl(context, idendation + 1);
                ConcreteTree.RepCpsStoDecl repcpsstodecl = parseRepCpsStoDecl(context, idendation + 1);
                return new ConcreteTree.CpsStoDecl(stodecl, repcpsstodecl, idendation);
            default:
                throw new ParserException("Invalid terminal in cpsStoDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepCpsStoDecl parseRepCpsStoDecl(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case SEMICOLON:
                consumeTerminal(context, Terminal.SEMICOLON);
                ConcreteTree.StoDecl stodecl = parseStoDecl(context, idendation + 1);
                ConcreteTree.RepCpsStoDecl repcpsstodecl = parseRepCpsStoDecl(context, idendation + 1);
                return new ConcreteTree.RepCpsStoDeclSemicolon(stodecl, repcpsstodecl, idendation);
            case DO:
                return new ConcreteTree.RepCpsStoDeclEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repCpsStoDecl(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ProgParamList parseProgParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
                consumeTerminal(context, Terminal.LPAREN);
                ConcreteTree.OptProgParamList optprogparamlist = parseOptProgParamList(context, idendation + 1);
                consumeTerminal(context, Terminal.RPAREN);
                return new ConcreteTree.ProgParamList(optprogparamlist, idendation);
            default:
                throw new ParserException("Invalid terminal in progParamList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptProgParamList parseOptProgParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case FLOWMODE:
            case IDENT:
            case CHANGEMODE:
                ConcreteTree.ProgParam progparam = parseProgParam(context, idendation + 1);
                ConcreteTree.RepProgParamList repprogparamlist = parseRepProgParamList(context, idendation + 1);
                return new ConcreteTree.OptProgParamListExpression(progparam, repprogparamlist, idendation);
            case RPAREN:
                return new ConcreteTree.OptProgParamListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optProgParamList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepProgParamList parseRepProgParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case COMMA:
                consumeTerminal(context, Terminal.COMMA);
                ConcreteTree.ProgParam progparam = parseProgParam(context, idendation + 1);
                ConcreteTree.RepProgParamList repprogparamlist = parseRepProgParamList(context, idendation + 1);
                return new ConcreteTree.RepProgParamListComma(progparam, repprogparamlist, idendation);
            case RPAREN:
                return new ConcreteTree.RepProgParamListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repProgParamList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ProgParam parseProgParam(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case IDENT:
            case CHANGEMODE:
                ConcreteTree.OptChangemode optchangemode2 = parseOptChangemode(context, idendation + 1);
                ConcreteTree.TypedIdent typeident2 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.ProgParamExpression(optchangemode2, typeident2, idendation);
            case FLOWMODE:
                Tokens.FlowModeToken flowmode1 = (Tokens.FlowModeToken) consumeTerminal(context, Terminal.FLOWMODE);
                ConcreteTree.OptChangemode optchangemode1 = parseOptChangemode(context, idendation + 1);
                ConcreteTree.TypedIdent typeident1 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.ProgParamFlowmode(flowmode1, optchangemode1, typeident1, idendation);

            default:
                throw new ParserException("Invalid terminal in progParam(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ParamList parseParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
                consumeTerminal(context, Terminal.LPAREN);
                ConcreteTree.OptParamList optparamlist = parseOptParamList(context, idendation + 1);
                consumeTerminal(context, Terminal.RPAREN);
                return new ConcreteTree.ParamList(optparamlist, idendation);
            default:
                throw new ParserException("Invalid terminal in paramList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptParamList parseOptParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case FLOWMODE:
            case IDENT:
            case CHANGEMODE:
            case MECHMODE:
                ConcreteTree.Param param = parseParam(context, idendation + 1);
                ConcreteTree.RepParamList repparamlist = parseRepParamList(context, idendation + 1);
                return new ConcreteTree.OptParamListRepeating(param, repparamlist, idendation);
            case RPAREN:
                return new ConcreteTree.OptParamListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optParamList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepParamList parseRepParamList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case COMMA:
                consumeTerminal(context, Terminal.COMMA);
                ConcreteTree.Param param = parseParam(context, idendation + 1);
                ConcreteTree.RepParamList repparamlist = parseRepParamList(context, idendation + 1);
                return new ConcreteTree.RepParamListComma(param, repparamlist, idendation);
            case RPAREN:
                return new ConcreteTree.RepParamListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repParamList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Param parseParam(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case IDENT:
            case CHANGEMODE:
            case MECHMODE:
                ConcreteTree.OptMechmode optmechmode1 = parseOptMechmode(context, idendation + 1);
                ConcreteTree.OptChangemode optchangemode1 = parseOptChangemode(context, idendation + 1);
                ConcreteTree.TypedIdent typedindent1 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.ParamExpression(optmechmode1, optchangemode1, typedindent1, idendation);
            case FLOWMODE:
                Tokens.FlowModeToken flowmode1 = (Tokens.FlowModeToken) consumeTerminal(context, Terminal.FLOWMODE);
                ConcreteTree.OptMechmode optmechmode2 = parseOptMechmode(context, idendation + 1);
                ConcreteTree.OptChangemode optchangemode2 = parseOptChangemode(context, idendation + 1);
                ConcreteTree.TypedIdent typedindent2 = parseTypedIdent(context, idendation + 1);
                return new ConcreteTree.ParamFlowmode(flowmode1, optmechmode2, optchangemode2, typedindent2, idendation);
            default:
                throw new ParserException("Invalid terminal in param(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.TypedIdent parseTypedIdent(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case IDENT:
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                consumeTerminal(context, Terminal.COLON);
                Tokens.TypeToken type = (Tokens.TypeToken) consumeTerminal(context, Terminal.TYPE);
                return new ConcreteTree.TypedIdent(identifier, type.getType(), idendation);
            default:
                throw new ParserException("Invalid terminal in typedIdent(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Cmd parseCmd(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case SKIP:
                consumeTerminal(context, Terminal.SKIP);
                return new ConcreteTree.CmdSkip(idendation);
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Expr expr1 = parseExpr(context, idendation + 1);
                ConcreteTree.RepExprList repexprlist1 = parseRepExprList(context, idendation + 1);
                consumeTerminal(context, Terminal.BECOMES);
                ConcreteTree.Expr expr2 = parseExpr(context, idendation + 1);
                ConcreteTree.RepExprList repexprlist2 = parseRepExprList(context, idendation + 1);
                return new ConcreteTree.CmdExpression(expr1, repexprlist1, expr2, repexprlist2, idendation);
            case IF:
                consumeTerminal(context, Terminal.IF);
                ConcreteTree.Expr expr3 = parseExpr(context, idendation + 1);
                consumeTerminal(context, Terminal.THEN);
                ConcreteTree.CpsCmd cpscmd1 = parseCpsCmd(context, idendation + 1);
                ConcreteTree.RepElseif repelseif = parseRepElseif(context, idendation + 1);
                ConcreteTree.OptElse optelse1 = parseOptElse(context, idendation + 1);
                consumeTerminal(context, Terminal.ENDIF);
                return new ConcreteTree.CmdIf(expr3, cpscmd1, repelseif, optelse1, idendation);
            case GUARDEDIF:
                consumeTerminal(context, Terminal.GUARDEDIF);
                ConcreteTree.Expr expr4 = parseExpr(context, idendation + 1);
                parseArrowTerm(context, idendation);
                parseCpsArrowTerm(context, idendation);
                consumeTerminal(context, Terminal.GUARDEDENDIF);
            case SWITCH:
                consumeTerminal(context, Terminal.SWITCH);
                ConcreteTree.Expr expr5 = parseExpr(context, idendation + 1);
                consumeTerminal(context, Terminal.CASE);
                Tokens.LiteralToken literal1 = (Tokens.LiteralToken) consumeTerminal(context, Terminal.LITERAL);
                consumeTerminal(context, Terminal.THEN);
                ConcreteTree.CpsCmd cpscmd2 = parseCpsCmd(context, idendation + 1);
                ConcreteTree.RepCase repcase1 = parseRepCase(context, idendation + 1);
                ConcreteTree.OptDefault optdefault1 = parseOptDefault(context, idendation + 1);
                consumeTerminal(context, Terminal.ENDSWITCH);
                return new ConcreteTree.CmdSwitch(expr5, literal1, cpscmd2, repcase1, optdefault1, idendation);
            case WHILE:
                consumeTerminal(context, Terminal.WHILE);
                ConcreteTree.Expr expr6 = parseExpr(context, idendation + 1);
                consumeTerminal(context, Terminal.DO);
                ConcreteTree.CpsCmd cpscmd3 = parseCpsCmd(context, idendation + 1);
                consumeTerminal(context, Terminal.ENDWHILE);
                return new ConcreteTree.CmdWhile(expr6, cpscmd3, idendation);
            case CALL:
                consumeTerminal(context, Terminal.CALL);
                Tokens.IdentifierToken identifier1 = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.ExprList exprlist1 = parseExprList(context, idendation + 1);
                ConcreteTree.OptGlobInits optglobinits1 = parseOptGlobInits(context, idendation + 1);
                return new ConcreteTree.CmdCall(identifier1, exprlist1, optglobinits1, idendation);
            case DEBUGIN:
                consumeTerminal(context, Terminal.DEBUGIN);
                ConcreteTree.Expr expr7 = parseExpr(context, idendation + 1);
                return new ConcreteTree.CmdDebugIn(expr7, idendation);
            case DEBUGOUT:
                consumeTerminal(context, Terminal.DEBUGOUT);
                ConcreteTree.Expr expr8 = parseExpr(context, idendation + 1);
                return new ConcreteTree.CmdDebugOut(expr8, idendation);
            default:
                throw new ParserException("Invalid terminal in cmd(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.CpsCmd parseCpsCmd(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case DEBUGOUT:
            case DEBUGIN:
            case CALL:
            case WHILE:
            case SWITCH:
            case IF:
            case GUARDEDIF:
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
            case SKIP:
                ConcreteTree.Cmd cmd = parseCmd(context, idendation + 1);
                ConcreteTree.RepCpsCmd repcpscmd = parseRepCpsCmd(context, idendation + 1);
                return new ConcreteTree.CpsCmd(cmd, repcpscmd, idendation);
            default:
                throw new ParserException("Invalid terminal in cpsCmd(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepCpsCmd parseRepCpsCmd(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case SEMICOLON:
                consumeTerminal(context, Terminal.SEMICOLON);
                ConcreteTree.Cmd cmd = parseCmd(context, idendation + 1);
                ConcreteTree.RepCpsCmd repcpscmd = parseRepCpsCmd(context, idendation + 1);
                return new ConcreteTree.RepCpsCmdSemicolon(cmd, repcpscmd, idendation);
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
                return new ConcreteTree.RepCpsCmdEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repCpsCmd(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptGlobInits parseOptGlobInits(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case INIT:
                consumeTerminal(context, Terminal.INIT);
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.RepIdents repidents = parseRepIdents(context, idendation + 1);
                return new ConcreteTree.OptGlobInitsInit(identifier, repidents, idendation);
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
                return new ConcreteTree.OptGlobInitsEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optGlobInits(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepIdents parseRepIdents(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case COMMA:
                consumeTerminal(context, Terminal.COMMA);
                Tokens.IdentifierToken identifier = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.RepIdents repidents = parseRepIdents(context, idendation + 1);
                return new ConcreteTree.RepIdentsComma(identifier, repidents, idendation);
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
                return new ConcreteTree.RepIdentsEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repIdents(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Expr parseExpr(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Term1 term1 = parseTerm1(context, idendation + 1);
                ConcreteTree.RepTerm1 repterm1 = parseRepTerm1(context, idendation + 1);
                return new ConcreteTree.Expr(term1, repterm1, idendation);
            default:
                throw new ParserException("Invalid terminal in expr(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepTerm1 parseRepTerm1(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case BOOLOPR:
                Tokens.BoolOprToken boolopr1 = (Tokens.BoolOprToken) consumeTerminal(context, Terminal.BOOLOPR);
                ConcreteTree.Term1 term1 = parseTerm1(context, idendation + 1);
                ConcreteTree.RepTerm1 repterm1 = parseRepTerm1(context, idendation + 1);
                return new ConcreteTree.RepTerm1BoolOpr(boolopr1, term1, repterm1, idendation);
            case RPAREN:
            case DO:
            case THEN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
            case COMMA:
                return new ConcreteTree.RepTerm1Epsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repTerm2(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Term1 parseTerm1(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Term2 term2 = parseTerm2(context, idendation + 1);
                ConcreteTree.RepTerm2 repterm2 = parseRepTerm2(context, idendation + 1);
                return new ConcreteTree.Term1(term2, repterm2, idendation);
            default:
                throw new ParserException("Invalid terminal in term1(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepTerm2 parseRepTerm2(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case RELOPR:
                Tokens.RelOprToken relopr1 = (Tokens.RelOprToken) consumeTerminal(context, Terminal.RELOPR);
                ConcreteTree.Term2 term2 = parseTerm2(context, idendation + 1);
                ConcreteTree.RepTerm2 repterm2 = parseRepTerm2(context, idendation + 1);
                return new ConcreteTree.RepTerm2RelOpr(relopr1, term2, repterm2, idendation);
            case RPAREN:
            case DO:
            case THEN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
            case COMMA:
            case BOOLOPR:
                return new ConcreteTree.RepTerm2Epsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repTerm2(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Term2 parseTerm2(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Term3 term3 = parseTerm3(context, idendation + 1);
                ConcreteTree.RepTerm3 repterm3 = parseRepTerm3(context, idendation + 1);
                return new ConcreteTree.Term2(term3, repterm3, idendation);
            default:
                throw new ParserException("Invalid terminal in term2(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepTerm3 parseRepTerm3(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case ADDOPR:
                Tokens.AddOprToken addopr = (Tokens.AddOprToken) consumeTerminal(context, Terminal.ADDOPR);
                ConcreteTree.Term3 term3 = parseTerm3(context, idendation + 1);
                ConcreteTree.RepTerm3 repterm3 = parseRepTerm3(context, idendation + 1);
                return new ConcreteTree.RepTerm3AddOpr(addopr, term3, repterm3, idendation);
            case RPAREN:
            case DO:
            case THEN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
            case COMMA:
            case BOOLOPR:
            case RELOPR:
                return new ConcreteTree.RepTerm3Epsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repTerm3(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Term3 parseTerm3(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Factor factor = parseFactor(context, idendation + 1);
                ConcreteTree.RepFactor repfactor = parseRepFactor(context, idendation + 1);
                return new ConcreteTree.Term3(factor, repfactor, idendation);
            default:
                throw new ParserException("Invalid terminal in term3(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepFactor parseRepFactor(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case MULTOPR:
                Tokens.MultOprToken multopr = (Tokens.MultOprToken) consumeTerminal(context, Terminal.MULTOPR);
                ConcreteTree.Factor factor = parseFactor(context, idendation + 1);
                ConcreteTree.RepFactor repfactor = parseRepFactor(context, idendation + 1);
                return new ConcreteTree.RepFactorMultOpr(multopr, factor, repfactor, idendation);
            case RPAREN:
            case DO:
            case THEN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
            case COMMA:
            case BOOLOPR:
            case RELOPR:
            case ADDOPR:
                return new ConcreteTree.RepFactorEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repFactor(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.Factor parseFactor(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LITERAL:
                Tokens.LiteralToken literal1 = (Tokens.LiteralToken) consumeTerminal(context, Terminal.LITERAL);
                return new ConcreteTree.FactorLiteral(literal1, idendation);
            case IDENT:
                Tokens.IdentifierToken identifier1 = (Tokens.IdentifierToken) consumeTerminal(context, Terminal.IDENT);
                ConcreteTree.OptInitOrExprList optinitorexprlist1 = parseOptInitOrExprList(context, idendation + 1);
                return new ConcreteTree.FactorIdent(identifier1, optinitorexprlist1, idendation);
            case ADDOPR:
            case NOT:
                ConcreteTree.MonadicOpr monadicopr1 = parseMonadicOpr(context, idendation + 1);
                ConcreteTree.Factor factor1 = parseFactor(context, idendation + 1);
                return new ConcreteTree.FactorExpression(monadicopr1, factor1, idendation);
            case LPAREN:
                consumeTerminal(context, Terminal.LPAREN);
                ConcreteTree.Expr expr1 = parseExpr(context, idendation + 1);
                consumeTerminal(context, Terminal.RPAREN);
                return new ConcreteTree.FactorLparen(expr1, idendation);
            default:
                throw new ParserException("Invalid terminal in factor(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptInitOrExprList parseOptInitOrExprList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case INIT:
                consumeTerminal(context, Terminal.INIT);
                return new ConcreteTree.OptInitOrExprListInit(idendation);
            case LPAREN:
                ConcreteTree.ExprList exprlist = parseExprList(context, idendation + 1);
                return new ConcreteTree.OptInitOrExprListLparen(exprlist, idendation);
            case RPAREN:
            case DO:
            case THEN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case ELSE:
            case ELSEIF:
            case GUARDEDENDIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
            case COMMA:
            case BOOLOPR:
            case RELOPR:
            case ADDOPR:
            case MULTOPR:
                return new ConcreteTree.OptInitOrExprListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optInitOrExprList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.MonadicOpr parseMonadicOpr(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case NOT:
                // FIXME: Fix known errata
                Tokens.BoolOprToken boolopr = (Tokens.BoolOprToken) consumeTerminal(context, Terminal.NOT);
                return new ConcreteTree.MonadicOprNot(boolopr, idendation);
            case ADDOPR:
                Tokens.AddOprToken addopr = (Tokens.AddOprToken) consumeTerminal(context, Terminal.ADDOPR);
                return new ConcreteTree.MonadicOprAddopr(addopr, idendation);
            default:
                throw new ParserException("Invalid terminal in monadicOpr(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ExprList parseExprList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
                consumeTerminal(context, Terminal.LPAREN);
                ConcreteTree.OptExprList optexprlist = parseOptExprList(context, idendation + 1);
                consumeTerminal(context, Terminal.RPAREN);
                return new ConcreteTree.ExprList(optexprlist, idendation);
            default:
                throw new ParserException("Invalid terminal in exprList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptExprList parseOptExprList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case LPAREN:
            case ADDOPR:
            case NOT:
            case IDENT:
            case LITERAL:
                ConcreteTree.Expr expr = parseExpr(context, idendation + 1);
                ConcreteTree.RepExprList repexprlist = parseRepExprList(context, idendation + 1);
                return new ConcreteTree.OptExprListExpression(expr, repexprlist, idendation);
            case RPAREN:
                return new ConcreteTree.OptExprListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optExprList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.ArrowTerm parseArrowTerm (Context context, int identation) throws ParserException {
        consumeTerminal(context, Terminal.GUARDOPR);
        //PARSE EXPRESSION
        consumeTerminal(context, Terminal.ARROWOPR);
        //PARSE CpsCmd
        return null;
    }

    private ConcreteTree.CpsArrowTerm parseCpsArrowTerm (Context context, int identation) throws ParserException {
        if (context.getTerminal() == Terminal.GUARDEDENDIF) {
          return null;
        }
        parseArrowTerm(context, identation + 1);
        //ConcreteTree.CpsArrowTerm;
        return null;
    }

    private ConcreteTree.RepExprList parseRepExprList(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case COMMA:
                consumeTerminal(context, Terminal.COMMA);
                ConcreteTree.Expr expr = parseExpr(context, idendation + 1);
                ConcreteTree.RepExprList repexprlist = parseRepExprList(context, idendation + 1);
                return new ConcreteTree.RepExprListComma(expr, repexprlist, idendation);
            case RPAREN:
            case ENDWHILE:
            case ENDSWITCH:
            case DEFAULT:
            case CASE:
            case ENDIF:
            case GUARDEDENDIF:
            case ELSE:
            case ELSEIF:
            case ENDPROC:
            case ENDFUN:
            case ENDPROGRAM:
            case SEMICOLON:
            case BECOMES:
                return new ConcreteTree.RepExprListEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repExprList(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepCase parseRepCase(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case CASE:
                consumeTerminal(context, Terminal.CASE);
                Tokens.LiteralToken literal = (Tokens.LiteralToken) consumeTerminal(context, Terminal.LITERAL);
                consumeTerminal(context, Terminal.THEN);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                ConcreteTree.RepCase repcase = parseRepCase(context, idendation + 1);
                return new ConcreteTree.RepCaseCase(literal, cpscmd, repcase, idendation);
            case ENDSWITCH:
            case DEFAULT:
                return new ConcreteTree.RepCaseEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repCase(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptDefault parseOptDefault(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case DEFAULT:
                consumeTerminal(context, Terminal.DEFAULT);
                consumeTerminal(context, Terminal.THEN);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                return new ConcreteTree.OptDefaultDefault(cpscmd, idendation);
            case ENDSWITCH:
                return new ConcreteTree.OptDefaultEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optDefault(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.OptElse parseOptElse(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case ELSE:
                consumeTerminal(context, Terminal.ELSE);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                return new ConcreteTree.OptElseElse(cpscmd, idendation);
            case ENDIF:
                return new ConcreteTree.OptElseEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in optElse(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }

    private ConcreteTree.RepElseif parseRepElseif(Context context, int idendation) throws ParserException {
        switch (context.getTerminal()) {
            case ELSEIF:
                consumeTerminal(context, Terminal.ELSEIF);
                ConcreteTree.Expr expr = parseExpr(context, idendation + 1);
                consumeTerminal(context, Terminal.THEN);
                ConcreteTree.CpsCmd cpscmd = parseCpsCmd(context, idendation + 1);
                ConcreteTree.RepElseif repelseif = parseRepElseif(context, idendation + 1);
                return new ConcreteTree.RepElseifElseif(expr, cpscmd, repelseif, idendation);
            case ENDIF:
            case ELSE:
                return new ConcreteTree.RepElseifEpsilon(idendation);
            default:
                throw new ParserException("Invalid terminal in repElseif(" + context.getToken().getRow() + ":" + context.getToken().getColumn() + "): " + context.getTerminal());
        }
    }
}
