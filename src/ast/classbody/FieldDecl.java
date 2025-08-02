package ast.classbody;

import ast.AST;
import ast.expressions.Expression;
import ast.misc.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class FieldDecl extends AST implements NameDecl, VarDecl {

    /**
     * A {@link Var} object representing a field variable for an object.
     */
    private Var fieldVariable;

    public Modifiers mod;

    /**
     * Default constructor for {@link FieldDecl}.
     */
    public FieldDecl() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link FieldDecl}.
     * @param t later
     * @param m later
     * @param v later
     */
    public FieldDecl(Token t, Modifier m, Var v) {
        super(t);
        this.mod = new Modifiers(m);
        this.fieldVariable = v;

        addChild(this.fieldVariable);
        setParent();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInitialValue() { return fieldVariable.getInitialValue() != null; };

    /**
     * {@inheritDoc}
     */
    public Name getVariableName() { return fieldVariable.getVariableName(); }

    /**
     * {@inheritDoc}
     */
    public Expression getInitialValue() { return fieldVariable.getInitialValue(); }

    /**
     * {@inheritDoc}
     */
    public Type getDeclaredType() { return fieldVariable.getDeclaratedType(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return fieldVariable.toString(); }

    /**
     * {@inheritDoc}
     */
    public AST asAST() { return this; }

    public AST getDecl() { return this; }
    public String getDeclName() { return fieldVariable.toString();}

    private void setFieldVariable(Var fieldVariable) {
        this.fieldVariable = fieldVariable;
    }

    /**
     * Checks if the current AST node is a {@link FieldDecl}.
     * @return Boolean
     */
    public boolean isFieldDecl() { return true; }

    /**
     * Type cast method for {@link FieldDecl}.
     * @return FieldDecl
     */
    public FieldDecl asFieldDecl() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { this.fieldVariable = node.asVar(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link FieldDecl}
     */
    @Override
    public AST deepCopy() {
        return new FieldDeclBuilder()
                .setMetaData(this)
                .setMod(mod)
                .setVar(this.fieldVariable.deepCopy().asVar())
                .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitFieldDecl(this); }

    /**
     * Internal class that builds a {@link FieldDecl} object.
     */
    public static class FieldDeclBuilder extends NodeBuilder {

        /**
         * {@link FieldDecl} object we are building.
         */
        private final FieldDecl fd = new FieldDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the field declaration's {@link #mod}.
         * @param mod Modifier representing the access ability of the modifier.
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setMod(Modifiers mod) {
            fd.mod = mod;
            return this;
        }

        /**
         * Sets the field declaration's {@link #fieldVariable}.
         * @param var Variable that represents the field
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setVar(Var var) {
            fd.setFieldVariable(var);
            return this;
        }

        /**
         * Creates a {@link FieldDecl} object.
         * @return {@link FieldDecl}
         */
        public FieldDecl create() {
            super.saveMetaData(fd);
            fd.addChild(fd.fieldVariable);
            return fd;
        }
    }
}
