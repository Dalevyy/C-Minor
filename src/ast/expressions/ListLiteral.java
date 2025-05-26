package ast.expressions;

import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class ListLiteral extends Literal {

    private final Vector<Expression> inits;

    public ListLiteral() { this(new Token(),new Vector<>()); }

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
