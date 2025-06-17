package ast.expressions;

import ast.AST;
import ast.types.*;
import token.*;
import utilities.Visitor;

// Something to do with constant values
public class CastExpr extends Expression {

    private Type targetType;
    private Expression exprToCast;

    public CastExpr(Token t, Type tt, Expression e) {
        super(t);
        this.targetType = tt;
        this.exprToCast = e;

        addChild(this.targetType);
        addChild(this.exprToCast);
        setParent();
    }

    public Type castType() { return targetType; }
    public Expression castExpr() { return exprToCast; }

    public boolean isCastExpr() { return true; }
    public CastExpr asCastExpr() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                targetType = n.asType();
                break;
            case 1:
                exprToCast = n.asExpression();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitCastExpr(this); }
}
