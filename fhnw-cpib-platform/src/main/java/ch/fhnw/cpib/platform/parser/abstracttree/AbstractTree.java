package ch.fhnw.cpib.platform.parser.abstracttree;

import ch.fhnw.cpib.platform.checker.*;
import ch.fhnw.cpib.platform.javavm.*;
import ch.fhnw.cpib.platform.generator.GeneratorException;
import ch.fhnw.cpib.platform.scanner.tokens.Terminal;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.*;

public class AbstractTree {

    public static class Program extends AbstractNode {

        public final Tokens.IdentifierToken identifier;

        public final ProgParam progparam;

        public final Declaration declaration;

        public final Cmd cmd;

        public Program(Tokens.IdentifierToken identifier, ProgParam progparam, Declaration declaration, Cmd cmd) {
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

        public int generateCode(int loc) throws GeneratorException {
            int loc1 = loc;
            int loc2 = loc;

            if (declaration != null) {
                loc1 = declaration.nextDecl.code(loc1);
                loc2 = declaration.code(loc1 + 1);
                CompilerContext.getcodeArray().put(loc1, new UncondJump(loc2));
                loc2 = cmd.code(loc2, false);
            }
            for (Routine routine : Compiler.getRoutineTable().getTable().values()) {
                routine.codeCalls();
            }
            Compiler.getcodeArray().put(loc2, new Stop());
            return -1;
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
        public Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException {
            //check if function exist in global routine table
            Routine function = new Routine(identifier.getName(), RoutineType.FUNCTION);
            //store function in global routine table if not
            if (!checker.getGlobalRoutineTable().insert(function)) {
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
        public void generateCode(TypeSpec.Builder typescpecbuilder) {
            MethodSpec.Builder methodspecbuilder = MethodSpec.methodBuilder(identifier.getName());
            methodspecbuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC);

            if (param != null) {
                param.generateCode(methodspecbuilder);
            }

            storedeclaration.generateCode(methodspecbuilder);
            StoDecl stodecl = (StoDecl) storedeclaration;
            TypedIdentType typedidenttype = (TypedIdentType) stodecl.typedident;
            switch (typedidenttype.getParameterType()) {
                case BOOL:
                    methodspecbuilder.returns(boolean.class);
                    break;
                case INT:
                    methodspecbuilder.returns(int.class);
                    break;
                case INT64:
                default:
                    methodspecbuilder.returns(long.class);
                    break;
            }

            if (declaration != null) {
                declaration.generateCode(methodspecbuilder);
            }

            cmd.generateCode(methodspecbuilder);

            methodspecbuilder.addStatement("return " + typedidenttype.getParameterName());

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

    public static class ProcDecl extends Declaration {

        public final Tokens.IdentifierToken identifier;

        public final Param param;

        public final GlobalImport globalimport;

        public final Declaration declaration;

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
        public Tokens.TypeToken.Type checkCode(Checker checker) throws CheckerException {
            //store function in global procedure table
            Routine procedure = new Routine(identifier.getName(), RoutineType.PROCEDURE);
            checker.getGlobalRoutineTable().insert(procedure);
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addStatement("");
            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            expression1.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(" = ");
            expression2.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(";" + System.lineSeparator());

            if (expressionlist1 != null && expressionlist2 != null) {
                expressionlist1.generateCode(methodscpecbuilder);
                methodscpecbuilder.addCode(" = ");
                expressionlist2.generateCode(methodscpecbuilder);
                methodscpecbuilder.addCode(";" + System.lineSeparator());
            }

            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode("if(");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(") {" + System.lineSeparator());

            cmd.generateCode(methodscpecbuilder);

            methodscpecbuilder.addCode("}" + System.lineSeparator());

            if (repcondcmd != null) {
                repcondcmd.generateCode(methodscpecbuilder);
            }

            if (othercmd != null) {
                methodscpecbuilder.beginControlFlow("else");
                othercmd.generateCode(methodscpecbuilder);
                methodscpecbuilder.endControlFlow();

            }
            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {

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
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode("while(");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(") {" + System.lineSeparator());

            cmd.generateCode(methodscpecbuilder);

            methodscpecbuilder.addCode("}" + System.lineSeparator());
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
        public void checkCode(Checker checker) throws CheckerException {
            if (globalinit != null) {
                globalinit.checkCode();
            }
            if (getNextCmd() != null) {
                getNextCmd().checkCode(checker);
            }
        }

        @Override
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            routinecall.generateCode(methodscpecbuilder);
            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            // FIXME: Fix known errata
            methodscpecbuilder.addStatement("System.out.println(\"Input a value:\")");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(" = scanner.nextInt();" + System.lineSeparator());
            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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
        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addStatement("System.out.println(\"Output of value is:\")");
            methodscpecbuilder.addCode("System.out.println(");
            expression.generateCode(methodscpecbuilder);
            methodscpecbuilder.addCode(");" + System.lineSeparator());

            if (getNextCmd() != null) {
                getNextCmd().generateCode(methodscpecbuilder);
            }
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

        @Override
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode(literal.getValue());
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
                HashMap map = checker.getGlobalRoutineTable().getTable();
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            methodscpecbuilder.addCode(identifier.getName());
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

        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            routinecall.generateCode(methodspecbuilder);
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            if (operation.getOperation() == Tokens.OperationToken.Operation.MINUS) {
                methodscpecbuilder.addCode(" -");
                expression.generateCode(methodscpecbuilder);
            } else if (operation.getTerminal() == Terminal.NOT) {
                // FIXME: Fix known errata
                throw new RuntimeException("The generator just hit a known errata in the monadic expression");
            }
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

        public void generateCode(MethodSpec.Builder methodspecbuilder) {
            methodspecbuilder.addCode("(");
            expression1.generateCode(methodspecbuilder);
            switch (operation.getOperation()) {
                case PLUS:
                    methodspecbuilder.addCode(" + ");
                    break;
                case MINUS:
                    methodspecbuilder.addCode(" - ");
                    break;
                case AND:
                    methodspecbuilder.addCode(" && ");
                    break;
                case OR:
                    methodspecbuilder.addCode(" || ");
                    break;
                case CAND:
                    methodspecbuilder.addCode(" & ");
                    break;
                case COR:
                    methodspecbuilder.addCode(" | ");
                    break;
                case TIMES:
                    methodspecbuilder.addCode(" * ");
                    break;
                case DIVE:
                    methodspecbuilder.addCode(" / ");
                    break;
                case MODE:
                    methodspecbuilder.addCode(" % ");
                    break;
                case EQ:
                    methodspecbuilder.addCode(" == ");
                    break;
                case NE:
                    methodspecbuilder.addCode(" != ");
                    break;
                case LT:
                    methodspecbuilder.addCode(" < ");
                    break;
                case GT:
                    methodspecbuilder.addCode(" > ");
                    break;
                case LE:
                    methodspecbuilder.addCode(" <= ");
                    break;
                case GE:
                    methodspecbuilder.addCode(" >= ");
                    break;
                default:
                    throw new RuntimeException("Invalid operation found!");

            }
            expression2.generateCode(methodspecbuilder);
            methodspecbuilder.addCode(")");
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
            Routine calledroutine = checker.getGlobalRoutineTable().lookup(identifier.getName());
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

        public void generateCode(MethodSpec.Builder methodscpecbuilder) {
            // FIXME: Implement code generation
            throw new RuntimeException("Code generation not implemented yet!");
        }
    }
}
