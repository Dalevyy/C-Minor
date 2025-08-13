package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link Expression} representing the {@code parent} keyword.
 * @author Daniel Levy
 */
public class ParentStmt extends Expression {

    /**
     * Default constructor for {@link ParentStmt}.
     */
    public ParentStmt() { this(new Token()); }

    /**
     * Main constructor for {@link ParentStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public ParentStmt(Token metaData) { super(metaData); }

    /**
     * {@inheritDoc}
     */
    public boolean isParentStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ParentStmt asParentStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(int pos, AST newNode) {
        throw new RuntimeException("A parent statement can not be updated.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ParentStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitParentStmt(this); }

    /**
     * Internal class that builds an {@link ParentStmt} object.
     */
    public static class ParentStmtBuilder extends NodeBuilder {

        /**
         * {@link ParentStmt} object we are building.
         */
        private final ParentStmt ps = new ParentStmt();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ParentStmtBuilder}.
         */
        public ParentStmtBuilder setMetaData(AST node) {
            super.setMetaData(ps,node);
            return this;
        }

        /**
         * Creates a {@link ParentStmt} object.
         * @return {@link ParentStmt}
         */
        public ParentStmt create() {
            return ps;
        }
    }
}
