package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

public class Endl extends Expression {

    public Endl(Token t) { super(t); }

    public boolean isEndl() { return true; }
    public Endl asEndl() { return this; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("An endl expression can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitEndl(this); }
}
