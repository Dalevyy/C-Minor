package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * Internal class
 */
public class This extends Expression {

    public This() { super(new Token()); }

    public boolean isThis() { return true; }
    public This asThis() { return this; }

    @Override
    public String toString() { return "this"; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A this statement can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitThis(this); }
}
