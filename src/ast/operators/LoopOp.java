package ast.operators;

import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class LoopOp extends Operator {

    public enum LoopType { INCL, EXCL, EXCL_L, EXCL_R }
    private static Vector<String> names = new Vector<>(new String[]{"..", "<..<", "<..", "..<"});

    private LoopType op;

    public LoopOp(Token t, LoopType lt) {
        super(t);
        this.op = lt;
    }

    public LoopType loopType() { return op; }
    public boolean isLoopOp() { return true; }
    public LoopOp asLoopOp() { return this; }

    @Override
    public String toString() { return names.get(op.ordinal()); }

    @Override
    public void visit(Visitor v) { v.visitLoopOp(this); }
}
