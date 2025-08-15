package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.Name;
import cminor.ast.misc.NameDecl;
import cminor.ast.misc.Var;
import cminor.ast.misc.VarDecl;
import cminor.ast.types.Type;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node representing the declaration of a local variable.
 * @author Daniel Levy
 */
public class LocalDecl extends Statement implements NameDecl, VarDecl {

    /**
     * A {@link Var} object representing a local variable.
     */
    private Var localVariable;

    /**
     * Default constructor for {@link LocalDecl}.
     */
    public LocalDecl(){ this(new Token(),null);}

    /**
     * Main constructor for {@link LocalDecl}.
     * @param metaData {@link Token} containing the source code metadata that will be stored with this node.
     * @param localVariable A {@link Var} instance containing information about the local variable.
     */
    public LocalDecl(Token metaData, Var localVariable) {
        super(metaData);

        this.localVariable = localVariable;

        addChildNode(this.localVariable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInitialValue() { return localVariable.getInitialValue() != null; };

    /**
     * {@inheritDoc}
     */
    public Name getVariableName() { return localVariable.getVariableName(); }

    /**
     * {@inheritDoc}
     */
    public Expression getInitialValue() { return localVariable.getInitialValue(); }

    /**
     * {@inheritDoc}
     */
    public void setInitialValue(Expression init) { localVariable.setInitialValue(init);}

    /**
     * {@inheritDoc}
     */
    public Type getType() { return localVariable.getDeclaratedType(); }

    /**
     * {@inheritDoc}
     */
    public void setType(Type type) { localVariable.setDeclaredType(type);}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return localVariable.toString(); }

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
    public String getDeclName() { return localVariable.toString(); }

    /**
     * {@inheritDoc}
     */
    public boolean isMethod() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isFunction() { return false; }

    /**
     * {@inheritDoc}
     * @return
     */
    public boolean isLocalDecl() { return true; }

    /**
     * {@inheritDoc}
     * @return
     */
    public LocalDecl asLocalDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { localVariable = node.asSubNode().asVar(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new LocalDeclBuilder()
                   .setMetaData(this)
                   .setVar(localVariable.deepCopy().asSubNode().asVar())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitLocalDecl(this); }

    /**
     * Internal class that builds a {@link LocalDecl} object.
     */
    public static class LocalDeclBuilder extends NodeBuilder {

        /**
         * {@link LocalDecl} object we are building.
         */
        private final LocalDecl ld = new LocalDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link LocalDeclBuilder}.
         */
        public LocalDeclBuilder setMetaData(AST node) {
            super.setMetaData(ld, node);
            return this;
        }

        /**
         * Sets the local declaration's {@link #localVariable}.
         * @param var {@link Var} that represents the variable created by the local declaration.
         * @return Current instance of {@link LocalDeclBuilder}.
         */
        public LocalDeclBuilder setVar(Var var) {
            ld.localVariable = var;
            return this;
        }

        /**
         * Creates a {@link LocalDecl} object.
         * @return {@link LocalDecl}
         */
        public LocalDecl create() {
            ld.addChildNode(ld.localVariable);
            return ld;
        }
    }
}
