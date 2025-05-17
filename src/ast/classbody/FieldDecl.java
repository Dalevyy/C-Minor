package ast.classbody;

import ast.*;
import ast.misc.Modifier;
import ast.misc.Modifiers;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class FieldDecl extends AST implements NameNode {

    public Modifiers mod;

    private final Var var;
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
    public void setType(Type t) { this.type = t; }
    public Type type() { return type; }

    public boolean isFieldDecl() { return true; }
    public FieldDecl asFieldDecl() { return this; }

    @Override
    public String toString() { return var().toString(); }

    @Override
    public void visit(Visitor v) { v.visitFieldDecl(this); }
}
