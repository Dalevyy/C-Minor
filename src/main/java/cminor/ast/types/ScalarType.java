package cminor.ast.types;

import cminor.ast.*;
import cminor.token.*;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/*
___________________________ ScalarType ___________________________
The first level of C Minor's primitive types will be scalar types
denoted by the ScalarType node. These types will include String,
Text, and Real.
__________________________________________________________________
*/
public class ScalarType extends Type {

    public enum Scalars { STR, TEXT, REAL }
    private static final Vector<String> names = new Vector<>(new String[]{"String", "Text", "Real" });

    private Scalars specificType;

    public ScalarType() { this(new Token(),null); }

    public ScalarType(Scalars s) {
        super(new Token());
        this.specificType = s;
    }

    public ScalarType(Token t, Scalars s) {
        super(t);
        this.specificType = s;
    }

    public boolean isScalarType() { return true; }
    public ScalarType asScalarType() { return this; }
    public Scalars getScalarType() { return specificType; }

    private void setSpecificType(Scalars specificType) {
        this.specificType = specificType;
    }

    @Override
    public String typeName() { return names.get(specificType.ordinal()); }

    @Override
    public String toString() { return typeName();  }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ScalarType}
     */
    @Override
    public AST deepCopy() {
        return new ScalarTypeBuilder()
                   .setMetaData(this)
                   .setSpecificScalarType(this.specificType)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitScalarType(this); }

    /**
     * Internal class that builds a {@link ScalarType} object.
     */
    public static class ScalarTypeBuilder extends NodeBuilder {

        /**
         * {@link ScalarType} object we are building.
         */
        private final ScalarType st = new ScalarType();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ScalarTypeBuilder
         */
        public ScalarTypeBuilder setMetaData(AST node) {
            super.setMetaData(st,node);
            return this;
        }

        /**
         * Sets the scalar type's {@link #specificType}.
         * @param specificType {@link Scalars} representing the actual type the scalar type represents
         * @return ScalarTypeBuilder
         */
        public ScalarTypeBuilder setSpecificScalarType(Scalars specificType) {
            st.setSpecificType(specificType);
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
