package ast.expressions;

import ast.expressions.This;
import ast.misc.Name;
import token.Token;
import utilities.Visitor;

public class NameExpr extends Expression {

    private final Name name;
    private This thisObj;

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

    public void setNameInClass() {
        thisObj = new This();
        this.children.add(0,thisObj);
    }

    public This getThis() { return thisObj; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitNameExpr(this); }
}
