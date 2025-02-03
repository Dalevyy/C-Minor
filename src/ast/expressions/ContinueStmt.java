package ast.expressions;

import token.Token;
import utilities.Visitor;

public class ContinueStmt extends Expression {

    public ContinueStmt(Token t) { super(t); }

    public boolean isContinueStmt() { return true; }
    public ContinueStmt asContinueStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitContinueStmt(this); }
}

