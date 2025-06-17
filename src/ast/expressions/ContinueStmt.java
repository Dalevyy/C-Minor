package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

public class ContinueStmt extends Expression {

    public ContinueStmt(Token t) { super(t); }

    public boolean isContinueStmt() { return true; }
    public ContinueStmt asContinueStmt() { return this; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A continue statement can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitContinueStmt(this); }
}

