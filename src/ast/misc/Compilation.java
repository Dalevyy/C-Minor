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

    public SymbolTable globalTable;

    private final Vector<Import> imports;
    private final Vector<EnumDecl> enumDecls;
    private final Vector<GlobalDecl> globalDecls;
    private final Vector<ClassDecl> classDecls;
    private final Vector<FuncDecl> funcDecls;
    private final MainDecl mainFunc;

    public Compilation() {
        this.globalTable = new SymbolTable();
        this.imports = new Vector<>();
        this.enumDecls = new Vector<>();
        this.globalDecls = new Vector<>();
        this.classDecls = new Vector<>();
        this.funcDecls = new Vector<>();
        this.mainFunc = new MainDecl();
    }

    public Compilation(Token t, Vector<Import> ims, Vector<EnumDecl> ed, Vector<GlobalDecl> gd,
                       Vector<ClassDecl> cd, Vector<FuncDecl> fd, MainDecl m) {
        super(t);
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
        setParent();
    }

    public Vector<EnumDecl> enumDecls() { return enumDecls; }
    public Vector<GlobalDecl> globalDecls() { return globalDecls; }
    public Vector<ClassDecl> classDecls() { return classDecls; }
    public Vector<FuncDecl> funcDecls() { return funcDecls; }
    public MainDecl mainDecl() { return mainFunc; }

    public void mergeCompilationUnit(Compilation c) {
        globalTable.setParent(c.globalTable);
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

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A compilation unit can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitCompilation(this); }
}
