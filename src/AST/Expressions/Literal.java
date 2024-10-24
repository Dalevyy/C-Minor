package AST.Expressions;

import AST.*;
import Token.*;
import Utilities.PokeVisitor;

// Leaf Node
public class Literal extends Expression {

    public enum ConstantKind { BOOL, INT, CHAR, STR, TEXT, REAL, LIST }

    private ConstantKind kind;

    public Literal(Token t, ConstantKind ck) {
        super(t);
        this.kind = ck;
    }

    public ConstantKind getConstantKind() { return kind; }

    public boolean isLiteral() { return true; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsLiteral(this); }
}
