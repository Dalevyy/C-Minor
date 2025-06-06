package ast.statements;

import ast.expressions.*;
import token.*;
import utilities.Vector;

import utilities.Visitor;
import utilities.SymbolTable;

public class ChoiceStmt extends Statement {

    public SymbolTable symbolTable;

    private final Expression expr;
    private final Vector<CaseStmt> caseStmts;
    private final BlockStmt otherBlock;

    public ChoiceStmt(Token t, Expression e, BlockStmt b) { this(t,e,null,b); }

    public ChoiceStmt(Token t, Expression e, Vector<CaseStmt> cs, BlockStmt b) {
        super(t);
        this.expr = e;
        this.caseStmts = cs;
        this.otherBlock = b;

        addChild(this.expr);
        addChild(this.caseStmts);
        addChild(this.otherBlock);
        setParent();
    }

    public Expression choiceExpr() { return expr; }
    public Vector<CaseStmt> caseStmts() { return caseStmts; }
    public BlockStmt otherBlock() { return otherBlock; }

    public boolean isChoiceStmt() { return true; }
    public ChoiceStmt asChoiceStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitChoiceStmt(this); }
}
