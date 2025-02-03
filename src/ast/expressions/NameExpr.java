package ast.expressions;

import ast.*;
import token.*;
import utilities.Visitor;

public class NameExpr extends Expression {

    private Name name;

    public NameExpr(Token t, Name n) {
        super(t);
        this.name = n;

        addChild(this.name);
        setParent();
    }

    public Name getName() { return name; }
    public boolean isNameExpr() { return true; }
    public NameExpr asNameExpr() { return this; }

    public void evaluate() {}

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitNameExpr(this); }
}
