package AST.Operators;

import AST.*;
import Token.*;
import Utilities.PokeVisitor;

// Leaf Node
public class UnaryOp extends Operator {

    public static enum UnaryType { NEGATE, NOT }
    public static String[] names = { "~", "not" };

    private UnaryType uOp;

    public UnaryOp(Token t, UnaryType uo) {
        super(t);
        this.uOp = uo;
    }

    public UnaryType getUnaryOp() { return uOp; }
    public boolean isUnaryOp() { return false; }

    @Override
    public String toString() { return names[uOp.ordinal()];}

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsUnaryOp(this); }
}
