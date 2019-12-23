package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.generator.CompilerContext;
import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IInstructions;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

import java.util.ArrayList;
import java.util.List;

public class Routine {

    private Scope scope;

    private String identifier;

    private RoutineType routinetype;

    private Tokens.TypeToken.Type returntype;

    private List<Parameter> param = new ArrayList<>();

    private List<AbstractTree.GlobalImport> globalimports = new ArrayList<>();

    private List<Integer> calls = new ArrayList<Integer>();

    private int address;

    public Routine(String identifier, RoutineType routinetype) {
        this.identifier = identifier;
        this.routinetype = routinetype;
        this.scope = new Scope();
    }

    public Routine(String identifier, RoutineType routinetype, Tokens.TypeToken.Type returntype) {
        this(identifier, routinetype);
        this.returntype = returntype;
    }

    public Scope getScope() {
        return scope;
    }

    public String getIdentifier() {
        return identifier;
    }

    public RoutineType getRoutineType() {
        return routinetype;
    }

    public Tokens.TypeToken.Type getReturnType() {
        return returntype;
    }

    public final void addCall(final int loc) {
        calls.add(loc);
    }

    public void addGlobalImport(AbstractTree.GlobalImport globalimport) {
        globalimports.add(globalimport);
    }

    public List<Parameter> getParameters() {
        return this.param;
    }

    public void addParameter(Parameter p) {
        param.add(p);
    }

    public final void codeCalls() throws ICodeArray.CodeTooSmallError {
        for (int loc : calls) {
            CompilerContext.getcodeArray().put(loc, new IInstructions.Call(address));;
        }
    }

}
