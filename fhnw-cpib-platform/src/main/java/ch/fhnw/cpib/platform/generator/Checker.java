package ch.fhnw.cpib.platform.generator;

import ch.fhnw.cpib.platform.checker.RoutineTable;
import ch.fhnw.cpib.platform.checker.Scope;
import ch.fhnw.cpib.platform.checker.StoreTable;
import ch.fhnw.cpib.platform.checker.SwitchTable;
import ch.fhnw.cpib.platform.javavm.CodeArray;
import ch.fhnw.cpib.platform.javavm.ICodeArray;
import ch.fhnw.cpib.platform.javavm.IVirtualMachine;
import ch.fhnw.cpib.platform.parser.abstracttree.AbstractTree;
import java.util.HashMap;

public final class Checker {

    private static final int STORE_SIZE = 1024;

    private static RoutineTable routineTable = new RoutineTable();
    private static StoreTable globalStoreTable = new StoreTable();
    private static StoreTable arrayRangeTable = new StoreTable();
    private static SwitchTable globalSwitchTable = new SwitchTable();
    private static HashMap<String,Integer> identTable = new HashMap<String,Integer>();
    private static Scope scope = null;
    private static IVirtualMachine vm /*= new VirtualMachine(null, STORE_SIZE)*/;
    private static CodeArray codeArray = new CodeArray(STORE_SIZE);
    private static int stackAddressHelper = 0;
    private static AbstractTree.ProcDecl declaration;

    private static HashMap<String,String[]> procidentTable = new HashMap<String,String[]>();
    //private static CodeArray tempcodeArray = new CodeArray(STORE_SIZE);

    public static IVirtualMachine getVM() {
        return vm;
    }

    public static StoreTable getArrayStoreTable() {
    	return arrayRangeTable;
    }

    public static HashMap<String, String[]> getprocIdentTable() {
        return procidentTable;
    }

    public static void addprocIdentTable(String ident, String[] posMech) {
        procidentTable.put(ident, posMech);
    }

    public static void addIdentTable(String name, int i) {
        identTable.put(name, new Integer(i));
    }

    public static HashMap<String, Integer> getIdentTable() {
        return identTable;
    }

    public static StoreTable getGlobalStoreTable() {
        return globalStoreTable;
    }

    public static RoutineTable getRoutineTable() {
        return routineTable;
    }

    public static Scope getScope() {
        return scope;
    }

    public static int getstackAddressHelper() {
        return stackAddressHelper;
    }

    public static void setstackAddressHelper(int offset) {
        stackAddressHelper = stackAddressHelper + offset;
    }


    public static ICodeArray getcodeArray() {
        return codeArray;
    }

    public static SwitchTable getGlobalSwitchTable() {
        return globalSwitchTable;
    }

    //public static ICodeArray gettempcodeArray() {
    //    return tempcodeArray;
    //}

    public static void setScope(final Scope scope) {
        Checker.scope = scope;
    }

}
