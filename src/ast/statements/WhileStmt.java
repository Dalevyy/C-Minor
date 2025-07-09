package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class WhileStmt extends Statement {

    public SymbolTable symbolTable;

    private Expression cond;
    private BlockStmt whileBlock;

    public WhileStmt() { this(new Token(),null,null); }
    public WhileStmt(Token t, Expression cond, BlockStmt whileBlock) {
        super(t);
        this.cond = cond;
        this.whileBlock = whileBlock;

        addChild(this.cond);
        addChild(this.whileBlock);
        setParent();
    }

    public Expression condition() { return cond; }
    public BlockStmt whileBlock() { return whileBlock; }

    public boolean isWhileStmt() { return true; }
    public WhileStmt asWhileStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                cond = n.asExpression();
                break;
            case 1:
                whileBlock = n.asStatement().asBlockStmt();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link WhileStmt}
     */
    @Override
    public AST deepCopy() {
        return new WhileStmtBuilder()
                   .setMetaData(this)
                   .setCondition(this.cond.deepCopy().asExpression())
                   .setBlockStmt(this.whileBlock.deepCopy().asStatement().asBlockStmt())
                   .setSymbolTable(this.symbolTable)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitWhileStmt(this); }

    public static class WhileStmtBuilder extends NodeBuilder {
        private final WhileStmt ws = new WhileStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return WhileStmtBuilder
         */
        public WhileStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public WhileStmtBuilder setCondition(Expression cond) {
            ws.cond = cond;
            return this;
        }

        public WhileStmtBuilder setBlockStmt(BlockStmt whileBlock) {
            ws.whileBlock = whileBlock;
            return this;
        }

        public WhileStmtBuilder setSymbolTable(SymbolTable st) {
            ws.symbolTable = st;
            return this;
        }

        public WhileStmt create() {
            super.saveMetaData(ws);
            ws.addChild(ws.cond);
            ws.addChild(ws.whileBlock);
            return ws;
        }
    }
}
