package ast.operators;

import token.*;
import utilities.Vector;
import utilities.Visitor;

// Leaf node
public class AssignOp extends Operator {

    public static enum AssignType { EQ, PLUSEQ, MINUSEQ, MULTEQ, DIVEQ, MODEQ, EXPEQ }
    private static Vector<String> names = new Vector<>(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "**="});

    private AssignType myOp;

    public AssignOp(AssignType op) { this(new Token(),op); }
    public AssignOp(Token t, AssignType op) {
        super(t);
        this.myOp = op;
    }

    public AssignType getAssignOp() { return myOp; }
    public boolean isAssignOp() { return true; }
    public AssignOp asAssignOp() { return this; }

    @Override
    public String toString() { return names.get(myOp.ordinal()); }

    @Override
    public void visit(Visitor v) { v.visitAssignOp(this); }
}
