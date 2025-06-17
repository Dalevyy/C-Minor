package ast.statements;

import ast.AST;
import token.*;
import utilities.Vector;

import utilities.Visitor;

public class BlockStmt extends Statement {

    private final Vector<LocalDecl> decls;
    private final Vector<Statement> stmts;

    public BlockStmt() {
        this.decls = new Vector<>();
        this.stmts = new Vector<>();
    }

    public BlockStmt(Vector<Statement> s) { this(new Token(),new Vector<>(),s); }
    public BlockStmt(Vector<LocalDecl> ld, Vector<Statement> s) { this(new Token(),ld,s); }
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

    public void addDecl(Vector<LocalDecl> ld) { this.decls.merge(ld); }
    public void addStmt(Statement s) { this.stmts.add(s); }

    public boolean isBlockStmt() { return true; }
    public BlockStmt asBlockStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        if(pos < decls.size()) {
            decls.remove(pos);
            decls.add(pos,n.asStatement().asLocalDecl());
        }
        else {
            pos -= stmts.size();
            stmts.remove(pos);
            stmts.add(pos,n.asStatement());
        }
    }

    @Override
    public void visit(Visitor v) { v.visitBlockStmt(this); }

    public static class BlockStmtBuilder {
        private final BlockStmt bs = new BlockStmt(new Vector<>(),new Vector());

        public BlockStmtBuilder addDecl(LocalDecl ld) {
            bs.addDecl(new Vector<>(ld));
            return this;
        }

        public BlockStmtBuilder addStmt(Statement s) {
            bs.addStmt(s);
            return this;
        }

        public BlockStmt createBlockStmt() { return bs; }
    }
}
