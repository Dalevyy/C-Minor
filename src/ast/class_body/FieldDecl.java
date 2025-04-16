package ast.class_body;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class FieldDecl extends AST implements NameNode {

    public Modifiers mod;

    private Var var;
    private Type type;

    public FieldDecl(Token t, Modifier m, Var v, Type type) {
        super(t);
        this.mod = new Modifiers(m);
        this.var = v;
        this.type = type;

        addChild(this.var);
        addChild(this.type);
        setParent();
    }

    public AST decl() { return this; }

    public Var var() { return var; }
    public Type type() { return type; }

    public boolean isFieldDecl() { return true; }
    public FieldDecl asFieldDecl() { return this; }

    @Override
    public String toString() { return var().toString(); }

    @Override
    public void visit(Visitor v) { v.visitFieldDecl(this); }
}
