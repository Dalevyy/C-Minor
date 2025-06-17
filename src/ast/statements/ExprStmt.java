package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Visitor;

public class ExprStmt extends Statement {

    private Expression expr;

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
    public void update(int pos, AST n) {
        expr = n.asExpression();
    }

    @Override
    public void visit(Visitor v) { v.visitExprStmt(this); }
}
