package ast;

import ast.expressions.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

/*
 +-----------------------------------------------------------+
 +                            Var                            +
 +-----------------------------------------------------------+

A Var is composed of 3 parts:
    1. A name representing the Var
    2. An optional expression that the Var is set to
    3. An optional type denoting what the type of the Var is

Parent Node: Compilation
*/
public class Var extends AST {

    private Name name;
    private Expression init;
    private Type type;

    public Var(Token t, Name name) { this(t,name,null); }

    public Var(Token t, Name name, Expression init) {
        super(t);
        this.name = name;
        this.init = init;

        addChild(this.name);
        addChild(this.init);
        setParent();
    }

    public Name name() { return name; }
    public Expression init() { return init;}
    public Type type() { return type; }

    public boolean isVar() { return true; }
    public Var asVar() { return this; }

    public void setType(Type t) { this.type = t; }

    @Override
    public String toString() { return this.name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitVar(this); }
}
