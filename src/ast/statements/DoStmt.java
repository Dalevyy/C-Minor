package ast.statements;

import ast.AST;
import ast.classbody.MethodDecl;
import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class DoStmt extends Statement {

    public SymbolTable symbolTable;

    private BlockStmt doBlock;
    private Expression cond;

    public DoStmt() { this(new Token(),null,null); }
    public DoStmt(Token t, BlockStmt db, Expression c) {
        super(t);
        this.doBlock = db;
        this.cond = c;

        addChild(this.doBlock);
        addChild(this.cond);
        setParent();
    }

    public BlockStmt doBlock() { return doBlock; }
    public Expression condition() { return cond; }

    public boolean isDoStmt() { return true; }
    public DoStmt asDoStmt() { return this; }

    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                doBlock = node.asStatement().asBlockStmt();
                break;
            case 1:
                cond = node.asExpression();
                break;
        }
    }

    @Override
    public AST deepCopy() {
        return new DoStmtBuilder()
                   .setMetaData(this)
                   .setBlockStmt(this.doBlock.deepCopy().asStatement().asBlockStmt())
                   .setCondition(this.cond.deepCopy().asExpression())
                   .setSymbolTable(this.symbolTable)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitDoStmt(this); }

    public static class DoStmtBuilder extends NodeBuilder {
        private final DoStmt ds = new DoStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return DoStmtBuilder
         */
        public DoStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public DoStmtBuilder setBlockStmt(BlockStmt doBlock) {
            ds.doBlock = doBlock;
            return this;
        }

        public DoStmtBuilder setCondition(Expression cond) {
            ds.cond = cond;
            return this;
        }

        public DoStmtBuilder setSymbolTable(SymbolTable st) {
            ds.symbolTable = st;
            return this;
        }

        public DoStmt create() {
            super.saveMetaData(ds);
            ds.addChild(ds.doBlock);
            ds.addChild(ds.cond);
            return ds;
        }
    }
}
