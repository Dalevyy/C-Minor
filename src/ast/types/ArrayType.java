package ast.types;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * A structured type representing an array.
 * <p><br>
 *     In C Minor, an array represents a block of continuous memory
 *     that is statically sized. During compilation, the size of an
 *     array must be known, and the user is not able to change the
 *     size of an array once declared.
 * </p>
 * @author Daniel Levy
 */
public class ArrayType extends Type {

    private Type baseType;
    public int numOfDims;

    public ArrayType() { this(new Token(),null,0); }
    public ArrayType(Type bt, int num) { this(new Token(),bt,num); }
    public ArrayType(Token t, Type bt, int num) {
        super(t);
        this.baseType = bt;
        this.numOfDims = num;
    }

    public Type baseType() { return baseType; }

    public int getNumOfDims() {
        return numOfDims;
    }

    public void setBaseType(Type baseType) { this.baseType = baseType;}

    private void setNumOfDims(int numOfDims) {
        this.numOfDims = numOfDims;
    }

    public boolean isArrayType() { return true; }
    public ArrayType asArrayType() { return this; }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= numOfDims; i++) {
            if(i == numOfDims)
                sb.append(baseType.typeName());
            else
                sb.append("Array[");
        }

        sb.append("]".repeat(Math.max(0, numOfDims)));
        return sb.toString();
    }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ArrayType}
     */
    @Override
    public AST deepCopy() {
        return new ArrayTypeBuilder()
                   .setMetaData(this)
                   .setBaseType(this.baseType.deepCopy().asType())
                   .setNumOfDims(this.numOfDims)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitArrayType(this); }

    /**
     * Internal class that builds an {@link ArrayType} object.
     */
    public static class ArrayTypeBuilder extends NodeBuilder {

        /**
         * {@link ArrayType} object we are building.
         */
        private final ArrayType at = new ArrayType();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ArrayTypeBuilder
         */
        public ArrayTypeBuilder setMetaData(AST node) {
            super.setMetaData(at,node);
            return this;
        }

        /**
         * Sets the array type's {@link #baseType}.
         * @param baseType Type that represents the values stored by the array
         * @return ArrayTypeBuilder
         */
        public ArrayTypeBuilder setBaseType(Type baseType) {
            at.setBaseType(baseType);
            return this;
        }

        /**
         * Sets the array type's {@link #numOfDims}.
         * @param numOfDims Int representing how many dimensions the array has
         * @return ArrayTypeBuilder
         */
        public ArrayTypeBuilder setNumOfDims(int numOfDims) {
            at.setNumOfDims(numOfDims);
            return this;
        }

        /**
         * Creates a {@link ArrayType} object.
         * @return {@link ArrayType}
         */
        public ArrayType create() {
            return at;
        }
    }
}
