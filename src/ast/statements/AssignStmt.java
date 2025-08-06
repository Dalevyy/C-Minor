package ast.statements;

import ast.AST;
import ast.expressions.*;
import ast.operators.AssignOp;
import ast.operators.AssignOp.AssignType;
import ast.types.ArrayType;
import token.*;
import utilities.Visitor;

public class AssignStmt extends Statement {

    private Expression LHS;
    private Expression RHS;
    private AssignOp op;

    public AssignStmt() { this(new Token(),null,null,null); }
    public AssignStmt(Token t, Expression LHS, Expression RHS, AssignOp op) {
        super(t);
        this.LHS = LHS;
        this.RHS = RHS;
        this.op = op;

        addChildNode(this.LHS);
        addChildNode(this.RHS);
        addChildNode(this.op);
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

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link AssignStmt}
     */
    @Override
    public AST deepCopy() {
        return new AssignStmtBuilder()
                   .setMetaData(this)
                   .setLHS(this.LHS().deepCopy().asExpression())
                   .setRHS(this.RHS().deepCopy().asExpression())
                   .setAssignOp(this.op.deepCopy().asOperator().asAssignOp())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.
            visitAssignStmt(this); }

    public static class AssignStmtBuilder extends NodeBuilder {
        private final AssignStmt as = new AssignStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return AssignStmtBuilder
         */
        public AssignStmtBuilder setMetaData(AST node) {
            super.setMetaData(as,node);
            return this;
        }

        public AssignStmtBuilder setLHS(Expression LHS) {
            as.LHS = LHS;
            return this;
        }

        public AssignStmtBuilder setRHS(Expression RHS) {
            as.RHS = RHS;
            return this;
        }

        public AssignStmtBuilder setAssignOp(AssignOp op) {
            as.op = op;
            return this;
        }

        public AssignStmt create() {
            as.addChildNode(as.LHS);
            as.addChildNode(as.RHS);
            as.addChildNode(as.op);
            return as;
        }
    }
}
