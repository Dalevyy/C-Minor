package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.operators.BinaryOp;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing a binary expression.
 * <p><br>
 *     In {@link BinaryOp}, there is a list of all valid binary operators
 *     that can form a binary expression in C Minor. Many of the same binary
 *     operations found in other languages are supported in C Minor.
 * </p>
 * @author Daniel Levy
 */
public class BinaryExpr extends Expression {

    /**
     * Expression found on the left-hand side of the {@link BinaryExpr}.
     */
    private Expression LHS;

    /**
     * Expression found on the right-hand side of the {@link BinaryExpr}.
     */
    private Expression RHS;

    /**
     * Binary operation the {@link BinaryExpr} will perform on the {@link #LHS} and {@link #RHS}.
     */
    private BinaryOp binOp;

    /**
     * Default constructor for {@link BinaryExpr}.
     */
    public BinaryExpr() { this(new Token(),null,null,null); }

    /**
     * Main constructor for {@link BinaryExpr}.
     * @param metaData Token containing metadata we want to save
     * @param LHS Expression to save into {@link #LHS}
     * @param RHS Expression to save into {@link #RHS}
     * @param op Binary operator to save into {@link #binOp}
     */
    public BinaryExpr(Token metaData, Expression LHS, Expression RHS, BinaryOp op) {
        super(metaData);
        this.LHS = LHS;
        this.RHS = RHS;
        this.binOp = op;

        addChildNode(this.LHS);
        addChildNode(this.RHS);
        addChildNode(this.binOp);
    }

    /**
     * Getter for {@link #LHS}.
     * @return Expression
     */
    public Expression getLHS() { return LHS; }

    /**
     * Getter for {@link #RHS}.
     * @return Expression
     */
    public Expression getRHS() { return RHS; }

    /**
     * Getter for {@link #binOp}.
     * @return Binary Operator
     */
    public BinaryOp getBinaryOp() { return binOp; }

    /**
     * Setter for {@link #LHS}.
     * @param LHS Expression
     */
    private void setLHS(Expression LHS) { this.LHS = LHS; }

    /**
     * Setter for {@link #RHS}.
     * @param RHS Expression
     */
    private void setRHS(Expression RHS) { this.RHS = RHS; }

    /**
     * Setter for {@link #binOp}.
     * @param binOp Binary Operator
     */
    private void setBinaryOp(BinaryOp binOp) { this.binOp = binOp; }

    /**
     * Checks if the current AST node is a {@link BinaryExpr}.
     * @return Boolean
     */
    public boolean isBinaryExpr() { return true; }

    /**
     * Type cast method for {@link BinaryExpr}
     * @return BinaryExpr
     */
    public BinaryExpr asBinaryExpr() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
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
            case 2:
                binOp = node.asOperator().asBinaryOp();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link BinaryExpr}
     */
    @Override
    public AST deepCopy() {
       return new BinaryExprBuilder()
                  .setMetaData(this)
                  .setLHS(this.getLHS().deepCopy().asExpression())
                  .setRHS(this.getRHS().deepCopy().asExpression())
                  .setBinaryOp(this.getBinaryOp().deepCopy().asOperator().asBinaryOp())
                  .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitBinaryExpr(this); }

    /**
     * Internal class that builds a {@link BinaryExpr} object.
     */
    public static class BinaryExprBuilder extends NodeBuilder {

        /**
         * {@link BinaryExpr} object we are building.
         */
        private final BinaryExpr be = new BinaryExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return BinaryExprBuilder
         */
        public BinaryExprBuilder setMetaData(AST node) {
            super.setMetaData(be,node);
            return this;
        }

        /**
         * Sets the binary expression's {@link #LHS}.
         * @param LHS Expression that appears on the left of the {@link BinaryExpr}
         * @return BinaryExprBuilder
         */
        public BinaryExprBuilder setLHS(Expression LHS) {
            be.setLHS(LHS);
            return this;
        }

        /**
         * Sets the binary expression's {@link #RHS}.
         * @param RHS Expression that appears on the right of the {@link BinaryExpr}
         * @return BinaryExprBuilder
         */
        public BinaryExprBuilder setRHS(Expression RHS) {
            be.setRHS(RHS);
            return this;
        }

        /**
         * Sets the binary expression's {@link #binOp}.
         * @param binOp Binary operation that will be performed on the {@link BinaryExpr}
         * @return BinaryExprBuilder
         */
        public BinaryExprBuilder setBinaryOp(BinaryOp binOp) {
            be.setBinaryOp(binOp);
            return this;
        }

        /**
         * Creates a {@link BinaryExpr} object.
         * @return {@link BinaryExpr}
         */
        public BinaryExpr create() {
            be.addChildNode(be.getLHS());
            be.addChildNode(be.getRHS());
            be.addChildNode(be.getBinaryOp());
            return be;
        }
    }
}
