package ast.expressions;

import ast.AST;
import ast.operators.BinaryOp;
import token.Token;
import utilities.Visitor;

public class BinaryExpr extends Expression {

    private Expression LHS;
    private Expression RHS;
    private BinaryOp op;

    public BinaryExpr(Token t, Expression LHS, Expression RHS, BinaryOp op) {
        super(t);
        this.LHS = LHS;
        this.RHS = RHS;
        this.op = op;

        addChild(this.LHS);
        addChild(this.RHS);
        addChild(this.op);
        setParent();
    }

    public Expression LHS() { return LHS; }
    public Expression RHS() { return RHS; }
    public BinaryOp binaryOp() { return op; }

    public boolean isBinaryExpr() { return true; }
    public BinaryExpr asBinaryExpr() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                LHS = n.asExpression();
                break;
            case 1:
                RHS = n.asExpression();
                break;
            case 2:
                op = n.asOperator().asBinaryOp();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitBinaryExpr(this); }

    public static class BinaryExprBuilder {
        private Expression LHS;
        private Expression RHS;
        private BinaryOp op;

        public BinaryExprBuilder setLHS(Expression LHS) {
            this.LHS = LHS;
            return this;
        }

        public BinaryExprBuilder setRHS(Expression RHS) {
            this.RHS = RHS;
            return this;
        }

        public BinaryExprBuilder setBinaryOp(BinaryOp op) {
            this.op = op;
            return this;
        }

        public BinaryExpr createBinaryExpr() { return new BinaryExpr(new Token(),LHS,RHS,op); }
    }
}
