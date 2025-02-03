package ast.statements;

import token.*;
import utilities.Visitor;

// Leaf Node
public class StopStmt extends Statement {

    public StopStmt(Token t) { super(t); }

    public boolean isStopStmt() { return true; }
    public StopStmt asStopStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitStopStmt(this); }
}
