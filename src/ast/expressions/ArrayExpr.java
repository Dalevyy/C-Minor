package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the dereferencing of arrays.
 * <p><br>
 *     In C Minor, arrays are dereferenced in the same way as many other
 *     languages, specifically in the form {@code a[i]+}. {@code a} will
 *     represent the array that is dereferenced, and {@code [i]+} represents
 *     one or more possible indices the user wants to access. Alongside arrays,
 *     this node class will also represent list dereferences since they are
 *     syntactically the same as array dereferences.
 * </p>
 * @author Daniel Levy
 */
public class ArrayExpr extends Expression {

    /**
     * The array we are trying to access an element from.
     */
    private Expression target;

    /**
     * Vector containing each array index the user wants to access.
     */
    private Vector<Expression> index;

    /**
     * Default constructor for {@link ArrayExpr}.
     */
    public ArrayExpr() { this(new Token(),null,new Vector<>()); }

    /**
     * Main constructor for {@link ArrayExpr}.
     * @param metaData Token containing metadata we want to save
     * @param target Expression to save into {@link #target}
     * @param index Vector of expressions to save into {@link #index}
     */
    public ArrayExpr(Token metaData, Expression target, Vector<Expression> index) {
        super(metaData);
        this.target = target;
        this.index = index;

        addChild(this.target);
        addChild(this.index);
    }

    /**
     * Getter for {@link #target}.
     * @return Expression
     */
    public Expression getArrayTarget() { return target; }

    /**
     * Getter for {@link #index}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getArrayIndex() { return index; }

    /**
     * Setter for {@link #target}.
     * @param target Expression
     */
    private void setArrayTarget(Expression target) { this.target = target; }

    /**
     * Setter for {@link #index}.
     * @param index Vector of Expressions
     */
    private void setArrayIndices(Vector<Expression> index) { this.index = index; }

    /**
     * Checks if the current AST node is an {@link ArrayExpr}.
     * @return Boolean
     */
    public boolean isArrayExpr() { return true; }

    /**
     * Type cast method for {@link ArrayExpr}
     * @return ArrayExpr
     */
    public ArrayExpr asArrayExpr() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the name of the array we are accessing.
     */
    @Override
    public String toString() { return target.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                target = node.asExpression();
                break;
            default:
                index.remove(pos-1);
                index.add(pos-1,node.asExpression());
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ArrayExpr}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> indices = new Vector<>();
        for(Expression e : this.index)
            indices.add(e.deepCopy().asExpression());

        return new ArrayExprBuilder()
                   .setMetaData(this)
                   .setTarget(this.target.deepCopy().asExpression())
                   .setIndex(indices)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitArrayExpr(this); }

    /**
     * Internal class that builds an {@link ArrayExpr} object.
     */
    public static class ArrayExprBuilder extends NodeBuilder {

        /**
         * {@link ArrayExpr} object we are building.
         */
        private final ArrayExpr ae = new ArrayExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ArrayExprBuilder
         */
        public ArrayExprBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the array target.
         * @param target Expression representing the array we want to access
         * @return ArrayExprBuilder
         */
        public ArrayExprBuilder setTarget(Expression target) {
            ae.setArrayTarget(target);
            return this;
        }

        /**
         * Sets the array indices.
         * @param index Vector of expressions that correspond to each index
         * @return ArrayExprBuilder
         */
        public ArrayExprBuilder setIndex(Vector<Expression> index) {
            ae.setArrayIndices(index);
            return this;
        }

        /**
         * Creates an {@link ArrayExpr} object.
         * @return {@link ArrayExpr}
         */
        public ArrayExpr create() {
            super.saveMetaData(ae);
            ae.addChild(ae.getArrayTarget());
            ae.addChild(ae.getArrayIndex());
            return ae;
        }
    }
}
