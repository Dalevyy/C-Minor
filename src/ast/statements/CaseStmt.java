package ast.statements;

import ast.*;
import token.*;
import utilities.*;

public class CaseStmt extends Statement {

    public SymbolTable symbolTable;

    private Label myLabel;
    private BlockStmt myBlock;

    public CaseStmt(Token t, Label l, BlockStmt b) {
        super(t);
        this.myLabel = l;
        this.myBlock = b;

        addChild(this.myLabel);
        addChild(this.myBlock);
        setParent();
    }

    public Label choiceLabel() { return myLabel; }
    public BlockStmt caseBlock() { return myBlock; }

    public boolean isCaseStmt() { return true; }
    public CaseStmt asCaseStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitCaseStmt(this); }
}
