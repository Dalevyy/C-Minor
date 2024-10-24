package Utilities;

import AST.*;
import java.util.HashMap;

public class SymbolTable {

    HashMap<String, ID> decls;

    SymbolTable parent;

    public SymbolTable() {
        this.decls = new HashMap<String, ID>();
        this.parent = null;
    }

    public SymbolTable(SymbolTable parent) {
        this.decls = new HashMap<String, ID>();
        this.parent = parent;
    }

    public ID find(final String s) {
        ID myDecl = decls.get(s);
        if(myDecl != null)
            return myDecl;
        if(parent == null)
            return null;
        return parent.find(s);
    }

    public boolean put(final String s, ID myID) {
        ID myDecl = find(s);
        if(myDecl != null) return false;
        decls.put(s, myID);
        return true;
    }

    public ID remove(final String s) {
        return decls.remove(s);
    }

    public void dump() { decls.clear(); }

    public SymbolTable openScope(String n) {
        return new SymbolTable(this);
    }

    public SymbolTable closeScope() {
        return parent;
    }
}
