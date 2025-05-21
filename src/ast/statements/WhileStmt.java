package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class WhileStmt extends Statement {

    public SymbolTable symbolTable;

    private final Expression cond;
    private final BlockStmt whileBlock;

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
    public void visit(Visitor v) { v.visitWhileStmt(this); }
}
