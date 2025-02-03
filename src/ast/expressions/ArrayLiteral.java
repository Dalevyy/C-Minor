package ast.expressions;

import ast.*;
import token.*;
import utilities.Visitor;

public class ArrayLiteral extends Expression {

    private Vector<Expression> exprs;
    private Vector<Expression> args;

    public ArrayLiteral(Token t, Vector<Expression> e, Vector<Expression> a) {
        super(t);
        this.exprs = e;
        this.args = a;

        addChild(this.exprs);
        addChild(this.args);
        setParent();
    }

    public Vector<Expression> arrayExprs() { return exprs; }
    public Vector<Expression> arrayArgs() { return args; }

    public boolean isArrayLiteral() { return true; }
    public ArrayLiteral asArrayLiteral() { return this; }

    public void evaluate() {}

    @Override
    public void visit(Visitor v) { v.visitArrayLiteral(this); }
}
