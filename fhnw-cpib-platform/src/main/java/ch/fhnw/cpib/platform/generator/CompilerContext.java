package ch.fhnw.cpib.platform.generator;

import java.util.HashMap;
import ch.fhnw.cpib.platform.checker.*;
import ch.fhnw.cpib.platform.javavm.*;

public class CompilerContext {
    //Groesse der einzelnen speicher plätze der vm
    private static final int STORE_SIZE = 1024;
    //List der Routinen (Procedures oder Functions)
    private static RoutineTable routineTable = new RoutineTable();
    //Liste der Global deklarierten Variablen verwaltet den Store
    private static StoreTable globalStoreTable = new StoreTable();
    //Identifier Table (Ich weiss noch nicht für was der ist)
    private static HashMap<String,Integer> identTable = new HashMap<String,Integer>();
    //Scope der Funktion oder des Blockes (Besitzt einen eigenen StoreTable für die eigenen lokalen Variablen)
    private static Scope scope = null;
    //VM Interface
    private static IVirtualMachine vm /*= new VirtualMachine(null, STORE_SIZE)*/;
    //Ergebnis Array der Code-Generierung enthält alle Instructions zum ausführen
    private static CodeArray codeArray = new CodeArray(STORE_SIZE);
    //
    private static int stackAddressHelper = 0; //TODO what is this?
    //private static DeclarationProcedure declaration; //TODO what is this?
}
