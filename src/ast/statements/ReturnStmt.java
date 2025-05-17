package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;

public class ReturnStmt extends Statement {

    private final Expression expr;

    public ReturnStmt(Expression e) { this(new Token(),e); }
    public ReturnStmt(Token t, Expression e) {
        super(t);
        this.expr = e;

        addChild(this.expr);
        setParent();
    }

    public Expression expr() { return expr; }

    public boolean isReturnStmt() { return true; }
    public ReturnStmt asReturnStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitReturnStmt(this); }

    public static class ReturnStmtBuilder {
        private Expression expr;

        public ReturnStmtBuilder setReturnExpr(Expression e) {
            this.expr = e;
            return this;
        }

        public ReturnStmt createReturnStmt() { return new ReturnStmt(expr); }

    }
}
