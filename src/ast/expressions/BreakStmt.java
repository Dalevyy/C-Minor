package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

public class BreakStmt extends Expression {

    public BreakStmt(Token t) { super(t); }

    public boolean isBreakStmt() { return true; }
    public BreakStmt asBreakStmt() { return this; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A break statement can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitBreakStmt(this); }
}
