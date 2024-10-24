package AST;

import AST.Expressions.*;
import AST.Types.*;
import Token.*;
import Utilities.PokeVisitor;

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
    }

    public String getID() { return name.getName(); }
    public Name getNameNode() { return name; }
    public Expression getInit() { return init;}
    public Type getType() { return type; }

    public Var asVar() { return this; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsVar(this); }
}
