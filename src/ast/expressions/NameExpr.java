package ast.expressions;

import ast.*;
import token.*;
import utilities.Visitor;

public class NameExpr extends Expression {

    private final Name name;

    public NameExpr(String s) { this(new Token(),new Name(s)); }
    public NameExpr(Name n) { this(new Token(),n); }
    public NameExpr(Token t, Name n) {
        super(t);
        this.name = n;

        addChild(this.name);
        setParent();
    }

    public Name getName() { return name; }
    public boolean isNameExpr() { return true; }
    public NameExpr asNameExpr() { return this; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitNameExpr(this); }
}
