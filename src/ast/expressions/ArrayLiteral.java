package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * Array Literal
 */
public class ArrayLiteral extends Literal {

    private final Vector<Expression> dims;
    private final Vector<Expression> inits;

    public final int numOfDims;

    public ArrayLiteral() { this(new Token(),new Vector<>(),new Vector<>()); }

    public ArrayLiteral(Token t, Vector<Expression> de, Vector<Expression> al) {
        super(t,ConstantKind.ARR);
        this.dims = de;
        this.inits = al;

        if(de.isEmpty())
            this.numOfDims = 1;
        else
            this.numOfDims = de.size();

        addChild(this.dims);
        addChild(this.inits);
        setParent();
    }

    public Vector<Expression> arrayDims() { return dims; }
    public Vector<Expression> arrayInits() { return inits; }

    public boolean isArrayLiteral() { return true; }
    public ArrayLiteral asArrayLiteral() { return this; }

    @Override
    public void update(int pos, AST n) {
        if(pos < dims.size()) {
            dims.remove(pos);
            dims.add(pos,n.asExpression());
        }
        else {
            pos -= inits.size();
            inits.remove(pos);
            inits.add(pos,n.asExpression());
        }
    }

    @Override
    public void visit(Visitor v) { v.visitArrayLiteral(this); }
}
