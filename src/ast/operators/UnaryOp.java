package ast.operators;

import token.*;
import utilities.Visitor;

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
    public UnaryOp asUnaryOp() { return this; }

    @Override
    public String toString() { return names[uOp.ordinal()];}

    @Override
    public void visit(Visitor v) { v.visitUnaryOp(this); }
}
