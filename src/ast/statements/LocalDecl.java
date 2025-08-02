package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.misc.Name;
import ast.misc.NameDecl;
import ast.misc.Var;
import ast.misc.VarDecl;
import ast.types.*;
import token.*;
import utilities.Visitor;

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

        addChild(this.localVariable);
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
    public Type getDeclaredType() { return localVariable.getDeclaratedType(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return localVariable.toString(); }

    /**
     * {@inheritDoc}
     */
    public AST asAST() { return this; }

    public AST getDecl() { return this; }
    public String getDeclName() { return localVariable.toString(); }

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

    @Override
    public void update(int pos, AST node) { localVariable = node.asVar(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link LocalDecl}
     */
    @Override
    public AST deepCopy() {
        return new LocalDeclBuilder()
                   .setMetaData(this)
                   .setVar(this.localVariable.deepCopy().asVar())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitLocalDecl(this); }

    public static class LocalDeclBuilder extends NodeBuilder {
        private final LocalDecl ld = new LocalDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return LocalDeclBuilder
         */
        public LocalDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the local declaration's {@link #localVariable}.
         * @param var Variable that represents the local declaration
         * @return LocalDeclBuilder
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
            super.saveMetaData(ld);
            ld.addChild(ld.localVariable);
            return ld;
        }
    }
}
