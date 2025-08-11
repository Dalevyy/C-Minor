package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.ScopeDecl;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a while loop.
 * @author Daniel Levy
 */
public class WhileStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the current {@link WhileStmt}.
     */
    public SymbolTable scope;

    /**
     * An {@link Expression} representing the condition to execute the while loop.
     */
    private Expression condition;

    /**
     * A {@link BlockStmt} representing the body of the while loop.
     */
    private BlockStmt body;

    /**
     * Default constructor for {@link WhileStmt}.
     */
    public WhileStmt() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link WhileStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param condition {@link Expression} to store into {@link #condition}.
     * @param body {@link BlockStmt} to store into {@link #body}.
     */
    public WhileStmt(Token metaData, Expression condition, BlockStmt body) {
        super(metaData);

        this.condition = condition;
        this.body = body;

        addChildNode(this.condition);
        addChildNode(this.body);
    }

    /**
     * Getter method for {@link #condition}.
     * @return {@link Expression}
     */
    public Expression getCondition() { return condition; }

    /**
     * Getter method for {@link #body}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getBody() { return body; }

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
    public boolean isWhileStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public WhileStmt asWhileStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                condition = node.asExpression();
                break;
            case 1:
                body = node.asStatement().asBlockStmt();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new WhileStmtBuilder()
                   .setMetaData(this)
                   .setCondition(condition.deepCopy().asExpression())
                   .setBlockStmt(body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitWhileStmt(this); }

    /**
     * Internal class that builds a {@link WhileStmt} object.
     */
    public static class WhileStmtBuilder extends NodeBuilder {

        /**
         * {@link WhileStmt} object we are building.
         */
        private final WhileStmt ws = new WhileStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link WhileStmtBuilder}.
         */
        public WhileStmtBuilder setMetaData(AST node) {
            super.setMetaData(ws, node);
            return this;
        }

        /**
         * Sets the while statement's {@link #condition}.
         * @param condition {@link Expression} representing the condition of the while loop.
         * @return Current instance of {@link WhileStmtBuilder}.
         */
        public WhileStmtBuilder setCondition(Expression condition) {
            ws.condition = condition;
            return this;
        }

        /**
         * Sets the while statement's {@link #body}.
         * @param body {@link BlockStmt} representing the body of the while loop.
         * @return Current instance of {@link WhileStmtBuilder}.
         */
        public WhileStmtBuilder setBlockStmt(BlockStmt body) {
            ws.body = body;
            return this;
        }

        /**
         * Creates a {@link WhileStmt} object.
         * @return {@link WhileStmt}
         */
        public WhileStmt create() {
            ws.addChildNode(ws.condition);
            ws.addChildNode(ws.body);
            return ws;
        }
    }
}
