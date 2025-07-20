package utilities;

import ast.AST;
import ast.misc.NameNode;
import ast.topleveldecls.FuncDecl;
import java.util.HashMap;
import java.util.HashSet;

// SymbolTable needs to be fixed for imports... no lol
public class SymbolTable {

    private final HashMap<String, NameNode> varNames;
    private final HashSet<String> methodNames;
    private SymbolTable parent;
    private SymbolTable importParent;

    public SymbolTable() {
        varNames = new HashMap<>();
        methodNames = new HashSet<>();

    }

    public SymbolTable(SymbolTable p) {
        this();
        parent = p;
    }

    public void setParent(SymbolTable p) { parent = p; }
    public SymbolTable getParent() { return parent; }

    public void setImportParent(SymbolTable ip) {
        if(importParent != null)
            importParent.setImportParent(ip);
        else
            importParent = ip;
    }

    public SymbolTable getImportParent() { return importParent; }

    public SymbolTable getRootTable() {
        if(parent == null)
            return this;
        else
            return parent.getRootTable();
    }

    public Vector<FuncDecl> getAllFuncNames() {
        SymbolTable rootTable = getRootTable();
        Vector<FuncDecl> lst = new Vector<>();

        for(NameNode name : getRootTable().getAllNames().values())
            if(name.decl().isTopLevelDecl() && name.decl().asTopLevelDecl().isFuncDecl())
                lst.add(name.decl().asTopLevelDecl().asFuncDecl());

        return lst;
    }

    public void addName(String name, NameNode n) { varNames.put(name,n); }
    public void addNameToRootTable(String name, NameNode n) {
        if(parent != null)
            parent.addNameToRootTable(name,n);
        else
            this.addName(name,n);
    }

    public void addMethod(String methodName) { methodNames.add(methodName); }

    public NameNode findName(String name) {
        NameNode nameFound = varNames.get(name);
        if(nameFound != null) return nameFound;
        else if(parent != null) return parent.findName(name);
        else if(importParent != null) return importParent.findName(name);
        else return null;
    }

    public NameNode findName(AST node) { return findName(node.toString()); }

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

    public boolean hasMethod(String name) { return methodNames.contains(name); }

    public boolean hasMethodSomewhere(String name) {
        if(hasMethod(name)) return true;
        else if(parent != null) return parent.hasMethodSomewhere(name);
        else if(importParent != null) return importParent.hasMethod(name);
        else return false;
    }

    public void removeName(String name) {
        if(varNames.remove(name) == null) { parent.removeName(name); }
        // Gets the job done :)
        if(name.contains("/") && methodNames.contains(name.substring(0,name.indexOf("/")))) {
             for(String n : varNames.keySet()) {
                 if(n.contains("/") && n.substring(0,n.indexOf("/")).equals(name.substring(0,name.indexOf("/")))) {
                     return;
                 }
             }
             methodNames.remove(name.substring(0,name.indexOf("/")));
        }
    }

    public HashMap<String,NameNode> getAllNames() { return varNames; }
    public HashSet<String> getMethodNames() { return methodNames; }

    public boolean isNameUsedAnywhere(String name) {
        return hasNameSomewhere(name) || hasMethodSomewhere(name);
    }

    public SymbolTable openNewScope() { return new SymbolTable(this); }
    public SymbolTable closeScope() { return parent; }

    public String indent(int level) { return "  ".repeat(level); }

    public void addEntries(StringBuilder sb, int level, HashSet<SymbolTable> visited) {
        StringBuilder constructs = new StringBuilder();
        constructs.append("\n" + indent(level+1) + "Top Level Names");
        sb.append(indent(level+1) + "Variable Names");
        for(var entry : varNames.entrySet()) {
            sb.append("\n" + indent(level + 1));
            if (entry.getValue().decl().isStatement()) {
                sb.append(indent(level + 2) + "Local: " + entry.getKey());
            }
            else if (entry.getValue().decl().isTopLevelDecl()) {
                if (entry.getValue().decl().asTopLevelDecl().isEnumDecl()) {
                    constructs.append("\n" + indent(level + 2) + "Enum: " + entry.getKey());
                } else if (entry.getValue().decl().asTopLevelDecl().isGlobalDecl()) {
                    sb.append("\n" + indent(level + 2) + "Global: " + entry.getKey());
                } else if (entry.getValue().decl().asTopLevelDecl().isClassDecl()) {
                    constructs.append("\n" + indent(level + 2) + "Class: " + entry.getKey());
                } else if (entry.getValue().decl().asTopLevelDecl().isFuncDecl()) {
                    constructs.append("\n" + indent(level + 2) + "Function: " + entry.getKey());
                }
            }
            else if (entry.getValue().decl().isFieldDecl()) {
                sb.append(indent(level + 2) + "Field: " + entry.getKey());
            }
            else if (entry.getValue().decl().isMethodDecl()) {
                sb.append(indent(level + 2) + "Method: " + entry.getKey());
            }
        }
        sb.append(constructs);
    }

    public String symbolTableToString(HashSet<SymbolTable> visited, int level) {
        StringBuilder sb = new StringBuilder();
        visited.add(this);
        sb.append(indent(level) + "Level " + (level+1) + "\n");
        addEntries(sb,level,visited);

        if(parent != null && !visited.contains(parent)) { parent.addEntries(sb,level+1,visited); }
        else if(importParent != null && !visited.contains(parent)) { importParent.addEntries(sb,level+1,visited); }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        HashSet<SymbolTable> visited = new HashSet<>();
        sb.append("____________________________ SYMBOL TABLE ____________________________\n");
        sb.append(symbolTableToString(visited,0));
        sb.append("\n______________________________________________________________________\n");
        return sb.toString();
    }
}
