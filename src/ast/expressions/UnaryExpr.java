package ast.expressions;

import ast.AST;
import ast.operators.*;
import token.*;
import utilities.Visitor;

public class UnaryExpr extends Expression {

    private Expression expr;
    private UnaryOp op;

    public UnaryExpr(Token t, Expression expr, UnaryOp op) {
        super(t);
        this.expr = expr;
        this.op = op;

        addChild(this.expr);
        addChild(this.op);
        setParent();
    }

    public Expression expr() { return expr; }
    public UnaryOp unaryOp() { return op; }

    public boolean isUnaryExpr() { return true; }
    public UnaryExpr asUnaryExpr() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                expr = n.asExpression();
                break;
            case 1:
                op = n.asOperator().asUnaryOp();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitUnaryExpr(this); }
}
