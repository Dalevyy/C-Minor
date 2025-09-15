package cminor.utilities;

import cminor.ast.AST;
import cminor.ast.expressions.Invocation;
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

    /**
     * Adds a declared name into the scope's {@link #names}
     * @param node A {@link NameDecl} we wish to add into the current scope.
     */
    public void addName(NameDecl node) { names.put(node.getDeclName(),node); }

    /**
     * Adds a declared name into the scope's {@link #names}
     * @param name The name we wish to add to the table as a string
     * @param node A {@link NameDecl} we wish to add into the current scope.
     */
    public void addName(String name, NameDecl node) { names.put(name,node); }

    /**
     * Checks if a passed {@link NameDecl} is contained in the current scope.
     * @param name The name we wish to check if it exists in the current scope.
     * @return {@code True} if the {@link NameDecl} is contained in the current scope, {@code False} otherwise.
     */
    public boolean hasName(String name) { return names.containsKey(name) || hasImportedName(name); }

    /**
     * Checks if a passed {@link NameDecl} is contained in the current scope.
     * @param node The {@link AST} node we want to see if it's in the scope.
     * @return {@code True} if the {@link NameDecl} is contained in the current scope, {@code False} otherwise.
     */
    public boolean hasName(AST node) { return hasName(node.toString()); }

    public boolean hasImportedName(String name) { return importParent != null && importParent.hasName(name); }

    /**
     * Checks if a name exists somewhere in a user's program.
     * @param node The {@link AST} node we wish to see if a declaration exists for.
     * @return {@code True} if the name was found, {@code False} otherwise.
     */
    public boolean hasNameInProgram(AST node) { return hasNameInProgram(node.toString()); }

    /**
     * Checks if a name exists somewhere in a user's program.
     * @param name The name we wish to see if it was declared in the program.
     * @return {@code True} if the name was found, {@code False} otherwise.
     */
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

    /**
     * Finds and return a {@link NameDecl} in the scope hierarchy.
     * @param node The {@link AST} node we wish to find a {@link NameDecl} of.
     * @return A {@link NameDecl} if we find the closest declaration for the name, {@code null} otherwise.
     */
    public AST findName(AST node) { return findName(node.toString()); }

    /**
     * Removes a name from the scope hierarchy.
     * <p>
     *     This method should only be called when a {@link cminor.messages.CompilationMessage} is thrown
     *     when interpretation mode is executing.
     * </p>
     * @param node The {@link AST} node we wish to remove from the current scope.
     */
    public void removeName(AST node) {
        if(names.remove(node.toString()) == null && parent != null)
            parent.removeName(node);
    }

    /**
     * Adds a function/method to the scope's {@link #methods}.
     * @param method The {@link NameDecl} representing a function/method that will be added.
     */
    public void addMethod(NameDecl method) {
        if(!methods.containsKey(method.getDeclName()))
            methods.put(method.getDeclName(), new SymbolTable());

        SymbolTable methodTable = methods.get(method.getDeclName());
        if(method.getDecl().isTopLevelDecl() && method.getDecl().asTopLevelDecl().isFuncDecl()) {
            FuncDecl fd = method.getDecl().asTopLevelDecl().asFuncDecl();
            methodTable.addName(fd.getParamSignature(), method);
        }
        else {
            MethodDecl md = method.getDecl().asClassNode().asMethodDecl();
            if(!methodTable.hasName(md.getParamSignature()))
                methodTable.addName(md.getParamSignature(), method);
        }
    }

    public void addMethods(SymbolTable classTable) {
        for(String methodName : classTable.methods.keySet()) {
            for(NameDecl method : classTable.methods.get(methodName).names.values())
                addMethod(method);
        }
    }

    /**
     * Checks if the method name currently exists in the method table.
     * @param name String representing the method name we wish to search for.
     * @return {@code True} if the method was declared in the program, {@code False} otherwise.
     */
    public boolean hasMethodName(String name) {
        if(methods.containsKey(name))
            return true;
        else if(parent != null)
            return parent.hasMethodName(name);
        else if(importParent != null)
            return importParent.hasMethodName(name);
        else
            return false;
    }

    /**
     * Checks if the method name currently exists in the method table.
     * @param node The {@link AST} node we wish to check for the existence of a method.
     * @return {@code True} if the method was declared in the program, {@code False} otherwise.
     */
    public boolean hasMethodName(AST node) { return hasMethodName(node.toString()); }

    /**
     * Checks if a function/method overload exists within the {@link #methods} table.
     * <p>
     *     We will use the parameter signature of the function/method to store each valid
     *     overload in the {@link #methods} table. If we know that a parameter signature already
     *     exists for a given function/method, that implies that the overload already exists
     *     which means we have a redeclaration. This helper is used by the {@link cminor.namechecker.NameChecker}
     *     in order to determine if any function overloads are redeclared.
     * </p>
     * @param node The {@link NameDecl} we wish to check for an overload.
     * @return {@code True} if the overload exists, {@code False} otherwise.
     */
    public boolean hasMethodOverload(NameDecl node) {
        // No overload exists if the method name wasn't previously declared!
        if(!methods.containsKey(node.getDeclName()))
            return false;

        // Remember, this method is always called by a FuncDecl or MethodDecl visit!
        SymbolTable overloads = methods.get(node.getDeclName());
        String paramSignature;

        if(node.getDecl().isTopLevelDecl())
            paramSignature = node.getDecl().asTopLevelDecl().asFuncDecl().getParamSignature();
        else
            paramSignature = node.getDecl().asClassNode().asMethodDecl().getParamSignature();


        return overloads.hasName(paramSignature);
    }

    /**
     * Checks if a valid overload exists for a method.
     * <p>
     *     Note: This helper is used by the {@link cminor.typechecker.TypeChecker}!
     * </p>
     * @param node An {@link AST} node we wish to check for a method overload of.
     * @param argSignature The string representation of an argument signature.
     * @return {@code True} if the overload exists, {@code False} otherwise.
     */
    public boolean hasMethodOverload(AST node, String argSignature) {
        return hasMethodOverload(node.toString(),argSignature);
    }

    /**
     * Checks if a valid overload exists for a method.
     * <p>
     *     Note: This helper is used by the {@link cminor.typechecker.TypeChecker}!
     * </p>
     * @param name The method we wish to check for an overload (as a string).
     * @param argSignature The string representation of an argument signature.
     * @return {@code True} if the overload exists, {@code False} otherwise.
     */
    private boolean hasMethodOverload(String name, String argSignature) {
        SymbolTable overloads = methods.get(name);

        if(overloads == null)
            return parent.hasMethodOverload(name,argSignature);

        return overloads.hasName(argSignature);
    }

    /**
     * Finds a method in the {@link #methods} table.
     * <p>
     *     This is used by the {@link cminor.namechecker.NameChecker} to deal with redeclaration errors!
     * </p>
     * @param method The {@link NameDecl} we want to retrieve a previous declaration of.
     * @return The {@link AST} representing the method declaration or an Exception if no method was found.
     */
    public AST findMethod(NameDecl method) {
        if(method.getDecl().isTopLevelDecl())
            return findMethod(method.getDeclName(),method.getDecl().asTopLevelDecl().asFuncDecl().getParamSignature());
        else if(method.getDecl().isClassNode())
            return findMethod(method.getDeclName(),method.getDecl().asClassNode().asMethodDecl().getParamSignature());
        else
            throw new RuntimeException("The passed name declaration does not represent a function or method.");
    }

    /**
     * Retrieves a method overload from the {@link #methods} table.
     * @param methodName The method we wish to find the declaration of.
     * @param signature The string representation of the method's signature.
     * @return An {@link AST} node representing a {@link FuncDecl} or {@link MethodDecl}
     */
    public AST findMethod(String methodName, String signature) {
        SymbolTable methodTable = methods.get(methodName);

        if(methodTable == null) {
            if(parent != null)
                return parent.findMethod(methodName,signature);
            return null;
        }

        NameDecl d = methodTable.names.get(signature);
        return d == null ? null : d.getDecl();
    }

    /**
     * Retrieves a method overload from the {@link #methods} table.
     * @param in The {@link Invocation} we wish to find the corresponding method of.
     * @return An {@link AST} node representing a {@link FuncDecl} or {@link MethodDecl}
     */
    public AST findMethod(Invocation in) { return findMethod(in.getName().toString(),in.getSignature()); }

    /**
     * Retrieves the global scope of the program.
     * @return {@link SymbolTable} representing the global scope.
     */
    public SymbolTable getGlobalScope() {
        SymbolTable root = this;

        while(root.parent != null)
            root = root.parent;

        return root;
    }

    /**
     * Creates a new scope to store names into.
     * @return A {@link SymbolTable} representing the newly opened scope.
     */
    public SymbolTable openScope() { return new SymbolTable(this); }

    /**
     * Creates a new scope based on an already existing scope.
     * <p>
     *     This will be used by classes in order to access any inherited fields and methods.
     * </p>
     * @param st The {@link SymbolTable} we wish to make the parent of the new scope we are opening.
     * @return The {@link SymbolTable} we have created.
     */
    public SymbolTable openScope(SymbolTable st) { return new SymbolTable(st); }

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
