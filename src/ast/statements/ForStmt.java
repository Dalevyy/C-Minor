package ast.statements;

import ast.AST;
import ast.expressions.*;
import ast.operators.LoopOp;
import token.*;
import utilities.Vector;
import utilities.Visitor;
import utilities.SymbolTable;

public class ForStmt extends Statement {

    public SymbolTable symbolTable;

    private LocalDecl loopControlVar;
    private Expression LHS;
    private Expression RHS;
    private LoopOp lOp;
    private BlockStmt body;

    public ForStmt(){this(new Token(),null,null,null,null,null); }
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
    }

    public LocalDecl loopVar() { return this.loopControlVar; }
    public Expression condLHS() { return this.LHS; }
    public Expression condRHS() { return this.RHS; }
    public LoopOp loopOp() { return this.lOp; }
    public BlockStmt forBlock() { return body; }

    public boolean isForStmt() { return true; }
    public ForStmt asForStmt() { return this; }

    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                loopControlVar = node.asStatement().asLocalDecl();
                break;
            case 1:
                LHS = node.asExpression();
                break;
            case 2:
                RHS = node.asExpression();
                break;
            case 3:
                lOp = node.asOperator().asLoopOp();
                break;
            case 4:
                body = node.asStatement().asBlockStmt();
                break;
        }
    }

    @Override
    public AST deepCopy() {
        return new ForStmtBuilder()
                   .setMetaData(this)
                   .setLocalVar(this.loopControlVar.deepCopy().asStatement().asLocalDecl())
                   .setLHS(this.LHS.deepCopy().asExpression())
                   .setRHS(this.RHS.deepCopy().asExpression())
                   .setLoopOp(this.lOp.deepCopy().asOperator().asLoopOp())
                   .setLoopBlock(this.body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitForStmt(this); }

    public static class ForStmtBuilder extends NodeBuilder {
        private final ForStmt fs = new ForStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ForStmtBuilder
         */
        public ForStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public ForStmtBuilder setLocalVar(LocalDecl ld) {
            fs.loopControlVar = ld;
            return this;
        }

        public ForStmtBuilder setLHS(Expression LHS) {
            fs.LHS = LHS;
            return this;
        }

        public ForStmtBuilder setRHS(Expression RHS) {
            fs.RHS = RHS;
            return this;
        }

        public ForStmtBuilder setLoopOp(LoopOp LoOp) {
            fs.lOp = LoOp;
            return this;
        }

        public ForStmtBuilder setLoopBlock(BlockStmt bs) {
            fs.body = bs;
            return this;
        }

        public ForStmt create() {
            super.saveMetaData(fs);
            fs.addChild(fs.loopControlVar);
            fs.addChild(fs.LHS);
            fs.addChild(fs.RHS);
            fs.addChild(fs.lOp);
            fs.addChild(fs.body);
            return fs;
        }
    }
}
