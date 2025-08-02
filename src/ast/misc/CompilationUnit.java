package ast.misc;

import ast.AST;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.ImportDecl;
import ast.topleveldecls.MainDecl;
import token.Token;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link SubNode} type representing the compilation unit for a C Minor program.
 * <p>
 *     This node contains all the necessary information to analyze a user's program after a
 *     parse tree is generated. If we are in compilation mode, then a {@link CompilationUnit}
 *     will act as the root node. If we are in interpretation mode, then we will create a
 *     global {@link CompilationUnit} that will be used by the {@link interpreter.VM}. This
 *     compilation unit will store all constructs a user declares while executing the virtual
 *     environment.
 * </p>
 * @author Daniel Levy
 */
public class CompilationUnit extends SubNode implements ScopeDecl {

    /**
     * File that the current {@link CompilationUnit} was generated for.
     */
    private String fileName;

    /**
     * {@link SymbolTable} that represents the global scope of the file.
     */
    private SymbolTable globalScope;

    /**
     * List of imported files.
     */
    private Vector<ImportDecl> imports;

    /**
     * List of enums.
     */
    private Vector<EnumDecl> enums;

    /**
     * List of global variables.
     */
    private Vector<GlobalDecl> globals;

    /**
     * List of classes.
     */
    private Vector<ClassDecl> classes;

    /**
     * List of functions.
     */
    private Vector<FuncDecl> functions;

    /**
     * The main function of the program.
     * <p>
     *     This is not set during interpretation mode, and this field is only set
     *     in a single {@link CompilationUnit} during compilation mode.
     * </p>
     */
    private MainDecl main;

    /**
     * Default constructor for {@link CompilationUnit}.
     * <p>
     *     This will be called by the {@link interpreter.VM} when it initializes the virtual environment.
     * </p>
     */
    public CompilationUnit() {
        this(new Token(),"",new Vector<>(),new Vector<>(),new Vector<>(),new Vector<>(),new Vector<>(),new MainDecl());
        this.globalScope = new SymbolTable();
    }

    /**
     * Main constructor for {@link CompilationUnit}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param fileName String to store into {@link #fileName}.
     * @param imports {@link Vector} of {@link ImportDecl} to store into {@link #imports}.
     * @param enums {@link Vector} of {@link EnumDecl} to store into {@link #enums}.
     * @param globals {@link Vector} of {@link GlobalDecl} to store into {@link #globals}.
     * @param classes {@link Vector} of {@link ClassDecl} to store into {@link #classes}.
     * @param functions {@link Vector} of {@link FuncDecl} to store into {@link #functions}.
     * @param main {@link MainDecl} to store into {@link #main}.
     */
    public CompilationUnit(Token metaData, String fileName, Vector<ImportDecl> imports, Vector<EnumDecl> enums,
                           Vector<GlobalDecl> globals, Vector<ClassDecl> classes, Vector<FuncDecl> functions, MainDecl main) {
        super(metaData);
        this.fileName = fileName;
        this.imports = imports;
        this.enums = enums;
        this.globals = globals;
        this.classes = classes;
        this.functions = functions;
        this.main = main;

        addChildNode(this.imports);
        addChildNode(this.enums);
        addChildNode(this.globals);
        addChildNode(this.classes);
        addChildNode(this.functions);
        addChildNode(this.main);
    }

    /**
     * Getter method for {@link #imports}
     * @return {@link Vector} of imports.
     */
    public Vector<ImportDecl> getImports() { return imports; }

    /**
     * Getter method for {@link #enums}.
     * @return {@link Vector} of enums.
     */
    public Vector<EnumDecl> getEnums() { return enums; }

    /**
     * Getter method for {@link #globals}.
     * @return {@link Vector} of global variables.
     */
    public Vector<GlobalDecl> getGlobals() { return globals; }

    /**
     * Getter method for {@link #classes}.
     * @return {@link Vector} of classes.
     */
    public Vector<ClassDecl> getClasses() { return classes; }

    /**
     * Getter method for {@link #functions}.
     * @return {@link Vector} of functions.
     */
    public Vector<FuncDecl> getFunctions() { return functions; }

    /**
     * Getter method for {@link #main}.
     * @return {@link MainDecl}
     */
    public MainDecl getMain() { return main; }

    /**
     * Adds a new {@link ClassDecl} to the current {@link CompilationUnit}.
     * <p>
     *     This method is only called by the {@link interpreter.VM} any time a class is
     *     written by the user. It will be saved into the VM's virtual environment, so the
     *     user can continue to use it after it was declared.
     * </p>
     * @param newClass The {@link ClassDecl} we wish to add to the VM's virtual environment.
     */
    public void addClassDecl(ClassDecl newClass) { classes.add(newClass); }

    /**
     * Adds a list of {@link ClassDecl} to the current {@link CompilationUnit}.
     * <p>
     *     In interpretation mode, a user is able to import files directly in the {@link interpreter.VM}.
     *     As a result, we need to make sure that all classes from the imported files are added to the
     *     virtual environment which is what this method will do for us in the {@link interpreter.Interpreter}.
     * </p>
     * @param classes A {@link Vector} of classes that comes from an imported file when written in the VM.
     */
    public void addClassDecl(Vector<ClassDecl> classes) { this.classes.addAll(classes); }

