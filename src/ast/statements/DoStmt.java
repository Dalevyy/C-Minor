package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class DoStmt extends Statement {

    public SymbolTable symbolTable;

    private BlockStmt doBlock;
    private Expression cond;

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
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                doBlock = n.asStatement().asBlockStmt();
                break;
            case 1:
                cond = n.asExpression();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitDoStmt(this); }
}
