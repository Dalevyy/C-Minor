package ast;

import ast.expressions.*;
import ast.types.*;
import token.*;
import utilities.Visitor;


/*
___________________________ Var ___________________________
A Var node is an internal representation of any variable in
C Minor. A Var is composed of 3 parts:
    1. A name representing the Var
    2. An optional expression that the Var is set to
    3. An optional type denoting what the type of the Var is
___________________________________________________________
*/
public class Var extends AST {

    private Name name;
    private Expression init;
    private Type type;

    public Var(Token t, Name name) { this(t,name,null,null); }
    public Var(Token t, Name name, Expression init) { this(t,name,null,init); }
    public Var(Token t, Name name, Type type) { this(t,name,type,null); }

    public Var(Token t, Name name, Type type, Expression init) {
        super(t);
        this.name = name;
        this.type = type;
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
    public void setInit(Expression e) { this.init = e; }

    @Override
    public String toString() { return this.name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitVar(this); }
}
