package AST.Expressions;

import AST.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

public class InStmt extends Expression {

    private Vector<Expression> exprs;

    public InStmt(Token t, Vector<Expression> e) {
        super(t);
        this.exprs = e;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> getInExprs() { return exprs; }

    public boolean isInStmt() { return true; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsInStmt(this); }
}
