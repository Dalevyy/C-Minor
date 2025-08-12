package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing a field expression.
 * <p><br>
 *     In C Minor, field expressions are written in the same way
 *     as other languages such as C++. ThisStmt class will also work
 *     with complex field expressions, and the AST will grow to the
 *     right as the {@link #accessExpr} will contain smaller {@link FieldExpr}'s.
 * </p>
 * @author Daniel Levy
 */
public class FieldExpr extends Expression {

    /**
     * The object that accesses a field or method.
     */
    private Expression target;

    /**
     * The expression the {@link #target} accesses.
     */
    private Expression accessExpr;

    /**
     * Flag indicating if the current {@link FieldExpr} checks if its object is null or not.
     */
    private boolean isNullCheck;

    /**
     * Flag indicating if the current {@link FieldExpr} represents a method invocation.
     */
    private boolean isInvocation;

    /**
     * Default constructor for {@link FieldExpr}.
     */
    public FieldExpr() { this(new Token(),null,null,false); }

    /**
     * Main constructor for {@link FieldExpr}.
     * @param metaData Token containing metadata we want to save
     * @param target Expression to save into {@link #target}
     * @param accessExpr Expression to save into {@link #accessExpr}
     * @param nullCheck Boolean to save into {@link #isNullCheck}
     */
    public FieldExpr(Token metaData, Expression target, Expression accessExpr, boolean nullCheck) {
        super(metaData);
        this.target = target;
        this.accessExpr = accessExpr;
        this.isNullCheck = nullCheck;
        if(this.accessExpr != null)
         this.isInvocation = this.accessExpr.isInvocation();

        addChildNode(this.target);
        addChildNode(this.accessExpr);
    }

    /**
     * Checks if the {@link #target} will have a null check performed on it.
     * @return Boolean
     */
    public boolean isNullCheck() { return isNullCheck; }

    /**
     * Checks if the current {@link FieldExpr} invokes a method.
     * @return Boolean
     */
    public boolean isMethodInvocation() { return isInvocation; }

    /**
     * Getter for {@link #target}.
     * @return Expression
     */
    public Expression getTarget() { return this.target; }

    /**
     * Getter for {@link #accessExpr}.
     * @return Expression
     */
    public Expression getAccessExpr() { return this.accessExpr; }

    /**
     * Returns the final field that the current expression will access.
     * @return String
     */
    public String getFieldName() {
        Expression fe = this;
        while(fe.isFieldExpr())
            fe = fe.asFieldExpr().getAccessExpr();
        return fe.toString();
    }

    /**
     * Setter for {@link #target}.
     * @param target Expression
     */
    private void setTarget(Expression target) { this.target = target; }

    /**
     * Setter for {@link #accessExpr}.
     * @param accessExpr Expression
     */
    private void setAccessExpr(Expression accessExpr) {
        this.accessExpr = accessExpr;
        this.isInvocation = this.accessExpr.isInvocation();
    }

    /**
     * Setter for {@link #isNullCheck}
     */
    private void setNullCheck() { this.isNullCheck = true; }

    /**
     * Checks if the current AST node is a {@link FieldExpr}.
     * @return Boolean
     */
    public boolean isFieldExpr() { return true; }

    /**
     * Type cast method for {@link FieldExpr}
     * @return FieldExpr
     */
    public FieldExpr asFieldExpr() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the name of the field/method being accessed.
     */
    @Override
    public String toString() { return accessExpr.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                target = node.asExpression();
                break;
            case 1:
                accessExpr = node.asExpression();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link FieldExpr}
     */
    @Override
    public AST deepCopy() {
        FieldExprBuilder feb = new FieldExprBuilder();

        if(this.isNullCheck)
            feb.setNullCheck();

        return feb.setMetaData(this)
                  .setTarget(this.target.deepCopy().asExpression())
                  .setAccessExpr(this.accessExpr.deepCopy().asExpression())
                  .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitFieldExpr(this); }

    /**
     * Internal class that builds a {@link FieldExpr} object.
     */
    public static class FieldExprBuilder extends NodeBuilder {

        /**
         * {@link FieldExpr} object we are building.
         */
        private final FieldExpr fe = new FieldExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return FieldExprBuilder
         */
        public FieldExprBuilder setMetaData(AST node) {
            super.setMetaData(fe,node);
            return this;
        }

        /**
         * Sets the field expression's {@link #target}.
         * @param target Expression that represents some object
         * @return FieldExprBuilder
         */
        public FieldExprBuilder setTarget(Expression target) {
            fe.setTarget(target);
            return this;
        }

        /**
         * Sets the field expression's {@link #accessExpr}.
         * @param accessExpr Expression that represents what field/method the {@link #target} accesses
         * @return FieldExprBuilder
         */
        public FieldExprBuilder setAccessExpr(Expression accessExpr) {
            fe.setAccessExpr(accessExpr);
            return this;
        }

        /**
         * Sets the field expression to represent a null check.
         * @return FieldExprBuilder
         */
        public FieldExprBuilder setNullCheck() {
            fe.setNullCheck();
            return this;
        }

        /**
         * Creates a {@link FieldExpr} object.
         * @return {@link FieldExpr}
         */
        public FieldExpr create() {
            fe.addChildNode(fe.target);
            fe.addChildNode(fe.accessExpr);
            return fe;
        }
    }
}
