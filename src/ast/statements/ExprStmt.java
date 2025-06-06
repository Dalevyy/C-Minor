package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;

public class ExprStmt extends Statement {

    private final Expression expr;

    public ExprStmt(Token t, Expression e) {
        super(t);
        this.expr = e;

        addChild(this.expr);
        setParent();
    }

    public Expression getExpression() { return expr; }

    public boolean isExprStmt() { return true; }
    public ExprStmt asExprStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitExprStmt(this); }
}
