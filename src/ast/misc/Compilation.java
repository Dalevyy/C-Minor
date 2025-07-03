package ast.misc;

import ast.AST;
import ast.topleveldecls.*;
import token.*;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/*
_______________________ Compilation _______________________
A Compilation node will be the root node for every C Minor
AST, and it will point to all TopLevelDecls.
___________________________________________________________
*/
public class Compilation extends AST {

    private String fileName;
    public SymbolTable globalTable;

    private Vector<ImportDecl> imports;
    private Vector<EnumDecl> enumDecls;
    private Vector<GlobalDecl> globalDecls;
    private Vector<ClassDecl> classDecls;
    private Vector<FuncDecl> funcDecls;
    private MainDecl mainFunc;

    public Compilation() {
        this.globalTable = new SymbolTable();
        this.imports = new Vector<>();
        this.enumDecls = new Vector<>();
        this.globalDecls = new Vector<>();
        this.classDecls = new Vector<>();
        this.funcDecls = new Vector<>();
        this.mainFunc = new MainDecl();
    }

    public Compilation(Token t, String fileName,Vector<ImportDecl> ims, Vector<EnumDecl> ed, Vector<GlobalDecl> gd,
                       Vector<ClassDecl> cd, Vector<FuncDecl> fd, MainDecl m) {
        super(t);
        this.fileName = fileName;
        imports = ims;
        enumDecls = ed;
        globalDecls = gd;
        classDecls = cd;
        funcDecls = fd;
        mainFunc = m;

        addChild(imports);
        addChild(enumDecls);
        addChild(globalDecls);
        addChild(classDecls);
        addChild(funcDecls);
        addChild(mainFunc);
    }

    public Vector<ImportDecl> imports() { return imports; }
    public Vector<EnumDecl> enumDecls() { return enumDecls; }
    public Vector<GlobalDecl> globalDecls() { return globalDecls; }
    public Vector<ClassDecl> classDecls() { return classDecls; }
    public Vector<FuncDecl> funcDecls() { return funcDecls; }
    public MainDecl mainDecl() { return mainFunc; }

    public void mergeCompilationUnit(Compilation c) {
        this.addEnumDecl(c.enumDecls);
        this.addGlobalDecl(c.globalDecls);
        this.addClassDecl(c.classDecls);
        this.addFuncDecl(c.funcDecls);
    }

    public void addEnumDecl(EnumDecl ed) { enumDecls.add(ed); }
    public void addEnumDecl(Vector<EnumDecl> ed) { enumDecls.addAll(ed); }

    public void addGlobalDecl(GlobalDecl gd) { globalDecls.add(gd); }
    public void addGlobalDecl(Vector<GlobalDecl> gd) { globalDecls.addAll(gd); }

    public void addClassDecl(ClassDecl cd) { classDecls.add(cd); }
    public void addClassDecl(Vector<ClassDecl> cd) { classDecls.addAll(cd); }

    public void addFuncDecl(FuncDecl fd) { funcDecls.add(fd); }
    public void addFuncDecl(Vector<FuncDecl> fd) { funcDecls.addAll(fd); }

    public boolean isCompilation() { return true; }
    public Compilation asCompilation() { return this; }

    public String getFile() { return fileName; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A compilation unit can not be updated."); }

    @Override
    public AST deepCopy() {
        Vector<ImportDecl> imports = new Vector<>();
        Vector<EnumDecl> enums = new Vector<>();
        Vector<GlobalDecl> globals = new Vector<>();
        Vector<ClassDecl> classes = new Vector<>();
        Vector<FuncDecl> funcs = new Vector<>();

        for(ImportDecl im : this.imports)
            imports.add(im.deepCopy().asTopLevelDecl().asImport());
        for(EnumDecl ed : this.enumDecls)
            enums.add(ed.deepCopy().asTopLevelDecl().asEnumDecl());
        for(GlobalDecl gd : this.globalDecls)
            globals.add(gd.deepCopy().asTopLevelDecl().asGlobalDecl());
        for(ClassDecl cd : this.classDecls)
            classes.add(cd.deepCopy().asTopLevelDecl().asClassDecl());
        for(FuncDecl fd : this.funcDecls)
            funcs.add(fd.deepCopy().asTopLevelDecl().asFuncDecl());

        CompilationBuilder cb = new CompilationBuilder();
        if(this.mainFunc != null)
            cb.setMainFunc(this.mainFunc.deepCopy().asTopLevelDecl().asMainDecl());

        return cb.setMetaData(this)
                 .setImportDecls(imports)
                 .setEnumDecls(enums)
                 .setGlobalDecls(globals)
                 .setClassDecls(classes)
                 .setFuncDecls(funcs)
                 .create();
    }

    @Override
    public void visit(Visitor v) { v.visitCompilation(this); }

    public static class CompilationBuilder extends NodeBuilder {
        private final Compilation c = new Compilation();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return CompilationBuilder
         */
        public CompilationBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public CompilationBuilder setFileName(String fileName) {
            c.fileName = fileName;
            return this;
        }

        public CompilationBuilder setImportDecls(Vector<ImportDecl> id) {
            c.imports = id;
            return this;
        }

        public CompilationBuilder setEnumDecls(Vector<EnumDecl> ed) {
            c.enumDecls = ed;
            return this;
        }

        public CompilationBuilder setGlobalDecls(Vector<GlobalDecl> gd) {
            c.globalDecls = gd;
            return this;
        }

        public CompilationBuilder setClassDecls(Vector<ClassDecl> cd) {
            c.classDecls = cd;
            return this;
        }

        public CompilationBuilder setFuncDecls(Vector<FuncDecl> fd) {
            c.funcDecls = fd;
            return this;
        }

        public CompilationBuilder setMainFunc(MainDecl md) {
            c.mainFunc = md;
            return this;
        }

        public Compilation create() {
            super.saveMetaData(c);
            c.addChild(c.imports);
            c.addChild(c.enumDecls);
            c.addChild(c.globalDecls);
            c.addChild(c.classDecls);
            c.addChild(c.funcDecls);
            c.addChild(c.mainFunc);
            return c;
        }
    }
}
