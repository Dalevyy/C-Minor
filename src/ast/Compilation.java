package ast;

import ast.top_level_decls.*;
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

    private final Vector<EnumDecl> enumDecls;
    private final Vector<GlobalDecl> globalDecls;
    private final Vector<ClassDecl> classDecls;
    private final Vector<FuncDecl> funcDecls;
    private final MainDecl mainFunc;

    public Compilation() {
        this.globalTable = new SymbolTable();
        this.enumDecls = new Vector<>();
        this.globalDecls = new Vector<>();
        this.classDecls = new Vector<>();
        this.funcDecls = new Vector<>();
        this.mainFunc = new MainDecl();
    }

    public Compilation(Token t, Vector<EnumDecl> ed, Vector<GlobalDecl> gd,
                       Vector<ClassDecl> cd, Vector<FuncDecl> fd, MainDecl m) {
        super(t);
        enumDecls = ed;
        globalDecls = gd;
        classDecls = cd;
        funcDecls = fd;
        mainFunc = m;

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

    public void addEnumDecl(EnumDecl ed) { this.enumDecls.add(ed); }
    public void addGlobalDecl(Vector<GlobalDecl> gd) { this.globalDecls.merge(gd); }
    public void addClassDecl(ClassDecl cd) { this.classDecls.add(cd); }
    public void addFuncDecl(FuncDecl fd) { this.funcDecls.add(fd); }

    public boolean isCompilation() { return true; }
    public Compilation asCompilation() { return this; }

    @Override
    public void visit(Visitor v) { v.visitCompilation(this); }
}
