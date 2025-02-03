package ast.expressions;

import ast.Vector;
import token.*;
import utilities.Visitor;

public class ListLiteral extends Literal {

    private Vector<Expression> exprs;

    public ListLiteral(Token t, Vector<Expression> e) {
        super(t,ConstantKind.LIST);
        this.exprs = e;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> exprs() { return exprs; }

    public boolean isListLiteral() { return true; }
    public ListLiteral asListLiteral() { return this; }

    public void evaluate() {}

    @Override
    public void visit(Visitor v) { v.visitListLiteral(this); }
}
