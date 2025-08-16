package cminor.ast.types;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A structured {@link Type} representing an array.
 * <p>
 *     In C Minor, an array represents a block of continuous memory
 *     that is statically sized. During compilation, the size of an
 *     array must be known, and the user is not able to change the
 *     size of an array once declared.
 * </p>
 * @author Daniel Levy
 */
public class ArrayType extends Type {

    /**
     * The number of dimensions represented by the current {@link ArrayType}.
     */
    public int dims;

    /**
     * The {@link Type} that the actual elements inside the array represents.
     */
    protected Type baseType;

    /**
     * Default constructor for {@link ArrayType}.
     */
    public ArrayType() { this(new Token(),null,0); }

    /**
     * Main constructor for {@link ArrayType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param baseType {@link Type} to store into {@link #baseType}.
     * @param dims {@code int} to store into {@link #dims}.
     */
    public ArrayType(Token metaData, Type baseType, int dims) {
        super(metaData);
        this.baseType = baseType;
        this.dims = dims;
    }

    /**
     * Checks if two arrays are assignment compatible with each other.
     * <p>
     *     For two arrays to be assignment compatible, there are two criteria that needs to be met.
     *     <ol>
     *         <li>Both arrays must have the same number of dimensions.</li>
     *         <li>Both arrays must have the same base type.</li>
     *     </ol>
     * </p>
     * @param LHS The first {@link ArrayType} to compare.
     * @param RHS The second {@link ArrayType} to compare.
     * @return {@code True} if the arrays are assignment compatible with each other, {@code False} otherwise.
     */
    public static boolean isArrayAssignmentCompatible(ArrayType LHS, ArrayType RHS) {
        if(LHS.getDims() != RHS.getDims())
            return false;

        if(!Type.assignmentCompatible(LHS.getBaseType(), RHS.getBaseType())) {
            // REMEMBER!!!! A variable can be assigned an empty list.
            return LHS.getBaseType().isVoid() || RHS.getBaseType().isVoid();
        }

        return true;
    }

    /**
     * Getter method for {@link #dims}.
     * @return The number of dimensions the {@link ArrayType} has.
     */
    public int getDims() { return dims; }

    /**
     * Getter method for {@link #baseType}.
     * @return The {@link Type} that the array's elements stores.
     */
    public Type getBaseType() { return baseType; }

    /**
     * Setter method for {@link #baseType}.
     * @param baseType {@link Type} we wish to replace the array's {@link #baseType}.
     */
    public void setBaseType(Type baseType) { this.baseType = baseType; }

    /**
     * {@inheritDoc}
     */
    public boolean isArray() { return true; }

    /**
     * {@inheritDoc}
     */
    public ArrayType asArray() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= dims; i++) {
            if(i == dims)
                sb.append(baseType.getTypeName());
            else
                sb.append("Array[");
        }

        sb.append("]".repeat(Math.max(0, dims)));
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ArrayTypeBuilder()
                   .setMetaData(this)
                   .setBaseType(baseType.deepCopy().asType())
                   .setNumOfDims(dims)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
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
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ArrayTypeBuilder}.
         */
        public ArrayTypeBuilder setMetaData(AST node) {
            super.setMetaData(at,node);
            return this;
        }

        /**
         * Sets the array type's {@link #baseType}.
         * @param baseType {@link Type} that represents the values stored by the array.
         * @return Current instance of {@link ArrayTypeBuilder}.
         */
        public ArrayTypeBuilder setBaseType(Type baseType) {
            at.baseType = baseType;
            return this;
        }

        /**
         * Sets the array type's {@link #dims}.
         * @param dims {@code Int} representing how many dimensions the array type has.
         * @return Current instance of {@link ArrayTypeBuilder}.
         */
        public ArrayTypeBuilder setNumOfDims(int dims) {
            at.dims = dims;
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
