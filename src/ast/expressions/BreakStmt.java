package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the {@code break} keyword.
 * @author Daniel Levy
 */
public class BreakStmt extends Expression {

    /**
     * Default constructor for {@link BreakStmt}.
     */
    public BreakStmt() { this(new Token()); }

    /**
     * Main constructor for {@link BreakStmt}.
     * @param metaData Token containing metadata we want to save
     */
    public BreakStmt(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is a {@link BreakStmt}.
     * @return Boolean
     */
    public boolean isBreakStmt() { return true; }

    /**
     * Type cast method for {@link BreakStmt}
     * @return BreakStmt
     */
    public BreakStmt asBreakStmt() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A break statement can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link BreakStmt}
     */
    @Override
    public AST deepCopy() {
        return new BreakStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitBreakStmt(this); }

    /**
     * Internal class that builds a {@link BreakStmt} object.
     */
    public static class BreakStmtBuilder extends NodeBuilder {

        /**
         * {@link BreakStmt} object we are building.
         */
        private final BreakStmt bs = new BreakStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return BreakStmtBuilder
         */
        public BreakStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Creates a {@link BreakStmt} object.
         * @return {@link BreakStmt }
         */
        public BreakStmt create() {
            super.saveMetaData(bs);
            return bs;
        }
    }
}
