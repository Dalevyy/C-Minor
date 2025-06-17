package ast.statements;

import ast.AST;
import ast.expressions.*;
import ast.operators.AssignOp;
import ast.operators.AssignOp.AssignType;
import token.*;
import utilities.Visitor;

public class AssignStmt extends Statement {

    private Expression LHS;
    private Expression RHS;
    private AssignOp op;

    public AssignStmt(Expression LHS, Expression RHS, AssignOp op) { this(new Token(),LHS,RHS,op,false); }
    public AssignStmt(Token t, Expression LHS, Expression RHS, AssignOp op) { this(t,LHS,RHS,op,false); }
    public AssignStmt(Token t, Expression LHS, Expression RHS, AssignOp op, boolean rt) {
        super(t);
        this.LHS = LHS;
        this.RHS = RHS;
        this.op = op;

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
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                LHS = n.asExpression();
                break;
            case 1:
                RHS = n.asExpression();
                break;
            case 2:
                op = n.asOperator().asAssignOp();
                break;
        }
    }


    @Override
    public void visit(Visitor v) { v.
            visitAssignStmt(this); }

    public static class AssignStmtBuilder {
        private Expression LHS;
        private Expression RHS;
        private AssignOp op;

        public AssignStmtBuilder setLHS(Expression LHS) {
            this.LHS = LHS;
            return this;
        }

        public AssignStmtBuilder setRHS(Expression RHS) {
            this.RHS = RHS;
            return this;
        }

        public AssignStmtBuilder setAssignOp(AssignType op) {
            this.op = new AssignOp(op);
            return this;
        }

        public AssignStmt createAssignStmt() { return new AssignStmt(LHS,RHS,op); }
    }
}
