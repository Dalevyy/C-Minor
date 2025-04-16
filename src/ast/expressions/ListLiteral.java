package ast.expressions;

import ast.Vector;
import token.*;
import utilities.Visitor;

public class ListLiteral extends Literal {

    private Vector<Expression> inits;

    public ListLiteral(Token t, Vector<Expression> e) {
        super(t,ConstantKind.LIST);
        this.inits = e;

        addChild(this.inits);
        setParent();
    }

    public Vector<Expression> inits() { return inits; }

    public boolean isListLiteral() { return true; }
    public ListLiteral asListLiteral() { return this; }

    @Override
    public void visit(Visitor v) { v.visitListLiteral(this); }
}
