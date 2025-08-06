package ast.misc;

import ast.AST;
import ast.expressions.Expression;
import ast.types.Type;
import token.Token;
import utilities.Visitor;

/**
 * A {@link SubNode} type representing a variable.
 * @author Daniel Levy
 */
public class Var extends SubNode {

    /**
     * The {@link Name} of the variable.
     */
    private Name variableName;

    /**
     * The initial value of the variable, represented as an {@link Expression}. This field
     * will only be set when a user explicitly initializes a variable.
     */
    private Expression initialValue;

    /**
     * The type bound to the variable. This will be given by the user (unless we are parsing a field
     * constant in a {@link ast.expressions.NewExpr} or an enum constant).
     */
    private Type declaredType;

    /**
     * Default constructor for {@link Var}.
     */
    public Var() { this(new Token(),null,null,null); }

    /**
     * Constructor for instantiating a {@link Var} without an initial value (for enum constants).
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param variableName {@link Name} to store into {@link #variableName}.
     */
    public Var(Token metaData, Name variableName) { this(metaData, variableName, null, null); }

    public Var(Token metaData, Name variableName, Type t) { this(metaData, variableName, null, t); }

    /**
     * Constructor for instantiating a {@link Var} with an initial value (for field and enum constants).
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param variableName {@link Name} to store into {@link #variableName}.
     * @param initialValue {@link Expression} to store into {@link #initialValue}.
     */
    public Var(Token metaData, Name variableName, Expression initialValue) {
        this(metaData, variableName, initialValue, null);
    }

    /**
     * Main constructor for instantiating a {@link Var}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param variableName {@link Name} to store into {@link #variableName}.
     * @param initialValue {@link Expression} to store into {@link #initialValue}.
     * @param declaredType {@link Type} to store into {@link #declaredType}.
     */
    public Var(Token metaData, Name variableName, Expression initialValue, Type declaredType) {
        super(metaData);

        this.variableName = variableName;
        this.initialValue = initialValue;
        this.declaredType = declaredType;

        addChildNode(this.variableName);
        addChildNode(this.initialValue);
    }

    /**
     * Getter method to retrieve {@link #variableName}.
     * @return {@link Name} object stored in {@link #variableName}.
     */
    public Name getVariableName() { return variableName; }

    /**
     * Getter method to retrieve {@link #initialValue}.
     * @return {@link Expression} object stored in {@link #initialValue}.
     */
    public Expression getInitialValue() { return initialValue;}

    /**
     * Setter method to set {@link #initialValue}. This will be called when a default value needs
     * to be saved into the current {@link Var}.
     * @param initialValue {@link Expression} representing a default value we want to save into the variable.
     */
    public void setInitialValue(Expression initialValue) {
        if(initialValue != null)
            this.initialValue = initialValue;
    }

    /**
     * Getter method to retrieve {@link #declaredType}.
     * @return {@link Type} representing the type given to the variable (if applicable).
     */
    public Type getDeclaratedType() { return declaredType; }

    /**
     * {@inheritDoc}
     */
    public boolean isVar() { return true; }

    /**
     * {@inheritDoc}
     */
    public Var asVar() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                this.variableName = node.asSubNode().asName();
                break;
            case 1:
                this.initialValue = node.asExpression();
                break;
        }
    }

    /**
     * Returns the name of the variable.
     * @return String representation of the variable's name.
     */
    @Override
    public String toString() { return variableName.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        VarBuilder vb = new VarBuilder();

        if(initialValue != null)
            vb.setInitialValue(initialValue.deepCopy().asExpression());

        if(declaredType != null)
            vb.setType(declaredType.deepCopy().asType());

        return vb.setMetaData(this)
                 .setName(variableName.deepCopy().asSubNode().asName())
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitVar(this); }

    /**
     * Internal class that builds a {@link Var} object.
     */
    public static class VarBuilder extends NodeBuilder {

        /**
         * {@link Var} object we are building.
         */
        private final Var v = new Var();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link VarBuilder}.
         */
        public VarBuilder setMetaData(AST node) {
            super.setMetaData(v, node);
            return this;
        }

        /**
         * Sets the variable's {@link #variableName}.
         * @param name {@link Name} representing the name of the variable.
         * @return Current instance of {@link VarBuilder}.
         */
        public VarBuilder setName(Name name) {
            v.variableName = name;
            return this;
        }

        /**
         * Sets the variable's {@link #initialValue}
         * @param initialValue The {@link Expression} representing the initial value of the variable.
         * @return Current instance of {@link VarBuilder}.
         */
        public VarBuilder setInitialValue(Expression initialValue) {
            v.initialValue = initialValue;
            return this;
        }

        /**
         * Sets the variable's {@link #declaredType}.
         * @param type The {@link Type} representing the data the variable will store.
         * @return Current instance of {@link VarBuilder}.
         */
        public VarBuilder setType(Type type) {
            v.declaredType = type;
            return this;
        }

        public Var create() {
            v.addChildNode(v.variableName);
            v.addChildNode(v.initialValue);
            return v;
        }
    }
}
