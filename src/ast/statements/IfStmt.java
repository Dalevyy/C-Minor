package ast.statements;

import ast.AST;
import ast.expressions.Expression;
import ast.misc.ScopeDecl;
import token.Token;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link Statement} node representing an if statement.
 * @author Daniel Levy
 */
public class IfStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the if branch.
     */
    public SymbolTable ifScope;

    /**
     * The scope opened by the else branch (if applicable).
     */
    public SymbolTable elseScope;

    /**
     * The {@link Expression} representing the condition to execute the if branch.
     */
    private Expression condition;

    /**
     * The {@link BlockStmt} representing the body of the if branch.
     */
    private BlockStmt ifBody;

    /**
     * A {@link Vector} of if statements representing each else if branch.
     */
    private Vector<IfStmt> elifs;

    /**
     * The {@link BlockStmt} representing the body of the else branch (if applicable.)
     */
    private BlockStmt elseBody;

    /**
     * Default Constructor for {@link IfStmt}.
     */
    public IfStmt(){ this(new Token(),null,null,new Vector<>(),null); }

    /**
     * Constructor to generate an {@link IfStmt} with only an if branch.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param condition {@link Expression} to store into {@link #condition}.
     * @param ifBody {@link BlockStmt} to store into {@link #ifBody}.
     */
    public IfStmt(Token metaData, Expression condition, BlockStmt ifBody) {
        this(metaData, condition, ifBody, new Vector<>(),null);
    }

    /**
     * Constructor to generate an {@link IfStmt} with an if branch and elif branches.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param condition {@link Expression} to store into {@link #condition}.
     * @param ifBody {@link BlockStmt} to store into {@link #ifBody}.
     * @param elifs {@link Vector} of {@link IfStmt} to store into {@link #elifs}.
     */
    public IfStmt(Token metaData, Expression condition, BlockStmt ifBody, Vector<IfStmt> elifs) {
        this(metaData, condition, ifBody, elifs, null);
    }

    /**
     * Constructor to generate an {@link IfStmt} with an if, elif, and else branches.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param condition {@link Expression} to store into {@link #condition}.
     * @param ifBody {@link BlockStmt} to store into {@link #ifBody}.
     * @param elifs {@link Vector} of {@link IfStmt} to store into {@link #elifs}.
     * @param elseBody {@link BlockStmt} to store into {@link #elseBody}.
     */
    public IfStmt(Token metaData, Expression condition, BlockStmt ifBody, Vector<IfStmt> elifs, BlockStmt elseBody) {
        super(metaData);

        this.condition = condition;
        this.ifBody = ifBody;
        this.elifs = elifs;
        this.elseBody = elseBody;

        addChildNode(this.condition);
        addChildNode(this.ifBody);
        addChildNode(this.elifs);
        addChildNode(this.elseBody);
    }

    /**
     * Getter method for {@link #condition}.
     * @return {@link Expression}
     */
    public Expression getCondition() { return condition; }

    /**
     * Getter method for {@link #ifBody}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getIfBody() { return ifBody; }

    /**
     * Getter method for {@link #elifs}.
     * @return {@link Vector} of if statements
     */
    public Vector<IfStmt> getElifs() { return elifs; }

    /**
     * Getter method for {@link #elseBody}
     * @return {@link BlockStmt}
     */
    public BlockStmt getElseBody() { return elseBody; }

    /**
     * Getter method for {@link #ifScope}.
     * @return {@link SymbolTable}
     */
    public SymbolTable getIfScope() { return ifScope; }

    /**
     * Getter method for {@link #elseScope}.
     * @return {@link SymbolTable}
     */
    public SymbolTable getElseScope() { return elseScope; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable newScope) {
        if(ifScope == null)
            ifScope = newScope;
        else if(elseScope == null)
            elseScope = newScope;
    }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() {
        throw new RuntimeException("An if statement stores two scopes, so use the respective getter methods.");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIfStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public IfStmt asIfStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                condition = node.asExpression();
                break;
            case 1:
                ifBody = node.asStatement().asBlockStmt();
                break;
            default:
                if(getElifs().size()-pos-2 >= 1) {
                    elifs.remove(pos-2);
                    elifs.add(pos-2,node.asStatement().asIfStmt());
                }
                else
                    elseBody = node.asStatement().asBlockStmt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        IfStmtBuilder ib = new IfStmtBuilder();

        Vector<IfStmt> elifs = new Vector<>();
        for(IfStmt is : this.elifs)
            elifs.add(is.deepCopy().asStatement().asIfStmt());

        if(elseBody != null)
            ib.setElseBody(elseBody.deepCopy().asStatement().asBlockStmt());

        return ib.setMetaData(this)
                  .setCondition(condition.deepCopy().asExpression())
                  .setIfBody(ifBody.deepCopy().asStatement().asBlockStmt())
                  .setElifs(elifs)
                  .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitIfStmt(this); }

    /**
     * Internal class that builds an {@link IfStmt} object.
     */
    public static class IfStmtBuilder extends NodeBuilder {

        /**
         * {@link IfStmt} object we are building.
         */
        private final IfStmt is = new IfStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link IfStmtBuilder}.
         */
        public IfStmtBuilder setMetaData(AST node) {
            super.setMetaData(is, node);
            return this;
        }

        /**
         * Sets the if statement's {@link #condition}.
         * @param condition {@link Expression} representing the if branch conditional expression.
         * @return Current instance of {@link IfStmtBuilder}.
         */
        public IfStmtBuilder setCondition(Expression condition) {
            is.condition = condition;
            return this;
        }

        /**
         * Sets the if statement's {@link #ifBody}.
         * @param ifBody {@link BlockStmt} representing the body of the if branch.
         * @return Current instance of {@link IfStmtBuilder}.
         */
        public IfStmtBuilder setIfBody(BlockStmt ifBody) {
            is.ifBody = ifBody;
            return this;
        }

        /**
         * Sets the if statement's {@link #elifs}.
         * @param elifs {@link Vector} containing the else if branches for the current if statement.
         * @return Current instance of {@link IfStmtBuilder}.
         */
        public IfStmtBuilder setElifs(Vector<IfStmt> elifs) {
            is.elifs = elifs;
            return this;
        }

        /**
         * Sets the if statement's {@link #elseBody}.
         * @param elseBody {@link BlockStmt} representing the body of the else branch.
         * @return Current instance of {@link IfStmtBuilder}.
         */
        public IfStmtBuilder setElseBody(BlockStmt elseBody) {
            is.elseBody = elseBody;
            return this;
        }

        /**
         * Creates an {@link IfStmt} object.
         * @return {@link IfStmt}
         */
        public IfStmt create(){
            is.addChildNode(is.condition);
            is.addChildNode(is.ifBody);
            is.addChildNode(is.elifs);
            is.addChildNode(is.elseBody);
            return is;
        }
    }
}

