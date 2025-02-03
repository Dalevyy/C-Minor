package ast.expressions;

import token.Token;
import utilities.Visitor;

public class BreakStmt extends Expression {

    public BreakStmt(Token t) { super(t); }

    @Override
    public void evaluate() { }

    public boolean isBreakStmt() { return true; }
    public BreakStmt asBreakStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitBreakStmt(this); }
}
