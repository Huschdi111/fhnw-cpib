package ch.fhnw.cpib.platform.checker;

import java.util.HashMap;

import ch.fhnw.cpib.platform.javavm.*;

public class Checker {
    //Groesse der einzelnen speicher plätze der vm
    private static final int STORE_SIZE = 1024;
    //List der Routinen (Procedures oder Functions)
    private static RoutineTable routineTable = new RoutineTable();
    //Liste der Global deklarierten Variablen verwaltet den Store
    private static StoreTable globalStoreTable = new StoreTable();
    //IdentifierTable Maped identifier auf eine bestimmte addresse auf dem Stack für die globalen Variablen
    private static HashMap<String,Integer> identTable = new HashMap<String,Integer>();
    //Scope der Funktion oder des Blockes (Besitzt einen eigenen StoreTable für die eigenen lokalen Variablen)
    private static Scope scope = null;
    //VM Interface
    private static IVirtualMachine vm /*= new VirtualMachine(null, STORE_SIZE)*/;
    //Ergebnis Array der Code-Generierung enthält alle Instructions zum ausführen
    private static CodeArray codeArray = new CodeArray(STORE_SIZE);

    private static SwitchTable globalSwitchTable = new SwitchTable();
    //
    private static int stackAddressHelper = 0; //TODO what is this?
    //private static DeclarationProcedure declaration; //TODO what is this?

    //mapped einen identifier auf eine Addresse und eine Referenz {String add, String mechmode}
    private static HashMap<String,String[]> procidentTable = new HashMap<String,String[]>();


    /*GETTER AND SETTER*/
    public static IVirtualMachine getVM() { return vm; }

    public static HashMap<String, String[]> getprocIdentTable() { return procidentTable; }

    public static void addprocIdentTable(String ident, String[] posMech) { procidentTable.put(ident, posMech); }

    public static void addIdentTable(String name, int i) { identTable.put(name, new Integer(i)); }

    public static HashMap<String, Integer> getIdentTable() { return identTable; }

    public static StoreTable getGlobalStoreTable() { return globalStoreTable; }

    public static RoutineTable getRoutineTable() { return routineTable; }

    public static SwitchTable getGlobalSwitchTable() { return globalSwitchTable; }

    public static Scope getScope() { return scope; }

    public static int getstackAddressHelper() { return stackAddressHelper; }

    public static void setstackAddressHelper(int offset) { stackAddressHelper = stackAddressHelper + offset; }

    public static ICodeArray getcodeArray() { return codeArray; }

    public static void setScope(final Scope scope) { Checker.scope = scope; }

    private Checker() {
        throw new AssertionError("Instantiating utility class...");
    }

}
