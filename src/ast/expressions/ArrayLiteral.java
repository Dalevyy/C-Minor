package ast.expressions;

import ast.*;
import token.*;
import utilities.Visitor;

public class ArrayLiteral extends Expression {

    private Vector<Expression> dimExprs;
    private Vector<Expression> inits;

    public ArrayLiteral(Token t, Vector<Expression> e, Vector<Expression> a) {
        super(t);
        this.dimExprs = e;
        this.inits = a;

        addChild(this.dimExprs);
        addChild(this.inits);
        setParent();
    }

    public Vector<Expression> arrayDims() { return dimExprs; }
    public Vector<Expression> arrayInits() { return inits; }

    public boolean isArrayLiteral() { return true; }
    public ArrayLiteral asArrayLiteral() { return this; }

    @Override
    public void visit(Visitor v) { v.visitArrayLiteral(this); }
}
