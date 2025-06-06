package ast.classbody;

import ast.*;
import token.*;
import utilities.*;

public class ClassBody extends AST {

    private final Vector<FieldDecl> fieldDecls;
    private final Vector<MethodDecl> methodDecls;

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
