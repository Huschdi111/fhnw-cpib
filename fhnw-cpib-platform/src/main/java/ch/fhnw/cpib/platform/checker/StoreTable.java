package ch.fhnw.cpib.platform.checker;

import ch.fhnw.cpib.platform.scanner.tokens.Tokens;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StoreTable {

    private Map<String, Store> storeMap;

    public StoreTable() {
        storeMap = new TreeMap<>();
    }

    public boolean addStore(final Store store) {
        if (!storeMap.containsKey(store.getIdentifier())) {
            storeMap.put(store.getIdentifier(), store);
            return true;
        } else {
            return false;
        }
    }

    public Tokens.TypeToken.Type getType(final String identifier) {
        if (storeMap.containsKey(identifier)) {
            return storeMap.get(identifier).getType();
        } else {
            return null;
        }
    }

    public Store getStore(final String identifier) {
        return storeMap.get(identifier);
    }

    public Map<String, Store> getTable() {
        return storeMap;
    }

    public StoreTable clone() {
        StoreTable newTable = new StoreTable();
        Set<String> keys = storeMap.keySet();
        for (String key : keys) {
            Store store = (Store)storeMap.get(key);
            newTable.addStore(store.clone());
        }
        return newTable;
    }

}
