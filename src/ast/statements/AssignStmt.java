package ast.statements;

import ast.expressions.*;
import ast.operators.*;
import token.*;
import utilities.Visitor;

public class AssignStmt extends Statement {

    private final Expression LHS;
    private final Expression RHS;
    private final AssignOp op;

    public boolean retyped;

    public AssignStmt(Expression LHS, Expression RHS, AssignOp op) { this(new Token(),LHS,RHS,op,false); }
    public AssignStmt(Token t, Expression LHS, Expression RHS, AssignOp op) { this(t,LHS,RHS,op,false); }
    public AssignStmt(Token t, Expression LHS, Expression RHS, AssignOp op, boolean retyped) {
        super(t);
        this.LHS = LHS;
        this.RHS = RHS;
        this.op = op;
        this.retyped = retyped;

        addChild(this.LHS);
        addChild(this.RHS);
        addChild(this.op);
        setParent();
    }

    public Expression LHS() { return this.LHS; }
    public Expression RHS() { return this.RHS; }
    public AssignOp assignOp() { return this.op; }

    public boolean isAssignStmt() { return true; }
    public AssignStmt asAssignStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitAssignStmt(this); }
}
