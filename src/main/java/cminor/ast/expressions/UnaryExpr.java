package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.operators.UnaryOp;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing a unary expression.
 * <p><br>
 *     In {@link UnaryOp}, there is a list of all valid unary operators
 *     that can form a unary expression in C Minor. There are only two
 *     such operators.
 * </p>
 * @author Daniel Levy
 */
public class UnaryExpr extends Expression {

    /**
     * Expression that will be operated on.
     */
    private Expression expr;

    /**
     * Unary operation that will be performed on {@link #expr}.
     */
    private UnaryOp unOp;

    /**
     * Default constructor for {@link UnaryExpr}.
     */
    public UnaryExpr() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link UnaryExpr}.
     * @param metaData Token containing metadata we want to save
     * @param expr Expression to save into {@link #expr}
     * @param unOp Unary operator to save into {@link #unOp}
     */
    public UnaryExpr(Token metaData, Expression expr, UnaryOp unOp) {
        super(metaData);
        this.expr = expr;
        this.unOp = unOp;

        addChildNode(this.expr);
        addChildNode(this.unOp);
    }

    /**
     * Getter for {@link #expr}.
     * @return Expression
     */
    public Expression getExpr() { return expr; }

    /**
     * Getter for {@link #unOp}.
     * @return Unary Operator
     */
    public UnaryOp getUnaryOp() { return unOp; }

    /**
     * Setter for {@link #expr}.
     * @param expr Expression
     */
    private void setExpr(Expression expr) { this.expr = expr; }

    /**
     * Setter for {@link #unOp}.
     * @param unOp Unary Operator
     */
    private void setUnaryOp(UnaryOp unOp) { this.unOp = unOp; }

    /**
     * Checks if the current AST node is a {@link UnaryExpr}.
     * @return Boolean
     */
    public boolean isUnaryExpr() { return true; }

    /**
     * Type cast method for {@link UnaryExpr}.
     * @return UnaryExpr
     */
    public UnaryExpr asUnaryExpr() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                this.expr = node.asExpression();
                break;
            case 1:
                this.unOp = node.asOperator().asUnaryOp();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link UnaryExpr}
     */
    @Override
    public AST deepCopy() {
        return new UnaryExprBuilder()
                   .setMetaData(this)
                   .setExpr(this.expr.deepCopy().asExpression())
                   .setUnaryOp(this.unOp.deepCopy().asOperator().asUnaryOp())
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitUnaryExpr(this); }

    /**
     * Internal class that builds a {@link UnaryExpr} object.
     */
    public static class UnaryExprBuilder extends NodeBuilder {

        /**
         * {@link UnaryExpr} object we are building.
         */
        private final UnaryExpr ue = new UnaryExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return UnaryExprBuilder
         */
        public UnaryExprBuilder setMetaData(AST node) {
            super.setMetaData(ue,node);
            return this;
        }

        /**
         * Sets the unary expression's {@link #expr}.
         * @param expr Expression that will be operated on.
         * @return UnaryExprBuilder
         */
        public UnaryExprBuilder setExpr(Expression expr) {
            ue.setExpr(expr);
            return this;
        }

        /**
         * Sets the unary expression's {@link #unOp}.
         * @param unOp Unary operation that will be performed on the {@link UnaryExpr}
         * @return UnaryExprBuilder
         */
        public UnaryExprBuilder setUnaryOp(UnaryOp unOp) {
            ue.setUnaryOp(unOp);
            return this;
        }

        /**
         * Creates a {@link UnaryExpr} object.
         * @return {@link UnaryExpr}
         */
        public UnaryExpr create() {
            ue.addChildNode(ue.expr);
            ue.addChildNode(ue.unOp);
            return ue;
        }
    }
}
