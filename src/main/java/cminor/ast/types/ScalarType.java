package cminor.ast.types;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link Type} representing all primitive types in C Minor.
 * <p>
 *     A scalar is the name given to all primitive types. There are currently 7
 *     scalar types available in C Minor. When this particular class is instantiated,
 *     it will represent either a {@code Real}, {@code String}, or {@code Text} scalar type.
 * </p>
 * @author Daniel Levy
 */
public class ScalarType extends Type {

    /**
     * The actual type that the current {@link ScalarType} represents.
     */
    protected Scalars scalar;

    /**
     * Default constructor for {@link ScalarType}.
     */
    public ScalarType() { this(new Token(),null); }

    /**
     * Constructor to generate a {@link ScalarType} when given a specific type.
     * @param scalar {@link Scalars} to store into {@link #scalar}.
     */
    public ScalarType(Scalars scalar) { this(new Token(),scalar); }

    /**
     * Main constructor for {@link ScalarType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param scalar {@link Scalars} to store into {@link #scalar}.
     */
    public ScalarType(Token metaData, Scalars scalar) {
        super(metaData);
        this.scalar = scalar;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReal() { return scalar == Scalars.REAL; }

    /**
     * {@inheritDoc}
     */
    public boolean isString() { return scalar == Scalars.STR; }

    /**
     * {@inheritDoc}
     */
    public boolean isText() { return scalar == Scalars.TEXT; }

    /**
     * {@inheritDoc}
     */
    public boolean isScalar() { return true; }

    /**
     * {@inheritDoc}
     */
    public ScalarType asScalar() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() { return scalar.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ScalarTypeBuilder()
                   .setMetaData(this)
                   .setSpecificScalarType(scalar)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitScalarType(this); }

    /**
     * An enum that stores all possible primitive types that represent a {@link ScalarType}.
     */
    public enum Scalars {
        STR("String"),
        TEXT("Text"),
        REAL("Real"),
        INT("Int"),
        CHAR("Char"),
        BOOL("Bool"),
        ENUM("Enum");

        /**
         * The string representation of the type.
         */
        private final String type;

        /**
         * Default constructor for {@link Scalars}.
         * @param type {@link String} to store into {@link #type}.
         */
        Scalars(String type) { this.type = type; }

        /**
         * Returns the {@link #type} field.
         * @return String representation of the current {@link ScalarType}.
         */
        @Override
        public String toString() { return type; }
    }

    /**
     * Internal class that builds a {@link ScalarType} object.
     */
    public static class ScalarTypeBuilder extends NodeBuilder {

        /**
         * {@link ScalarType} object we are building.
         */
        private final ScalarType st = new ScalarType();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ScalarTypeBuilder}.
         */
        public ScalarTypeBuilder setMetaData(AST node) {
            super.setMetaData(st,node);
            return this;
        }

        /**
         * Sets the scalar type's {@link #scalar}.
         * @param scalar The {@link Scalars} value representing the actual type of the scalar type.
         * @return Current instance of {@link ScalarTypeBuilder}.
         */
        public ScalarTypeBuilder setSpecificScalarType(Scalars scalar) {
            st.scalar = scalar;
            return this;
        }

        /**
         * Creates a {@link ScalarType} object.
         * @return {@link ScalarType}
         */
        public ScalarType create() {
            return st;
        }
    }
}
