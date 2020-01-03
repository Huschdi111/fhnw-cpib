package ch.fhnw.cpib.platform.parser.abstracttree;

import ch.fhnw.cpib.platform.checker.*;
import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IInstructions;
import ch.fhnw.cpib.platform.parser.concretetree.ConcreteTree;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;
import java.util.*;

public class AbstractTree {

    public static class Program extends AbstractNode {

        public final Tokens.IdentifierToken identifier;

        public final ProgParam progparam;

        public final Declaration declaration;

        public final Cmd cmd;

        public Program(Tokens.IdentifierToken identifier, ProgParam progparam
            , Declaration declaration, Cmd cmd) {
            super(0);
            this.identifier = identifier;
            this.progparam = progparam;
            this.declaration = declaration;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<Program>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (progparam != null ? progparam : getBody("<NoProgramParameter/>"))
                + (declaration != null ? declaration : getBody("<NoDeclarations/>"))
                + (cmd != null ? cmd : getBody("<NoCmd/>"))
                + ("</Program>");
        }

        public void checkCode() throws CheckerException {
            if (progparam != null) {
                progparam.checkCode();
            }
            if (declaration != null) {
                declaration.checkCode();
                declaration.checkLocale(-1);
            }
            Checker.setScope(new Scope(Checker.getGlobalStoreTable().clone()));
            if(cmd != null) cmd.checkCode(false);
        }

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            System.out.println("PROGRAM generate");
            int loc1 = loc;
            int loc2 = loc;

            if (declaration != null)
                if(declaration instanceof ProcDecl || declaration instanceof FunDecl){
                    loc1 = declaration.nextdeclaration.generateCode(loc1);
                    loc2 = declaration.generateCode(loc1+1);
                    Checker.getcodeArray().put(loc1, new IInstructions.UncondJump(loc2));
                }else{
                    loc2 = declaration.generateCode(loc1);
                }
            loc2 = cmd.generateCode(loc2, false);
            for (Routine routine : Checker.getRoutineTable().getTable().values()) {
                routine.codeCalls();
            }
            Checker.getcodeArray().put(loc2, new IInstructions.Stop());
            return loc2;
        }

