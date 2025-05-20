package ast.expressions;

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
    public void visit(Visitor v) { v.visitThis(this); }
}
