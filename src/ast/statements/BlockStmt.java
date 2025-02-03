package ast.statements;

import ast.Vector;
import token.*;
import utilities.Visitor;

public class BlockStmt extends Statement {

    private Vector<LocalDecl> decls;
    private Vector<Statement> stmts;

    public BlockStmt(Token t, Vector<LocalDecl> vd, Vector<Statement> s) {
        super(t);
        this.decls = vd;
        this.stmts = s;

        addChild(this.decls);
        addChild(this.stmts);
        setParent();
    }

    public Vector<LocalDecl> decls() { return decls; }
    public Vector<Statement> stmts() { return stmts; }

    public boolean isBlockStmt() { return true; }
    public BlockStmt asBlockStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitBlockStmt(this); }
}
