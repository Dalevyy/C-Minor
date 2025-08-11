package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.ScopeDecl;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a do while loop.
 * @author Daniel Levy
 */
public class DoStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the current {@link DoStmt}.
     */
    public SymbolTable scope;

    /**
     * The {@link BlockStmt} representing the loop's body.
     */
    private BlockStmt body;

    /**
     * An {@link Expression} representing the conditional expression to execute the loop.
     */
    private Expression condition;

    /**
     * Default constructor for {@link DoStmt}.
     */
    public DoStmt() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link DoStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param body {@link BlockStmt} to store into {@link #body}.
     * @param condition {@link Expression} to store into {@link #condition}.
     */
    public DoStmt(Token metaData, BlockStmt body, Expression condition) {
        super(metaData);

        this.body = body;
        this.condition = condition;

        addChildNode(this.body);
        addChildNode(this.condition);
    }

    /**
     * Getter method for {@link #body}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getBody() { return body; }

    /**
     * Getter method for {@link #condition}.
     * @return {@link Expression}
     */
    public Expression getCondition() { return condition; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable newScope) { scope = (scope == null) ? newScope : scope; }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() { return scope; }

    /**
     * {@inheritDoc}
     */
    public boolean isDoStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public DoStmt asDoStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                body = node.asStatement().asBlockStmt();
                break;
            case 1:
                condition = node.asExpression();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new DoStmtBuilder()
                   .setMetaData(this)
                   .setBody(body.deepCopy().asStatement().asBlockStmt())
                   .setCondition(condition.deepCopy().asExpression())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitDoStmt(this); }

    /**
     * Internal class that builds a {@link DoStmt} object.
     */
    public static class DoStmtBuilder extends NodeBuilder {

        /**
         * {@link DoStmt} object we are building.
         */
        private final DoStmt ds = new DoStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link DoStmtBuilder}.
         */
        public DoStmtBuilder setMetaData(AST node) {
            super.setMetaData(ds, node);
            return this;
        }

        /**
         * Sets the do statement's {@link #body}.
         * @param body {@link BlockStmt} representing the body of the do statement.
         * @return Current instance of {@link DoStmtBuilder}.
         */
        public DoStmtBuilder setBody(BlockStmt body) {
            ds.body = body;
            return this;
        }

        /**
         * Sets the do statement's {@link #condition}.
         * @param condition {@link Expression} representing the condition to execute the do statement.
         * @return Current instance of {@link DoStmtBuilder}.
         */
        public DoStmtBuilder setCondition(Expression condition) {
            ds.condition = condition;
            return this;
        }

        /**
         * Creates a {@link DoStmt} object.
         * @return {@link DoStmt}
         */
        public DoStmt create() {
            ds.addChildNode(ds.body);
            ds.addChildNode(ds.condition);
            return ds;
        }
    }
}
