package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class WhileStmt extends Statement {

    public SymbolTable symbolTable;

    private Expression cond;
    private Statement nextExpr;
    private BlockStmt whileBlock;

    public WhileStmt(Token t, Expression cond, BlockStmt whileBlock) { this(t,cond,null,whileBlock); }

    public WhileStmt(Token t, Expression cond, Statement nextExpr, BlockStmt whileBlock) {
        super(t);
        this.cond = cond;
        this.nextExpr = nextExpr;
        this.whileBlock = whileBlock;

        addChild(this.cond);
        addChild(this.nextExpr);
        addChild(this.whileBlock);
        setParent();
    }

    public Expression condition() { return cond; }
    public Statement nextExpr() { return nextExpr; }
    public BlockStmt whileBlock() { return whileBlock; }

    public boolean isWhileStmt() { return true; }
    public WhileStmt asWhileStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitWhileStmt(this); }
}
