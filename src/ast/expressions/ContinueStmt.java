package ast.expressions;

import ast.AST;
import ast.types.VoidType;
import token.Token;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the {@code continue} keyword.
 * @author Daniel Levy
 */
public class ContinueStmt extends Expression {

    /**
     * Default constructor for {@link ContinueStmt}.
     */
    public ContinueStmt() { this(new Token()); }

    /**
     * Main constructor for {@link ContinueStmt}.
     * @param metaData Token containing metadata we want to save
     */
    public ContinueStmt(Token metaData) {
        super(metaData);
        this.type = new VoidType();
    }

    /**
     * Checks if the current AST node is a {@link ContinueStmt}.
     * @return Boolean
     */
    public boolean isContinueStmt() { return true; }

    /**
     * Type cast method for {@link ContinueStmt}
     * @return ContinueStmt
     */
    public ContinueStmt asContinueStmt() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A continue statement can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ContinueStmt}
     */
    @Override
    public AST deepCopy() {
        return new ContinueStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitContinueStmt(this); }

    /**
     * Internal class that builds a {@link ContinueStmt} object.
     */
    public static class ContinueStmtBuilder extends NodeBuilder {

        /**
         * {@link ContinueStmt} object we are building.
         */
        private final ContinueStmt cs = new ContinueStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ContinueStmtBuilder
         */
        public ContinueStmtBuilder setMetaData(AST node) {
            super.setMetaData(cs,node);
            return this;
        }

        /**
         * Creates a {@link ContinueStmt} object.
         * @return {@link ContinueStmt}
         */
        public ContinueStmt create() {
            return cs;
        }
    }
}

