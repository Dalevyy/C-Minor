package utilities;

import ast.*;
import java.util.HashMap;
import java.util.HashSet;


// SymbolTable needs to be fixed for imports
public class SymbolTable {

    private HashMap<String, NameNode> varNames;
    private HashMap<String, SymbolTable> methodNames;
    private HashMap<String, Object> runtimeStack;
    private SymbolTable parent;
    private SymbolTable importParent;

    public SymbolTable() {
        varNames = new HashMap<String, NameNode>();
        methodNames = new HashMap<String, SymbolTable>();
        runtimeStack = new HashMap<String, Object>();
    }

    public SymbolTable(SymbolTable p) {
        this();
        parent = p;
    }

    public void setValueInRuntimeStack(String name, Object value) {
        if(!runtimeStack.containsKey(name) && parent != null)
            parent.setValueInRuntimeStack(name,value);
        else
            runtimeStack.put(name,value);
    }

    public Object getValueInRuntimeStack(String name) {
        Object valueFound = runtimeStack.get(name);
        if(valueFound != null) return valueFound;
        else if(parent != null) return parent.getValueInRuntimeStack(name);
        else return null;
    }

    public void setParent(SymbolTable p) { parent = p; }
    public SymbolTable getParent() { return parent; }

    public void setImportParent(SymbolTable ip) { importParent = ip; }
    public SymbolTable getImportParent() { return importParent; }

    public void addName(String name, NameNode n) { varNames.put(name,n); }
    public void addMethod(String nameSignature, SymbolTable st) { methodNames.put(nameSignature,st); }

    public NameNode findName(String name) {
        NameNode nameFound = varNames.get(name);
        if(nameFound != null) return nameFound;
        else if(parent != null) return parent.findName(name);
        else if(importParent != null) return importParent.findName(name);
        else return null;
    }

    public SymbolTable findMethod(String name) {
        SymbolTable methodFound = methodNames.get(name);
        if(methodFound != null) return methodFound;
        else if(parent != null) return parent.findMethod(name);
        else if(importParent != null) return importParent.findMethod(name);
        else return null;
    }

    public boolean hasName(String name) {
        return varNames.containsKey(name) || hasNameInImportTable(name);
    }

    public boolean hasNameSomewhere(String name) {
        if(hasName(name)) return true;
        else if(parent != null) return parent.hasNameSomewhere(name);
        else if(importParent != null) return importParent.hasName(name);
        else return false;
    }

    public boolean hasNameInImportTable(String name) {
        if(importParent != null)
            return importParent.hasName(name);
        return false;
    }

    public boolean hasMethod(String name) { return methodNames.containsKey(name); }

    public boolean hasMethodSomewhere(String name) {
        if(hasMethod(name)) return true;
        else if(parent != null) return parent.hasMethodSomewhere(name);
        else if(importParent != null) return importParent.hasMethod(name);
        else return false;
    }

    public boolean hasMethodInImportTable(String name) {
        if(importParent != null)
            return importParent.hasMethod(name);
        return false;
    }

    public NameNode getVarName(String n) { return varNames.get(n); }

    public HashMap<String,NameNode> getVarNames() { return varNames; }
    public HashMap<String,SymbolTable> getMethodNames() { return methodNames; }

    public boolean isNameUsedAnywhere(String name) {
        return hasNameSomewhere(name) || hasMethodSomewhere(name);
    }

    public SymbolTable openNewScope() { return new SymbolTable(this); }
    public SymbolTable closeScope() { return parent; }

    public String indent(int level) { return "|  ".repeat(level); }

    public void addEntries(StringBuilder sb, int level, HashSet<SymbolTable> visited) {
        sb.append("\n" + level + ". Variable Names");
        for(var entry : varNames.entrySet()) {
            sb.append(indent(level+1));
            sb.append("\nName: " + entry.getKey());
        }
        sb.append("\n" + level + ". Construct Names");
        for(var entry : methodNames.entrySet()) {
            if(!visited.contains(entry.getValue())) {
                visited.add(entry.getValue());
                entry.getValue().addEntries(sb,level+1,visited);
            }
        }
    }

    public String symbolTableToString(HashSet<SymbolTable> visited, int level) {
        StringBuilder sb = new StringBuilder();
        visited.add(this);

        addEntries(sb,level,visited);

        if(parent != null && !visited.contains(parent))
            parent.addEntries(sb,level+1,visited);
        else if(importParent != null && !visited.contains(parent))
            importParent.addEntries(sb,level+1,visited);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        HashSet<SymbolTable> visited = new HashSet<SymbolTable>();
        sb.append("____________________________ SYMBOL TABLE ____________________________\n");
        sb.append(symbolTableToString(visited,0));
        sb.append("______________________________________________________________________\n");
        return sb.toString();
    }
}
