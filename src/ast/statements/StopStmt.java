package ast.statements;

import ast.AST;
import token.*;
import utilities.Visitor;

// Leaf Node
public class StopStmt extends Statement {

    public StopStmt(Token t) { super(t); }

    public boolean isStopStmt() { return true; }
    public StopStmt asStopStmt() { return this; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A stop statement can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitStopStmt(this); }
}
