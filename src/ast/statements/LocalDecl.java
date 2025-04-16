package ast.statements;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class LocalDecl extends Statement implements NameNode {

    private Var myVar;
    private Type type;

    public LocalDecl(Token t, Var v, Type type) {
        super(t);
        this.myVar = v;
        this.type = type;

        addChild(this.myVar);
        addChild(this.type);
        setParent();
    }

    public Var var() { return myVar; }

    public Type type() { return type; }
    public String toString() { return myVar.toString(); }

    public AST decl() { return this; }

    public boolean isLocalDecl() { return true; }
    public LocalDecl asLocalDecl() { return this; }

    @Override
    public void visit(Visitor v) { v.visitLocalDecl(this); }
}
