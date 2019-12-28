package ch.fhnw.cpib.platform.parser.abstracttree;

import ch.fhnw.cpib.platform.checker.*;
import ch.fhnw.cpib.platform.checker.Checker;
import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IInstructions;
import ch.fhnw.cpib.platform.scanner.tokens.Terminal;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;

import javax.lang.model.element.Modifier;
import java.util.*;

public class AbstractTree {

    public static class Program extends AbstractNode {

        public final Tokens.IdentifierToken identifier;

        public final ProgParam progparam;

        public final Param.Declaration declaration;

        public final Cmd cmd;

        public Program(Tokens.IdentifierToken identifier, ProgParam progparam, Param.Declaration declaration, Cmd cmd) {
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

        public void checkCode(Checker checker) throws CheckerException {
            if (progparam != null) {
                progparam.checkCode(checker);
            }
            if (declaration != null) {
                declaration.checkCode(checker);
            }
            if (cmd != null) {
                cmd.checkCode(checker);
            }
        }

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            int loc2 = loc;

            if (declaration != null)
                if(declaration instanceof Param.ProcDecl || declaration instanceof Param.FunDecl){
                    loc1 = declaration.nextdeclaration.generateCode(loc1);
                    loc2 = declaration.generateCode(loc1+1);
                    Checker.getcodeArray().put(loc1, new IInstructions.UncondJump(loc2));
                }else{
                    loc2 = declaration.generateCode(loc1);
                }
            loc2 = cmd.generateCode(loc2, false);
            for (Routine routine : Checker.getRoutineTable().getTable().values()) {
                routine.code();
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

        public void checkCode(Checker checker) throws CheckerException {
            //check if identifier exist in global store table
            if (checker.getGlobalStoreTable().getStore(typedident.getIdentifier().getName()) != null) {
                throw new CheckerException("Identifier " + typedident.getIdentifier().getName() + " is already declared");
            }
            //store identifier in global store table
            checker.getGlobalStoreTable().addStore(new Store(typedident.getIdentifier().getName(), typedident.getType(), changemode.getChangeMode() == Tokens.ChangeModeToken.ChangeMode.CONST));
            if (nextprogparam != null) {
                nextprogparam.checkCode(checker);
            }
        }

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            typedident.generateCode(methodscpecbuilder);
            TypedIdentType typedidenttype = (TypedIdentType) typedident;
            switch (typedidenttype.getParameterType()) {
                case BOOL:
                    methodscpecbuilder.addCode(" = false;" + System.lineSeparator());
                    break;
                case INT:
                    methodscpecbuilder.addCode(" = 0;" + System.lineSeparator());
                    break;
                case INT64:
                default:
                    methodscpecbuilder.addCode(" = 0L;" + System.lineSeparator());
                    break;
            }

            if (nextprogparam != null) {
                nextprogparam.generateCode(methodscpecbuilder);
            }
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

        public void checkCode(Checker checker, Routine routine) throws CheckerException {
            Store store = checker.getGlobalStoreTable().getStore(typedident.getIdentifier().getName());
            switch (flowmode.getFlowMode()) {
                case IN:
                    //passing parameter must be constant
                    if (store != null && mechmode.getMechMode() == Tokens.MechModeToken.MechMode.REF && !store.isConst()) {
                        throw new CheckerException("IN reference parameter can not be var! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName(), typedident.getType(), flowmode.getFlowMode(), mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                case INOUT:
                    if (routine.getRoutineType() != RoutineType.PROCEDURE) {
                        throw new CheckerException("INOUT parameter in function declaration! Ident: " + store.getIdentifier());
                    }
                    if (store != null && store.isConst()) {
                        throw new CheckerException("INOUT parameter can not be constant! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName(), typedident.getType(), flowmode.getFlowMode(), mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                case OUT:
                    if (routine.getRoutineType() != RoutineType.PROCEDURE) {
                        throw new CheckerException("OUT parameter in function declaration! Ident: " + store.getIdentifier());
                    }
                    routine.addParameter(new Parameter(typedident.getIdentifier().getName(), typedident.getType(), flowmode.getFlowMode(), mechmode.getMechMode(), changemode.getChangeMode()));
                    break;
                default:
                    break;
            }
            if (nextparam != null) {
                nextparam.checkCode(checker, routine);
            }
        }

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            TypedIdentType typedidenttype = (TypedIdentType) typedident;
            switch (typedidenttype.getParameterType()) {
                case BOOL:
                    methodscpecbuilder.addParameter(boolean.class, typedidenttype.getParameterName());
                    break;
                case INT:
                    methodscpecbuilder.addParameter(int.class, typedidenttype.getParameterName());
                    break;
                case INT64:
                default:
                    methodscpecbuilder.addParameter(long.class, typedidenttype.getParameterName());
                    break;
            }

            if (nextparam != null) {
                nextparam.generateCode(methodscpecbuilder);
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

        public abstract Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException;

        public abstract void generateCode(MethodSpec.Builder methodscpecbuilder);
    }

    public static class StoDecl extends Declaration {

        public final Tokens.ChangeModeToken changemode;

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
        public Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException {
            //check if global scope applies
            StoreTable storetable;
            if (checker.getScope() == null) {
                storetable = checker.getGlobalStoreTable();
            } else {
                storetable = checker.getScope().getStoreTable();
            }

            //check if identifier exist in global store table
            String identifier = typedident.getIdentifier().getName();
            if (storetable.getStore(identifier) != null) {
                throw new CheckerException("Identifier " + typedident.getIdentifier().getName() + " is already declared");
            }
            //store identifier in global store table
            storetable.addStore(new Store(typedident.getIdentifier().getName(), typedident.getType(), false));

            Store store = storetable.getStore(typedident.getIdentifier().getName());
            store.setRelative(true);
            store.setReference(false);

            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode(checker);
            }
            return null;
        }

        @Override
        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            TypedIdentType typedidenttype = (TypedIdentType) typedident;
            switch (typedidenttype.getParameterType()) {
                case BOOL:
                    methodspecbuilder.addStatement("boolean " + typedidenttype.getParameterName() + " = false");
                    break;
                case INT:
                    methodspecbuilder.addStatement("int " + typedidenttype.getParameterName() + " = 0");
                    break;
                case INT64:
                default:
                    methodspecbuilder.addStatement("long " + typedidenttype.getParameterName() + " = 0L");
                    break;
            }

            if (getNextDeclaration() != null) {
                getNextDeclaration().generateCode(methodspecbuilder);
            }
        }

        @Override
        public void generateCode(TypeSpec.Builder typespecbuilder) {
            TypedIdentType typedidenttype = (TypedIdentType) typedident;
            switch (typedidenttype.getParameterType()) {
                case BOOL:
                    FieldSpec.Builder fieldspecbuilder1 = FieldSpec.builder(boolean.class, typedidenttype.getParameterName());
                    fieldspecbuilder1.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
                    fieldspecbuilder1.initializer("false");
                    typespecbuilder.addField(fieldspecbuilder1.build());
                    break;
                case INT:
                    FieldSpec.Builder fieldspecbuilder2 = FieldSpec.builder(int.class, typedidenttype.getParameterName());
                    fieldspecbuilder2.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
                    fieldspecbuilder2.initializer("0");
                    typespecbuilder.addField(fieldspecbuilder2.build());
                    break;
                case INT64:
                default:
                    FieldSpec.Builder fieldspecbuilder3 = FieldSpec.builder(long.class, typedidenttype.getParameterName());
                    fieldspecbuilder3.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
                    fieldspecbuilder3.initializer("0L");
                    typespecbuilder.addField(fieldspecbuilder3.build());
                    break;
            }
            if (getNextDeclaration() != null) {
                getNextDeclaration().generateCode(typespecbuilder);
            }
        }
    }

    public static class FunDecl extends Declaration {

        public final Tokens.IdentifierToken identifier;

        public final Param param;

        public final Declaration storedeclaration;

        public final StoreExpr.GlobalImport globalimport;

        public final Declaration declaration;

        public final Cmd cmd;

        public FunDecl(Tokens.IdentifierToken identifier, Param param, Declaration storedeclaration, StoreExpr.GlobalImport globalimport, Declaration declaration, Cmd cmd, Declaration nextdeclaration, int idendation) {
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
        public Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException {
            //check if function exist in global routine table
            Routine function = new Routine(identifier.getName(), RoutineType.FUNCTION);
            //store function in global routine table if not
            if (!checker.getRoutineTable().insert(function)) {
                throw new CheckerException("Function " + identifier.getName() + " is already declared.");
            }
            checker.setScope(function.getScope());
            if (param != null) {
                param.checkCode(checker, function);
            }
            if (storedeclaration != null) {
                storedeclaration.checkCode(checker);
            }
            if (globalimport != null) {
                globalimport.checkCode(function);
            }
            if (cmd != null) {
                cmd.checkCode(checker);
            }
            checker.setScope(null);
            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode(checker);
            }
            return null;
        }

        @Override
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {

        }

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
                int loc1 = loc;
                Routine routine = Checker.getRoutineTable().lookup(identation.getValue());
                Checker.setScope(routine.getScope());
                routine.setAddress(loc1);
                int i = 0 - routine.getParameters().size();
                for (Parameter p : routine.getParameters()){
                    if (p.getMechMode().getValue().toString().toUpperCase().equals("COPY")){
                        //Compiler.getcodeArray().put(loc1, new AllocBlock(1));
                        Checker.getprocIdentTable().put(p.getType().getIdent().getValue(), new String[] {i+"",p.getMechMode().getValue().toString()});
                        //Compiler.getcodeArray().put(loc1, new Deref());
                        //Compiler.getcodeArray().put(loc1, new IInstructions.Store());
                    }else{
                        //Compiler.getcodeArray().put(loc1, new AllocBlock(1));
                        Checker.getprocIdentTable().put(p.getType().getIdent().getValue(), new String[] {i+"","REF"});
                        //Compiler.getcodeArray().put(loc1, new LoadAddrRel(Compiler.getIdentTable().get(p.getType().getIdent()).intValue()));
                        //Compiler.getcodeArray().put(loc1, new IInstructions.Store());
                    }
                    i += 1;
                }
                Checker.getprocIdentTable().put(returnDecl.typedIdent.getIdent().getValue(), new String[] {(0- routine.getParamList().size() - 1) +"","REF"});
                //returnDecl.code(loc1);
                //LoadAddrRel of Variables.
                // Compiler.getVM().Enter(loc1++, routine.getInOutCopyCount() +
                // getCount(), 0);
                //Compiler.getcodeArray().put(loc1, new AllocBlock(1));
                //loc1 = param.codeIn(loc1, routine.getParamList().size(), 0);
                loc1 = cmd.generateCode(loc1, true);
                //loc1 = param.codeOut(loc1, routine.getParamList().size(), 0);
                // Compiler.getVM().Return(loc1++, 0);
                Checker.getcodeArray().put(loc1, new IInstructions.Return(1));
                //Compiler.setScope(null);
                return ++loc1;
                // return (nextDecl!=null?nextDecl.code(loc1):loc1);
                // return (nextDecl!=null?nextDecl.code(loc1):loc1);
                // Zuerst AllocBlock für return value
                // Parameter auf Stack legen in korrekter Form (LValue und RValue
                // check)
                // call ablegen mit addresse der func im codeArray
            }
    }

    public static class ProcDecl extends Param.Declaration {

        public final Tokens.IdentifierToken identifier;

        public final Param param;

        public final StoreExpr.GlobalImport globalimport;

        public final Param.Declaration declaration;

        public final Cmd cmd;

        public ProcDecl(Tokens.IdentifierToken identifier, Param param, StoreExpr.GlobalImport globalimport, Param.Declaration declaration, Param.Declaration nextdeclaration, Cmd cmd, int idendation) {
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
        public Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException {
            //store function in global procedure table
            Routine procedure = new Routine(identifier.getName(), RoutineType.PROCEDURE);
            checker.getRoutineTable().insert(procedure);
            checker.setScope(procedure.getScope());
            if (param != null) {
                param.checkCode(checker, procedure);
            }
            if (globalimport != null) {
                globalimport.checkCode(procedure);
            }
            if (cmd != null) {
                cmd.checkCode(checker);
            }
            if (declaration != null) {
                declaration.checkCode(checker);
            }
            checker.setScope(null);
            if (getNextDeclaration() != null) {
                getNextDeclaration().checkCode(checker);
            }
            return null;
        }

        @Override
        public void generateCode(TypeSpec.Builder typescpecbuilder) {
            MethodSpec.Builder methodspecbuilder = MethodSpec.methodBuilder(identifier.getName());
            methodspecbuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC);

            if (param != null) {
                param.generateCode(methodspecbuilder);
            }

            if (declaration != null) {
                declaration.generateCode(methodspecbuilder);
            }

            cmd.generateCode(methodspecbuilder);

            if (getNextDeclaration() != null) {
                getNextDeclaration().generateCode(typescpecbuilder);
            }

            typescpecbuilder.addMethod(methodspecbuilder.build());
        }

        @Override
        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            // Just make the compiler happy
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

        public abstract void checkCode(Checker checker) throws CheckerException;

        public abstract int code(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError;
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
        public void checkCode(Checker checker) throws CheckerException {
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }
        @Override
        public int code(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return (nextcmd != null ? nextcmd.checkCode(loc, routine) : loc);
        }
    }

    public static class AssiCmd extends Cmd {

        public final Expression expression1;

        public final StoreExpr.ExpressionList expressionlist1;

        public final Expression expression2;

        public final StoreExpr.ExpressionList expressionlist2;

        public AssiCmd(Expression expression1, StoreExpr.ExpressionList expressionlist1, Expression expression2, StoreExpr.ExpressionList expressionlist2, Cmd nextcmd, int idendation) {
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
        public void checkCode(Checker checker) throws CheckerException {
            List<ExpressionInfo> targetExprInfos = new ArrayList<>();
            List<ExpressionInfo> sourceExprInfos = new ArrayList<>();

            ExpressionInfo sourceExprInfo = expression2.checkCode(checker);
            ExpressionInfo targetExprInfo = expression1.checkCode(checker);

            if (expressionlist1 != null) {
                expressionlist1.checkCode(checker, targetExprInfos);
            }
            if (expressionlist2 != null) {
                expressionlist2.checkCode(checker, sourceExprInfos);
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
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int code(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {
            int loc1;

            if (expression2 instanceof StoreExpr.DyadicExpr && ((StoreExpr.DyadicExpr) expression2).getOperator().getValue() == OperatorAttribute.DOT) {
                expression2 = (StoreExpr) ((StoreExpr.DyadicExpr) expression2).getExpr1();
            }

            if (expression1 instanceof StoreExpr.DyadicExpr
                && ((StoreExpr.DyadicExpr) expression1).getOperator().getValue() == OperatorAttribute.DOT) {
                expression1 = (StoreExpr) ((StoreExpr.DyadicExpr) expression1).getExpr1();
            }

            if (expression1 instanceof ExprAr) {
                Checker.getcodeArray().put(loc,
                    new IInstructions.LoadAddrRel(getArrayAdress(((ExprArray) expression1).ident.getValue())));
                loc1 = loc + 1;
                if(((ExprArray) expression1).expression instanceof ExprStore){
                    loc1 = ((ExprStore)((ExprArray) expression1).expression).codeRef(loc1, true, true, routine);
                }else{
                    loc1 = ((ExprArray) expression1).expression.code(loc1, routine);
                }


                // Compiler.getcodeArray().put(loc+1,new LoadImInt(new
                // Integer(((ExprArray)targetExpression).expression.getValue()).intValue()));
                CompilerException.getcodeArray().put(loc1,
                    new IInstructions.LoadImInt(getArrayOffset(((ExprArray) expression1).ident.getValue())));
                Checker.getcodeArray().put(++loc1, new IInstructions.SubInt());
                CompilerException.getcodeArray().put(++loc1, new IInstructions.AddInt());
                loc1++;
            } else {
                loc1 = expression1.generateCode(loc, routine);
            }

            if (!(expression1 instanceof ExprStore)) {
                loc1 = expression1.generateCode(loc1, routine);
                Checker.getcodeArray().put(loc1++, new IInstructions.Store());
            } else {
                loc1 = ((ExprStore) expression1).codeRef(loc1, true, true, routine);
                // Compiler.getVM().Store(loc1++);
                Checker.getcodeArray().put(loc1++, new IInstructions.Store());
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
        public void checkCode(Checker checker) throws CheckerException {
            ExpressionInfo exprinfo = expression.checkCode(checker);
            Switch s = new Switch(exprinfo.getName(), exprinfo.getType());
            //store switch with name and type
            checker.getGlobalSwitchTable().insert(s);
            repcasecmd.checkCode(checker);
            if (cmd != null) {
                cmd.checkCode(checker);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        @Override//TODO implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode("switch(");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(") {" + System.lineSeparator());

            repcasecmd.generateCode(methodscpecbuilder);

            if (cmd != null) {
                methodscpecbuilder.beginControlFlow("default: ");
                cmd.generateCode(methodscpecbuilder);
                methodscpecbuilder.addStatement("break");
                methodscpecbuilder.endControlFlow();
            }

            methodscpecbuilder.addCode("}" + System.lineSeparator());

            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void checkCode(Checker checker) throws CheckerException {
            HashMap<String, Switch> map = checker.getGlobalSwitchTable().getTable();
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
                checker.getGlobalSwitchTable().insert(s);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        @Override//TODO implement
        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            methodspecbuilder.beginControlFlow("case " + literal.getValue() + " :");
            cmd.generateCode(methodspecbuilder);
            methodspecbuilder.addStatement("break");
            methodspecbuilder.endControlFlow();
            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodspecbuilder);
            }
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
        public void checkCode(Checker checker) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode(checker);
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("IF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (repcondcmd != null) {
                repcondcmd.checkCode(checker);
            }
            if (othercmd != null) {
                othercmd.checkCode(checker);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = expression.generateCode(loc, routine);
            int loc2 = ifCmd.code(loc1 + 1, routine);
            // Compiler.getVM().CondJump(loc1, loc2 + 1);
            Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc2 + 1));
            int loc3 = elseCmd.code(loc2 + 1, routine);
            // Compiler.getVM().UncondJump(loc2, loc3);
            Checker.getcodeArray().put(loc2, new IInstructions.UncondJump(loc3));
            return (nextcmd != null ? nextcmd.code(loc3, routine) : loc3);
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
        public void checkCode(Checker checker) throws CheckerException {
            repArrowCmd.checkCode(checker);
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
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
        public void checkCode(Checker checker) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode(checker);
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("IF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (cmd != null) {
                cmd.checkCode(checker);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        @Override//TODO implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {

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
        public void checkCode(Checker checker) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode(checker);
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL) {
                throw new CheckerException("ELSEIF condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (repcondcmd != null) {
                repcondcmd.checkCode(checker);
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        @Override//TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode("else if(");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(") {" + System.lineSeparator());

            cmd.generateCode(methodscpecbuilder);

            methodscpecbuilder.addCode("}" + System.lineSeparator());

            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void checkCode(Checker checker) throws CheckerException {
            //check expr return type is from type BOOL
            ExpressionInfo exprinfo = expression.checkCode(checker);
            if (exprinfo.getType() != Tokens.TypeToken.Type.BOOL && exprinfo.getType() != null) {
                throw new CheckerException("WHILE condition needs to be BOOL. Current type: " + exprinfo.getType());
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            return 0;
        }

        @Override
        public int code(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = expression.generateCode(loc, routine);
            int loc2 = cmd.generateCode(loc1 + 1, routine);
            // Compiler.getVM().CondJump(loc1, loc2 + 1);
            // Compiler.getVM().UncondJump(loc2, loc);
            Checker.getcodeArray().put(loc1, new IInstructions.CondJump(loc2 + 1));
            Checker.getcodeArray().put(loc2, new IInstructions.UncondJump(loc));
            return (nextcmd != null ? nextcmd.generateCode(loc2 + 1, routine) : (loc2 + 1);
        }
    }

    public static class ProcCallCmd extends Cmd {

        public final StoreExpr.RoutineCall routinecall;

        public final StoreExpr.GlobalInit globalinit;

        public ProcCallCmd(StoreExpr.RoutineCall routinecall, StoreExpr.GlobalInit globalinit, Cmd nextcmd, int idendation) {
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
        public void checkCode(Checker checker) throws CheckerException {
            if (globalinit != null) {
                globalinit.checkCode();
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1 = loc;
            loc1 = routinecall.getExprList().code(loc1, routine);
            Checker.getRoutineTable().getRoutine(routinecall.getIdendation().getValue()).addCall(loc1++);
            return (nextcmd != null ? nextcmd.generateCode(loc1, routine) : loc1);
        }
    }

    public static class InputCmd extends Cmd {

        public final Expression expression;

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
        public void checkCode(Checker checker) throws CheckerException {
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1;
            if (expression instanceof StoreExpr.DyadicExpr && ((StoreExpr.DyadicExpr) expression).getOperator().getValue() == OperatorAttribute.DOT) {
                ExprStore expr1 = (ExprStore) ((StoreExpr.DyadicExpr) expression).getExpr1();
                expression = expr1;
                loc1 = ((ExprStore) expression).codeRef(loc, true, false, routine); // TODO
            } else if (expression instanceof ExprArray) {
                loc1 = ((ExprArray) expression).codeRef(loc, routine);
            } else {
                loc1 = ((ExprStore) expression).codeRef(loc, true, false, routine); // TODO
            }

            if (type.getValue() == TypeAttribute.BOOL) {
                // Compiler.getVM().BoolOutput(loc1++, ((ExprStore)
                // expr).getIdent().getValue());
                if (expression instanceof ExprStore) {
                    CompilerException.getcodeArray().put(loc1++, new IInstructions.InputBool(((ExprStore) expression).getIdent().getValue()));
                } else if (expression instanceof ExprArray) {
                    CompilerException.getcodeArray().put(loc1++, new IInstructions.InputBool(((ExprArray) expression).getIdent().getValue()));
                } else {
                    throw new IllegalArgumentException("Wrong Expression while code generation");
                }

            } else {
                // Compiler.getVM().IntOutput(loc1++, ((ExprStore)
                // expr).getIdent().getValue());
                if (expression instanceof ExprStore) {
                    CompilerException.getcodeArray().put(loc1++, new IInstructions.InputInt(((ExprStore) expression).getIdent().getValue()));
                } else if (expression instanceof ExprArray) {
                    CompilerException.getcodeArray().put(loc1++, new IInstructions.InputInt(((ExprArray) expression).getIdent().getValue()));
                } else {
                    throw new IllegalArgumentException("Wrong Expression while code generation");
                }
                // Compiler.getcodeArray().put(loc1++, new
                // OutputInt(((ExprStore) expr).getIdent().getValue()));
            }
            return (nextcmd != null ? nextcmd.generateCode(loc1, routine) : loc1);
        }
    }

    public static class OutputCmd extends Cmd {

        public final Expression expression;

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
        public void checkCode(Checker checker) throws CheckerException {
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1;
            if (expression instanceof StoreExpr.DyadicExpr && ((StoreExpr.DyadicExpr) expression).getOperation.getValue() == OperatorAttribute.DOT) {
                ExprStore expr1 = (ExprStore) ((StoreExpr.DyadicExpr) expression).getExpr1();
                ExprStore expr2 = (ExprStore) ((StoreExpr.DyadicExpr) expression).getExpr2();
                Store store = (Store) Checker.getGlobalStoreTable()
                    .getStore(expr2.getIdent().getValue() + "." + expr1.getIdent().getValue());
                expr1.setIdent(store.getType().getIdent());
                expression = expr1;
                loc1 = expression.generateCode(loc, routine);
            } else if (expression instanceof ExprArray) {
                loc1 = ((ExprArray) expression).code(loc, routine);
            } else {
                loc1 = ((ExprStore) expression).code(loc, routine);
                Checker.getcodeArray().put(loc1++, new IInstructions.Deref());
            }

            if (type.getValue() == TypeAttribute.BOOL) {
                // Compiler.getVM().BoolOutput(loc1++, ((ExprStore)
                // expr).getIdent().getValue());
                if (expression instanceof ExprStore) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputBool(((ExprStore) expression).getIdent().getValue()));
                } else if (expression instanceof ExprArray) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputBool(((ExprArray) expression).getIdent().getValue()));
                } else {
                    throw new IllegalArgumentException("Wrong Expression while code generation");
                }

            } else {
                // Compiler.getVM().IntOutput(loc1++, ((ExprStore)
                // expr).getIdent().getValue());
                if (expression instanceof ExprStore) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputInt(((ExprStore) expression).getIdent().getValue()));
                } else if (expression instanceof ExprArray) {
                    Checker.getcodeArray().put(loc1++, new IInstructions.OutputInt(((ExprArray) expression).getIdent().getValue()));
                } else {
                    throw new IllegalArgumentException("Wrong Expression while code generation");
                }
                // Compiler.getcodeArray().put(loc1++, new
                // OutputInt(((ExprStore) expr).getIdent().getValue()));
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

    public static class TypedIdentType extends TypedIdent<Tokens.IdentifierToken> {

        public final Tokens.IdentifierToken identifier;

        public final Tokens.TypeToken.Type type;

        public TypedIdentType(Tokens.IdentifierToken identifier, Tokens.TypeToken.Type type, int idendation) {
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

        @Override//TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            switch (type) {
                case BOOL:
                    methodscpecbuilder.addCode("boolean");
                    break;
                case INT:
                    methodscpecbuilder.addCode("int");
                    break;
                case INT64:
                default:
                    methodscpecbuilder.addCode("long");
                    break;
            }
            methodscpecbuilder.addCode(" " + identifier.getName());
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

        public abstract ExpressionInfo checkCode(Checker checker) throws CheckerException;

        abstract int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError;
    }

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

        public ExpressionInfo checkCode() throws CheckerException {
            //check Lvalue
            throw new CheckerException("Found literal " + literal.getValue() + "in the left part of an assignement");
        }

        @Override
        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
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
            // Compiler.getVM().IntLoad(loc, literal.getLiteral());
            Checker.getcodeArray().put(loc, new IInstructions.LoadImInt(literal.getLiteral()));
            return loc + 1;
        }
    }

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
        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
            //check if global scope applies
            StoreTable storetable;
            if (checker.getScope() == null) {
                storetable = checker.getGlobalStoreTable();
            } else {
                storetable = checker.getScope().getStoreTable();
            }

            Store store = storetable.getStore(identifier.getName());
            if (store == null) {
                //check if store is declared on global scope
                store = checker.getGlobalStoreTable().getStore(identifier.getName());
                //check if identifier in routine defined
                HashMap map = checker.getRoutineTable().getTable();
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
                //store not initialized variable because before a initialisation of a variable i can be used
                if (checker.getGlobalStoreTable().getStore(identifier.getName()) == null) {
                    checker.getGlobalStoreTable().addStore(new Store(identifier.getName(), null, false));
                    return new ExpressionInfo(identifier.getName(), null);
                }
                //throw exception in the end
                //if (store == null) {
                //    throw new CheckerException("Identifier " + identifier.getName() + " is not declared");
                //}
            }
            return new ExpressionInfo(store.getIdentifier(), store.getType());
        }


        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            Store store = (Store) Checker.getScope().getStoreTable().getStore(getIdendation().getValue());
            int loc1 = loc;
            if (routine) {
                if (Checker.getprocIdentTable().containsKey(ident.getValue())) {
                    //Compiler.getcodeArray().put(loc, new LoadAddrRel(Integer.parseInt(Compiler.getprocIdentTable().get(ident.getValue())[0])));
                    if(store==null){
                        Checker.getcodeArray().put(loc1++, new IInstructions.LoadAddrRel(Integer.parseInt(Checker.getprocIdentTable().get(ident.getValue())[0])));
                        return loc1;
                    }else{
                        loc1 = store.codeRef(loc, true, false, routine);
                        return loc1;
                    }

                } else {
                    Checker.addIdentTable(ident.getValue(), loc);
                    return ((store != null) ? store.codeLoad(loc, routine) : loc);
                }
            } else {
                if (Checker.getIdentTable().containsKey(ident.getValue())) {
                    Checker.getcodeArray().put(loc,
                        new IInstructions.LoadAddrRel(Checker.getIdentTable().get(ident.getValue()).intValue()));
                    return loc + 1;
                } else {
                    Checker.addIdentTable(ident.getValue(), loc);
                    return ((store != null) ? store.codeLoad(loc, routine) : loc);
                }
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

        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
            return routinecall.checkCode(checker);
        }
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            routinecall.generateCode(methodspecbuilder);
        }



        @Override
        int generateCode(int loc, boolean routine) throws ICodeArray.CodeTooSmallError { // TODO
            int loc1 = loc;
            Checker.getcodeArray().put(loc1++, new IInstructions.AllocBlock(1)); //referenz neu spechern?
            loc1 = routineCall.getExprList().code(loc1, routine);
            Checker.getRoutineTable().getRoutine(routineCall.getIdent().getValue()).addCall(loc1++);
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
        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
            return expression.checkCode(checker);
        }
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            if (operation.getOperation() == Tokens.OperationToken.Operation.MINUS) {
                methodscpecbuilder.addCode(" -");
                expression.generateCode(methodscpecbuilder);
            } else if (operation.getTerminal() == Terminal.NOT) {
                // FIXME: Fix known errata
                throw new RuntimeException("The generator just hit a known errata in the monadic expression");
            }
        }

        @Override
        int generateCode(int loc, boolean routine) {
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
        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
            ExpressionInfo exprinfo1 = expression1.checkCode(checker);
            ExpressionInfo exprinfo2 = expression2.checkCode(checker);
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

        public int generateCode(int loc) throws ICodeArray.CodeTooSmallError {

            int loc1 = expression1.generateCode(loc);
            int loc2;
            switch (operation.getOperation()) {
                case PLUS:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.AddInt());
                    return loc2++;
                case MINUS:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.SubInt());
                    return loc2++;
                case AND://TODO FIX
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.AddInt());
                    return loc2++;
                case OR: //TODO FIX
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.AddInt());
                    return loc2++;
                case CAND: //TODO FIX
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.AddInt());
                    return loc2++;
                case COR: //TODO FIX
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.AddInt());
                    return loc2++;
                case TIMES:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.MultInt());
                    return loc2++;
                case DIVE:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.DivTruncInt());
                    return loc2++;
                case MODE://TODO ????
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.Mode());
                    return loc2++;
                case EQ:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.EqInt());
                    return loc2++;
                case NE:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.NeInt());
                    return loc2++;
                case LT:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.LtInt());
                    return loc2++;
                case GT:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.GtInt());
                    return loc2++;
                case LE:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.LeInt());
                    return loc2++;
                case GE:
                    loc2 = expression2.generateCode(loc1);
                    Checker.getcodeArray().put(loc2, new IInstructions.GeInt());
                    return loc2++;
                default:
                    throw new RuntimeException("Invalid operation found!");

            }
        }

        @Override
        public int generateCode(final int loc, boolean routine) throws ICodeArray.CodeTooSmallError {
            int loc1;

            if (expression1 instanceof ExprStore) {
                loc1 = ((ExprStore) expression1).codeRef(loc, true, true, routine);
            } else if (expression1 instanceof ExprFunCall) {
                loc1 = ((ExprFunCall) expression1).code(loc, routine);
            } else {

                loc1 = expression1.code(loc, routine);
            }

            if (operation.getOperation() != OperatorAttribute.CAND && operator.getValue() != OperatorAttribute.COR) {

                if (expression2 instanceof ExprStore) {
                    loc1 = ((ExprStore) expression2).codeRef(loc1, true, true, routine);
                } else if (expression2 instanceof ExprFunCall) {
                    loc1 = ((ExprFunCall) expression2).code(loc1, routine);
                } else {

                    loc1 = expression2.code(loc1, routine);
                }

                switch (operation.getOperation()) {
                    case DOT:
                        break;
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
            } else if (operator.getValue() == OperatorAttribute.CAND) {
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

        public ExpressionInfo checkCode(Checker checker) throws CheckerException {
            Routine calledroutine = checker.getRoutineTable().lookup(identifier.getName());
            if (calledroutine == null) {
                throw new CheckerException("Routine " + identifier.getName() + " is not declared.");
            }

            List<ExpressionInfo> exprinfos = new ArrayList<>();
            if (expressionlist != null) {
                expressionlist.checkCode(checker, exprinfos);
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
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            methodspecbuilder.addCode(identifier.getName() + "(");
            if (expressionlist != null) {
                expressionlist.generateCode(methodspecbuilder);
            }
            methodspecbuilder.addCode(");" + System.lineSeparator());
        }
    }

    public static class ExpressionList extends AbstractNode {

        public final Expression expression;

        public final ExpressionList expressionlist;

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

        public void checkCode(Checker checker, List<ExpressionInfo> expressioninfos) throws CheckerException {
            expressioninfos.add(expression.checkCode(checker));
            if (expressionlist != null) {
                expressionlist.checkCode(checker, expressioninfos);
            }
        }
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            expression.generateCode(methodscpecbuilder);
            if (expressionlist != null) {
                methodscpecbuilder.addCode(", ");
                expressionlist.generateCode(methodscpecbuilder);
            }
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
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            // FIXME: Implement code generation
            throw new RuntimeException("Code generation not implemented yet!");
        }
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
        }
        //TODO Implement
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            // FIXME: Implement code generation
            throw new RuntimeException("Code generation not implemented yet!");
        }
    }
}
    }
}
