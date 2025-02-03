package ast.expressions;

import ast.operators.*;
import token.*;
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
    public void visit(Visitor v) { v.visitBinaryExpr(this); }
}
