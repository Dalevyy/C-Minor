package ast.class_body;

import ast.*;
import token.*;
import utilities.*;

public class ClassBody extends AST {

    Vector<FieldDecl> fieldDecls;
    Vector<MethodDecl> methodDecls;

    public ClassBody(Token t, Vector<FieldDecl> dd, Vector<MethodDecl> md) {
        super(t);
        this.fieldDecls = dd;
        this.methodDecls = md;

        addChild(this.fieldDecls);
        addChild(this.methodDecls);
        setParent();
    }

    public Vector<FieldDecl> fieldDecls() { return fieldDecls; }
    public Vector<MethodDecl> methodDecls() { return methodDecls; }

    public boolean isClassBody() { return true; }
    public ClassBody asClassBody() { return this; }

    @Override
    public void visit(Visitor v) { v.visitClassBody(this); }
}