    /**
     * Adds a new {@link FuncDecl} to the current {@link CompilationUnit}.
     * <p>
     *     This method is only called by the {@link interpreter.VM} any time a function is
     *     written by the user.
     * </p>
     * @param function The {@link FuncDecl} we wish to add to the VM's virtual environment.
     */
    public void addFuncDecl(FuncDecl function) { functions.add(function); }

    /**
     * Adds a list of {@link FuncDecl} to the current {@link CompilationUnit}.
     * <p>
     *     This method is the same as {@link #addClassDecl(Vector)}, but for functions.
     * </p>
     * @param functions A {@link Vector} of functions that comes from an imported file when written in the VM.
     */
    public void addFuncDecl(Vector<FuncDecl> functions) { this.functions.addAll(functions); }

    /**
     * Resets the current {@link CompilationUnit} and removes all of its stored constructs.
     * <p>
     *     This method should only be called by the {@link interpreter.VM} when a user writes
     *     {@code #clear} in order to remove any declared constructs within the VM.
     * </p>
     */
    public void reset() {
        this.globalScope.clear();
        this.imports.clear();
        this.enums.clear();
        this.globals.clear();
        this.classes.clear();
        this.functions.clear();
        this.main = null;
    }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() { return (globalScope != null) ? globalScope : null; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable st) { globalScope = (globalScope == null) ? st : globalScope; }

    /**
     * {@inheritDoc}
     */
    public boolean isCompilation() { return true; }

    /**
     * {@inheritDoc}
     */
    public CompilationUnit asCompilation() { return this; }

    /**
     * Returns the file name associated with the current {@link CompilationUnit}.
     * @return {@link #fileName}
     */
    @Override
    public String toString() { return fileName; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A compilation unit can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<ImportDecl> imports = new Vector<>();
        Vector<EnumDecl> enums = new Vector<>();
        Vector<GlobalDecl> globals = new Vector<>();
        Vector<ClassDecl> classes = new Vector<>();
        Vector<FuncDecl> functions = new Vector<>();

        for(ImportDecl im : this.imports)
            imports.add(im.deepCopy().asTopLevelDecl().asImport());
        for(EnumDecl ed : this.enums)
            enums.add(ed.deepCopy().asTopLevelDecl().asEnumDecl());
        for(GlobalDecl gd : this.globals)
            globals.add(gd.deepCopy().asTopLevelDecl().asGlobalDecl());
        for(ClassDecl cd : this.classes)
            classes.add(cd.deepCopy().asTopLevelDecl().asClassDecl());
        for(FuncDecl fd : this.functions)
            functions.add(fd.deepCopy().asTopLevelDecl().asFuncDecl());

        CompilationUnitBuilder cb = new CompilationUnitBuilder();

        if(main != null)
            cb.setMainFunc(main.deepCopy().asTopLevelDecl().asMainDecl());

        return cb.setMetaData(this)
                 .setFileName(fileName)
                 .setImportDecls(imports)
                 .setEnumDecls(enums)
                 .setGlobalDecls(globals)
                 .setClassDecls(classes)
                 .setFuncDecls(functions)
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitCompilationUnit(this); }

    /**
     * Internal class that builds a {@link CompilationUnit} object.
     */
    public static class CompilationUnitBuilder extends NodeBuilder {

        /**
         * {@link CompilationUnit} object we are building.
         */
        private final CompilationUnit cu = new CompilationUnit();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setMetaData(AST node) {
            super.setMetaData(cu,node);
            return this;
        }

        /**
         * Sets the compilation unit's {@link #fileName}.
         * @param fileName String name of the file.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setFileName(String fileName) {
            cu.fileName = fileName;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #imports}.
         * @param imports {@link Vector} of imports.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setImportDecls(Vector<ImportDecl> imports) {
            cu.imports = imports;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #enums}.
         * @param enums {@link Vector} of enums.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setEnumDecls(Vector<EnumDecl> enums) {
            cu.enums = enums;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #globals}.
         * @param globals {@link Vector} of global variables.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setGlobalDecls(Vector<GlobalDecl> globals) {
            cu.globals = globals;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #classes}.
         * @param classes {@link Vector} of classes.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setClassDecls(Vector<ClassDecl> classes) {
            cu.classes = classes;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #functions}.
         * @param functions {@link Vector} of functions.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setFuncDecls(Vector<FuncDecl> functions) {
            cu.functions = functions;
            return this;
        }

        /**
         * Sets the compilation unit's {@link #main}.
         * @param main {@link MainDecl} representing the main function of the program.
         * @return Current instance of {@link CompilationUnitBuilder}.
         */
        public CompilationUnitBuilder setMainFunc(MainDecl main) {
            cu.main = main;
            return this;
        }

        /**
         * Creates a {@link CompilationUnit} object.
         * @return {@link CompilationUnit}
         */
        public CompilationUnit create() {
            cu.addChildNode(cu.imports);
            cu.addChildNode(cu.enums);
            cu.addChildNode(cu.globals);
            cu.addChildNode(cu.classes);
            cu.addChildNode(cu.functions);
            cu.addChildNode(cu.main);
            return cu;
        }
    }
}
