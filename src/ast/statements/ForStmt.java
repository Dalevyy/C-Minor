package ast.statements;

import ast.AST;
import ast.expressions.*;
import ast.operators.LoopOp;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class ForStmt extends Statement {

    public SymbolTable symbolTable;

    private LocalDecl loopControlVar;
    private Expression LHS;
    private Expression RHS;
    private LoopOp lOp;
    private BlockStmt body;

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
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                loopControlVar = n.asStatement().asLocalDecl();
                break;
            case 1:
                LHS = n.asExpression();
                break;
            case 2:
                RHS = n.asExpression();
                break;
            case 3:
                lOp = n.asOperator().asLoopOp();
                break;
            case 4:
                body = n.asStatement().asBlockStmt();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitForStmt(this); }
}
