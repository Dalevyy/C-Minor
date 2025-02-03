package ast.expressions;

import ast.types.*;
import token.*;
import utilities.Visitor;

// Something to do with constant values
public class CastExpr extends Expression {

    private Type targetType;
    private Expression expr;

    public CastExpr(Token t, Type tt, Expression e) {
        super(t);
        this.targetType = tt;
        this.expr = e;

        addChild(this.targetType);
        addChild(this.expr);
        setParent();
    }

    public Type castType() { return targetType; }
    public Expression castExpr() { return expr; }

    public boolean isCastExpr() { return true; }
    public CastExpr asCastExpr() { return this; }

    @Override
    public void visit(Visitor v) { v.visitCastExpr(this); }
}
