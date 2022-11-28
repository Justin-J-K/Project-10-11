import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, SymbolEntry> classScope;
    private Map<String, SymbolEntry> subScope;
    private int numStatic, numField, numArg, numVar;

    public SymbolTable() {
        classScope = new HashMap<>();
        subScope = new HashMap<>();
    }

    public void startSubroutine() {
        subScope.clear();
        numArg = 0;
        numVar = 0;
    }

    /**
     * defines a new identifier for a given name, type, and kind
     * @param name
     * @param type
     * @param kind STATIC, FIELD, ARG, or VAR
     */
    public void define(String name, String type, Kind kind) {
        int currentIndex = varCount(kind);
        incrementVarCount(kind);

        SymbolEntry newEntry = new SymbolEntry(type, kind, currentIndex);

        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            classScope.put(name, newEntry);
        } else {
            subScope.put(name, newEntry);
        }
    }

    public int varCount(Kind kind) {
        int result = 0;

        switch (kind) {
            case STATIC:
                result = numStatic;
                break;
            case FIELD:
                result = numField;
                break;
            case ARG:
                result = numArg;
                break;
            case VAR:
                result = numVar;
        }

        return result;
    }

    /**
     * returns kind of identifier named name
     * @param name
     * @return STATIC, FIELD, ARG, VAR, or null
     */
    public Kind kindOf(String name) {
        SymbolEntry entry = find(name);

        if (entry == null)
            return null;

        return entry.getKind();
    }

    public String typeOf(String name) {
        SymbolEntry entry = find(name);

        if (entry == null)
            return null;

        return entry.getType();
    }

    public int indexOf(String name) {
        SymbolEntry entry = find(name);

        if (entry == null)
            return 0;

        return entry.getIndex();
    }

    private void incrementVarCount(Kind kind) {
        switch (kind) {
            case STATIC:
                numStatic++;
                break;
            case FIELD:
                numField++;
                break;
            case ARG:
                numArg++;
                break;
            case VAR:
                numVar++;
        }
    }

    /**
     * find the symbol entry in either the class or subroutine scope 
     * @param name
     * @return SymbolEntry
     */
    private SymbolEntry find(String name) {
        SymbolEntry entry = classScope.get(name);

        if (entry == null) {
            entry = subScope.get(name);
        }

        return entry;
    }

    private class SymbolEntry {
        private String type;
        private Kind kind;
        private int index;

        public SymbolEntry(String t, Kind k, int i) {
            type = t;
            kind = k;
            index = i;
        }

        public String getType() {
            return type;
        }

        public Kind getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }
}
