package ast.topleveldecls;

import ast.AST;
import ast.expressions.Expression;
import ast.misc.Name;
import ast.misc.NameDecl;
import ast.misc.Var;
import ast.misc.VarDecl;
import ast.types.*;
import token.Token;
import utilities.Visitor;

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

        addChild(this.globalVariable);
        setParent();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInitialValue() { return globalVariable.getInitialValue() != null; };

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

    // ????????????????
    public boolean isClassType() { return false; }
    public boolean isConstant() { return isConstant; }

    public boolean isGlobalDecl() { return true; }
    public GlobalDecl asGlobalDecl() { return this; }

    public AST getDecl() { return this; }
    public String getDeclName() { return globalVariable.toString(); };


    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link GlobalDecl}
     */
    @Override
    public AST deepCopy() {
        return new GlobalDeclBuilder()
                   .setMetaData(this)
                   .setVar(this.globalVariable.deepCopy().asVar())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitGlobalDecl(this); }

    public static class GlobalDeclBuilder extends NodeBuilder {
        private final GlobalDecl gd = new GlobalDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return GlobalDeclBuilder
         */
        public GlobalDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the global declaration's {@link #globalVariable}.
         * @param var Variable that represents the global declaration
         * @return GlobalDeclBuilder
         */
        public GlobalDeclBuilder setVar(Var var) {
            gd.globalVariable = var;
            return this;
        }

        /**
         * Creates a {@link GlobalDecl} object.
         * @return {@link GlobalDecl}
         */
        public GlobalDecl create() {
            super.saveMetaData(gd);
            gd.addChild(gd.globalVariable);
            return gd;
        }
    }
}
