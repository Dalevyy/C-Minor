package ast;

import ast.top_level_decls.*;
import token.*;
import utilities.Visitor;

public class Compilation extends AST {

    private Vector<EnumDecl> enumDecls;
    private Vector<GlobalDecl> globalDecls;
    private Vector<ClassDecl> classDecls;
    private Vector<FuncDecl> funcDecls;
    private MainDecl mainFunc;

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

    public boolean isCompilation() { return true; }
    public Compilation asCompilation() { return this; }

    @Override
    public void visit(Visitor v) { v.visitCompilation(this); }
}
