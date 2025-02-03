package ast.expressions;

import token.Token;
import utilities.Visitor;

public class Endl extends Expression {

    public Endl(Token t) { super(t); }

    public boolean isEndl() { return true; }
    public Endl asEndl() { return this; }

    @Override
    public void visit(Visitor v) { v.visitEndl(this); }
}
