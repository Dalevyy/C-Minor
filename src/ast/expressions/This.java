package ast.expressions;

import token.Token;
import utilities.Visitor;

/**
 * Internal class
 */
public class This extends Expression {

    public This() { super(new Token()); }

    @Override
    public void visit(Visitor v) { v.visitThis(this); }
}
