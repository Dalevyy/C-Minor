package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.ScopeDecl;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a choice statement.
 * @author Daniel Levy
 */
public class ChoiceStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the current {@link ChoiceStmt).}
     */
    public SymbolTable scope;

    /**
     * The {@link Expression} that is evaluated to figure out which {@link CaseStmt} to execute.
     */
    private Expression choice;

    /**
     * A {@link Vector} containing all possible cases that can be executed.
     */
    private Vector<CaseStmt> cases;

    /**
     * The {@link BlockStmt} associated with the default case.
     */
    private BlockStmt defaultBody;

    /**
     * Default constructor for {@link ChoiceStmt}.
     */
    public ChoiceStmt() { this(new Token(), null, new Vector<>(), null); }

    /**
     * Main constructor for {@link ChoiceStmt}..
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param choice {@link Expression} to store into {@link #choice}.
     * @param cases {@link Vector} of {@link CaseStmt} to store into {@link #cases}.
     * @param defaultBody {@link BlockStmt} to store into {@link #defaultBody}.
     */
    public ChoiceStmt(Token metaData, Expression choice, Vector<CaseStmt> cases, BlockStmt defaultBody) {
        super(metaData);

        this.choice = choice;
        this.cases = cases;
        this.defaultBody = defaultBody;

        addChildNode(this.choice);
        addChildNode(this.cases);
        addChildNode(this.defaultBody);
    }

    /**
     * Getter method for {@link #choice}.
     * @return {@link Expression}
     */
    public Expression getChoiceValue() { return choice; }

    /**
     * Getter method for {@link #cases}.
     * @return {@link Vector} of case statements
     */
    public Vector<CaseStmt> getCases() { return cases; }

    /**
     * Getter method for {@link #defaultBody}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getDefaultBody() { return defaultBody; }

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
    public boolean isChoiceStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ChoiceStmt asChoiceStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                choice = node.asExpression();
                break;
            default:
                if(pos <= cases.size()) {
                    cases.remove(pos-1);
                    cases.add(pos-1, node.asStatement().asCaseStmt());
                }
                else
                    defaultBody = node.asStatement().asBlockStmt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        ChoiceStmtBuilder cb = new ChoiceStmtBuilder();
        Vector<CaseStmt> cases = new Vector<>();

        for(CaseStmt cs : this.cases)
            cases.add(cs.deepCopy().asStatement().asCaseStmt());

        return cb.setMetaData(this)
                 .setChoiceValue(choice.deepCopy().asExpression())
                 .setCases(cases)
                 .setDefaultBody(defaultBody.deepCopy().asStatement().asBlockStmt())
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitChoiceStmt(this); }

    /**
     * Internal class that builds a {@link ChoiceStmt} object.
     */
    public static class ChoiceStmtBuilder extends NodeBuilder {

        /**
         * {@link ChoiceStmt} object we are building.
         */
        private final ChoiceStmt cs = new ChoiceStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ChoiceStmtBuilder}.
         */
        public ChoiceStmtBuilder setMetaData(AST node) {
            super.setMetaData(cs, node);
            return this;
        }

        /**
         * Sets the choice statement's {@link #choice}.
         * @param choice {@link Expression} representing the value that will be chosen from.
         * @return Current instance of {@link ChoiceStmtBuilder}.
         */
        public ChoiceStmtBuilder setChoiceValue(Expression choice) {
            cs.choice = choice;
            return this;
        }

        /**
         * Sets the choice statement's {@link #cases}.
         * @param cases {@link Vector} containing all the case statements for the current choice statement.
         * @return Current instance of {@link ChoiceStmtBuilder}.
         */
        public ChoiceStmtBuilder setCases(Vector<CaseStmt> cases) {
            cs.cases = cases;
            return this;
        }

        /**
         * Sets the choice statement's {@link #defaultBody}.
         * @param defaultBody {@link BlockStmt} representing the default case to execute.
         * @return Current instance of {@link ChoiceStmtBuilder}.
         */
        public ChoiceStmtBuilder setDefaultBody(BlockStmt defaultBody) {
            cs.defaultBody = defaultBody;
            return this;
        }

        /**
         * Creates a {@link ChoiceStmt} object.
         * @return {@link ChoiceStmt}
         */
        public ChoiceStmt create() {
            cs.addChildNode(cs.choice);
            cs.addChildNode(cs.cases);
            cs.addChildNode(cs.defaultBody);
            return cs;
        }
    }
}
