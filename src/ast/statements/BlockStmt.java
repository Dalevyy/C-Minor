package ast.statements;

import ast.AST;
import token.*;
import utilities.Vector;

import utilities.Visitor;

public class BlockStmt extends Statement {

    private final Vector<LocalDecl> decls;
    private final Vector<Statement> stmts;

    public BlockStmt() {
        this(new Token(),new Vector<>(),new Vector<>());
    }

    public BlockStmt(Vector<Statement> s) { this(new Token(),new Vector<>(),s); }
    public BlockStmt(Vector<LocalDecl> ld, Vector<Statement> s) { this(new Token(),ld,s); }
    public BlockStmt(Token t, Vector<LocalDecl> vd, Vector<Statement> s) {
        super(t);
        this.decls = vd;
        this.stmts = s;

        addChildNode(this.decls);
        addChildNode(this.stmts);
    }

    public Vector<LocalDecl> decls() { return decls; }
    public Vector<Statement> stmts() { return stmts; }

    public void addDecl(Vector<LocalDecl> ld) { this.decls.merge(ld); }
    public void addStmt(Vector<Statement> stmts) { this.stmts.merge(stmts); }
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

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link BlockStmt}
     */
    @Override
    public AST deepCopy() {
        Vector<LocalDecl> decls = new Vector<>();
        Vector<Statement> stmts = new Vector<>();

        for(LocalDecl ld : this.decls)
            decls.add(ld.deepCopy().asStatement().asLocalDecl());
        for(Statement s : this.stmts)
            stmts.add(s.deepCopy().asStatement().asStatement());

        return new BlockStmtBuilder()
                   .setMetaData(this)
                   .setDecls(decls)
                   .setStmts(stmts)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitBlockStmt(this); }

    public static class BlockStmtBuilder extends NodeBuilder{
        private final BlockStmt bs = new BlockStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return BlockStmtBuilder
         */
        public BlockStmtBuilder setMetaData(AST node) {
            super.setMetaData(bs,node);
            return this;
        }

        public BlockStmtBuilder setDecls(Vector<LocalDecl> locals) {
            bs.addDecl(locals);
            return this;
        }

        public BlockStmtBuilder setStmts(Vector<Statement> stmts) {
            bs.addStmt(stmts);
            return this;
        }

        public BlockStmt create() {
            bs.addChildNode(bs.decls);
            bs.addChildNode(bs.stmts);
            return bs;
        }
    }
}
