package ast.expressions;

import token.*;
import utilities.Vector;
import utilities.Visitor;

public class InStmt extends Expression {

    private Vector<Expression> exprs;

    public InStmt(Token t, Vector<Expression> e) {
        super(t);
        this.exprs = e;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> inExprs() { return exprs; }
    public void setInExprs(Vector<Expression> exprs) { this.exprs = exprs; }

    public boolean isInStmt() { return true; }
    public InStmt asInStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitInStmt(this); }
}
