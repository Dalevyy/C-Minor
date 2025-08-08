package ast.misc;

import ast.AST;
import ast.expressions.Literal;
import token.Token;
import utilities.Visitor;

/**
 * A {@link SubNode} type representing the condition to execute a {@link ast.statements.CaseStmt}.
 * <p>
 *     Currently, labels are only used within a {@link ast.statements.ChoiceStmt} in order to keep
 *     track of which {@link ast.statements.CaseStmt} needs to be executed. A label will either
 *     represent a single constant value or a range of constant values.
 * </p>
 * @author Daniel Levy
 */
public class Label extends AST {

    /**
     * Either a single constant or the left constant in a {@link ast.statements.CaseStmt}.
     */
    private Literal leftConstant;

    /**
     * The right constant in a {@link ast.statements.CaseStmt} with a range label.
     */
    private Literal rightConstant;

    /**
     * Default constructor for {@link Label}.
     */
    public Label() { this(new Token(),null,null); }

    /**
     * Constructor to generate a {@link Label} with a single constant.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param leftConstant {@link Literal} to store into {@link #leftConstant}.
     */
    public Label(Token metaData, Literal leftConstant) { this(metaData, leftConstant, null); }

    /**
     * Main constructor for {@link Label}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param leftConstant {@link Literal} to store into {@link #leftConstant}.
     * @param rightConstant {@link Literal} to store into {@link #rightConstant}.
     */
    public Label(Token metaData, Literal leftConstant, Literal rightConstant) {
        super(metaData);

        this.leftConstant = leftConstant;
        this.rightConstant = rightConstant;

        addChildNode(this.leftConstant);
        addChildNode(this.rightConstant);
    }

    /**
     * Getter method for {@link #leftConstant}.
     * @return {@link Literal}
     */
    public Literal getLeftConstant() { return leftConstant; }

    /**
     * Getter method for {@link #rightConstant}.
     * @return {@link Literal}
     */
    public Literal getRightConstant() { return rightConstant; }

    /**
     * {@inheritDoc}
     */
    public boolean isLabel() { return true; }

    /**
     * {@inheritDoc}
     */
    public Label asLabel() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                leftConstant = node.asExpression().asLiteral();
                break;
            case 1:
                rightConstant = node.asExpression().asLiteral();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        LabelBuilder lb = new LabelBuilder();

        if(rightConstant != null)
            lb.setRightConstant(rightConstant.deepCopy().asExpression().asLiteral());

        return lb.setMetaData(this)
                 .setLeftConstant(leftConstant.deepCopy().asExpression().asLiteral())
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitLabel(this); }

    /**
     * Internal class that builds a {@link Label} object.
     */
    public static class LabelBuilder extends NodeBuilder {

        /**
         * {@link Label} object we are building.
         */
        private final Label l = new Label();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link LabelBuilder}.
         */
        public LabelBuilder setMetaData(AST node) {
            super.setMetaData(l, node);
            return this;
        }

        /**
         * Sets the label's {@link #leftConstant}.
         * @param leftConstant {@link Literal} representing a label's single constant or the left constant in a range.
         * @return Current instance of {@link LabelBuilder}.
         */
        public LabelBuilder setLeftConstant(Literal leftConstant) {
            l.leftConstant = leftConstant;
            return this;
        }

        /**
         * Sets the label's {@link #rightConstant}.
         * @param rightConstant {@link Literal} representing a label's right constant in a range.
         * @return Current instance of {@link LabelBuilder}.
         */
        public LabelBuilder setRightConstant(Literal rightConstant) {
            l.rightConstant = rightConstant;
            return this;
        }

        /**
         * Creates a {@link Label} object.
         * @return {@link Label}
         */
        public Label create() {
            l.addChildNode(l.leftConstant);
            l.addChildNode(l.rightConstant);
            return l;
        }
    }
}
