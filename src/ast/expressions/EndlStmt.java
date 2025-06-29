package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the {@code endl} keyword.
 * @author Daniel Levy
 */
public class EndlStmt extends Expression {

    /**
     * Default constructor for {@link EndlStmt}.
     */
    public EndlStmt() { super(new Token()); }

    /**
     * Main constructor for {@link EndlStmt}.
     * @param metaData Token containing metadata we want to save
     */
    public EndlStmt(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is an {@link EndlStmt}.
     * @return Boolean
     */
    public boolean isEndl() { return true; }

    /**
     * Type cast method for {@link EndlStmt}
     * @return EndlStmt
     */
    public EndlStmt asEndl() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("An endl expression can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link EndlStmt}
     */
    @Override
    public AST deepCopy() {
        return new EndlStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitEndl(this); }

    /**
     * Internal class that builds an {@link EndlStmt} object.
     */
    public static class EndlStmtBuilder extends NodeBuilder {

        /**
         * {@link EndlStmt} object we are building.
         */
        private final EndlStmt es = new EndlStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return EndlStmtBuilder
         */
        public EndlStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Creates a {@link EndlStmt} object.
         * @return {@link EndlStmt}
         */
        public EndlStmt create() {
            super.saveMetaData(es);
            return es;
        }
    }
}
