package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Visitor;

public class ExprStmt extends Statement {

    private Expression expr;

    public ExprStmt() { this(new Token(),null); }
    public ExprStmt(Token t, Expression e) {
        super(t);
        this.expr = e;

        addChildNode(this.expr);
    }

    public Expression getExpression() { return expr; }

    public boolean isExprStmt() { return true; }
    public ExprStmt asExprStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        expr = n.asExpression();
    }

    @Override
    public AST deepCopy() {
        return new ExprStmtBuilder()
                   .setMetaData(this)
                   .setExpression(this.expr.deepCopy().asExpression())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitExprStmt(this); }

    public static class ExprStmtBuilder extends NodeBuilder {
        private final ExprStmt es = new ExprStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ExprStmtBuilder
         */
        public ExprStmtBuilder setMetaData(AST node) {
            super.setMetaData(es,node);
            return this;
        }

        public ExprStmtBuilder setExpression(Expression expr) {
            es.expr = expr;
            return this;
        }

        public ExprStmt create(){
            es.addChildNode(es.expr);
            return es;
        }
    }
}
