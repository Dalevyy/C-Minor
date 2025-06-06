package ast.statements;

import ast.expressions.*;
import ast.operators.LoopOp;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class ForStmt extends Statement {

    public SymbolTable symbolTable;

    private final LocalDecl loopControlVar;
    private final Expression LHS;
    private final Expression RHS;
    private final LoopOp lOp;
    private final BlockStmt body;

    public ForStmt(Token t, LocalDecl ld, Expression LHS, Expression RHS, LoopOp lOp, BlockStmt b) {
        super(t);
        this.loopControlVar = ld;
        this.LHS = LHS;
        this.RHS = RHS;
        this.lOp = lOp;
        this.body = b;

        addChild(this.loopControlVar);
        addChild(this.LHS);
        addChild(this.RHS);
        addChild(this.lOp);
        addChild(this.body);
        setParent();
    }

    public LocalDecl loopVar() { return this.loopControlVar; }
    public Expression condLHS() { return this.LHS; }
    public Expression condRHS() { return this.RHS; }
    public LoopOp loopOp() { return this.lOp; }
    public BlockStmt forBlock() { return body; }

    public boolean isForStmt() { return true; }
    public ForStmt asForStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitForStmt(this); }
}
