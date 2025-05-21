package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class DoStmt extends Statement {

    public SymbolTable symbolTable;

    private final BlockStmt doBlock;
    private final Expression cond;

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
    public void visit(Visitor v) { v.visitDoStmt(this); }
}
