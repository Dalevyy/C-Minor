package ast.statements;

import ast.AST;
import ast.expressions.Expression;
import ast.operators.AssignOp;
import token.Token;
import utilities.Visitor;

/**
 * A {@link Statement} representing an assignment.
 * <p>
 *     In C Minor, an assignment is denoted by the keyword {@code set} or {@code retype}.
 * </p>
 * @author Daniel Levy
 */
public class AssignStmt extends Statement {

    /**
     * An {@link Expression} representing the LHS. This should be a variable we want to assign a new value to.
     */
    protected Expression LHS;

    /**
     * An {@link Expression} representing the RHS. This should be a value we want to assign to the {@link #LHS}.
     */
    protected Expression RHS;

    /**
     * The {@link AssignOp} we will need to perform.
     */
    protected AssignOp op;

    /**
     * Default constructor for {@link AssignStmt}.
     */
    public AssignStmt() { this(new Token(),null,null,null); }

    /**
     * Main constructor for {@link AssignStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param LHS {@link Expression} to store into {@link #LHS}.
     * @param RHS {@link Expression} to store into {@link #RHS}.
     * @param op {@link AssignOp} to store into {@link #op}.
     */
    public AssignStmt(Token metaData, Expression LHS, Expression RHS, AssignOp op) {
        super(metaData);

        this.LHS = LHS;
        this.RHS = RHS;
        this.op = op;

        addChildNode(this.LHS);
        addChildNode(this.RHS);
    }

    /**
     * Getter method for {@link #LHS}.
     * @return {@link Expression}
     */
    public Expression getLHS() { return this.LHS; }

    /**
     * Getter method for {@link #RHS}.
     * @return {@link Expression}
     */
    public Expression getRHS() { return this.RHS; }

    /**
     * Getter method for {@link #op}.
     * @return {@link AssignOp}
     */
    public AssignOp getOperator() { return this.op; }

    /**
     * {@inheritDoc}
     */
    public boolean isAssignStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public AssignStmt asAssignStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                LHS = node.asExpression();
                break;
            case 1:
                RHS = node.asExpression();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new AssignStmtBuilder()
                   .setMetaData(this)
                   .setLHS(LHS.deepCopy().asExpression())
                   .setRHS(RHS.deepCopy().asExpression())
                   .setAssignOp(op.deepCopy().asOperator().asAssignOp())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.
            visitAssignStmt(this); }

    /**
     * Internal class that builds an {@link AssignStmt} object.
     */
    public static class AssignStmtBuilder extends NodeBuilder {

        /**
         * {@link AssignStmt} object we are building.
         */
        private final AssignStmt as = new AssignStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link AssignStmtBuilder}.
         */
        public AssignStmtBuilder setMetaData(AST node) {
            super.setMetaData(as,node);
            return this;
        }

        /**
         * Sets the assignment's {@link #LHS}.
         * @param LHS {@link Expression} representing the LHS of the assignment.
         * @return Current instance of {@link AssignStmtBuilder}.
         */
        public AssignStmtBuilder setLHS(Expression LHS) {
            as.LHS = LHS;
            return this;
        }

        /**
         * Sets the assignment's {@link #RHS}.
         * @param RHS {@link Expression} representing the RHS of the assignment.
         * @return Current instance of {@link AssignStmtBuilder}.
         */
        public AssignStmtBuilder setRHS(Expression RHS) {
            as.RHS = RHS;
            return this;
        }

        /**
         * Sets the assignment's {@link #op}.
         * @param op {@link AssignOp} representing the operation the assignment will perform.
         * @return Current instance of {@link AssignStmtBuilder}.
         */
        public AssignStmtBuilder setAssignOp(AssignOp op) {
            as.op = op;
            return this;
        }

        /**
         * Creates an {@link AssignStmt} object.
         * @return {@link AssignStmt}
         */
        public AssignStmt create() {
            as.addChildNode(as.LHS);
            as.addChildNode(as.RHS);
            return as;
        }
    }
}
