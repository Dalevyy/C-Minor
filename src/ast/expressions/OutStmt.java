package ast.expressions;

import token.*;
import utilities.Vector;
import utilities.Visitor;

public class OutStmt extends Expression {

    private Vector<Expression> exprs;

    public OutStmt(Token t, Vector<Expression> e) {
        super(t);
        this.exprs = e;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> outExprs() { return exprs; }
    public void setOutExprs(Vector<Expression> exprs) { this.exprs = exprs; }

    public boolean isOutStmt() { return true; }
    public OutStmt asOutStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitOutStmt(this); }
}
