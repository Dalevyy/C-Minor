package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing the {@code this} keyword.
 * <p><br>
 *     This is an internal node class that keeps track of whether or
 *     not a name is actually referring to a field inside of a class.
 *     When this is the case, we have to append the {@code this} keyword
 *     to the name and turn it into a field expression which is done in
 *     {@link micropasses.FieldRewrite}.
 * </p>
 * @author Daniel Levy
 */
public class ThisStmt extends Expression {

    /**
     * Default constructor for {@link ThisStmt}.
     */
    public ThisStmt() { super(new Token()); }

    /**
     * Checks if the current AST node is a {@link ThisStmt}.
     * @return Boolean
     */
    public boolean isThisStmt() { return true; }

    /**
     * Type cast method for {@link ThisStmt}
     * @return ThisStmt
     */
    public ThisStmt asThisStmt() { return this; }

    /**
     * {@code toString} method.
     * @return String that represents "this"
     */
    @Override
    public String toString() { return "this"; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A this statement can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ThisStmt}
     */
    @Override
    public AST deepCopy() {
        return new ThisStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitThis(this); }

    /**
     * Internal class that builds a {@link ThisStmt} object.
     */
    public static class ThisStmtBuilder extends NodeBuilder {

        /**
         * {@link ThisStmt} object we are building.
         */
        private final ThisStmt ts = new ThisStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ThisStmtBuilder
         */
        public ThisStmtBuilder setMetaData(AST node) {
            super.setMetaData(ts,node);
            return this;
        }

        /**
         * Creates a {@link ThisStmt} object.
         * @return {@link ThisStmt}
         */
        public ThisStmt create() {
            return ts;
        }
    }
}
