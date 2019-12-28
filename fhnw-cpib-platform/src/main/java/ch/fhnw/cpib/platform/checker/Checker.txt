package ch.fhnw.cpib.platform.checker;

public class Checker {

    private StoreTable globalStoreTable = new StoreTable();

    private RoutineTable globalRoutineTable = new RoutineTable();

    private SwitchTable globalSwitchTable = new SwitchTable();

    private Scope scope = null;

    public StoreTable getGlobalStoreTable() {
        return globalStoreTable;
    }

    public RoutineTable getGlobalRoutineTable() {
        return globalRoutineTable;
    }

    public SwitchTable getGlobalSwitchTable() {
        return globalSwitchTable;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
