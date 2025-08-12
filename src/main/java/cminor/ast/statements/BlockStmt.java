package cminor.ast.statements;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a block.
 * @author Daniel Levy
 */
public class BlockStmt extends Statement {

    /**
     * {@link Vector} that stores all local variables declared in the block.
     */
    private Vector<LocalDecl> locals;

    /**
     * {@link Vector} that stores all statements written in the block.
     */
    private Vector<Statement> stmts;

    /**
     * Default constructor for {@link BlockStmt}.
     */
    public BlockStmt() { this(new Token(), new Vector<>(), new Vector<>()); }

    /**
     * Main constructor for {@link BlockStmt}..
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param locals {@link Vector} of {@link LocalDecl} to store into {@link #locals}.
     * @param stmts {@link Vector} of {@link Statement} to store into {@link #stmts}.
     */
    public BlockStmt(Token metaData, Vector<LocalDecl> locals, Vector<Statement> stmts) {
        super(metaData);

        this.locals = locals;
        this.stmts = stmts;

        addChildNode(this.locals);
        addChildNode(this.stmts);
    }

    /**
     * Getter method for {@link #locals}.
     * @return {@link Vector} of local declarations
     */
    public Vector<LocalDecl> getLocalDecls() { return locals; }

    /**
     * Getter method for {@link #stmts}.
     * @return {@link Vector} of statements
     */
    public Vector<Statement> getStatements() { return stmts; }

    /**
     * {@inheritDoc}
     */
    public boolean isBlockStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public BlockStmt asBlockStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        if(pos < locals.size()) {
            locals.remove(pos);
            locals.add(pos, node.asStatement().asLocalDecl());
        }
        else {
            pos -= stmts.size();
            stmts.remove(pos);
            stmts.add(pos, node.asStatement());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<LocalDecl> locals = new Vector<>();
        Vector<Statement> stmts = new Vector<>();

        for(LocalDecl ld : this.locals)
            locals.add(ld.deepCopy().asStatement().asLocalDecl());
        for(Statement s : this.stmts)
            stmts.add(s.deepCopy().asStatement().asStatement());

        return new BlockStmtBuilder()
                   .setMetaData(this)
                   .setDecls(locals)
                   .setStmts(stmts)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitBlockStmt(this); }

    /**
     * Internal class that builds a {@link BlockStmt} object.
     */
    public static class BlockStmtBuilder extends NodeBuilder{

        /**
         * {@link BlockStmt} object we are building.
         */
        private final BlockStmt bs = new BlockStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link BlockStmtBuilder}.
         */
        public BlockStmtBuilder setMetaData(AST node) {
            super.setMetaData(bs, node);
            return this;
        }

        /**
         * Sets the block statement's {@link #locals}.
         * @param locals {@link Vector} containing all local declarations for the block.
         * @return Current instance of {@link BlockStmtBuilder}.
         */
        public BlockStmtBuilder setDecls(Vector<LocalDecl> locals) {
            bs.locals = locals;
            return this;
        }

        /**
         * Sets the block statement's {@link #stmts}.
         * @param stmts {@link Vector} containing all statements for the block.
         * @return Current instance of {@link BlockStmtBuilder}.
         */
        public BlockStmtBuilder setStmts(Vector<Statement> stmts) {
            bs.stmts = stmts;
            return this;
        }

        /**
         * Creates a {@link BlockStmt} object.
         * @return {@link BlockStmt}
         */
        public BlockStmt create() {
            bs.addChildNode(bs.locals);
            bs.addChildNode(bs.stmts);
            return bs;
        }
    }
}
