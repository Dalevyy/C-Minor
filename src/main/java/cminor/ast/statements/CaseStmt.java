package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.misc.Label;
import cminor.ast.misc.ScopeDecl;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a case statement found inside a {@link ChoiceStmt}.
 * @author Daniel Levy
 */
public class CaseStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the current {@link CaseStmt}.
     */
    public SymbolTable scope;

    /**
     * {@link Label} associated with executing this {@link CaseStmt}.
     */
    private Label label;

    /**
     * The {@link BlockStmt} representing the body for the {@link CaseStmt}.
     */
    private BlockStmt body;

    /**
     * Default constructor for {@link CaseStmt}.
     */
    public CaseStmt() { this(new Token(), null, null); }

    /**
     * Main constructor for {@link CaseStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param label {@link Label} to store into {@link #label}.
     * @param body {@link BlockStmt} to store into {@link #body}.
     */
    public CaseStmt(Token metaData, Label label, BlockStmt body) {
        super(metaData);

        this.label = label;
        this.body = body;

        addChildNode(this.body);
    }

    /**
     * Getter method for {@link #label}.
     * @return {@link Label}
     */
    public Label getLabel() { return label; }

    /**
     * Getter method for {@link #body}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getBody() { return body; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable newScope) { scope = (scope == null) ? newScope : scope; }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() { return scope; }

    /**
     * {@inheritDoc}
     */
    public boolean isCaseStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public CaseStmt asCaseStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                label = node.asSubNode().asLabel();
                break;
            case 1:
                body = node.asStatement().asBlockStmt();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new CaseStmtBuilder()
                   .setMetaData(this)
                   .setLabel(label.deepCopy().asSubNode().asLabel())
                   .setBody(body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitCaseStmt(this); }

    /**
     * Internal class that builds a {@link CaseStmt} object.
     */
    public static class CaseStmtBuilder extends NodeBuilder {

        /**
         * {@link CaseStmt} object we are building.
         */
        private final CaseStmt cs = new CaseStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link CaseStmtBuilder}.
         */
        public CaseStmtBuilder setMetaData(AST node) {
            super.setMetaData(cs, node);
            return this;
        }

        /**
         * Sets the case statement's {@link #label}.
         * @param label {@link Label} representing the condition needed to execute the case statement.
         * @return Current instance of {@link CaseStmtBuilder}.
         */
        public CaseStmtBuilder setLabel(Label label) {
            cs.label = label;
            return this;
        }

        /**
         * Sets the case statement's {@link #body}.
         * @param body {@link BlockStmt} representing the body of the case statement.
         * @return Current instance of {@link CaseStmtBuilder}.
         */
        public CaseStmtBuilder setBody(BlockStmt body) {
            cs.body = body;
            return this;
        }

        /**
         * Creates a {@link CaseStmt} object.
         * @return {@link CaseStmt}
         */
        public CaseStmt create() {
            cs.addChildNode(cs.body);
            return cs;
        }
    }
}
