package ast.statements;

import ast.AST;
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

        addChildNode(this.myLabel);
        addChildNode(this.caseBlock);
    }

    public Label choiceLabel() { return myLabel; }
    public BlockStmt caseBlock() { return caseBlock; }

    public boolean isCaseStmt() { return true; }
    public CaseStmt asCaseStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                myLabel = n.asSubNode().asLabel();
                break;
            case 1:
                caseBlock = n.asStatement().asBlockStmt();
                break;
        }
    }

    @Override
    public AST deepCopy() {
        return null; //please do later awwwwwwwwwwwwwwwwwwwwwwwwwww
    }


    @Override
    public void visit(Visitor v) { v.visitCaseStmt(this); }
}
