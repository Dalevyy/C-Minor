package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Visitor;

public class ReturnStmt extends Statement {

    private Expression expr;

    public ReturnStmt() { this(new Token(),null); }
    public ReturnStmt(Expression e) { this(new Token(),e); }
    public ReturnStmt(Token metaData, Expression e) {
        super(metaData);
        this.expr = e;

        addChild(this.expr);
    }

    public Expression expr() { return expr; }

    public boolean isReturnStmt() { return true; }
    public ReturnStmt asReturnStmt() { return this; }

    @Override
    public void update(int pos, AST node) { expr = node.asExpression(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ReturnStmt}
     */
    @Override
    public AST deepCopy() {
        return new ReturnStmtBuilder()
                   .setMetaData(this)
                   .setReturnExpr(this.expr.deepCopy().asExpression())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitReturnStmt(this); }

    public static class ReturnStmtBuilder extends NodeBuilder {
        private final ReturnStmt rs = new ReturnStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ReturnStmtBuilder
         */
        public ReturnStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public ReturnStmtBuilder setReturnExpr(Expression e) {
            rs.expr = e;
            return this;
        }

        public ReturnStmt create() {
            super.saveMetaData(rs);
            rs.addChild(rs.expr);
            return rs;
        }

    }
}
