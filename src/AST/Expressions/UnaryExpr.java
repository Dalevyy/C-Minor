package AST.Expressions;

import AST.*;
import AST.Operators.*;
import Token.*;
import Utilities.PokeVisitor;

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

    public Expression getExpr() { return expr; }
    public UnaryOp getUnaryOp() { return op; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsUnaryExpr(this); }
}
