package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} representing an expression.
 * <p>
 *     This is an internal class for the parser to handle all expressions
 *     that are not written inside of a {@link Statement}. This type will
 *     technically never be seen.
 * </p>
 * @author Daniel Levy
 */
public class ExprStmt extends Statement {

    /**
     * The {@link Expression} that the statement represents.
     */
    private Expression expr;

    /**
     * Default constructor for {@link ExprStmt}.
     */
    public ExprStmt() { this(new Token(),null); }

    public ExprStmt(Expression expr) { this(new Token(),expr); }

    /**
     * Main constructor for {@link ExprStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param expr {@link Expression} to store into {@link #expr}.
     */
    public ExprStmt(Token metaData, Expression expr) {
        super(metaData);

        this.expr = expr;

        addChildNode(this.expr);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExprStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ExprStmt asExprStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { expr = node.asExpression(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ExprStmtBuilder()
                   .setMetaData(this)
                   .setExpression(expr.deepCopy().asExpression())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitExprStmt(this); }

    /**
     * Internal class that builds an {@link ExprStmt} object.
     */
    public static class ExprStmtBuilder extends NodeBuilder {

        /**
         * {@link ExprStmt} object we are building.
         */
        private final ExprStmt es = new ExprStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ExprStmtBuilder}.
         */
        public ExprStmtBuilder setMetaData(AST node) {
            super.setMetaData(es,node);
            return this;
        }

        /**
         * Sets the expression statement's {@link #expr}.
         * @param expr {@link Expression} that the expression statement is representing.
         * @return Current instance of {@link ExprStmtBuilder}.
         */
        public ExprStmtBuilder setExpression(Expression expr) {
            es.expr = expr;
            return this;
        }

        /**
         * Creates an {@link ExprStmt} object.
         * @return {@link ExprStmt}
         */
        public ExprStmt create(){
            es.addChildNode(es.expr);
            return es;
        }
    }
}
