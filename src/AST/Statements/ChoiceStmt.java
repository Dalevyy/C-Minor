package AST.Statements;

import AST.*;
import AST.Expressions.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

public class ChoiceStmt extends Statement {

    private Expression expr;
    private Vector<CaseStmt> caseStmts;
    private BlockStmt block;

    public ChoiceStmt(Token t, Expression e, BlockStmt b) { this(t,e,null,b); }

    public ChoiceStmt(Token t, Expression e, Vector<CaseStmt> cs, BlockStmt b) {
        super(t);
        this.expr = e;
        this.caseStmts = cs;
        this.block = b;

        addChild(this.expr);
        addChild(this.caseStmts);
        addChild(this.block);
        setParent();
    }

    public Expression getChoiceExpr() { return expr; }
    public Vector<CaseStmt> getCaseStmts() { return caseStmts; }
    public BlockStmt getBlock() { return block; }

    public boolean isChoiceStmt() { return true; }
    
    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsChoiceStmt(this); }
}
