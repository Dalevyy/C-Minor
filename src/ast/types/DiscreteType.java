package ast.types;

import ast.AST;
import ast.operators.UnaryOp;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/*
___________________________ DiscreteType ___________________________
The second level of C Minor's primitive types will be discrete types
denoted by the DiscreteType node. These types will include Int, Char,
Bool, and also Enum.
____________________________________________________________________
*/
public class DiscreteType extends Type {

    public enum Discretes { INT, CHAR, BOOL, ENUM }
    private static final Vector<String> names = new Vector<>(new String[]{ "Int", "Char", "Bool", "Enum" });

    protected Discretes specificType;

    public DiscreteType() { this(new Token(),null); }
    public DiscreteType(Discretes d) { this(new Token(),d); }
    public DiscreteType(Token t, Discretes d) {
        super(t);
        this.specificType = d;
    }

    public boolean isDiscreteType() { return true; }
    public DiscreteType asDiscreteType() { return this; }
    public Discretes getDiscreteType() { return specificType; }

    private void setSpecificType(Discretes specificType) {
        this.specificType = specificType;
    }

    @Override
    public String typeName() { return names.get(specificType.ordinal()); }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link DiscreteType}
     */
    @Override
    public AST deepCopy() {
        return new DiscreteTypeBuilder()
                   .setMetaData(this)
                   .setSpecificDiscreteType(this.specificType)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitDiscreteType(this); }

    /**
     * Internal class that builds a {@link DiscreteType} object.
     */
    public static class DiscreteTypeBuilder extends NodeBuilder {

        /**
         * {@link DiscreteType} object we are building.
         */
        private final DiscreteType dt = new DiscreteType();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return DiscreteTypeBuilder
         */
        public DiscreteTypeBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the discrete type's {@link #specificType}.
         * @param specificType {@link Discretes} representing the actual type the discrete type represents
         * @return DiscreteTypeBuilder
         */
        public DiscreteTypeBuilder setSpecificDiscreteType(Discretes specificType) {
            dt.setSpecificType(specificType);
            return this;
        }

        /**
         * Creates a {@link DiscreteType} object.
         * @return {@link DiscreteType}
         */
        public DiscreteType create() {
            super.saveMetaData(dt);
            return dt;
        }
    }
}
