package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class DoStmt extends Statement {

    public SymbolTable symbolTable;

    private final BlockStmt doBlock;
    private final Statement nextExpr;
    private final Expression cond;

    public DoStmt(Token t, BlockStmt db, Expression c) { this(t,db,null,c); }

    public DoStmt(Token t, BlockStmt db, Statement ne, Expression c) {
        super(t);
        this.doBlock = db;
        this.nextExpr = ne;
        this.cond = c;

        addChild(this.doBlock);
        addChild(this.nextExpr);
        addChild(this.cond);
        setParent();
    }

    public BlockStmt doBlock() { return doBlock; }
    public Statement nextExpr() { return nextExpr; }
    public Expression condition() { return cond; }

    public boolean isDoStmt() { return true; }
    public DoStmt asDoStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitDoStmt(this); }
}
