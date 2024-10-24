package AST.Expressions;

import AST.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

public class ListLiteral extends Literal {

    private Vector<Expression> exprs;

    public ListLiteral(Token t, Vector<Expression> e) {
        super(t,ConstantKind.LIST);
        this.exprs = e;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> getExpressions() { return exprs; }
    public boolean isListLiteral() { return true; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsListLiteral(this); }
}
