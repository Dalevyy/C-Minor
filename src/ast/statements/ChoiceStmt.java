package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Vector;

import utilities.Visitor;
import utilities.SymbolTable;

public class ChoiceStmt extends Statement {

    public SymbolTable symbolTable;

    private Expression expr;
    private final Vector<CaseStmt> caseStmts;
    private BlockStmt otherBlock;

    public ChoiceStmt(Token t, Expression e, BlockStmt b) { this(t,e,new Vector<>(),b); }

    public ChoiceStmt(Token t, Expression e, Vector<CaseStmt> cs, BlockStmt b) {
        super(t);
        this.expr = e;
        this.caseStmts = cs;
        this.otherBlock = b;

        addChildNode(this.expr);
        addChildNode(this.caseStmts);
        addChildNode(this.otherBlock);
    }

    public Expression choiceExpr() { return expr; }
    public Vector<CaseStmt> caseStmts() { return caseStmts; }
    public BlockStmt otherBlock() { return otherBlock; }

    public boolean isChoiceStmt() { return true; }
    public ChoiceStmt asChoiceStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                expr = n.asExpression();
                break;
            default:
                if(pos <= caseStmts.size()) {
                    caseStmts.remove(pos-1);
                    caseStmts.add(pos-1,n.asStatement().asCaseStmt());
                }
                else
                    otherBlock = n.asStatement().asBlockStmt();
        }
    }

    @Override
    public AST deepCopy() {
        return null;//do later
    }

    @Override
    public void visit(Visitor v) { v.visitChoiceStmt(this); }
}
