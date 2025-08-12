package cminor.ast.topleveldecls;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.Name;
import cminor.ast.misc.NameDecl;
import cminor.ast.misc.Var;
import cminor.ast.misc.VarDecl;
import cminor.ast.types.*;
import cminor.token.Token;
import cminor.utilities.Visitor;

public class GlobalDecl extends TopLevelDecl implements NameDecl, VarDecl {

    /**
     * A {@link Var} object representing a global variable.
     */
    private Var globalVariable;

    /**
     * A flag denoting whether the global variable is mutable.
     * <ul>
     *     <li>
     *         If {@code True}, the variable is represented by the {@code const} keyword.
     *     </li>
     *     <li>
     *         If {@code False}, the variable is represented by the {@code global} keyword.
     *     </li>
     * </ul>
     */
    private final boolean isConstant;

    /**
     * Default constructor for {@link GlobalDecl}.
     */
    public GlobalDecl() { this(new Token(),null); }

    /**
     * Constructor for {@link GlobalDecl} that initializes a {@code global} variable.
     * @param metaData {@link Token} containing the source code metadata that will be stored with this node.
     * @param globalVariable A {@link Var} instance containing information about the global variable.
     */
    public GlobalDecl(Token metaData, Var globalVariable) { this(metaData,globalVariable,false); }

    /**
     * Main constructor for {@link GlobalDecl}.
     * @param metaData {@link Token} containing the source code metadata that will be stored with this node.
     * @param globalVariable A {@link Var} instance containing information about the global variable.
     * @param isConstant A boolean value denoting if the global variable actually represents a constant.
     */
    public GlobalDecl(Token metaData, Var globalVariable, boolean isConstant) {
        super(metaData);
        this.globalVariable = globalVariable;
        this.isConstant = isConstant;

        addChildNode(this.globalVariable);
    }

    /**
     * Checks if the current {@link GlobalDecl} represents a constant variable.
     * @return {@code True} if the {@link GlobalDecl} is a constant, {@code False} otherwise.
     */
    public boolean isConstant() { return isConstant; }

    /**
     * {@inheritDoc}
     */
    public boolean hasInitialValue() { return globalVariable.getInitialValue() != null; }

    /**
     * {@inheritDoc}
     */
    public Name getVariableName() { return globalVariable.getVariableName(); }

    /**
     * {@inheritDoc}
     */
    public Expression getInitialValue() { return globalVariable.getInitialValue(); }

    /**
     * {@inheritDoc}
     */
    public void setInitialValue(Expression init) { globalVariable.setInitialValue(init); }

    /**
     * {@inheritDoc}
     */
    public Type getDeclaredType() { return globalVariable.getDeclaratedType(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return globalVariable.toString(); }

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
    public String getDeclName() { return globalVariable.toString(); }

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
     */
    public boolean isGlobalDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public GlobalDecl asGlobalDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(int pos, AST node) { this.globalVariable = node.asSubNode().asVar();}

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new GlobalDeclBuilder()
                   .setMetaData(this)
                   .setVar(globalVariable.deepCopy().asSubNode().asVar())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitGlobalDecl(this); }

    /**
     * Internal class that builds a {@link GlobalDecl} object.
     */
    public static class GlobalDeclBuilder extends NodeBuilder {

        /**
         * {@link GlobalDecl} object we are building.
         */
        private final GlobalDecl gd = new GlobalDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link GlobalDeclBuilder}.
         */
        public GlobalDeclBuilder setMetaData(AST node) {
            super.setMetaData(gd,node);
            return this;
        }

        /**
         * Sets the global declaration's {@link #globalVariable}.
         * @param globalVariable {@link Var} that represents the variable created by the global declaration.
         * @return Current instance of {@link GlobalDeclBuilder}
         */
        public GlobalDeclBuilder setVar(Var globalVariable) {
            gd.globalVariable = globalVariable;
            return this;
        }

        /**
         * Creates a {@link GlobalDecl} object.
         * @return {@link GlobalDecl}
         */
        public GlobalDecl create() {
            gd.addChildNode(gd.globalVariable);
            return gd;
        }
    }
}
