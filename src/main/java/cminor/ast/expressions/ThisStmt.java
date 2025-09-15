package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link Expression} representing the {@code this} keyword.
 * <p><
 *     This is an internal node class that keeps track of whether a name
 *     is actually referring to a field inside a class. When this is the case,
 *     we have to append the {@code this} keyword to the name and turn it into
 *     a field expression which is done in {@link cminor.micropasses.FieldRewriter}.
 * </p>
 * @author Daniel Levy
 */
public class ThisStmt extends Expression {

    /**
     * Default constructor for {@link ThisStmt}.
     */
    public ThisStmt() { super(new Token()); }

    /**
     * Retrieves the current class the {@link ThisStmt} is found in.
     * @return {@link ClassDecl}
     */
    public ClassDecl getClassDecl() {
        AST node = this;

        while(!node.isTopLevelDecl() || !node.asTopLevelDecl().isClassDecl())
            node = node.getParent();

        return node.asTopLevelDecl().asClassDecl();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isThisStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ThisStmt asThisStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return "this"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A this statement can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ThisStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@inheritDoc}
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
