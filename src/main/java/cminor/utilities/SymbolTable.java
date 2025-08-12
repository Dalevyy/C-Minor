package cminor.utilities;

import cminor.ast.AST;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.misc.NameDecl;
import java.util.HashMap;
import java.util.Iterator;
import cminor.utilities.Vector.VectorIterator;

public class SymbolTable {

    /**
     * A {@link HashMap} that tracks all declared variables names in a program.
     */
    private HashMap<String, NameDecl> names;

    /**
     * A {@link HashMap} that tracks all declared functions/methods in a program.
     * <p>
     *     This will only be used by the global scope and class scopes.
     * </p>
     */
    private HashMap<String, SymbolTable> methods;

    /**
     * The parent {@link SymbolTable} that the current scope is nested in.
     */
    private SymbolTable parent;

    /**
     * A parent {@link SymbolTable} that points to an imported file's scope (if applicable).
     */
    private SymbolTable importParent;

    /**
     * String name for the current scope (used for debugging purposes).
     */
    private String description;

    /**
     * Default constructor for {@link SymbolTable}.
     */
    public SymbolTable() {
        this.names = new HashMap<>();
        this.methods = new HashMap<>();
    }

    /**
     * Constructor for {@link SymbolTable} to create a new nested scope.
     * @param prevScope {@link SymbolTable} that the new scope resides within.
     */
    public SymbolTable(SymbolTable prevScope) {
        this();
        this.parent = prevScope;
    }

    public SymbolTable getMethods() {
        SymbolTable currentScope = this;

        while(currentScope.parent != null) {
            // Outside of the global scope, a class scope will be the only scope that uses both the names
            // and method tables which is why this condition needs to exist.
            if(!currentScope.names.isEmpty() && !currentScope.methods.isEmpty())
                break;
            currentScope = currentScope.parent;
        }

        return currentScope;
    }

    /**
     * Adds a declared name into the scope's {@link #names}
     * @param node A {@link NameDecl} we wish to add into the current scope.
     */
    public void addName(NameDecl node) { names.put(node.getDeclName(),node); }

    public void addName(String name, NameDecl node) { names.put(name,node); }

    /**
     * Checks if a passed {@link NameDecl} is contained in the current scope.
     * @param node The {@link AST} node we want to see if it's in the scope.
     * @return {@code True} if the {@link NameDecl} is contained in the current scope, {@code False} otherwise.
     */
    public boolean hasName(AST node) { return hasName(node.toString()); }

    public boolean hasName(String name) { return names.containsKey(name) || hasImportedName(name); }

    public boolean hasImportedName(String name) { return importParent != null && importParent.hasName(name); }

    public boolean hasNameInProgram(AST node) { return hasNameInProgram(node.toString()); }

    private boolean hasNameInProgram(String name) {
        if(names.containsKey(name))
            return true;
        else if(parent != null)
            return parent.hasNameInProgram(name);
        else if(importParent != null)
            return importParent.hasNameInProgram(name);
        else
            return false;
    }

    /**
     * Finds and return a {@link NameDecl} in the scope hierarchy.
     * @param name The name we are searching for in the scope hierarchy.
     * @return A {@link NameDecl} if we find the closest declaration for the name, {@code null} otherwise.
     */
    public AST findName(String name) {
        NameDecl node = names.get(name);

        if(node != null)
            return node.getDecl();
        else if(parent != null)
            return parent.findName(name);
        else if(importParent != null)
            return importParent.findName(name);
        else
            return null;
    }

    public AST findName(AST node) { return findName(node.toString()); }

    /**
     * Removes a name from the scope hierarchy.
     * <p>
     *     This method should only be called when a {@link messages.CompilationMessage} is thrown
     *     when interpretation mode is executing.
     * </p>
     * @param node The {@link AST} node we wish to remove from the current scope.
     */
    public void removeName(AST node) {
        if(names.remove(node.toString()) == null)
            parent.removeName(node);
    }