        public String getProgramName() {
            return identifier.getName();
        }
    }

    public static class ProgParam extends AbstractNode {

        public final Tokens.FlowModeToken flowmode;

        public final Tokens.ChangeModeToken changemode;

        public final TypedIdent typedident;

        public final ProgParam nextprogparam;

        public ProgParam(Tokens.FlowModeToken flowmode, Tokens.ChangeModeToken changemode, TypedIdent typedident, ProgParam nextprogparam, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.changemode = changemode;
            this.typedident = typedident;
            this.nextprogparam = nextprogparam;
        }

        @Override
        public String toString() {
            return getHead("<ProgParam>")
                + getBody("<Mode Name='FLOWMODE' Attribute='" + flowmode.getFlowMode() + "'/>'")
                + getBody("<Mode Name='CHANGEMODE' Attribute='" + changemode.getChangeMode() + "'/>'")
                + typedident
                + (nextprogparam != null ? nextprogparam : getBody("<NoNextProgParam/>"))
                + getHead("</ProgParam>");
        }

        public void checkCode() throws CheckerException {
            //check if identifier exist in global store table
            if (!Checker.getGlobalStoreTable().addStore(buildStore())) {
                throw new CheckerException("Identifier " + getIdent() + " is already declared");
            }

            if (nextprogparam != null) {
                nextprogparam.checkCode();
            }
        }

        private String getIdent() {
            try {
                return typedident.getIdentifier().getName();
            }catch(NullPointerException exp) {
                return null;
            }
        }

        private Store buildStore() {
            return new Store(getIdent(), typedident.getType(),
                changemode.getChangeMode() == Tokens.ChangeModeToken.ChangeMode.CONST);
        }
    }

    public static class Param extends AbstractNode {

        public final Tokens.FlowModeToken flowmode;

        public final Tokens.MechModeToken mechmode;

        public final Tokens.ChangeModeToken changemode;

        public final TypedIdent typedident;

        public final Param nextparam;

        public Param(Tokens.FlowModeToken flowmode, Tokens.MechModeToken mechmode, Tokens.ChangeModeToken changemode, TypedIdent typedident, Param nextparam, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.mechmode = mechmode;
            this.changemode = changemode;
            this.typedident = typedident;
            this.nextparam = nextparam;
        }

        @Override
        public String toString() {
            return getHead("<Param>")
                + getBody("<Mode Name='FLOWMODE' Attribute='" + flowmode.getFlowMode() + "'/>'")
                + getBody("<Mode Name='MECHMODE' Attribute='" + mechmode.getMechMode() + "'/>'")
                + getBody("<Mode Name='CHANGEMODE' Attribute='" + changemode.getChangeMode() + "'/>'")
                + typedident
                + (nextparam != null ? nextparam : getBody("<NoNextParam/>"))
                + getHead("</Param>");
        }

        public void checkCode(Routine routine) throws CheckerException {
            Store store = buildStore();
            Checker.getScope().getStoreTable().addStore(store);
            switch (flowmode.getFlowMode()) {
                case IN:
                    //passing parameter must be constant
                    if (store != null && mechmode.getMechMode() == Tokens.MechModeToken.MechMode.REF && !store.isConst()) {
                        throw new CheckerException("IN reference parameter can not be var! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName()
                        , typedident.getType(), flowmode.getFlowMode()
                        , mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                case INOUT:
                    if (routine.getRoutineType() != RoutineType.PROCEDURE) {
                        throw new CheckerException("INOUT parameter in function declaration! Ident: " + store.getIdentifier());
                    }
                    if (store != null && store.isConst()) {
                        throw new CheckerException("INOUT parameter can not be constant! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName()
                        , typedident.getType(), flowmode.getFlowMode()
                        , mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                case OUT:
                    if (routine.getRoutineType() != RoutineType.PROCEDURE) {
                        throw new CheckerException("OUT parameter in function declaration! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName()
                        , typedident.getType(), flowmode.getFlowMode()
                        , mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                default:
                    break;
            }
            if (nextparam != null) {
                nextparam.checkCode(routine);
            }
        }

        public int calculateAddress(final int count, final int locals) {
            int locals1 = locals;
            Store store = Checker.getScope().getStoreTable().getStore(typedident.getIdentifier().getName());
            if (mechmode.getMechMode() == Tokens.MechModeToken.MechMode.REF) {
                store.setAddress(-count);
                store.setRelative(true);
                if (mechmode.getMechMode() == Tokens.MechModeToken.MechMode.REF) {
                    store.setReference(true);
                } else {
                    store.setReference(false);
                }
            } else {
                store.setAddress(2 + ++locals1);
                store.setRelative(true);
                store.setReference(false);
            }

            return (nextparam != null ? nextparam.calculateAddress(count - 1, locals1) : locals1);
        }

        public int generateCode(int loc, int numOfParams) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            Checker.getcodeArray().put(loc1++,new IInstructions.AllocBlock(numOfParams));
            return loc1;
        }

        private Store buildStore() {
            return new Store(getIdent(), typedident.getType(),
                changemode.getChangeMode() == Tokens.ChangeModeToken.ChangeMode.CONST);
        }

        private String getIdent() {
            return typedident.getIdentifier().getName();
        }
    }

    public abstract static class Declaration extends AbstractNode {

        public final Declaration nextdeclaration;

        public Declaration(Declaration nextdeclaration, int idendation) {
            super(idendation);
            this.nextdeclaration = nextdeclaration;
        }

        public Declaration getNextDeclaration() {
            return nextdeclaration;
        }

        public abstract Tokens.TypeToken.Type checkCode() throws CheckerException;

        public abstract int checkLocale(final int locals) throws CheckerException;

        public abstract int generateCode(int loc) throws ICodeArray.CodeTooSmallError;

        public abstract String getIdent();
    }

    /*Defines the Decleration of a variable*/
    public static class StoDecl extends Declaration {
        //var, const modifier
        public final Tokens.ChangeModeToken changemode;
        //type info
        public final TypedIdent typedident;

        public StoDecl(Tokens.ChangeModeToken changemode, TypedIdent typedident, Declaration nextdeclaration, int idendation) {
            super(nextdeclaration, idendation);
            this.changemode = changemode;
            this.typedident = typedident;
        }

        @Override
        public String toString() {
            return getHead("<StoDecl>")
                + getBody("<Mode Name='CHANGEMODE' Attribute='" + changemode.getChangeMode() + "'/>'")
                + typedident
                + (getNextDeclaration() != null ? getNextDeclaration() : getBody("<NoNextDeclaration/>"))
                + getHead("</StoDecl>");
        }

        @Override
        public Tokens.TypeToken.Type checkCode() throws CheckerException {
            Store store = buildStore();
            if (typedident instanceof TypedIdentType) {
                if (!Checker.getGlobalStoreTable().addStore(store)) {
                    throw new CheckerException("Identifier already declared: "
                        + getIdent() + ":line: " + typedident.getIdentifier().getRow());
                }
            } else {
                throw new CheckerException("Type is unknown : "
                    + getIdent() + ":line: " + typedident.getIdentifier().getRow());
            }

            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode();
            }
            return null;
        }

        @Override
        public int checkLocale(final int locals) throws CheckerException {
            if (locals < 0) {
                return -1;
            } else {
                Store store = checkStore();
                store.setAddress(2 + locals + 1);
                store.setRelative(true);
                store.setReference(false);
                return locals + 1;
            }
        }

        @Override
        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            System.out.println("STODECL generate");
            int loc1 = loc;
            Declaration dec = this;
            boolean nextDeclNull = false;

            while (!nextDeclNull) {
                if (dec.nextdeclaration == null) {
                    nextDeclNull = true;
                }
                //Allocate space and safe store in the right
                Checker.getcodeArray().put(loc1, new IInstructions.AllocBlock(1));
                Checker.addIdentTable(dec.getIdent(), Checker.getstackAddressHelper());
                Checker.setstackAddressHelper(1);
                loc1++;
                if(!nextDeclNull) dec = dec.nextdeclaration;
            }
            return loc1;
        }

        private Store buildStore() {
            return new Store(typedident.getIdentifier().getName(), typedident.getType(),
                changemode.getChangeMode() == Tokens.ChangeModeToken.ChangeMode.CONST);
        }

        public Store checkStore() throws CheckerException{
            Store store = buildStore();
            if (typedident instanceof TypedIdentType) {
                if (!Checker.getGlobalStoreTable().addStore(store)) {
                    throw new CheckerException("Identifier already declared: " + getIdent() + ":line: " + typedident.getIdentifier().getRow());
                }
            } else {
                throw new CheckerException("Type is unknown : " + getIdent() + ":line: " + typedident.getIdentifier().getRow());
            }
            return store;
        }

        @Override
        public String getIdent() {
           return typedident.getIdentifier().getName();
        }
    }

    public static class FunDecl extends Declaration {

        public final Tokens.IdentifierToken identifier;

        public final Param param;

        public final Declaration storedeclaration;

        public final GlobalImport globalimport;

        public final Declaration declaration;

        public final Cmd cmd;

        public FunDecl(Tokens.IdentifierToken identifier, Param param, Declaration storedeclaration, GlobalImport globalimport, Declaration declaration, Cmd cmd, Declaration nextdeclaration, int idendation) {
            super(nextdeclaration, idendation);
            this.identifier = identifier;
            this.param = param;
            this.storedeclaration = storedeclaration;
            this.globalimport = globalimport;
            this.declaration = declaration;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<FunDecl>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (param != null ? param : getBody("<NoParam/>"))
                + (storedeclaration != null ? storedeclaration : getBody("<NoNextStoreDeclaration/>"))
                + (globalimport != null ? globalimport : getBody("<NoGlobalImport/>"))
                + (cmd != null ? cmd : getBody("<NoCmd/>"))
                + (declaration != null ? declaration : getBody("<NoDeclaration/>"))
                + (getNextDeclaration() != null ? getNextDeclaration() : getBody("<NoNextDeclaration/>"))
                + getHead("</FunDecl>");
        }

        @Override
        public Tokens.TypeToken.Type checkCode() throws CheckerException {
            //check if function exist in global routine table
            Routine function = new Routine(identifier.getName(), RoutineType.FUNCTION);
            //store function in global routine table if not
            if (!Checker.getRoutineTable().insert(function)) {
                throw new CheckerException("Function " + identifier.getName() + " is already declared.");
            }
            Checker.setScope(function.getScope());
            if (param != null) {
                param.checkCode(function);
            }
            if (storedeclaration != null) {
                storedeclaration.checkCode();
            }
            if (globalimport != null) {
                globalimport.checkCode(function);
            }
            if (cmd != null) {
                cmd.checkCode(false); //TODO Lukas false or true
            }
            Checker.setScope(null);
            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode();
            }
            return null;
        }

        @Override
        public int checkLocale(int locals) throws CheckerException {
            return 0; //TODO Lukas
        }

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            Routine routine = Checker.getRoutineTable().lookup(identifier.getName());
            Checker.setScope(routine.getScope());
            routine.setAddress(loc1);
            int i = 0 - routine.getParameters().size();
            for (Parameter p : routine.getParameters()){
                if (p.getMechMode() == Tokens.MechModeToken.MechMode.COPY){
                    Checker.getprocIdentTable().put(p.getName(), new String[] {i+"",p.getMechMode().name()});
                }else{
                    Checker.getprocIdentTable().put(p.getName(), new String[] {i+"","REF"});
                }
                i += 1;
            }
            Checker.getprocIdentTable().put(storedeclaration.getIdent(), new String[] {(0- routine.getParameters().size() - 1) +"","REF"});
            loc1 = cmd.generateCode(loc1, true);
            Checker.getcodeArray().put(loc1, new IInstructions.Return(1));
            return ++loc1;
        }

        @Override
        public String getIdent() {
            return identifier.getName();
        }
    }

    /*Procedure Decleration*/
    public static class ProcDecl extends Declaration {

        //Name of the procedure
        public final Tokens.IdentifierToken identifier;

        //Parameter List
        public final Param param;

        public final GlobalImport globalimport;

        public final Declaration declaration;

        //First Cmd of this procedure
        public final Cmd cmd;

        public ProcDecl(Tokens.IdentifierToken identifier, Param param, GlobalImport globalimport, Declaration declaration, Declaration nextdeclaration, Cmd cmd, int idendation) {
            super(nextdeclaration, idendation);
            this.identifier = identifier;
            this.param = param;
            this.globalimport = globalimport;
            this.declaration = declaration;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<ProcDecl>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (param != null ? param : getBody("<NoNextParam/>"))
                + (globalimport != null ? globalimport : getBody("<NoGlobalImport/>"))
                + (cmd != null ? cmd : getBody("<NoCmd/>"))
                + (declaration != null ? declaration : getBody("<NoDeclaration/>"))
                + (getNextDeclaration() != null ? getNextDeclaration() : getBody("<NoNextDeclaration/>"))
                + getHead("</ProcDecl>");
        }

        @Override
        public Tokens.TypeToken.Type checkCode() throws CheckerException {
            //store function in global procedure table
            Routine procedure = new Routine(identifier.getName(), RoutineType.PROCEDURE);
            if(!Checker.getRoutineTable().insert(procedure))
                throw new CheckerException("No duplicate declaration of procedure: " + identifier.getName());
            Checker.setScope(procedure.getScope());
            if (param != null) {
                param.checkCode(procedure);
            }

            if (declaration != null) {
                if(declaration instanceof FunDecl || declaration instanceof ProcDecl )
                    throw new CheckerException("Function or Procedure can only be declared globally: " + identifier.getName());
                declaration.checkCode();
            }

            Checker.setScope(null);
            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode();
            }
            return null;
        }

        @Override
        public int checkLocale(int locals) throws CheckerException {
            //Checke das die Procedur nur global definiert werden darf
            if (locals >= 0) {
                throw new CheckerException("Function declarations are only allowed globally!");
            }
            Routine routine = Checker.getRoutineTable().getTable().get(identifier.getName());
            Checker.setScope(routine.getScope());
            if (globalimport != null)
                globalimport.checkCode(routine);

            int localsCount = 0;
            if(param != null)
                localsCount = param.calculateAddress(routine.getParameters().size(), 0);

            if (declaration != null) {
                if(declaration instanceof FunDecl || declaration instanceof ProcDecl )
                    throw new CheckerException("Function or Procedure can only be declared globally: " + identifier.getName());
                declaration.checkLocale(localsCount);
            }

            cmd.checkCode(false);
            Checker.setScope(null);
            return -1;
        }

        @Override
        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            Routine routine = Checker.getRoutineTable().getTable().get(getIdent());
            Checker.setScope(routine.getScope());
            routine.setAddress(loc1);
            //relative negative addressen
            int numOfParam = routine.getParameters().size();
            int i = 0 - numOfParam;
            for (Parameter p : routine.getParameters()){
                if (p.getMechMode() == Tokens.MechModeToken.MechMode.COPY){
                    Checker.getprocIdentTable().put(p.getName(), new String[] {i+"",p.getMechMode().name()});
                }else{
                    Checker.getprocIdentTable().put(p.getName(), new String[] {i+"","REF"});
                }
                i += 1;
            }
            param.generateCode(loc1, numOfParam);
            loc1 += numOfParam;
            loc1 = (cmd != null)? cmd.generateCode(loc1, true): loc1;
            Checker.getcodeArray().put(loc1++, new IInstructions.Return(1));
            return loc1;
        }

        @Override
        public String getIdent() {
            return identifier.getName();
        }
    }

    public abstract static class Cmd extends AbstractNode {

        public final Cmd nextcmd;

        public Cmd(Cmd nextcmd, int idendation) {
            super(idendation);
            this.nextcmd = nextcmd;
        }

        public Cmd getNextCmd() {
            return nextcmd;
        }

        public abstract void checkCode(boolean canInit) throws CheckerException;

        public abstract int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError;
    }

    public static class SkipCmd extends Cmd {

        public SkipCmd(Cmd nextcmd, int idendation) {
            super(nextcmd, idendation); // TODO: Check if skip does really what we think
        }

        @Override
        public String toString() {
            return getHead("<CmdSkip>")
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</CmdSkip>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return (nextcmd != null ? nextcmd.generateCode(loc, routine) : loc);
        }
    }

    public static class AssiCmd extends Cmd {

        public final Expression expression1;

        public final ExpressionList expressionlist1;

        public final Expression expression2;

        public final ExpressionList expressionlist2;

        public AssiCmd(Expression expression1, ExpressionList expressionlist1, Expression expression2, ExpressionList expressionlist2, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression1 = expression1;
            this.expressionlist1 = expressionlist1;
            this.expression2 = expression2;
            this.expressionlist2 = expressionlist2;
        }

        @Override
        public String toString() {
            return getHead("<AssiCmd>")
                + expression1
                + (expressionlist1 != null ? expressionlist1 : getBody("<NoNextExpressionList/>"))
                + expression2
                + (expressionlist2 != null ? expressionlist2 : getBody("<NoNextExpressionList/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</AssiCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            List<ExpressionInfo> targetExprInfos = new ArrayList<>();
            List<ExpressionInfo> sourceExprInfos = new ArrayList<>();

            ExpressionInfo sourceExprInfo = expression2.checkCode();
            ExpressionInfo targetExprInfo = expression1.checkCode();

            if (expressionlist1 != null) {
                expressionlist1.checkCode(targetExprInfos);
            }
            if (expressionlist2 != null) {
                expressionlist2.checkCode(sourceExprInfos);
            }

            //check if first type on each side has same type
            boolean normalassignmentvalid = targetExprInfo.getType() == sourceExprInfo.getType();

            //normal assignment
            if (expressionlist1 == null && expressionlist2 == null) {
                //check if null because variable can be used without initialisation
                if (!normalassignmentvalid && targetExprInfo.getType() != null && sourceExprInfo.getType() != null) {
                    //allow assignment INT to INT64
                    if (targetExprInfo.getType() != Tokens.TypeToken.Type.INT64 && sourceExprInfo.getType() != Tokens.TypeToken.Type.INT) {
                        throw new CheckerException("Assignment not possible due different datatypes: " +
                            targetExprInfo.getType() + " = " + sourceExprInfo.getType());
                    }
                }
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            System.out.println("ASSICMD generate");
            int loc1 = loc;
            if(expression1 instanceof StoreExpr) {
                int locAfAdress = expression1.generateCode(loc1, routine); //ladet die addresse
                int locAfValue = expression2.generateCode(locAfAdress, routine); // legt wert auf locAfAdresse ab
                Checker.getcodeArray().put(locAfValue, new IInstructions.Store());
                loc1 = locAfValue + 1;
            }
            return (getNextCmd() != null ? getNextCmd().generateCode(loc1, routine) : loc1);
        }
    }

    public static class SwitchCmd extends Cmd {

        public final Expression expression;

        public final RepCaseCmd repcasecmd;

        public final Cmd cmd;

        public SwitchCmd(Expression expression, RepCaseCmd repcasecmd, Cmd cmd, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
            this.repcasecmd = repcasecmd;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<SwitchCmd>")
                + expression
                + repcasecmd
                + (cmd != null ? cmd : getBody("<NoDefaultCmd/>"))
                + getHead("</SwitchCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            ExpressionInfo exprinfo = expression.checkCode();
            Switch s = new Switch(exprinfo.getName(), exprinfo.getType());
            //store switch with name and type
            Checker.getGlobalSwitchTable().insert(s);
            repcasecmd.checkCode(canInit);
            if (cmd != null) {
                cmd.checkCode(canInit);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return -1;
        }
    }

    public static class RepCaseCmd extends Cmd {

        public final Tokens.LiteralToken literal;

        public final Cmd cmd;

        public RepCaseCmd(Tokens.LiteralToken literal, Cmd cmd, RepCaseCmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.literal = literal;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<RepCaseCmd>")
                + getBody("<Literal Value='" + literal.getValue() + "'/>")
                + cmd
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextRepCaseCmd/>"))
                + getHead("</RepCaseCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            HashMap<String, Switch> map = Checker.getGlobalSwitchTable().getTable();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Switch s = (Switch) pair.getValue();
                //check if case literal vales are different
                if (!s.getSwitchCaseList().isEmpty()) {
                    for (SwitchCase c : s.getSwitchCaseList()) {
                        if (c.getLiteraltoken().getValue().equals(literal.getValue())) {
                            throw new CheckerException("Case literal values have the same value.");
                        }
                    }
                }

                //check if switch type and case literal type have the same type
                if (!s.getSwitchCaseList().isEmpty()) {
                    for (SwitchCase c : s.getSwitchCaseList()) {
                        if (s.getType() != literal.getType()) {
                            throw new CheckerException("SwitchCase expr and case literal are not from the same type. " +
                                "Current switch expr type: " + s.getType() +
                                "Current case literal type: " + literal.getType());
                        }
                    }
                }

                //add case literal with value and type
                SwitchCase switchCase = new SwitchCase(literal);
                //store case to switch
                s.addSwitchCase(switchCase);
                Checker.getGlobalSwitchTable().insert(s);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return -1;
        }
    }

    public static class CondCmd extends Cmd {

        public final Expression expression;

        public final Cmd cmd;

        public final RepCondCmd repcondcmd;

        public final Cmd othercmd;

        public CondCmd(Expression expression, Cmd cmd, RepCondCmd repcondcmd, Cmd othercmd, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
            this.cmd = cmd;
            this.repcondcmd = repcondcmd;
            this.othercmd = othercmd;
        }

        @Override
        public String toString() {
            return getHead("<CondCmd>")
                + expression
                + cmd
                + (repcondcmd != null ? repcondcmd : getBody("<NoNextRepCondCmd/>"))
                + (othercmd != null ? othercmd : getBody("<NoOtherCmd/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</CondCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode();
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("IF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (repcondcmd != null) {
                repcondcmd.checkCode(canInit);
            }
            if (othercmd != null) {
                othercmd.checkCode(canInit);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = expression.generateCode(loc, routine);
            int loc2 = cmd.generateCode(loc1 + 1, routine);
            // Compiler.getVM().CondJump(loc1, loc2 + 1);
            Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc2 + 1));
            int loc3;

            if(othercmd == null) loc3 = loc2;
            else loc3 = othercmd.generateCode(loc2 + 1, routine);
            // Compiler.getVM().UncondJump(loc2, loc3);
            Checker.getcodeArray().put(loc2, new IInstructions.UncondJump(loc3));
            return (nextcmd != null ? nextcmd.generateCode(loc3, routine) : loc3);
        }
    }

    public static class GuardedCondCmd extends Cmd {

        public final RepArrowCmd repArrowCmd;

        public GuardedCondCmd(RepArrowCmd repArrowCmd, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.repArrowCmd = repArrowCmd;
        }

        @Override
        public String toString() {
            return getHead("<GuardedCondCmd>")
                + repArrowCmd.toString()
                + getHead("</GuardedCondCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            repArrowCmd.checkCode(canInit);
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            loc1 = repArrowCmd.generateCode(loc1,  routine);
            return (nextcmd != null)? nextcmd.generateCode(loc1, routine) : loc1;
        }
    }

    public static class RepArrowCmd extends Cmd {

        public final Expression expression;

        public final Cmd cmd;

        public RepArrowCmd(Expression expression, Cmd cmd, RepArrowCmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<RepArrowCmd>") + getHead(expression.toString())
                + getHead(cmd.toString())
                + (nextcmd != null ? nextcmd.toString() : getBody("<NoCmd/>"))
                + getHead("</RepArrowCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode();
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("IF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (cmd != null) {
                cmd.checkCode(canInit);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int locJump = expression.generateCode(loc, routine);;
            int locCMD = locJump + 1;
            //Where to jump to locCMDEnd
            int locCMDEnd = cmd.generateCode(locCMD, routine);
            //reserve the space for unconditional jump until end of guard if is known
            int locUncondJump = locCMDEnd;
            locCMDEnd++;
            //Now add the Cond jump to the end of cmd decl
            Checker.getcodeArray().put(locJump, new IInstructions.CondJump(locCMDEnd));
            int locEndNextConditional = (nextcmd != null)? nextcmd.generateCode(locCMDEnd, routine): locCMDEnd;
            Checker.getcodeArray().put(locUncondJump, new IInstructions.UncondJump(locEndNextConditional));
            return locEndNextConditional;
        }
    }

    public static class RepCondCmd extends Cmd {

        public final Expression expression;

        public final Cmd cmd;

        public final RepCondCmd repcondcmd;

        public RepCondCmd(Expression expression, Cmd cmd, RepCondCmd repCondCmd, int idendation) {
            super(repCondCmd, idendation);
            this.expression = expression;
            this.cmd = cmd;
            this.repcondcmd = repCondCmd;
        }

        @Override
        public String toString() {
            return getHead("<RepCondCmd>")
                + expression
                + cmd
                + (repcondcmd != null ? repcondcmd : getBody("<NoNextRepCondCmd/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextRepCondCmd/>"))
                + getHead("</RepCondCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode();
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("ELSEIF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (repcondcmd != null) {
                repcondcmd.checkCode(canInit);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

    }

    public static class WhileCmd extends Cmd {

        public final Expression expression;

        public final Cmd cmd;

        public WhileCmd(Expression expression, Cmd cmd, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return getHead("<WhileCmd>")
                + expression
                + cmd
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoCmd/>"))
                + getHead("</WhileCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode();
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL && exprinfo.getType() != null) {
                throw new CheckerException("WHILE condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = expression.generateCode(loc, routine);
            int loc2 = cmd.generateCode(loc1 + 1, routine);
            // Compiler.getVM().CondJump(loc1, loc2 + 1);
            // Compiler.getVM().UncondJump(loc2, loc);
            Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc2 + 1));
            Checker.getcodeArray().put(loc2, new IInstructions.UncondJump(loc));
            return (nextcmd != null ? nextcmd.generateCode(loc2 + 1, routine) : (loc2 + 1));
        }
    }

    public static class ProcCallCmd extends Cmd {

        public final RoutineCall routinecall;

        public final GlobalInit globalinit;

        public ProcCallCmd(RoutineCall routinecall, GlobalInit globalinit, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.routinecall = routinecall;
            this.globalinit = globalinit;
        }

        @Override
        public String toString() {
            return getHead("<ProcCallCmd>")
                + routinecall
                + (globalinit != null ? globalinit : getBody("<NoGlobalInit/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</ProcCallCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            if (globalinit != null) {
                globalinit.checkCode();
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            loc1 = routinecall.expressionlist.generateCode(loc1, routine);
            Checker.getRoutineTable().lookup(routinecall.identifier.getName()).addCall(loc1++);
            return (nextcmd != null ? nextcmd.generateCode(loc1, routine) : loc1);
        }
    }

    public static class InputCmd extends Cmd {

        public final Expression expression;
        public Tokens.TypeToken.Type type;

        public InputCmd(Expression expression, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
        }

        @Override
        public String toString() {
            return getHead("<InputCmd>")
                + (expression != null ? expression : getBody("<NoExpression/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</InputCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            if (getNextCmd() != null) {
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1;
            loc1 = ((StoreExpr) expression).codeRef(loc, true, false, routine);
            if(expression instanceof StoreExpr) {
                if (type == Tokens.TypeToken.Type.BOOL) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.InputBool(((StoreExpr) expression).identifier.getName()));
                } else {
                    Checker.getcodeArray().put(loc1++, new IInstructions.InputInt(((StoreExpr) expression).identifier.getName()));
                }
            }else {
                throw new IllegalArgumentException("Wrong Expression while code generation");
            }
            return (nextcmd != null ? nextcmd.generateCode(loc1, routine) : loc1);
        }
    }

    public static class OutputCmd extends Cmd {

        public final Expression expression;
        public Tokens.TypeToken.Type type;

        public OutputCmd(Expression expression, Cmd nextcmd, int idendation) {
            super(nextcmd, idendation);
            this.expression = expression;
        }

        @Override
        public String toString() {
            return getHead("<OutputCmd>")
                + (expression != null ? expression : getBody("<NoExpression/>"))
                + (getNextCmd() != null ? getNextCmd() : getBody("<NoNextCmd/>"))
                + getHead("</OutputCmd>");
        }

        @Override
        public void checkCode(boolean canInit) throws CheckerException {
            if (getNextCmd() != null) {
                ExpressionInfo exInfo = expression.checkCode();
                type = exInfo.getType();
                getNextCmd().checkCode(canInit);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            System.out.println("OUTPUTCMD generate");
            int loc1;
            loc1 = expression.generateCode(loc, routine);
            Checker.getcodeArray().put(loc1++, new IInstructions.Deref());
            if (expression instanceof StoreExpr) {
                if (type == Tokens.TypeToken.Type.BOOL) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputBool(((StoreExpr) expression).identifier.getName()));
                } else {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputInt(((StoreExpr) expression).identifier.getName()));
                }
            }else {
                throw new IllegalArgumentException("Must be a StoreExpression for output");
            }
            return (nextcmd != null ? nextcmd.generateCode(loc1, routine) : loc1);
        }
    }

    public abstract static class TypedIdent<T> extends AbstractNode {

        public TypedIdent(int idendation) {
            super(idendation);
        }

        public abstract Tokens.IdentifierToken getIdentifier();

        public abstract Tokens.TypeToken.Type getType();
    }

    /*Defines the Type of a Store
    * Gets used in StoDecl as an attribute*/
    public static class TypedIdentType extends TypedIdent<Tokens.IdentifierToken> {

        public final Tokens.IdentifierToken identifier;

        public final Tokens.TypeToken.Type type;

        public TypedIdentType(Tokens.IdentifierToken identifier
            , Tokens.TypeToken.Type type, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public String toString() {
            return getHead("<TypedIdentType>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + getBody("<Type Type='" + type + "'/>")
                + getHead("</TypedIdentType>");
        }

        public String getParameterName() {
            return identifier.getName();
        }

        public Tokens.TypeToken.Type getParameterType() {
            return type;
        }

        @Override
        public Tokens.IdentifierToken getIdentifier() {
            return identifier;
        }

        @Override
        public Tokens.TypeToken.Type getType() {
            return type;
        }
    }

    public abstract static class Expression<T> extends AbstractNode {

        public Expression(int idendation) {
            super(idendation);
        }

        public abstract ExpressionInfo checkCode() throws CheckerException;

        public abstract int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError;
    }

    //Represents a plain Number
    public static class LiteralExpr extends Expression {

        public final Tokens.LiteralToken literal;

        public LiteralExpr(Tokens.LiteralToken literal, int idendation) {
            super(idendation);
            this.literal = literal;
        }

        @Override
        public String toString() {
            return getHead("<LiteralExpr>")
                + getBody("<Literal Value='" + literal.getValue() + "'/>")
                + getHead("</LiteralExpr>");
        }

        @Override
        public ExpressionInfo checkCode() throws CheckerException {
            if (literal.getType() == Tokens.TypeToken.Type.BOOL) {
                return new ExpressionInfo(String.valueOf(literal.getValue()), Tokens.TypeToken.Type.BOOL);
            } else if (literal.getType() == Tokens.TypeToken.Type.INT) {
                return new ExpressionInfo(String.valueOf(literal.getValue()), Tokens.TypeToken.Type.INT);
            } else if (literal.getType() == Tokens.TypeToken.Type.INT64) {
                return new ExpressionInfo(String.valueOf(literal.getValue()), Tokens.TypeToken.Type.INT64);
            }
            throw new CheckerException("Invalid literal type");
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            Checker.getcodeArray().put(loc1, new IInstructions.LoadImInt(Integer.valueOf(literal.getValue())));
            loc1++;
            return loc1;
        }
    }

    //Represents a Variable
    public static class StoreExpr extends Expression {

        public final Tokens.IdentifierToken identifier;

        public final boolean initialized;

        public StoreExpr(Tokens.IdentifierToken identifier, boolean initialized, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.initialized = initialized;
        }

        @Override
        public String toString() {
            return getHead("<StoreExpr>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + getBody("<Initialized>" + initialized + "</Initialized>")
                + getHead("</StoreExpr>");
        }

        @Override
        public ExpressionInfo checkCode() throws CheckerException {
            //check if global scope applies
            StoreTable storetable;
            if (Checker.getScope() == null) {
                storetable = Checker.getGlobalStoreTable();
            } else {
                storetable = Checker.getScope().getStoreTable();
            }

            Store store = storetable.getStore(identifier.getName());
            if (store == null) {
                //check if store is declared on global scope
                store = Checker.getGlobalStoreTable().getStore(identifier.getName());
                //check if identifier in routine defined
                HashMap map = Checker.getRoutineTable().getTable();
                Routine routine = null;
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    routine = (Routine) pair.getValue();
                    List<Parameter> parameters = routine.getParameters();
                    for (Parameter parameter : parameters) {
                        if (parameter.getName().equals(identifier.getName())) {
                            return new ExpressionInfo(parameter.getName(), parameter.getType());
                        }
                    }
                }
                //store not initialized variable because before a initialisation of a variable it can be used
                if (Checker.getGlobalStoreTable().getStore(identifier.getName()) == null) {
                    Checker.getGlobalStoreTable().addStore(new Store(identifier.getName(), null, false));
                    return new ExpressionInfo(identifier.getName(), null);
                }
            }
            return new ExpressionInfo(store.getIdentifier(), store.getType());
        }


        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            //Get the store table of this scope
            //if we are in a routine
            Store store = Checker.getScope().getStoreTable().getStore(identifier.getName());
            if (routine) {
                if (Checker.getprocIdentTable().containsKey(identifier.getName())) {
                    if(store==null){
                        Checker.getcodeArray().put(loc1++, new IInstructions.LoadAddrRel(Integer.parseInt(Checker.getprocIdentTable().get(identifier.getName())[0])));
                        return loc1;
                    }else{
                        loc1 = store.codeRef(loc, true, false, routine);
                        return loc1;
                    }
                } else {
                    Checker.addIdentTable(identifier.getName(), loc);
                    return ((store != null) ? store.codeLoad(loc, routine) : loc);
                }
            } else {
                store = Checker.getScope().getStoreTable().getStore(identifier.getName());
                Checker.getcodeArray().put(loc1, new IInstructions.LoadImInt(Checker.getIdentTable().get(identifier.getName())));
            }
            loc1++;
            return loc1;
        }
        //
        public int codeRef(final int loc, boolean rel, boolean ref, boolean routine) throws ICodeArray.CodeTooSmallError {
            Store store = Checker.getScope().getStoreTable().getStore(identifier.getName());
            return ((store != null) ? store.codeRef(loc, rel, ref, routine) : loc);
        }
    }

    public static class FunCallExpr extends Expression {

        public final RoutineCall routinecall;

        public FunCallExpr(RoutineCall routinecall, int idendation) {
            super(idendation);
            this.routinecall = routinecall;
        }

        @Override
        public String toString() {
            return getHead("<FunCallExpr>")
                + routinecall
                + getHead("</FunCallExpr>");
        }

        public ExpressionInfo checkCode() throws CheckerException {
            return routinecall.checkCode();
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError { // TODO
            int loc1 = loc;
            Checker.getcodeArray().put(loc1++, new IInstructions.AllocBlock(1)); //referenz neu spechern?
            loc1 = routinecall.expressionlist.generateCode(loc1, routine);
            Checker.getRoutineTable().lookup(routinecall.identifier.getName()).addCall(loc1++);
            return loc1;
        }
    }

    public static class MonadicExpr extends Expression {

        public final Tokens.OperationToken operation;

        public final Expression expression;

        public MonadicExpr(Tokens.OperationToken operation, Expression expression, int idendation) {
            super(idendation);
            this.operation = operation;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return getHead("<MonadicExpr>")
                + getBody("<Operation Operation='" + operation.getOperation() + "'/>")
                + expression
                + getHead("</MonadicExpr>");
        }

        @Override
        public ExpressionInfo checkCode() throws CheckerException {
            return expression.checkCode();
        }

        @Override
        public int generateCode(int loc, boolean routine) {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    public static class DyadicExpr extends Expression {

        public final Tokens.OperationToken operation;

        public final Expression expression1;

        public final Expression expression2;

        public DyadicExpr(Tokens.OperationToken operation, Expression expression1, Expression expression2, int idendation) {
            super(idendation);
            this.operation = operation;
            this.expression1 = expression1;
            this.expression2 = expression2;
        }

        @Override
        public String toString() {
            return getHead("<DyadicExpr>")
                + getBody("<Operation Operation='" + operation.getOperation() + "'/>")
                + expression1
                + expression2
                + getHead("</DyadicExpr>");
        }

        @Override
        public ExpressionInfo checkCode() throws CheckerException {
            ExpressionInfo exprinfo1 = expression1.checkCode();
            ExpressionInfo exprinfo2 = expression2.checkCode();
            Tokens.OperationToken.Operation opr = operation.getOperation();

            switch (opr) {
                case PLUS:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT64 && exprinfo2.getType() == Tokens.TypeToken.Type.INT64) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT64);
                    }
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT && exprinfo2.getType() == Tokens.TypeToken.Type.INT) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT);
                    }
                    break;
                case TIMES:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT64 && exprinfo2.getType() == Tokens.TypeToken.Type.INT64) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT64);
                    }
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT && exprinfo2.getType() == Tokens.TypeToken.Type.INT) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT);
                    }
                    break;
                case DIVE:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT64 && exprinfo2.getType() == Tokens.TypeToken.Type.INT64) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT64);
                    }
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT && exprinfo2.getType() == Tokens.TypeToken.Type.INT) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT);
                    }
                    break;
                case MODE:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT64 && exprinfo2.getType() == Tokens.TypeToken.Type.INT64) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT64);
                    }
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT && exprinfo2.getType() == Tokens.TypeToken.Type.INT) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT);
                    }
                    break;
                case EQ:
                    return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                case NE:
                    // Also booleans possible, no Exception thrown
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.BOOL && exprinfo2.getType() == Tokens.TypeToken.Type.BOOL) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                    }
                    break;
                case GT:
                    return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                case LT:
                    return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                case GE:
                    return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                case LE:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT64 && exprinfo2.getType() == Tokens.TypeToken.Type.INT64) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT64);
                    }
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.INT && exprinfo2.getType() == Tokens.TypeToken.Type.INT) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.INT);
                    }
                case CAND:
                    break;
                case COR:
                    break;
                case AND:
                    break;
                case OR:
                    if (exprinfo1.getType() == Tokens.TypeToken.Type.BOOL && exprinfo2.getType() == Tokens.TypeToken.Type.BOOL) {
                        return new ExpressionInfo(exprinfo1.getName(), Tokens.TypeToken.Type.BOOL);
                    }
                    break;
            }
            return new ExpressionInfo(exprinfo1.getName(), exprinfo1.getType());
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1;
            if (expression1 instanceof StoreExpr) {
                loc1 = ((StoreExpr) expression1).codeRef(loc, true, true, routine);
            } else if (expression1 instanceof FunCallExpr) {
                loc1 = expression1.generateCode(loc, routine);
            } else {
                loc1 = expression1.generateCode(loc, routine);
            }

            if (operation.getOperation() != Tokens.OperationToken.Operation.CAND
                && operation.getOperation() != Tokens.OperationToken.Operation.COR) {

                if (expression2 instanceof StoreExpr) {
                    loc1 = ((StoreExpr) expression2).codeRef(loc1, true, true, routine);
                } else if (expression2 instanceof StoreExpr) {
                    loc1 = ((FunCallExpr) expression2).generateCode(loc1, routine);
                } else {

                    loc1 = expression2.generateCode(loc1, routine);
                }

                switch (operation.getOperation()) {
                    case PLUS:
                        // Compiler.getVM().IntAdd(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.AddInt());
                        break;
                    case MINUS:
                        // Compiler.getVM().IntSub(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.SubInt());
                        break;
                    case TIMES:
                        // Compiler.getVM().IntMult(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.MultInt());
                        break;
                    case DIVE:
                        // Compiler.getVM().IntDiv(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.DivTruncInt());
                        break;
                    case MODE:
                        // Compiler.getVM().IntMod(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.ModTruncInt());
                        break;
                    case EQ:
                        // Compiler.getVM().IntEQ(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.EqInt());
                        break;
                    case NE:
                        // Compiler.getVM().IntNE(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.NeInt());
                        break;
                    case GT:
                        // Compiler.getVM().IntGT(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.GtInt());
                        break;
                    case LT:
                        // Compiler.getVM().IntLT(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.LtInt());
                        break;
                    case GE:
                        // Compiler.getVM().IntGE(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.GeInt());
                        break;
                    case LE:
                        // Compiler.getVM().IntLE(loc1);
                        Checker.getcodeArray().put(loc1, new IInstructions.LeInt());
                        break;
                    default:
                        throw new RuntimeException();
                }

                return loc1 + 1;
            } else if (operation.getOperation() == Tokens.OperationToken.Operation.CAND) {
                int loc2 = expression2.generateCode(loc1 + 1, routine);
                // Compiler.getVM().UncondJump(loc2++, loc2 + 1);
                // Compiler.getVM().CondJump(loc1, loc2);
                // Compiler.getVM().IntLoad(loc2++, 0);
                Checker.getcodeArray().put(loc2++, new IInstructions.UncondJump(loc2 + 1));
                Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc2));
                Checker.getcodeArray().put(loc2++, new IInstructions.LoadImInt(0));
                return loc2;
            } else {
                int loc2 = expression2.generateCode(loc1 + 2, routine);
                // Compiler.getVM().UncondJump(loc2++, loc2 + 1);
                // Compiler.getVM().CondJump(loc1, loc1 + 2);
                // Compiler.getVM().UncondJump(loc1 + 1, loc2);
                // Compiler.getVM().IntLoad(loc2++, 1);
                Checker.getcodeArray().put(loc2++, new IInstructions.UncondJump(loc2 + 1));
                Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc1 + 2));
                Checker.getcodeArray().put(loc1 + 1, new IInstructions.UncondJump(loc2));
                Checker.getcodeArray().put(loc2++, new IInstructions.LoadImInt(1));
                return loc2;
            }
        }
    }

    public static class RoutineCall extends AbstractNode {

        public final Tokens.IdentifierToken identifier;

        public final ExpressionList expressionlist;

        public RoutineCall(Tokens.IdentifierToken identifier, ExpressionList expressionlist, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.expressionlist = expressionlist;
        }

        @Override
        public String toString() {
            return getHead("<RoutineCall>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (expressionlist != null ? expressionlist : getBody("<NoNextExpressionList/>"))
                + getHead("</RoutineCall>");
        }

        public ExpressionInfo checkCode() throws CheckerException {
            Routine calledroutine = Checker.getRoutineTable().lookup(identifier.getName());
            if (calledroutine == null) {
                throw new CheckerException("Routine " + identifier.getName() + " is not declared.");
            }

            List<ExpressionInfo> exprinfos = new ArrayList<>();
            if (expressionlist != null) {
                expressionlist.checkCode(exprinfos);
            }
            List<Parameter> parameters = calledroutine.getParameters();

            //check number of arguments
            if (parameters.size() != exprinfos.size()) {
                throw new CheckerException("Routine call: Number of arguments don't match: " + identifier.getName() + " expected: " +
                    parameters.size() + ", call has " + exprinfos.size());
            }

            //check for type
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getType() != exprinfos.get(i).getType() && exprinfos.get(i).getType() != null) {
                    throw new CheckerException("Routine call: Type of " + (i + 1) + ". Argument does not match. Expected: "
                        + parameters.get(i).getType() + ", call has: " + exprinfos.get(i).getType());
                }
            }
            return new ExpressionInfo(calledroutine.getIdentifier(), calledroutine.getReturnType());
        }

        //TODO Implement Lukas
        public int generateCode(int loc, boolean procedure) {
            return -1;
        }
    }

    public static class ExpressionList extends AbstractNode {

        public final Expression expression;

        public final ExpressionList expressionlist;

        public ExpressionInfo info;

        public ExpressionList(Expression expression, ExpressionList expressionlist, int idendation) {
            super(idendation);
            this.expression = expression;
            this.expressionlist = expressionlist;
        }

        @Override
        public String toString() {
            return getHead("<ExpressionList>")
                + expression
                + (expressionlist != null ? expression : getBody("<NoNextExpressionList/>"))
                + getHead("</ExpressionList>");
        }

        public void checkCode(List<ExpressionInfo> expressioninfos) throws CheckerException {
            expressioninfos.add(expression.checkCode());
            if (expressionlist != null) {
                expressionlist.checkCode(expressioninfos);
            }
        }

        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            System.out.println("EXPRLIST generate");
            int loc1;
            if (info != null && info.getMechMode() == Tokens.MechModeToken.MechMode.COPY) {
                if (expression instanceof StoreExpr) {
                    loc1 = ((StoreExpr) expression).codeRef(loc, true, true, routine);
                } else {
                    loc1 = expression.generateCode(loc, routine);
                }
            } else {
                if(info==null){
                    if(expression instanceof StoreExpr){
                        loc1 = ((StoreExpr) expression).codeRef(loc, true, true, routine);
                    }else{
                        loc1 = expression.generateCode(loc, routine);
                    }

                }else{
                    loc1 = ((StoreExpr) expression).codeRef(loc, true, false, routine); // TODO
                }

            }

            return (expressionlist != null ? expressionlist.generateCode(loc1, routine) : loc1);
        }
    }

    public static class GlobalInit extends AbstractNode {

        public final Tokens.IdentifierToken identifier;

        public final GlobalInit nextglobalinit;

        public GlobalInit(Tokens.IdentifierToken identifier, GlobalInit nextglobalinit, int idendation) {
            super(idendation);
            this.identifier = identifier;
            this.nextglobalinit = nextglobalinit;
        }

        @Override
        public String toString() {
            return getHead("<GlobalInit>")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (nextglobalinit != null ? nextglobalinit : getBody("<NoNextGlobalInit/>"))
                + getHead("</GlobalInit>");
        }

        public void checkCode() {
            if (nextglobalinit != null) {
                nextglobalinit.checkCode();
            }
        }

        public int generateCode(int loc, boolean procedure) {
            return -1;
        } //TODO what is this
    }

    public static class GlobalImport extends AbstractNode {

        public final Tokens.FlowModeToken flowmode;

        public final Tokens.ChangeModeToken changemode;

        public final Tokens.IdentifierToken identifier;

        public final GlobalImport nextglobalimport;

        public GlobalImport(Tokens.FlowModeToken flowmode, Tokens.ChangeModeToken changemode, Tokens.IdentifierToken identifier, GlobalImport nextglobalimport, int idendation) {
            super(idendation);
            this.flowmode = flowmode;
            this.changemode = changemode;
            this.identifier = identifier;
            this.nextglobalimport = nextglobalimport;
        }

        @Override
        public String toString() {
            return getHead("<GlobalImport>")
                + getBody("<Mode Name='FLOWMODE' Attribute='" + flowmode.getFlowMode() + "'/>'")
                + getBody("<Mode Name='CHANGEMODE' Attribute='" + changemode.getChangeMode() + "'/>'")
                + getBody("<Ident Name='" + identifier.getName() + "'/>")
                + (nextglobalimport != null ? nextglobalimport : getBody("<NoNextGlobalImport/>"))
                + getHead("</GlobalImport>");
        }

        public void checkCode(Routine routine) {
            routine.addGlobalImport(this);
        } //TODO implement code checks
    }
}

