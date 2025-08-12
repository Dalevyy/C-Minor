package cminor.ast.statements;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} representing the {@code stop} keyword.
 * <p>
 *     The {@code stop} keyword is used when a user wishes to terminate the
 *     current execution of a C Minor program.
 * </p>
 * @author Daniel Levy
 */
public class StopStmt extends Statement {

    /**
     * Default constructor for {@link StopStmt}.
     */
    public StopStmt() { this(new Token()); }

    /**
     * Main constructor for {@link StopStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public StopStmt(Token metaData) { super(metaData); }

    /**
     * {@inheritDoc}
     */
    public boolean isStopStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public StopStmt asStopStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A stop statement can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new StopStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitStopStmt(this); }

    /**
     * Internal class that builds a {@link StopStmt} object.
     */
    public static class StopStmtBuilder extends NodeBuilder {

        /**
         * {@link StopStmt} object we are building.
         */
        private final StopStmt ss = new StopStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link StopStmtBuilder}.
         */
        public StopStmtBuilder setMetaData(AST node) {
            super.setMetaData(ss, node);
            return this;
        }

        /**
         * Creates a {@link StopStmt} object.
         * @return {@link StopStmt}
         */
        public StopStmt create() {
            return ss;
        }
    }
}
