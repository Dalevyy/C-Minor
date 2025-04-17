package ast.expressions;

import ast.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

public class ArrayLiteral extends Literal {

    private Vector<Expression> dimExprs;
    private Vector<Expression> inits;

    public final int numOfDims;

    public ArrayLiteral() { this(new Token(),new Vector<>(),new Vector<>()); }

    public ArrayLiteral(Token t, Vector<Expression> de, Vector<Expression> al) {
        super(t,ConstantKind.ARR);
        this.dimExprs = de;
        this.inits = al;

        if(de.size() == 0) { this.numOfDims = 1; }
        else { this.numOfDims = de.size(); }

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
