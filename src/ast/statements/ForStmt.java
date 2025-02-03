package ast.statements;

import ast.*;
import ast.expressions.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class ForStmt extends Statement {

    public SymbolTable symbolTable;

    private Vector<LocalDecl> forVars;
    private Expression cond;
    private Statement nextExpr;
    private BlockStmt body;

    public ForStmt(Token t, Vector<LocalDecl> fv, Expression c, Statement ne, BlockStmt b) {
        super(t);
        this.forVars = fv;
        this.cond = c;
        this.nextExpr = ne;
        this.body = b;

        addChild(this.cond);
        addChild(this.nextExpr);
        addChild(this.body);
        setParent();
    }

    public Vector<LocalDecl> forInits() { return forVars; }
    public Expression condition() { return cond; }
    public Statement nextExpr() { return nextExpr; }
    public BlockStmt forBlock() { return body; }

    public boolean isForStmt() { return true; }
    public ForStmt asForStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitForStmt(this); }
}