    /**
     * Adds a function/method to the scope's {@link #methods}.
     * @param method The {@link NameDecl} representing a function/method that will be added.
     */
    public void addMethod(NameDecl method) {
        if(!hasMethod(method))
            methods.put(method.getDeclName(), new SymbolTable());

        SymbolTable methodTable = methods.get(method.getDeclName());
        if(method.isFunction()) {
            FuncDecl fd = method.getDecl().asTopLevelDecl().asFuncDecl();
            methodTable.addName(fd.getParamSignature(), method);
        }
        else {
            MethodDecl md = method.getDecl().asClassNode().asMethodDecl();
            methodTable.addName(md.getParamSignature(), method);
        }
    }

    public boolean hasMethod(NameDecl node) { return methods.containsKey(node.toString()); }
    public boolean hasMethod(String name) { return methods.containsKey(name) || importParent.hasMethod(name); }

    /**
     * Checks if a function/method overload exists within the {@link #methods} table.
     * <p>
     *     We will use the parameter signature of the function/method to store each valid
     *     overload in the {@link #methods} table. If we know that a parameter signature already
     *     exists for a given function/method, that implies that the overload already exists
     *     which means we have a redeclaration.
     * </p>
     * @param method The {@link NameDecl} we wish to check for an overload.
     * @return {@code True} if the overload exists, {@code False} otherwise.
     */
    public boolean hasMethodOverload(NameDecl method) {
        if(!(method.isMethod() || method.isFunction()) || !hasMethod(method))
            return false;

        SymbolTable methodTable = methods.get(method.getDeclName());
        String paramSignature;

        if(method.isFunction())
            paramSignature = method.getDecl().asTopLevelDecl().asFuncDecl().getParamSignature();
        else
            paramSignature = method.getDecl().asClassNode().asMethodDecl().getParamSignature();

        return methodTable.hasName(paramSignature);
    }

    public AST findMethod(NameDecl method) {
        if(method.isFunction())
            return findMethod(method.getDeclName(),method.getDecl().asTopLevelDecl().asFuncDecl().getParamSignature());
        else if(method.isMethod())
            return findMethod(method.getDeclName(),method.getDecl().asClassNode().asMethodDecl().getParamSignature());
        else
            throw new RuntimeException("The passed name declaration does not represent a function or method.");
    }

    public AST findMethod(String methodName, String signature) {
        SymbolTable methodTable = methods.get(methodName);

        if(methodTable.hasName(signature))
            return methodTable.names.get(signature).getDecl();
        else
            return null;
    }

    /**
     * Creates a new scope to store names into.
     * @return A {@link SymbolTable} representing the newly opened scope.
     */
    public SymbolTable openScope() { return new SymbolTable(this); }

    /**
     * Closes the current scope.
     * @return The parent {@link SymbolTable} (if not in the global scope).
     */
    public SymbolTable closeScope() { return parent; }

    /**
     * Resets the current {@link SymbolTable} instance.
     */
    public void clear() {
        this.names = new HashMap<>();
        this.methods = new HashMap<>();
        this.parent = null;
        this.importParent = null;
    }

    /**
     * An internal iterator class that will go through {@link #names}.
     */
    public static class NameIterator implements Iterator {

        /**
         * An internal iterator to keep track of the names we have seen.
         */
        private final VectorIterator names;

        /**
         * Default constructor for {@link NameIterator}.
         * @param scope The {@link SymbolTable} we will create an iterator for.
         */
        public NameIterator(SymbolTable scope) {
            this.names = new VectorIterator(new Vector<>(scope.names.values().toArray()));
        }

        /**
         * Checks if {@link #names} can still be iterated.
         * @return {@code True} if the iterator has not checked all of {@link #names}, {@code False} otherwise.
         */
        public boolean hasNext() { return names.hasNext(); }

        /**
         * Returns the next {@link NameDecl} from {@link #names}.
         * @return {@link NameDecl}
         */
        public AST next() { return ((NameDecl)names.next()).getDecl(); }
    }
}
