package ch.fhnw.cpib.platform.parser.concretetree;

import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.checker.CheckerException;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.scanner.tokens.Terminal;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

public class ConcreteTree {

    public static class Program extends ConcreteNode {

        private final Tokens.IdentifierToken identifier;

        private final ProgParamList progparamlist;

        private final OptCpsDecl optcpsdecl;

        private final CpsCmd cpscmd;

        public Program(Tokens.IdentifierToken identifier, ProgParamList progparamlist, OptCpsDecl optcpsdecl, CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.progparamlist = progparamlist;
            this.optcpsdecl = optcpsdecl;
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<Program Name='" + identifier.getName() + "'>") + progparamlist + optcpsdecl + cpscmd + getHead("</Program>");
        }

        public AbstractTree.Program toAbstract() {
            return new AbstractTree.Program(identifier, progparamlist.toAbstract(1), optcpsdecl.toAbstract(1), cpscmd.toAbstract(1));
        }
    }

    public static abstract class Decl extends ConcreteNode {

        Decl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation);
    }

    public static class DeclSto extends Decl {

        private final StoDecl stodecl;

        public DeclSto(StoDecl stodecl, int idendation) {
            super(idendation);
            this.stodecl = stodecl;
        }

        @Override
        public String toString() {
            return getHead("<DeclSto>") + stodecl + getHead("</DeclSto>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return stodecl.toAbstract(repcpsdecl, idendation);
        }
    }

    public static class DeclFun extends Decl {

        private final FunDecl fundecl;

        public DeclFun(FunDecl fundecl, int idendation) {
            super(idendation);
            this.fundecl = fundecl;
        }

        @Override
        public String toString() {
            return getHead("<DeclFun>") + fundecl + getHead("</DeclFun>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return fundecl.toAbstract(repcpsdecl, idendation);
        }
    }

    public static class DeclProc extends Decl {

        private final ProcDecl procdecl;

        public DeclProc(ProcDecl procdecl, int idendation) {
            super(idendation);
            this.procdecl = procdecl;
        }

        @Override
        public String toString() {
            return getHead("<DeclProc>") + procdecl + getHead("</DeclProc>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return procdecl.toAbstract(repcpsdecl, idendation);
        }
    }

    public static abstract class StoDecl extends ConcreteNode {

        StoDecl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation);

        public abstract AbstractTree.Declaration toAbstract2(RepCpsStoDecl repcpsstodecl, int idendation);
    }

    public static class StoDeclIdent extends StoDecl {

        private final TypedIdent typedident;

        public StoDeclIdent(TypedIdent typedident, int idendation) {
            super(idendation);
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<StoDeclIdent>") + typedident + getHead("</StoDeclIdent>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            Tokens.ChangeModeToken changemode = new Tokens.ChangeModeToken(Terminal.CHANGEMODE, Tokens.ChangeModeToken.ChangeMode.CONST);
            return new AbstractTree.StoDecl(changemode, typedident.toAbstract(idendation + 1), repcpsdecl != null ? repcpsdecl.toAbstract(idendation + 1) : null, idendation);
        }

        @Override
        public AbstractTree.Declaration toAbstract2(RepCpsStoDecl repcpsstodecl, int idendation) {
            Tokens.ChangeModeToken changemode = new Tokens.ChangeModeToken(Terminal.CHANGEMODE, Tokens.ChangeModeToken.ChangeMode.CONST);
            return new AbstractTree.StoDecl(changemode, typedident.toAbstract(idendation + 1), repcpsstodecl.toAbstract(idendation + 1), idendation);
        }
    }

    public static class StoDeclChangemode extends StoDecl {

        private final Tokens.ChangeModeToken changemode;

        private final TypedIdent typedident;

        public StoDeclChangemode(Tokens.ChangeModeToken changemode, TypedIdent typedident, int idendation) {
            super(idendation);
            this.changemode = changemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<StoDeclChangemode Mode='" + changemode.getChangeMode() + "'>") + typedident + getHead("</StoDeclChangemode>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return new AbstractTree.StoDecl(changemode, typedident.toAbstract(idendation + 1), repcpsdecl != null ? repcpsdecl.toAbstract(idendation + 1) : null, idendation);
        }

        @Override
        public AbstractTree.Declaration toAbstract2(RepCpsStoDecl repcpsstodecl, int idendation) {
            return new AbstractTree.StoDecl(changemode, typedident.toAbstract(idendation + 1), repcpsstodecl.toAbstract(idendation + 1), idendation);
        }
    }

    public static class FunDecl extends ConcreteNode {

        private final Tokens.IdentifierToken identifier;

        private final ParamList paramlist;

        private final StoDecl stodecl;

        private final OptGlobImps optglobimps;

        private final OptCpsStoDecl optcpsstodecl;

        private final CpsCmd cpscmd;

        public FunDecl(Tokens.IdentifierToken identifier, ParamList paramlist, StoDecl stodecl, OptGlobImps optglobimps, OptCpsStoDecl optcpsstodecl, CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.paramlist = paramlist;
            this.stodecl = stodecl;
            this.optglobimps = optglobimps;
            this.optcpsstodecl = optcpsstodecl;
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<FunDecl Name='" + identifier.getName() + "'>") + paramlist + stodecl + optglobimps + optcpsstodecl + cpscmd + getHead("</FunDecl>");
        }

        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return new AbstractTree.FunDecl(identifier, paramlist.toAbstract(idendation + 1), stodecl.toAbstract(null, idendation + 1), optglobimps.toAbstract(idendation + 1), optcpsstodecl.toAbstract(idendation + 1), cpscmd.toAbstract(idendation + 1), repcpsdecl.toAbstract(idendation + 1), idendation);
        }
    }

    public static class ProcDecl extends ConcreteNode {

        private final Tokens.IdentifierToken identifier;

        private final ParamList paramlist;

        private final OptGlobImps optglobimps;

        private final OptCpsStoDecl optcpsstodecl;

        private final CpsCmd cpscmd;

        public ProcDecl(Tokens.IdentifierToken identifier, ParamList paramlist, OptGlobImps optglobimps, OptCpsStoDecl optcpsstodecl, CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.paramlist = paramlist;
            this.optglobimps = optglobimps;
            this.optcpsstodecl = optcpsstodecl;
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<ProcDecl Name='" + identifier.getName() + "'>") + paramlist + optglobimps + optcpsstodecl + cpscmd + getHead("</ProcDecl>");
        }

        public AbstractTree.Declaration toAbstract(RepCpsDecl repcpsdecl, int idendation) {
            return new AbstractTree.ProcDecl(identifier, paramlist.toAbstract(idendation + 1), optglobimps.toAbstract(idendation + 1), optcpsstodecl.toAbstract(idendation), repcpsdecl.toAbstract(idendation), cpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static abstract class OptGlobImps extends ConcreteNode {

        OptGlobImps(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.GlobalImport toAbstract(int idendation);
    }

    public static class OptGlobImpsGlobal extends OptGlobImps {

        private final GlobImps globimps;

        public OptGlobImpsGlobal(GlobImps globimps, int idendation) {
            super(idendation);
            this.globimps = globimps;
        }

        @Override
        public String toString() {
            return getHead("<OptGlobImpsGlobal>") + globimps + getHead("</OptGlobImpsGlobal>");
        }

        @Override
        public AbstractTree.GlobalImport toAbstract(int idendation) {
            return globimps.toAbstract(idendation);
        }
    }

    public static class OptGlobImpsEpsilon extends OptGlobImps {

        public OptGlobImpsEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptGlobImpsEpsilon/>");
        }

        @Override
        public AbstractTree.GlobalImport toAbstract(int idendation) {
            return null;
        }
    }

    public static class GlobImps extends ConcreteNode {

        private final GlobImp globimp;

        private final RepGlobImps repglobimps;

        public GlobImps(GlobImp globimp, RepGlobImps repglobimps, int idendation) {
            super(idendation);
            this.globimp = globimp;
            this.repglobimps = repglobimps;
        }

        @Override
        public String toString() {
            return getHead("<GlobImps>") + globimp + repglobimps + getHead("</GlobImps>");
        }

        public AbstractTree.GlobalImport toAbstract(int idendation) {
            return globimp.toAbstract(repglobimps, idendation);
        }
    }

    public static abstract class RepGlobImps extends ConcreteNode {

        RepGlobImps(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.GlobalImport toAbstract(int idendation);
    }

    public static class RepGlobImpsComma extends RepGlobImps {

        private final GlobImp globimp;

        private final RepGlobImps repglobimps;

        public RepGlobImpsComma(GlobImp globimp, RepGlobImps repglobimps, int idendation) {
            super(idendation);
            this.globimp = globimp;
            this.repglobimps = repglobimps;
        }

        @Override
        public String toString() {
            return getHead("<RepGlobImpsComma>") + globimp + repglobimps + getHead("</RepGlobImpsComma>");
        }

        public AbstractTree.GlobalImport toAbstract(int idendation) {
            return globimp.toAbstract(repglobimps, idendation);
        }
    }

    public static class RepGlobImpsEpsilon extends RepGlobImps {

        public RepGlobImpsEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepGlobImpsEpsilon/>");
        }

        public AbstractTree.GlobalImport toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class OptChangemode extends ConcreteNode {

        OptChangemode(int idendation) {
            super(idendation);
        }

        public abstract Tokens.ChangeModeToken toAbstract(int idendation);
    }

    public static class OptChangemodeChangemode extends OptChangemode {

        private final Tokens.ChangeModeToken changemode;

        public OptChangemodeChangemode(Tokens.ChangeModeToken changemode, int idendation) {
            super(idendation);
            this.changemode = changemode;
        }

        @Override
        public String toString() {
            return getHead("<OptChangemodeChangemode Mode='" + changemode.getChangeMode() + "'>") + getHead("</OptChangemodeChangemode>");
        }

        @Override
        public Tokens.ChangeModeToken toAbstract(int idenda) {
            return changemode;
        }
    }

    public static class OptChangemodeEpsilon extends OptChangemode {

        public OptChangemodeEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptChangemodeEpsilon/>");
        }

        @Override
        public Tokens.ChangeModeToken toAbstract(int idendation) {
            return new Tokens.ChangeModeToken(Terminal.CHANGEMODE, Tokens.ChangeModeToken.ChangeMode.CONST);
        }
    }

    public static abstract class OptMechmode extends ConcreteNode {

        OptMechmode(int idendation) {
            super(idendation);
        }

        public abstract Tokens.MechModeToken toAbstract(int idendation);
    }

    public static class OptMechmodeMechmode extends OptMechmode {

        private final Tokens.MechModeToken mechmode;

        public OptMechmodeMechmode(Tokens.MechModeToken mechmode, int idendation) {
            super(idendation);
            this.mechmode = mechmode;
        }

        @Override
        public String toString() {
            return getHead("<OptMechmodeMechmode Mode='" + mechmode.getMechMode() + "'>") + getHead("</OptMechmodeMechmode>");
        }

        public Tokens.MechModeToken toAbstract(int idendation) {
            return mechmode;
        }
    }

    public static class OptMechmodeEpsilon extends OptMechmode {

        public OptMechmodeEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptMechmodeEpsilon/>");
        }

        public Tokens.MechModeToken toAbstract(int idendation) {
            return new Tokens.MechModeToken(Terminal.MECHMODE, Tokens.MechModeToken.MechMode.COPY);
        }
    }

    public static abstract class GlobImp extends ConcreteNode {

        GlobImp(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.GlobalImport toAbstract(RepGlobImps repglobimps, int idendation);
    }

    public static class GlobImpExpression extends GlobImp {

        private final OptChangemode optchangemode;

        private final Tokens.IdentifierToken identifier;

        public GlobImpExpression(OptChangemode optchangemode, Tokens.IdentifierToken identifier, int idendation) {
            super(idendation);
            this.optchangemode = optchangemode;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return getHead("<GlobImp Name='" + identifier.getName() + "'>") + optchangemode + getHead("</GlobImp>");
        }

        @Override
        public AbstractTree.GlobalImport toAbstract(RepGlobImps repglobimps, int idendation) {
            Tokens.FlowModeToken flowmode = new Tokens.FlowModeToken(Terminal.FLOWMODE, Tokens.FlowModeToken.FlowMode.IN);
            return new AbstractTree.GlobalImport(flowmode, optchangemode.toAbstract(idendation + 1), identifier, repglobimps.toAbstract(idendation), idendation);
        }
    }

    public static class GlobImpFlowmode extends GlobImp {

        private final Tokens.FlowModeToken flowmode;

        private final OptChangemode optchangemode;

        private final Tokens.IdentifierToken identifier;

        public GlobImpFlowmode(Tokens.FlowModeToken flowmode, OptChangemode optchangemode, Tokens.IdentifierToken identifier, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.optchangemode = optchangemode;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return getHead("<GlobImp Mode='" + flowmode.getFlowMode() + "' Name='" + identifier.getName() + "'>") + optchangemode + getHead("</GlobImp>");
        }

        @Override
        public AbstractTree.GlobalImport toAbstract(RepGlobImps repglobimps, int idendation) {
            return new AbstractTree.GlobalImport(flowmode, optchangemode.toAbstract(idendation), identifier, repglobimps.toAbstract(idendation + 1), idendation);
        }
    }

    public static abstract class OptCpsDecl extends ConcreteNode {

        OptCpsDecl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(int idendation);
    }

    public static class OptCpsDeclGlobal extends OptCpsDecl {

        private final CpsDecl cpsdecl;

        public OptCpsDeclGlobal(CpsDecl cpsecl, int idendation) {
            super(idendation);
            this.cpsdecl = cpsecl;
        }

        @Override
        public String toString() {
            return getHead("<OptCpsDeclGlobal>") + cpsdecl + getHead("</OptCpsDeclGlobal>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(int idendation) {
            return cpsdecl.toAbstract(idendation);
        }
    }

    public static class OptCpsDeclEpsilon extends OptCpsDecl {

        public OptCpsDeclEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptCpsDeclEpsilon>") + getHead("</OptCpsDeclEpsilon>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(int idendation) {
            return null;
        }
    }

    public static class CpsDecl extends ConcreteNode {

        private final Decl decl;

        private final RepCpsDecl repcpsdecl;

        public CpsDecl(Decl decl, RepCpsDecl repcpsdecl, int idendation) {
            super(idendation);
            this.decl = decl;
            this.repcpsdecl = repcpsdecl;
        }

        @Override
        public String toString() {
            return getHead("<CpsDecl>") + decl + repcpsdecl + getHead("</CpsDecl>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return decl.toAbstract(repcpsdecl, idendation);
        }
    }

    public static abstract class RepCpsDecl extends ConcreteNode {

        RepCpsDecl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(int idendation);
    }

    public static class RepCpsDeclSemicolon extends RepCpsDecl {

        private final Decl decl;

        private final RepCpsDecl repcpsdecl;

        public RepCpsDeclSemicolon(Decl decl, RepCpsDecl repcpsdecl, int idendation) {
            super(idendation);
            this.decl = decl;
            this.repcpsdecl = repcpsdecl;
        }

        @Override
        public String toString() {
            return getHead("<RepCpsDeclSemicolon>") + decl + repcpsdecl + getHead("</RepCpsDeclSemicolon>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(int idendation) {
            return decl.toAbstract(repcpsdecl, idendation);
        }
    }

    public static class RepCpsDeclEpsilon extends RepCpsDecl {

        public RepCpsDeclEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepCpsDeclEpsilon/>");
        }

        @Override
        public AbstractTree.Declaration toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class OptCpsStoDecl extends ConcreteNode {

        OptCpsStoDecl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(int idendation);
    }

    public static class OptCpsStoDeclLocal extends OptCpsStoDecl {

        private final CpsStoDecl cpsstodecl;

        public OptCpsStoDeclLocal(CpsStoDecl cpsstodecl, int idendation) {
            super(idendation);
            this.cpsstodecl = cpsstodecl;
        }

        @Override
        public String toString() {
            return getHead("<OptCpsStoDeclLocal>") + cpsstodecl + getHead("</OptCpsStoDeclLocal>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return cpsstodecl.toAbstract(idendation);
        }
    }

    public static class OptCpsStoDeclEpsilon extends OptCpsStoDecl {

        public OptCpsStoDeclEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptCpsStoDeclEpsilon/>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return null;
        }
    }

    public static class CpsStoDecl extends ConcreteNode {

        private final StoDecl stodecl;

        private final RepCpsStoDecl repcpsstodecl;

        public CpsStoDecl(StoDecl stodecl, RepCpsStoDecl repcpsstodecl, int idendation) {
            super(idendation);
            this.stodecl = stodecl;
            this.repcpsstodecl = repcpsstodecl;
        }

        @Override
        public String toString() {
            return getHead("<CpsStoDecl>") + stodecl + repcpsstodecl + getHead("</CpsStoDecl>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return stodecl.toAbstract2(repcpsstodecl, idendation);
        }
    }

    public static abstract class RepCpsStoDecl extends ConcreteNode {

        RepCpsStoDecl(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Declaration toAbstract(int idendation);
    }

    public static class RepCpsStoDeclSemicolon extends RepCpsStoDecl {

        private final StoDecl stodecl;

        private final RepCpsStoDecl repcpsstodecl;

        public RepCpsStoDeclSemicolon(StoDecl stodecl, RepCpsStoDecl repcpsstodecl, int idendation) {
            super(idendation);
            this.stodecl = stodecl;
            this.repcpsstodecl = repcpsstodecl;
        }

        @Override
        public String toString() {
            return getHead("<RepCpsStoDeclSemicolon>") + stodecl + repcpsstodecl + getHead("</RepCpsStoDeclSemicolon>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return stodecl.toAbstract2(repcpsstodecl, idendation);
        }
    }

    public static class RepCpsStoDeclEpsilon extends RepCpsStoDecl {

        public RepCpsStoDeclEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepCpsStoDeclEpsilon>") + getHead("</RepCpsStoDeclEpsilon>");
        }

        public AbstractTree.Declaration toAbstract(int idendation) {
            return null;
        }
    }

    public static class ProgParamList extends ConcreteNode {

        private final OptProgParamList optprogparamlist;

        public ProgParamList(OptProgParamList optprogparamlist, int idendation) {
            super(idendation);
            this.optprogparamlist = optprogparamlist;
        }

        @Override
        public String toString() {
            return getHead("<ProgParamList>") + optprogparamlist + getHead("</ProgParamList>");
        }

        public AbstractTree.ProgParam toAbstract(int idendation) {
            return optprogparamlist.toAbstract(idendation);
        }
    }

    public static abstract class OptProgParamList extends ConcreteNode {

        OptProgParamList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.ProgParam toAbstract(int idendation);
    }

    public static class OptProgParamListExpression extends OptProgParamList {

        private final ProgParam progparam;

        private final RepProgParamList repprogparamlist;

        public OptProgParamListExpression(ProgParam progparam, RepProgParamList repprogparamlist, int idendation) {
            super(idendation);
            this.progparam = progparam;
            this.repprogparamlist = repprogparamlist;
        }

        @Override
        public String toString() {
            return getHead("<OptProgParamListExpression>") + progparam + repprogparamlist + getHead("</OptProgParamListExpression>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(int idendation) {
            return progparam.toAbstract(repprogparamlist, idendation);
        }
    }

    public static class OptProgParamListEpsilon extends OptProgParamList {

        public OptProgParamListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptProgParamListEpsilon/>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepProgParamList extends ConcreteNode {

        RepProgParamList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.ProgParam toAbstract(int idendation);
    }

    public static class RepProgParamListComma extends RepProgParamList {

        private final ProgParam progparam;

        private final RepProgParamList repprogparamlist;

        public RepProgParamListComma(ProgParam progparam, RepProgParamList repprogparamlist, int idendation) {
            super(idendation);
            this.progparam = progparam;
            this.repprogparamlist = repprogparamlist;
        }

        @Override
        public String toString() {
            return getHead("<RepProgParamListComma>") + progparam + repprogparamlist + getHead("</RepProgParamListComma>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(int idendation) {
            return progparam.toAbstract(repprogparamlist, idendation);
        }
    }

    public static class RepProgParamListEpsilon extends RepProgParamList {

        public RepProgParamListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepProgParamListEpsilon/>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class ProgParam extends ConcreteNode {

        ProgParam(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.ProgParam toAbstract(RepProgParamList repprogparamlist, int idendation);
    }

    public static class ProgParamExpression extends ProgParam {

        private final OptChangemode optchangemode;

        private final TypedIdent typedident;

        public ProgParamExpression(OptChangemode optchangemode, TypedIdent typedident, int idendation) {
            super(idendation);
            this.optchangemode = optchangemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<ProgParamExpression>") + optchangemode + typedident + getHead("<ProgParamExpression>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(RepProgParamList repprogparamlist, int idendation) {
            Tokens.FlowModeToken flowmode = new Tokens.FlowModeToken(Terminal.FLOWMODE, Tokens.FlowModeToken.FlowMode.IN);
            return new AbstractTree.ProgParam(flowmode, optchangemode.toAbstract(idendation), typedident.toAbstract(idendation + 1), repprogparamlist.toAbstract(idendation + 1), idendation);
        }
    }

    public static class ProgParamFlowmode extends ProgParam {

        private final Tokens.FlowModeToken flowmode;

        private final OptChangemode optchangemode;

        private final TypedIdent typedident;

        public ProgParamFlowmode(Tokens.FlowModeToken flowmode, OptChangemode optchangemode, TypedIdent typedident, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.optchangemode = optchangemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<ProgParamFlowmode Mode='" + flowmode.getFlowMode() + "'>") + optchangemode + typedident + getHead("<ProgParamFlowmode>");
        }

        @Override
        public AbstractTree.ProgParam toAbstract(RepProgParamList repprogparamlist, int idendation) {
            return new AbstractTree.ProgParam(flowmode, optchangemode.toAbstract(idendation), typedident.toAbstract(idendation + 1), repprogparamlist.toAbstract(idendation + 1), idendation);
        }
    }

    public static class ParamList extends ConcreteNode {

        private final OptParamList optparamlist;

        public ParamList(OptParamList optparamlist, int idendation) {
            super(idendation);
            this.optparamlist = optparamlist;
        }

        @Override
        public String toString() {
            return getHead("<ParamList>") + optparamlist + getHead("</ParamList>");
        }

        public AbstractTree.Param toAbstract(int idendation) {
            return optparamlist.toAbstract(idendation);
        }
    }

    public static abstract class OptParamList extends ConcreteNode {

        OptParamList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Param toAbstract(int idendation);
    }

    public static class OptParamListRepeating extends OptParamList {

        private final Param param;

        private final RepParamList repparamlist;

        public OptParamListRepeating(Param param, RepParamList repparamlist, int idendation) {
            super(idendation);
            this.param = param;
            this.repparamlist = repparamlist;
        }

        @Override
        public String toString() {
            return getHead("<OptParamListRepeating>") + param + repparamlist + getHead("</OptParamListRepeating>");
        }

        @Override
        public AbstractTree.Param toAbstract(int idendation) {
            return param.toAbstract(repparamlist, idendation);
        }
    }

    public static class OptParamListEpsilon extends OptParamList {

        public OptParamListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptParamListEpsilon/>");
        }

        @Override
        public AbstractTree.Param toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepParamList extends ConcreteNode {

        RepParamList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Param toAbstract(int idendation);
    }

    public static class RepParamListComma extends RepParamList {

        private final Param param;

        private final RepParamList repparamlist;

        public RepParamListComma(Param param, RepParamList repparamlist, int idendation) {
            super(idendation);
            this.param = param;
            this.repparamlist = repparamlist;
        }

        @Override
        public String toString() {
            return getHead("<RepParamListComma>") + param + repparamlist + getHead("</RepParamListComma>");
        }

        public AbstractTree.Param toAbstract(int idendation) {
            return param.toAbstract(repparamlist, idendation);
        }
    }

    public static class RepParamListEpsilon extends RepParamList {

        public RepParamListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepParamListEpsilon/>");
        }

        public AbstractTree.Param toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class Param extends ConcreteNode {

        Param(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Param toAbstract(RepParamList repparamlist, int idendation);
    }

    public static class ParamExpression extends Param {

        private final OptMechmode optmechmode;

        private final OptChangemode optchangemode;

        private final TypedIdent typedident;

        public ParamExpression(OptMechmode optmechmode, OptChangemode optchangemode, TypedIdent typedident, int idendation) {
            super(idendation);
            this.optmechmode = optmechmode;
            this.optchangemode = optchangemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<ParamExpression>") + optmechmode + optchangemode + typedident + getHead("</ParamExpression>");
        }

        @Override
        public AbstractTree.Param toAbstract(RepParamList repparamlist, int idendation) {
            Tokens.FlowModeToken flowmode = new Tokens.FlowModeToken(Terminal.FLOWMODE, Tokens.FlowModeToken.FlowMode.IN);
            return new AbstractTree.Param(flowmode, optmechmode.toAbstract(idendation + 1), optchangemode.toAbstract(idendation + 1), typedident.toAbstract(idendation + 1), repparamlist.toAbstract(idendation), idendation);
        }
    }

    public static class ParamFlowmode extends Param {

        private final Tokens.FlowModeToken flowmode;

        private final OptMechmode optmechmode;

        private final OptChangemode optchangemode;

        private final TypedIdent typedident;

        public ParamFlowmode(Tokens.FlowModeToken flowmode, OptMechmode optmechmode, OptChangemode optchangemode, TypedIdent typedident, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.optmechmode = optmechmode;
            this.optchangemode = optchangemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<ParamFlowmode Mode='" + flowmode.getFlowMode() + "'>") + optmechmode + optchangemode + typedident + getHead("</ParamFlowmode>");
        }

        @Override
        public AbstractTree.Param toAbstract(RepParamList repparamlist, int idendation) {
            return new AbstractTree.Param(flowmode, optmechmode.toAbstract(idendation), optchangemode.toAbstract(idendation), typedident.toAbstract(idendation + 1), repparamlist.toAbstract(idendation + 1), idendation);
        }
    }

    public static class TypedIdent extends ConcreteNode {

        private final Tokens.IdentifierToken identifier;

        private final Tokens.TypeToken.Type type;

        public TypedIdent(Tokens.IdentifierToken identifier, Tokens.TypeToken.Type type, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public String toString() {
            return getHead("<TypedIdent Name='" + identifier.getName() + "' Type='" + type + "'/>");
        }

        public AbstractTree.TypedIdent toAbstract(int idendation) {
            return new AbstractTree.TypedIdentType(identifier, type, idendation);
        }
    }

    public static abstract class Cmd extends ConcreteNode {

        Cmd(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation);
    }

    public static class CmdSkip extends Cmd {

        public CmdSkip(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<CmdSkip/>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.SkipCmd(repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdExpression extends Cmd {

        private final Expr expr1;

        private final RepExprList repexprlist1;

        private final Expr expr2;

        private final RepExprList repexprlist2;

        public CmdExpression(Expr expr1, RepExprList repexprlist1, Expr expr2, RepExprList repexprlist2, int idendation) {
            super(idendation);
            this.expr1 = expr1;
            this.repexprlist1 = repexprlist1;
            this.expr2 = expr2;
            this.repexprlist2 = repexprlist2;
        }

        @Override
        public String toString() {
            return getHead("<CmdExpression>") + expr1 + repexprlist1 + expr2 + repexprlist2 + getHead("</CmdExpression>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.AssiCmd(expr1.toAbstract(idendation), repexprlist1.toAbstract(idendation + 1), expr2.toAbstract(idendation), repexprlist2.toAbstract(idendation + 1), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdIf extends Cmd {

        private final Expr expr;

        private final CpsCmd cpscmd;

        private final RepElseif repelseif;

        private final OptElse optelse;

        public CmdIf(Expr expr, CpsCmd cpscmd, RepElseif repelseif, OptElse optelse, int idendation) {
            super(idendation);
            this.expr = expr;
            this.cpscmd = cpscmd;
            this.repelseif = repelseif;
            this.optelse = optelse;
        }

        @Override
        public String toString() {
            return getHead("<CmdIf>") + expr + cpscmd + repelseif + optelse + getHead("</CmdIf>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.CondCmd(expr.toAbstract(idendation), cpscmd.toAbstract(idendation + 1), repelseif.toAbstract(idendation + 1), optelse.toAbstract(idendation), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdIfGuarded extends Cmd {
        private final CpsArrowTerm cpsArrowTerm;

        public CmdIfGuarded(CpsArrowTerm cpsArrowTerm, int idendation) {
            super(idendation);
            this.cpsArrowTerm = cpsArrowTerm;
        }

        @Override
        public String toString() {
            return getHead("<CmdIfGuarded>") +  cpsArrowTerm + getHead("</CmdIfGuarded>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.GuardedCondCmd(cpsArrowTerm.toAbstract(idendation),repcpscmd.toAbstract(idendation), idendation);
        }
    }

    public static class ArrowTerm extends ConcreteNode {
        private final Expr expr;
        private final CpsCmd cpsCmd;

        public ArrowTerm(Expr expr, CpsCmd cpsCmd, int indentation) {
            super(indentation);
            this.expr = expr;
            this.cpsCmd = cpsCmd;
        }

        @Override
        public String toString() {
            return getHead("<ArrowTerm>") + expr +  cpsCmd + getHead("</ArrowTerm>");
        }
    }

    public static abstract class RepArrowTerm extends ConcreteNode {

        RepArrowTerm(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.RepArrowCmd toAbstract(int idendation);
    }

    public static class RepArrowTermMult extends RepArrowTerm {

        private final ArrowTerm arrowTerm;
        private final RepArrowTerm repArrowTerm;

        public RepArrowTermMult(ArrowTerm arrowTerm, RepArrowTerm repArrowTerm, int indentation) {
            super(indentation);
            this.arrowTerm = arrowTerm;
            this.repArrowTerm = repArrowTerm;
        }

        @Override
        public String toString() {
            return getHead("<RepArrowTermMult>") + arrowTerm + repArrowTerm + getHead("</RepArrowTermMult>");
        }

        @Override
        public AbstractTree.RepArrowCmd toAbstract(int idendation) {
            return new AbstractTree.RepArrowCmd(arrowTerm.expr.toAbstract(idendation + 1), arrowTerm.cpsCmd.toAbstract(idendation + 1), repArrowTerm.toAbstract(idendation), idendation);
        }
    }

    public static class RepArrowTermEpsilon extends RepArrowTerm {

        public RepArrowTermEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepArrowTermEpsilon/>");
        }

        @Override
        public AbstractTree.RepArrowCmd toAbstract(int idendation) {
            return null;
        }
    }

    public static class CpsArrowTerm extends ConcreteNode {

        private final ArrowTerm arrowTerm;
        private final RepArrowTerm repArrowTerm;

        public CpsArrowTerm(ArrowTerm arrowTerm, RepArrowTerm repArrowTerm, int indentation) {
            super(indentation);
            this.arrowTerm = arrowTerm;
            this.repArrowTerm = repArrowTerm;
        }

        @Override
        public String toString() {
            return getHead("<CpsArrowTerm>") + arrowTerm + repArrowTerm + getHead("</CpsArrowTerm>");
        }

        public AbstractTree.RepArrowCmd toAbstract(int identation) {
            return new AbstractTree.RepArrowCmd(arrowTerm.expr.toAbstract(identation + 1), arrowTerm.cpsCmd.toAbstract(identation + 1), repArrowTerm.toAbstract(identation), identation);
        }


    }

    public static class CmdSwitch extends Cmd {

        private final Expr expr;

        private final Tokens.LiteralToken literal;

        private final CpsCmd cpscmd;

        private final RepCase repcase;

        private final OptDefault optdefault;

        public CmdSwitch(Expr expr, Tokens.LiteralToken literal, CpsCmd cpscmd, RepCase repcase, OptDefault optdefault, int idendation) {
            super(idendation);
            this.expr = expr;
            this.literal = literal;
            this.cpscmd = cpscmd;
            this.repcase = repcase;
            this.optdefault = optdefault;
        }

        @Override
        public String toString() {
            return getHead("<CmdSwitch Value='" + literal.getValue() + "'>") + expr + cpscmd + repcase + optdefault + getHead("</CmdSwitch>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            AbstractTree.RepCaseCmd repcasecmd = new AbstractTree.RepCaseCmd(literal, cpscmd.toAbstract(idendation + 2), repcase.toAbstract(idendation + 2), idendation + 1);
            return new AbstractTree.SwitchCmd(expr.toAbstract(idendation), repcasecmd, optdefault.toAbstract(idendation), repcpscmd.toAbstract(idendation), idendation);
        }
    }

    public static class CmdWhile extends Cmd {

        private final Expr expr;

        private final CpsCmd cpscmd;

        public CmdWhile(Expr expr, CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.expr = expr;
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<CmdWhile>") + expr + cpscmd + getHead("</CmdWhile>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.WhileCmd(expr.toAbstract(idendation + 1), cpscmd.toAbstract(idendation + 1), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdCall extends Cmd {

        private final Tokens.IdentifierToken identifier;

        private final ExprList exprlist;

        private final OptGlobInits optglobinits;

        public CmdCall(Tokens.IdentifierToken identifier, ExprList exprlist, OptGlobInits optglobinits, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.exprlist = exprlist;
            this.optglobinits = optglobinits;
        }

        @Override
        public String toString() {
            return getHead("<CmdCall Name='" + identifier.getName() + "'>") + exprlist + optglobinits + getHead("</CmdCall>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.ProcCallCmd(new AbstractTree.RoutineCall(identifier, exprlist.toAbstract(idendation + 2), idendation + 1), optglobinits.toAbstract(idendation + 1), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdDebugIn extends Cmd {

        private final Expr expr;

        public CmdDebugIn(Expr expr, int idendation) {
            super(idendation);
            this.expr = expr;
        }

        @Override
        public String toString() {
            return getHead("<CmdDebugIn>") + expr + getHead("</CmdDebugInt>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.InputCmd(expr.toAbstract(idendation), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CmdDebugOut extends Cmd {

        private final Expr expr;

        public CmdDebugOut(Expr expr, int idendation) {
            super(idendation);
            this.expr = expr;
        }

        @Override
        public String toString() {
            return getHead("<CmdDebugOut>") + expr + getHead("</CmdDebugOut>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(RepCpsCmd repcpscmd, int idendation) {
            return new AbstractTree.OutputCmd(expr.toAbstract(idendation), repcpscmd.toAbstract(idendation + 1), idendation);
        }
    }

    public static class CpsCmd extends ConcreteNode {

        private final Cmd cmd;

        private final RepCpsCmd repcpscmd;

        public CpsCmd(Cmd cmd, RepCpsCmd repcpscmd, int idendation) {
            super(idendation);
            this.cmd = cmd;
            this.repcpscmd = repcpscmd;
        }

        @Override
        public String toString() {
            return getHead("<CpsCmd>") + cmd + repcpscmd + getHead("</CpsCmd>");
        }

        public AbstractTree.Cmd toAbstract(int idendation) {
            return cmd.toAbstract(repcpscmd, idendation);
        }
    }

    public static abstract class RepCpsCmd extends ConcreteNode {

        RepCpsCmd(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Cmd toAbstract(int idendation);
    }

    public static class RepCpsCmdSemicolon extends RepCpsCmd {

        private final Cmd cmd;

        private final RepCpsCmd repcpscmd;

        public RepCpsCmdSemicolon(Cmd cmd, RepCpsCmd repcpscmd, int idendation) {
            super(idendation);
            this.cmd = cmd;
            this.repcpscmd = repcpscmd;
        }

        @Override
        public String toString() {
            return getHead("<RepCpsCmdSemicolon>") + cmd + repcpscmd + getHead("</RepCpsCmdSemicolon>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(int idendation) {
            return cmd.toAbstract(repcpscmd, idendation);
        }
    }

    public static class RepCpsCmdEpsilon extends RepCpsCmd {

        public RepCpsCmdEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepCpsCmdEpsilon/>");
        }

        @Override
        public AbstractTree.Cmd toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class OptGlobInits extends ConcreteNode {

        OptGlobInits(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.GlobalInit toAbstract(int i);
    }

    public static class OptGlobInitsInit extends OptGlobInits {

        private final Tokens.IdentifierToken identifier;

        private final RepIdents repidents;

        public OptGlobInitsInit(Tokens.IdentifierToken identifier, RepIdents repidents, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.repidents = repidents;
        }

        @Override
        public String toString() {
            return getHead("<OptGlobInitsInit Name='" + identifier.getName() + "'>") + repidents + getHead("</OptGlobInitsInit>");
        }

        public AbstractTree.GlobalInit toAbstract(int idendation) {
            return new AbstractTree.GlobalInit(identifier, repidents.toAbstract(idendation + 1), idendation);
        }
    }

    public static class OptGlobInitsEpsilon extends OptGlobInits {

        public OptGlobInitsEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptGlobInitsEpsilon/>");
        }

        public AbstractTree.GlobalInit toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepIdents extends ConcreteNode {

        RepIdents(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.GlobalInit toAbstract(int idendation);
    }

    public static class RepIdentsComma extends RepIdents {

        private final Tokens.IdentifierToken identifier;

        private final RepIdents repidents;

        public RepIdentsComma(Tokens.IdentifierToken identifier, RepIdents repidents, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.repidents = repidents;
        }

        @Override
        public String toString() {
            return getHead("<RepIdentsComma Name='" + identifier.getName() + "'>") + repidents + getHead("</RepIdentsComma>");
        }

        public AbstractTree.GlobalInit toAbstract(int idendation) {
            return new AbstractTree.GlobalInit(identifier, repidents.toAbstract(idendation + 1), idendation);
        }
    }

    public static class RepIdentsEpsilon extends RepIdents {

        public RepIdentsEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepIdentsEpsilon/>");
        }

        public AbstractTree.GlobalInit toAbstract(int idendation) {
            return null;
        }
    }

    public static class Expr extends ConcreteNode {

        private final Term1 term1;

        private final RepTerm1 repterm1;

        public Expr(Term1 term1, RepTerm1 repterm1, int idendation) {
            super(idendation);
            this.term1 = term1;
            this.repterm1 = repterm1;
        }

        @Override
        public String toString() {
            return getHead("<Expr>") + term1 + repterm1 + getHead("</Expr>");
        }

        public AbstractTree.Expression toAbstract(int idendation) {
            return repterm1.toAbstract(term1.toAbstract(idendation), idendation);
        }
    }

    public static abstract class RepTerm1 extends ConcreteNode {

        RepTerm1(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation);
    }

    public static class RepTerm1BoolOpr extends RepTerm1 {

        private final Tokens.BoolOprToken boolopr;

        private final Term1 term1;

        private final RepTerm1 repterm1;

        public RepTerm1BoolOpr(Tokens.BoolOprToken boolopr, Term1 term1, RepTerm1 repterm1, int idendation) {
            super(idendation);
            this.boolopr = boolopr;
            this.term1 = term1;
            this.repterm1 = repterm1;
        }

        @Override
        public String toString() {
            return getHead("<RepTerm1BoolOpr Opr='" + boolopr.getOperation() + "'>") + term1 + repterm1 + getHead("</RepTerm1BoolOpr>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return repterm1.toAbstract(new AbstractTree.DyadicExpr(boolopr, expression, term1.toAbstract(idendation + 1), idendation), idendation + 1);
        }
    }

    public static class RepTerm1Epsilon extends RepTerm1 {

        public RepTerm1Epsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepTerm1Epsilon/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return expression;
        }
    }

    public static class Term1 extends ConcreteNode {

        private final Term2 term2;

        private final RepTerm2 repterm2;

        public Term1(Term2 term2, RepTerm2 repterm2, int idendation) {
            super(idendation);
            this.term2 = term2;
            this.repterm2 = repterm2;
        }

        @Override
        public String toString() {
            return getHead("<Term1>") + term2 + repterm2 + getHead("</Term1>");
        }

        public AbstractTree.Expression toAbstract(int idendation) {
            return repterm2.toAbstract(term2.toAbstract(idendation), idendation);
        }
    }

    public static abstract class RepTerm2 extends ConcreteNode {

        RepTerm2(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation);
    }

    public static class RepTerm2RelOpr extends RepTerm2 {

        private final Tokens.RelOprToken relopr;

        private final Term2 term2;

        private final RepTerm2 repterm2;

        public RepTerm2RelOpr(Tokens.RelOprToken relopr, Term2 term2, RepTerm2 repterm2, int idendation) {
            super(idendation);
            this.relopr = relopr;
            this.term2 = term2;
            this.repterm2 = repterm2;
        }

        @Override
        public String toString() {
            return getHead("<RepTerm2RelOpr Opr='" + relopr.getOperation() + "'>") + term2 + repterm2 + getHead("</RepTerm2RelOpr>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return repterm2.toAbstract(new AbstractTree.DyadicExpr(relopr, expression, term2.toAbstract(idendation + 1), idendation + 1), idendation);
        }
    }

    public static class RepTerm2Epsilon extends RepTerm2 {

        public RepTerm2Epsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepTerm2Epsilon/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return expression;
        }
    }

    public static class Term2 extends ConcreteNode {

        private final Term3 term3;

        private final RepTerm3 repterm3;

        public Term2(Term3 term3, RepTerm3 repterm3, int idendation) {
            super(idendation);
            this.term3 = term3;
            this.repterm3 = repterm3;
        }

        @Override
        public String toString() {
            return getHead("<Term2>") + term3 + repterm3 + getHead("</Term2>");
        }

        public AbstractTree.Expression toAbstract(int idendation) {
            return repterm3.toAbstract(term3.toAbstract(idendation), idendation);
        }
    }

    public static abstract class RepTerm3 extends ConcreteNode {

        RepTerm3(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation);
    }

    public static class RepTerm3AddOpr extends RepTerm3 {

        private final Tokens.AddOprToken addopr;

        private final Term3 term3;

        private final RepTerm3 repterm3;

        public RepTerm3AddOpr(Tokens.AddOprToken addopr, Term3 term3, RepTerm3 repterm3, int idendation) {
            super(idendation);
            this.addopr = addopr;
            this.term3 = term3;
            this.repterm3 = repterm3;
        }

        @Override
        public String toString() {
            return getHead("<RepTerm3AddOpr Opr='" + addopr.getOperation() + "'>") + term3 + repterm3 + getHead("</RepTerm3AddOpr>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return repterm3.toAbstract(new AbstractTree.DyadicExpr(addopr, expression, term3.toAbstract(idendation + 1), idendation), idendation);
        }
    }

    public static class RepTerm3Epsilon extends RepTerm3 {

        public RepTerm3Epsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepTerm3Epsilon/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return expression;
        }
    }

    public static class Term3 extends ConcreteNode {

        private final Factor factor;

        private final RepFactor repfactor;

        public Term3(Factor factor, RepFactor repfactor, int idendation) {
            super(idendation);
            this.factor = factor;
            this.repfactor = repfactor;
        }

        @Override
        public String toString() {
            return getHead("<Term3>") + factor + repfactor + getHead("</Term3>");
        }

        public AbstractTree.Expression toAbstract(int idendation) {
            return repfactor.toAbstract(factor.toAbstract(idendation + 1), idendation);
        }
    }

    public static abstract class RepFactor extends ConcreteNode {

        RepFactor(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation);
    }

    public static class RepFactorMultOpr extends RepFactor {

        private final Tokens.MultOprToken multopr;

        private final Factor factor;

        private final RepFactor repfactor;

        public RepFactorMultOpr(Tokens.MultOprToken multopr, Factor factor, RepFactor repfactor, int idendation) {
            super(idendation);
            this.multopr = multopr;
            this.factor = factor;
            this.repfactor = repfactor;
        }

        @Override
        public String toString() {
            return getHead("<RepFactorMultOpr Opr='" + multopr.getOperation() + "'>") + factor + repfactor + getHead("</RepFactorMultOpr>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return repfactor.toAbstract(new AbstractTree.DyadicExpr(multopr, expression, factor.toAbstract(idendation + 1), idendation + 1), idendation);
        }
    }

    public static class RepFactorEpsilon extends RepFactor {

        public RepFactorEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepFactorEpsilon/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(AbstractTree.Expression expression, int idendation) {
            return expression;
        }
    }

    public static abstract class Factor extends ConcreteNode {

        Factor(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(int idendation);
    }

    public static class FactorLiteral extends Factor {

        private final Tokens.LiteralToken literal;

        public FactorLiteral(Tokens.LiteralToken literal, int idendation) {
            super(idendation);
            this.literal = literal;
        }

        @Override
        public String toString() {
            return getHead("<FactorLiteral Name='" + literal.getValue() + "'") + getHead("</FactorLiteral>");
        }

        @Override
        public AbstractTree.Expression toAbstract(int idendation) {
            return new AbstractTree.LiteralExpr(literal, idendation);
        }
    }

    public static class FactorIdent extends Factor {

        private final Tokens.IdentifierToken identifier;

        private final OptInitOrExprList optinitorexprlist;

        public FactorIdent(Tokens.IdentifierToken identifier, OptInitOrExprList optinitorexprlist, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.optinitorexprlist = optinitorexprlist;
        }

        @Override
        public String toString() {
            return getHead("<FactorIdent Name='" + identifier.getName() + "'>") + optinitorexprlist + getHead("</FactorIdent>");
        }

        @Override
        public AbstractTree.Expression toAbstract(int idendation) {
            return optinitorexprlist.toAbstract(identifier, idendation);
        }
    }

    public static class FactorExpression extends Factor {

        private final MonadicOpr monadicopr;

        private final Factor factor;

        public FactorExpression(MonadicOpr monadicopr, Factor factor, int idendation) {
            super(idendation);
            this.monadicopr = monadicopr;
            this.factor = factor;
        }

        @Override
        public String toString() {
            return getHead("<FactorExpression>") + monadicopr + factor + getHead("</FactorExpression>");
        }

        @Override
        public AbstractTree.Expression toAbstract(int idendation) {
            return new AbstractTree.MonadicExpr(monadicopr.toAbstract(idendation + 1), factor.toAbstract(idendation + 1), idendation);
        }
    }

    public static class FactorLparen extends Factor {

        private final Expr expr;

        public FactorLparen(Expr expr, int idendation) {
            super(idendation);
            this.expr = expr;
        }

        @Override
        public String toString() {
            return getHead("<FactorLparen>") + expr + getHead("</FactorLparen>");
        }

        @Override
        public AbstractTree.Expression toAbstract(int idendation) {
            return expr.toAbstract(idendation);
        }
    }

    public static abstract class OptInitOrExprList extends ConcreteNode {

        OptInitOrExprList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Expression toAbstract(Tokens.IdentifierToken identifier, int idendation);
    }

    public static class OptInitOrExprListInit extends OptInitOrExprList {

        public OptInitOrExprListInit(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptInitOrExprListInit/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(Tokens.IdentifierToken identifier, int idendation) {
            return new AbstractTree.StoreExpr(identifier, true, idendation);
        }
    }

    public static class OptInitOrExprListLparen extends OptInitOrExprList {

        private final ExprList exprlist;

        public OptInitOrExprListLparen(ExprList exprlist, int idendation) {
            super(idendation);
            this.exprlist = exprlist;
        }

        @Override
        public String toString() {
            return getHead("<OptInitOrExprListLparen>") + exprlist + getHead("</OptInitOrExprListLparen>");
        }

        @Override
        public AbstractTree.Expression toAbstract(Tokens.IdentifierToken identifier, int idendation) {
            return new AbstractTree.FunCallExpr(new AbstractTree.RoutineCall(identifier, exprlist.toAbstract(idendation + 1), idendation), idendation);
        }
    }

    public static class OptInitOrExprListEpsilon extends OptInitOrExprList {

        public OptInitOrExprListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptInitOrExprListEpsilon/>");
        }

        @Override
        public AbstractTree.Expression toAbstract(Tokens.IdentifierToken identifier, int idendation) {
            return new AbstractTree.StoreExpr(identifier, false, idendation);
        }
    }

    public static abstract class MonadicOpr extends ConcreteNode {

        MonadicOpr(int idendation) {
            super(idendation);
        }

        public abstract Tokens.OperationToken toAbstract(int idendation);
    }

    public static class MonadicOprNot extends MonadicOpr {

        private final Tokens.BoolOprToken boolopr;

        public MonadicOprNot(Tokens.BoolOprToken boolopr, int idendation) {
            super(idendation);
            this.boolopr = boolopr;
        }

        @Override
        public String toString() {
            return getHead("<MonadicOprAddopr Operation='" + boolopr.getOperation() + "'/>");
        }

        @Override
        public Tokens.OperationToken toAbstract(int idendation) {
            return boolopr;
        }
    }

    public static class MonadicOprAddopr extends MonadicOpr {

        private final Tokens.AddOprToken addopr;

        public MonadicOprAddopr(Tokens.AddOprToken addopr, int idendation) {
            super(idendation);
            this.addopr = addopr;
        }

        @Override
        public String toString() {
            return getHead("<MonadicOprAddopr Operation='" + addopr.getOperation() + "'/>");
        }

        @Override
        public Tokens.OperationToken toAbstract(int idendation) {
            return addopr;
        }
    }

    public static class ExprList extends ConcreteNode {

        private final OptExprList optexprlist;

        public ExprList(OptExprList optexprlist, int idendation) {
            super(idendation);
            this.optexprlist = optexprlist;
        }

        @Override
        public String toString() {
            return getHead("<ExprList>") + optexprlist + getHead("</ExprList>");
        }

        public AbstractTree.ExpressionList toAbstract(int idendation) {
            return optexprlist.toAbstract(idendation);
        }
    }

    public static abstract class OptExprList extends ConcreteNode {

        OptExprList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.ExpressionList toAbstract(int idendation);
    }

    public static class OptExprListExpression extends OptExprList {

        private final Expr expr;

        private final RepExprList repexprlist;

        public OptExprListExpression(Expr expr, RepExprList repexprlist, int idendation) {
            super(idendation);
            this.expr = expr;
            this.repexprlist = repexprlist;
        }

        @Override
        public String toString() {
            return getHead("<OptExprListExpression>") + expr + repexprlist + getHead("</OptExprListExpression>");
        }

        @Override
        public AbstractTree.ExpressionList toAbstract(int idendation) {
            return new AbstractTree.ExpressionList(expr.toAbstract(idendation), repexprlist.toAbstract(idendation), idendation);
        }
    }

    public static class OptExprListEpsilon extends OptExprList {

        public OptExprListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptExprListEpsilon/>");
        }

        @Override
        public AbstractTree.ExpressionList toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepExprList extends ConcreteNode {

        RepExprList(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.ExpressionList toAbstract(int idendation);
    }

    public static class RepExprListComma extends RepExprList {

        private final Expr expr;

        private final RepExprList repexprlist;

        public RepExprListComma(Expr expr, RepExprList repexprlist, int idendation) {
            super(idendation);
            this.expr = expr;
            this.repexprlist = repexprlist;
        }

        @Override
        public String toString() {
            return getHead("<RepExprListComma>") + expr + repexprlist + getHead("</RepExprListComma>");
        }

        @Override
        public AbstractTree.ExpressionList toAbstract(int idendation) {
            return new AbstractTree.ExpressionList(expr.toAbstract(idendation + 1), repexprlist.toAbstract(idendation + 1), idendation);
        }
    }

    public static class RepExprListEpsilon extends RepExprList {

        public RepExprListEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepExprListEpsilon/>");
        }

        @Override
        public AbstractTree.ExpressionList toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepCase extends ConcreteNode {

        RepCase(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.RepCaseCmd toAbstract(int i);
    }

    public static class RepCaseCase extends RepCase {

        private final Tokens.LiteralToken literal;

        private final CpsCmd cpscmd;

        private final RepCase repcase;

        public RepCaseCase(Tokens.LiteralToken literal, CpsCmd cpscmd, RepCase repcase, int idendation) {
            super(idendation);
            this.literal = literal;
            this.cpscmd = cpscmd;
            this.repcase = repcase;
        }

        @Override
        public String toString() {
            return getHead("<RepCaseCase Name='" + literal.getValue() + "'>") + cpscmd + getHead("</RepCaseCase>");
        }

        public AbstractTree.RepCaseCmd toAbstract(int idendation) {
            return new AbstractTree.RepCaseCmd(literal, cpscmd.toAbstract(idendation + 1), repcase.toAbstract(idendation + 1), idendation);
        }
    }

    public static class RepCaseEpsilon extends RepCase {

        public RepCaseEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepCaseEpsilon/>");
        }

        public AbstractTree.RepCaseCmd toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class OptDefault extends ConcreteNode {

        OptDefault(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Cmd toAbstract(int i);
    }

    public static class OptDefaultDefault extends OptDefault {

        private final CpsCmd cpscmd;

        public OptDefaultDefault(CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<OptDefaultDefault/>") + cpscmd + getHead("</OptDefaultDefault>");
        }

        public AbstractTree.Cmd toAbstract(int idendation) {
            return cpscmd.toAbstract(idendation + 1);
        }
    }

    public static class OptDefaultEpsilon extends OptDefault {

        public OptDefaultEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptDefaultEpsilon/>");
        }

        public AbstractTree.Cmd toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class OptElse extends ConcreteNode {

        OptElse(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.Cmd toAbstract(int i);
    }

    public static class OptElseElse extends OptElse {

        private final CpsCmd cpscmd;

        public OptElseElse(CpsCmd cpscmd, int idendation) {
            super(idendation);
            this.cpscmd = cpscmd;
        }

        @Override
        public String toString() {
            return getHead("<OptElseElse>") + cpscmd + getHead("</OptElseElse>");
        }

        public AbstractTree.Cmd toAbstract(int idendation) {
            return cpscmd.toAbstract(idendation + 1);
        }
    }

    public static class OptElseEpsilon extends OptElse {

        public OptElseEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<OptElseEpsilon/>");
        }

        public AbstractTree.Cmd toAbstract(int idendation) {
            return null;
        }
    }

    public static abstract class RepElseif extends ConcreteNode {

        RepElseif(int idendation) {
            super(idendation);
        }

        public abstract AbstractTree.RepCondCmd toAbstract(int idendation);
    }

    public static class RepElseifElseif extends RepElseif {

        private final Expr expr;

        private final CpsCmd cpscmd;

        private final RepElseif repelseif;

        public RepElseifElseif(Expr expr, CpsCmd cpscmd, RepElseif repelseif, int idendation) {
            super(idendation);
            this.expr = expr;
            this.cpscmd = cpscmd;
            this.repelseif = repelseif;
        }

        @Override
        public String toString() {
            return getHead("<RepElseifElseif>") + expr + cpscmd + repelseif + getHead("</RepElseifElseif>");
        }

        public AbstractTree.RepCondCmd toAbstract(int idendation) {
            return new AbstractTree.RepCondCmd(expr.toAbstract(idendation), cpscmd.toAbstract(idendation + 1), repelseif.toAbstract(idendation), idendation);
        }
    }

    public static class RepElseifEpsilon extends RepElseif {

        public RepElseifEpsilon(int idendation) {
            super(idendation);
        }

        @Override
        public String toString() {
            return getHead("<RepElseifEpsilon/>");
        }

        public AbstractTree.RepCondCmd toAbstract(int idendation) {
            return null;
        }
    }
}
