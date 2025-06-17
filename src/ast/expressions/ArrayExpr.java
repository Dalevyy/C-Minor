package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class ArrayExpr extends Expression {

    private Expression target;
    private final Vector<Expression> index;

    public ArrayExpr(Token t, Expression target, Vector<Expression> i) {
        super(t);
        this.target = target;
        this.index = i;

        addChild(this.target);
        addChild(this.index);
        setParent();
    }

    public Expression arrayTarget() { return target; }
    public Vector<Expression> arrayIndex() { return index; }

    public boolean isArrayExpr() { return true; }
    public ArrayExpr asArrayExpr() { return this; }

    @Override
    public String toString() { return target.toString(); }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                target = n.asExpression();
                break;
            default:
                index.remove(pos-1);
                index.add(pos-1,n.asExpression());
        }
    }

    @Override
    public void visit(Visitor v) { v.visitArrayExpr(this); }
}
