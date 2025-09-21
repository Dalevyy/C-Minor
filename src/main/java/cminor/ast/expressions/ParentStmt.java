package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.topleveldecls.ClassDecl;
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
     * Checks if the {@link ParentStmt} was written inside a {@link FieldExpr}.
     * @return {@code True} if the {@code parent} keyword is contained in a field expression, {@code False} otherwise.
     */
    public boolean insideFieldExpr() {
        return parent != null && (parent.isExpression() && parent.asExpression().isFieldExpr());
    }

    /**
     * Checks if the parent keyword can properly be evaluated.
     * <p>
     *     By proper, we are referring to the fact that the 'parent' keyword
     *     should always be found at the start of a field expression!
     * </p>
     * @return {@code True} if the parent keyword was written correctly, {@code False} otherwise.
     */
    public boolean wasParentKeywordWrittenCorrectly() {
        AST node = parent;

        // If there is another field expression above the current one, then
        // the 'parent' keyword was not used at the start of the field expression!
        if(node.getParent().isExpression() && node.getParent().asExpression().isFieldExpr())
            return false;

        // If the field expression was written as '<...>.parent', this is not allowed!
        return !node.asExpression().asFieldExpr().getAccessExpr().isParentStmt();
    }

    /**
     * Returns the {@link ClassDecl} the current {@link ParentStmt} is in.
     * <p>
     *     This method should only be called by the {@link cminor.typechecker.TypeChecker},
     *     so we may correctly assign the right type to the 'parent' keyword!
     * </p>
     * @return The {@link ClassDecl} the 'parent' keyword is used in.
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
