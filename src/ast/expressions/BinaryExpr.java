package ast.expressions;

import ast.operators.*;
import token.*;
import utilities.Visitor;

public class BinaryExpr extends Expression {

    private final Expression LHS;
    private final Expression RHS;
    private final BinaryOp op;

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
