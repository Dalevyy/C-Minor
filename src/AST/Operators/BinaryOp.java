package AST.Operators;

import AST.*;
import Token.*;
import Utilities.PokeVisitor;

// Leaf node
public class BinaryOp extends Operator {

    public static enum BinaryType { EQEQ, NEQ, GT, GTEQ, LT, LTEQ, LTGT,
                              UFO, PLUS, MINUS, MULT, DIV, MOD, EXP, SLEFT, SRIGHT, INOF, BAND, XOR, BOR, AND, OR }
    public static String[] names = { "==", "!=", ">", ">=", "<", "<=", "<>", "<=>", "+", "-", "*", "/", "%", "**", "<<", ">>",
                                     "instanceof", "&", "^", "|", "and", "or"};

    private BinaryType bOp;

    public BinaryOp(Token t, BinaryType op) {
        super(t);
        this.bOp = op;
    }

    public BinaryType getBinaryOp() { return bOp; }
    public boolean isBinaryOp() { return true; }

    @Override
    public String toString() { return names[bOp.ordinal()]; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsBinaryOp(this); }
}
