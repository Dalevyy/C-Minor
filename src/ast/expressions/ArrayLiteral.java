package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the creation of an array within
 * C Minor.
 * <p><br>
 *     An array is created by typing {@code Array[i]*(a)} where {@code [i]*}
 *     represents the number of dimensions the array will have and {@code a}
 *     will be any initial values the array has. If a user wants to allocate
 *     memory for a 1D array, they may choose to omit the dimensions. Thus,
 *     the number of initial values specified for the array will be the number
 *     of memory locations the array has access to. For multidimensional arrays,
 *     the dimensions must be specified or else a type error will be generated.
 * </p>
 * @author Daniel Levy
 */
public class ArrayLiteral extends Literal {

    /**
     * Vector containing each dimension of the array.
     */
    private Vector<Expression> dims;

    /**
     * Vector containing all initial values of the array. (if applicable)
     */
    private Vector<Expression> inits;

    /**
     * Number of dimensions the {@link ArrayLiteral} has.
     */
    private final int numOfDims;

    /**
     * Default constructor for {@link ArrayLiteral}.
     */
    public ArrayLiteral() { this(new Token(),new Vector<>(),new Vector<>()); }

    /**
     * Main constructor for {@link ArrayLiteral}.
     * @param metaData Token containing metadata we want to save
     * @param dims Vector of expressions to save into {@link #dims}
     * @param inits Vector of expressions to save into {@link #inits}
     */
    public ArrayLiteral(Token metaData, Vector<Expression> dims, Vector<Expression> inits) {
        super(metaData, ConstantType.ARR);
        this.dims = dims;
        this.inits = inits;
        this.numOfDims = dims.isEmpty() ? 1 : dims.size();

        addChildNode(this.dims);
        addChildNode(this.inits);
    }

    /**
     * Getter for {@link #dims}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getArrayDims() { return dims; }

    /**
     * Getter for {@link #inits}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getArrayInits() { return inits; }

    /**
     * Getter for {@link #numOfDims}.
     * @return Int
     */
    public int getNumOfDims() { return numOfDims; }

    /**
     * Setter for {@link #dims}.
     * @param dims Vector of Expressions
     */
    private void setArrayDims(Vector<Expression> dims) { this.dims = dims; }

    /**
     * Setter for {@link #inits}.
     * @param inits Vector of Expressions
     */
    private void setArrayInits(Vector<Expression> inits) { this.inits = inits; }

    /**
     * Checks if the current AST node is an {@link ArrayLiteral}.
     * @return Boolean
     */
    public boolean isArrayLiteral() { return true; }

    /**
     * Type cast method for {@link ArrayLiteral}
     * @return ArrayLiteral
     */
    public ArrayLiteral asArrayLiteral() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        if(pos < dims.size()) {
            dims.remove(pos);
            dims.add(pos,node.asExpression());
        }
        else {
            pos -= inits.size();
            inits.remove(pos);
            inits.add(pos,node.asExpression());
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ArrayLiteral}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> dims = new Vector<>();
        Vector<Expression> inits = new Vector<>();

        for(Expression expr : this.dims)
            dims.add(expr.deepCopy().asExpression());
        for(Expression expr : this.inits)
            dims.add(expr.deepCopy().asExpression());

        return new ArrayLiteralBuilder()
                   .setMetaData(this)
                   .setArrayDims(dims)
                   .setArrayInits(inits)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitArrayLiteral(this); }

    /**
     * Internal class that builds an {@link ArrayExpr} object.
     */
    public static class ArrayLiteralBuilder extends NodeBuilder {

        /**
         * {@link ArrayLiteral} object we are building.
         */
        private final ArrayLiteral al = new ArrayLiteral();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ArrayLiteralBuilder
         */
        public ArrayLiteralBuilder setMetaData(AST node) {
            super.setMetaData(al,node);
            return this;
        }

        /**
         * Sets the array literal's dimensions.
         * @param dims Vector of expressions containing each dimension
         * @return ArrayLiteralBuilder
         */
        public ArrayLiteralBuilder setArrayDims(Vector<Expression> dims) {
            al.setArrayDims(dims);
            return this;
        }

        /**
         * Sets the array literal's initial values.
         * @param inits Vector of expressions that contain all initial values stored in the array.
         * @return ArrayLiteralBuilder
         */
        public ArrayLiteralBuilder setArrayInits(Vector<Expression> inits) {
            al.setArrayInits(inits);
            return this;
        }

        /**
         * Creates an {@link ArrayLiteral} object.
         * @return {@link ArrayLiteral}
         */
        public ArrayLiteral create() {
            al.addChildNode(al.getArrayDims());
            al.addChildNode(al.getArrayInits());
            return al;
        }
    }
}
