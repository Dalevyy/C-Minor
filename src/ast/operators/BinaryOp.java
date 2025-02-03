package ast.operators;

import token.*;
import utilities.Visitor;

// Leaf node
public class BinaryOp extends Operator {

    public static enum BinaryType { EQEQ, NEQ, GT, GTEQ, LT, LTEQ, LTGT,
                              UFO, PLUS, MINUS, MULT, DIV, MOD, EXP, SLEFT, SRIGHT, INOF, NINOF, AS, BAND, XOR, BOR, AND, OR }
    public static String[] names = { "==", "!=", ">", ">=", "<", "<=", "<>", "<=>", "+", "-", "*", "/", "%", "**", "<<", ">>",
                                     "instanceof", "!instanceof", "as?", "&", "^", "|", "and", "or"};

    private BinaryType bOp;

    public BinaryOp(Token t, BinaryType op) {
        super(t);
        this.bOp = op;
    }

    public BinaryType getBinaryType() { return bOp; }
    public boolean isBinaryOp() { return true; }
    public BinaryOp asBinaryOp() { return this; }

    @Override
    public String toString() { return names[bOp.ordinal()]; }

    @Override
    public void visit(Visitor v) { v.visitBinaryOp(this); }
}
