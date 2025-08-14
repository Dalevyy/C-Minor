package cminor.ast.types;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Type} representing a null value.
 * <p>
 *     A {@link VoidType} is the last structured type, and it appears at the
 *     bottom of the structured type hierarchy. This type is mainly used when
 *     working with {@code Void} return types.
 * </p>
 * @author Daniel Levy
 */
public class VoidType extends Type {

    /**
     * Default constructor for {@link VoidType}.
     */
    public VoidType() { this(new Token()); }

    /**
     * Main constructor for {@link VoidType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public VoidType(Token metaData) { super(metaData); }

    /**
     * {@inheritDoc}
     */
    public boolean isVoidType() { return true; }

    /**
     * {@inheritDoc}
     */
    public VoidType asVoidType() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() { return "Void"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new VoidTypeBuilder()
                   .setMetaData(this)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitVoidType(this); }

    /**
     * Internal class that builds a {@link VoidType} object.
     */
    public static class VoidTypeBuilder extends NodeBuilder {

        /**
         * {@link VoidType} object we are building.
         */
        private final VoidType vt = new VoidType();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link VoidTypeBuilder}.
         */
        public VoidTypeBuilder setMetaData(AST node) {
            super.setMetaData(vt,node);
            return this;
        }

        /**
         * Creates a {@link VoidType} object.
         * @return {@link VoidType}
         */
        public VoidType create() {
            return vt;
        }
    }
}
