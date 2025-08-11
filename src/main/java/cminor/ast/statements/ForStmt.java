package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.ScopeDecl;
import cminor.ast.operators.LoopOp;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing a for loop.
 * @author Daniel Levy
 */
public class ForStmt extends Statement implements ScopeDecl {

    /**
     * The scope opened by the current {@link ForStmt}.
     */
    public SymbolTable scope;

    /**
     * A {@link LocalDecl} representing the control variable of the for loop.
     */
    private LocalDecl controlVariable;

    /**
     * An {@link Expression} representing the starting value for {@link #controlVariable}.
     */
    private Expression startValue;

    /**
     * An {@link Expression} representing the final value for {@link #controlVariable} in order to terminate the loop.
     */
    private Expression endValue;

    /**
     * The {@link LoopOp} denoting if the {@link #startValue} and {@link #endValue} are executed with the loop.
     */
    private LoopOp operator;

    /**
     * A {@link BlockStmt} associated with the body of the for loop.
     */
    private BlockStmt body;

    /**
     * Default constructor for {@link ForStmt}.
     */
    public ForStmt(){ this(new Token(),null,null,null,null,null); }

    /**
     * Main constructor for {@link ForStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param controlVariable {@link LocalDecl} to store into {@link #controlVariable}.
     * @param startValue {@link Expression} to store into {@link #startValue}.
     * @param endValue {@link Expression} to store into {@link #endValue}.
     * @param operator {@link LoopOp} to store into {@link #operator}.
     * @param body {@link BlockStmt} to store into {@link #body}.
     */
    public ForStmt(Token metaData, LocalDecl controlVariable, Expression startValue,
                   Expression endValue, LoopOp operator, BlockStmt body) {
        super(metaData);

        this.controlVariable = controlVariable;
        this.startValue = startValue;
        this.endValue = endValue;
        this.operator = operator;
        this.body = body;

        addChildNode(this.controlVariable);
        addChildNode(this.startValue);
        addChildNode(this.endValue);
        addChildNode(this.body);
    }

    /**
     * Getter method for {@link #controlVariable}.
     * @return {@link LocalDecl}
     */
    public LocalDecl getControlVariable() { return controlVariable; }

    /**
     * Getter method for {@link #startValue}.
     * @return {@link Expression}
     */
    public Expression getStartValue() { return startValue; }

    /**
     * Getter method for {@link #endValue}.
     * @return {@link Expression}
     */
    public Expression getEndValue() { return endValue; }

    /**
     * Getter method for {@link #operator}.
     * @return {@link LoopOp}
     */
    public LoopOp getLoopOperator() { return operator; }

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
    public boolean isForStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ForStmt asForStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                controlVariable = node.asStatement().asLocalDecl();
                break;
            case 1:
                startValue = node.asExpression();
                break;
            case 2:
                endValue = node.asExpression();
                break;
            case 3:
                body = node.asStatement().asBlockStmt();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ForStmtBuilder()
                   .setMetaData(this)
                   .setControlVariable(controlVariable.deepCopy().asStatement().asLocalDecl())
                   .setStartValue(startValue.deepCopy().asExpression())
                   .setRHS(endValue.deepCopy().asExpression())
                   .setLoopOperator(operator.deepCopy().asOperator().asLoopOp())
                   .setBody(body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitForStmt(this); }

    /**
     * Internal class that builds a {@link ForStmt} object.
     */
    public static class ForStmtBuilder extends NodeBuilder {

        /**
         * {@link ForStmt} object we are building.
         */
        private final ForStmt fs = new ForStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setMetaData(AST node) {
            super.setMetaData(fs, node);
            return this;
        }

        /**
         * Sets the for statement's {@link #controlVariable}.
         * @param controlVariable {@link LocalDecl} representing the control variable of the for loop.
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setControlVariable(LocalDecl controlVariable) {
            fs.controlVariable = controlVariable;
            return this;
        }

        /**
         * Sets the for statement's {@link #startValue}.
         * @param startValue {@link Expression} representing the start value of the for loop.
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setStartValue(Expression startValue) {
            fs.startValue = startValue;
            return this;
        }

        /**
         * Sets the for statement's {@link #endValue}.
         * @param endValue {@link Expression} representing the end value of the for loop.
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setRHS(Expression endValue) {
            fs.endValue = endValue;
            return this;
        }

        /**
         * Sets the for statement's {@link #operator}.
         * @param operator {@link LoopOp} representing the range inclusivity of
         *                 the {@link #startValue} and {@link #endValue}.
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setLoopOperator(LoopOp operator) {
            fs.operator = operator;
            return this;
        }

        /**
         * Sets the for statement's {@link #body}.
         * @param body {@link BlockStmt} representing the body of the for loop.
         * @return Current instance of {@link ForStmtBuilder}.
         */
        public ForStmtBuilder setBody(BlockStmt body) {
            fs.body = body;
            return this;
        }

        /**
         * Creates a {@link ForStmt} object.
         * @return {@link ForStmt}
         */
        public ForStmt create() {
            fs.addChildNode(fs.controlVariable);
            fs.addChildNode(fs.startValue);
            fs.addChildNode(fs.endValue);
            fs.addChildNode(fs.body);
            return fs;
        }
    }
}
