package cminor.ast.classbody;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.Modifier;
import cminor.ast.misc.Name;
import cminor.ast.misc.NameDecl;
import cminor.ast.misc.Var;
import cminor.ast.misc.VarDecl;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.types.Type;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link ClassNode} that stores a field declaration written in a {@link ClassDecl}.
 * @author Daniel Levy
 */
public class FieldDecl extends ClassNode implements NameDecl, VarDecl {

    /**
     * A {@link Var} representing a field variable for an object.
     */
    private Var fieldVariable;

    /**
     * {@link Modifier} containing the access privilege of the field.
     */
    public Modifier mod;

    /**
     * Default constructor for {@link FieldDecl}.
     */
    public FieldDecl() { this(new Token(), null, null); }

    /**
     * Main constructor for {@link FieldDecl}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param mod {@link Modifier} that will be stored into {@link #mod}.
     * @param fieldVariable {@link Var} that will be stored into {@link #fieldVariable}.
     */
    public FieldDecl(Token metaData, Modifier mod, Var fieldVariable) {
        super(metaData);

        this.mod = mod;
        this.fieldVariable = fieldVariable;

        addChildNode(this.fieldVariable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInitialValue() { return fieldVariable.hasInitialValue(); }

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
    @Override
    public void setInitialValue(Expression init) { fieldVariable.setInitialValue(init); }

    /**
     * {@inheritDoc}
     */
    public Type getType() { return fieldVariable.getDeclaratedType(); }

    /**
     * {@inheritDoc}
     */
    public void setType(Type type) { fieldVariable.setDeclaredType(type);}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return fieldVariable.toString(); }

    /**
     * {@inheritDoc}
     */
    public AST asAST() { return this; }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return fieldVariable.toString();}

    /**
     * {@inheritDoc}
     */
    public ClassDecl getClassDecl() { return parent.asClassNode().getClassDecl(); }

    /**
     * {@inheritDoc}
     */
    public boolean isFieldDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public FieldDecl asFieldDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { fieldVariable = node.asSubNode().asVar(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new FieldDeclBuilder()
                   .setMetaData(this)
                   .setMod(mod)
                   .setFieldVariable(fieldVariable.deepCopy().asSubNode().asVar())
                   .create();
    }

    /**
     * {@inheritDoc}
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
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link FieldDeclBuilder}.
         */
        public FieldDeclBuilder setMetaData(AST node) {
            super.setMetaData(fd, node);
            return this;
        }

        /**
         * Sets the field declaration's {@link #mod}.
         * @param mod {@link ast.misc.Modifier} representing the access rules for the field.
         * @return Current instance of {@link FieldDeclBuilder}.
         */
        public FieldDeclBuilder setMod(Modifier mod) {
            fd.mod = mod;
            return this;
        }

        /**
         * Sets the field declaration's {@link #fieldVariable}.
         * @param fieldVariable {@link Var} that represents the variable the field declares.
         * @return Current instance of {@link FieldDeclBuilder}.
         */
        public FieldDeclBuilder setFieldVariable(Var fieldVariable) {
            fd.fieldVariable = fieldVariable;
            return this;
        }

        /**
         * Creates a {@link FieldDecl} object.
         * @return {@link FieldDecl}
         */
        public FieldDecl create() {
            fd.addChildNode(fd.fieldVariable);
            return fd;
        }
    }
}
