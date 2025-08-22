package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node that represents a return statement.
 * @author Daniel Levy
 */
public class ReturnStmt extends Statement {

    /**
     * An {@link Expression} representing the value that is returned (if applicable).
     */
    private Expression value;

    /**
     * Default constructor for {@link ReturnStmt}.
     */
    public ReturnStmt() { this(new Token(),null); }

    /**
     * Main constructor for {@link ReturnStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param value {@link Expression} to store into {@link #value}.
     */
    public ReturnStmt(Token metaData, Expression value) {
        super(metaData);

        this.value = value;

        addChildNode(this.value);
    }

    /**
     * Getter method for {@link #value}.
     * @return {@link Expression}
     */
    public Expression getReturnValue() { return value; }

    /**
     * Finds the location in which the current {@link ReturnStmt} is written in.
     * <p>
     *     We will either return a {@link cminor.ast.topleveldecls.FuncDecl}, {@link cminor.ast.classbody.MethodDecl},
     *     or a {@link cminor.ast.topleveldecls.MainDecl} depending on where the return statement was written.
     * </p>
     * @return An {@link AST} node representing one of the three constructs above or {@code null} if the construct is
     * not found in any of the three {@link AST nodes}.
     */
    public AST getFunctionLocation() {
        AST node = this;

        while(node != null) {
            if(node.isTopLevelDecl())
                if(node.asTopLevelDecl().isFuncDecl() || node.asTopLevelDecl().isMainDecl())
                    return node;
            if(node.isClassNode() && node.asClassNode().isMethodDecl())
                return node;

            node = node.getParent();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReturnStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ReturnStmt asReturnStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { value = node.asExpression(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        ReturnStmtBuilder rb = new ReturnStmtBuilder();

        if(value != null)
            rb.setReturnExpr(value.deepCopy().asExpression());

        return rb.setMetaData(this)
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitReturnStmt(this); }

    /**
     * Internal class that builds a {@link ReturnStmt} object.
     */
    public static class ReturnStmtBuilder extends NodeBuilder {

        /**
         * {@link ReturnStmt} object we are building.
         */
        private final ReturnStmt rs = new ReturnStmt();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ReturnStmtBuilder}.
         */
        public ReturnStmtBuilder setMetaData(AST node) {
            super.setMetaData(rs, node);
            return this;
        }

        /**
         * Sets the return statement's {@link #value}.
         * @param value {@link Expression} representing the value returned (if needed)
         * @return Current instance of {@link ReturnStmtBuilder}.
         */
        public ReturnStmtBuilder setReturnExpr(Expression value) {
            rs.value = value;
            return this;
        }

        /**
         * Creates a {@link ReturnStmt} object.
         * @return {@link ReturnStmt}
         */
        public ReturnStmt create() {
            rs.addChildNode(rs.value);
            return rs;
        }

    }
}
