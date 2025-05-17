package ast.statements;

import ast.misc.Label;
import token.*;
import utilities.*;

public class CaseStmt extends Statement {

    public SymbolTable symbolTable;

    private Label myLabel;
    private BlockStmt caseBlock;

    public CaseStmt(Token t, Label l, BlockStmt b) {
        super(t);
        this.myLabel = l;
        this.caseBlock = b;

        addChild(this.myLabel);
        addChild(this.caseBlock);
        setParent();
    }

    public Label choiceLabel() { return myLabel; }
    public BlockStmt caseBlock() { return caseBlock; }

    public boolean isCaseStmt() { return true; }
    public CaseStmt asCaseStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitCaseStmt(this); }
}
