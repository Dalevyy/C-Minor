package ast.expressions;

import token.*;
import utilities.Visitor;

public class ArrayExpr extends Expression {

    private Expression target;
    private Expression index;

    public ArrayExpr(Token t, Expression target, Expression i) {
        super(t);
        this.target = target;
        this.index = i;

        addChild(this.target);
        addChild(this.index);
        setParent();
    }

    public Expression arrayTarget() { return target; }
    public Expression arrayIndex() { return index; }

    public boolean isArrayExpr() { return true; }
    public ArrayExpr asArrayExpr() { return this; }

    @Override
    public void visit(Visitor v) { v.visitArrayExpr(this); }
}
