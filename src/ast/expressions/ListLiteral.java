package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the creation of a list within
 * C Minor.
 * <p><br>
 *     A list is created by typing {@code List(a)} where {@code a} will be any
 *     initial values the list will have. Since lists are dynamic in C Minor,
 *     the user does not need to specify any dimensions when allocating memory
 *     for a list. ThisStmt will be determined by the compiler.
 * </p>
 * @author Daniel Levy
 */
public class ListLiteral extends Literal {

    /**
     * Vector of expressions representing the initial values stored in the list.
     */
    private Vector<Expression> inits;

    /**
     * Default constructor for {@link ListLiteral}.
     */
    public ListLiteral() { this(new Token(),new Vector<>()); }

    /**
     * Main constructor for {@link ListLiteral}.
     * @param metaData Token containing metadata we want to save
     * @param inits Vector of expressions to save into {@link #inits}
     */
    public ListLiteral(Token metaData, Vector<Expression> inits) {
        super(metaData, ConstantType.LIST);
        this.inits = inits;

        addChild(this.inits);
    }

    /**
     * Getter for {@link #inits}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getInits() { return this.inits; }

    /**
     * Setter for {@link #inits}.
     * @param inits Vector of Expressions
     */
    private void setInits(Vector<Expression> inits) { this.inits = inits; }

    /**
     * Checks if the current AST node is a {@link ListLiteral}.
     * @return Boolean
     */
    public boolean isListLiteral() { return true; }

    /**
     * Type cast method for {@link ListLiteral}
     * @return ListLiteral
     */
    public ListLiteral asListLiteral() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        this.inits.remove(pos);
        this.inits.add(pos,node.asExpression());
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ListLiteral}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> inits = new Vector<>();
        for(Expression expr : this.inits)
            inits.add(expr.deepCopy().asExpression());

        return new ListLiteralBuilder()
                   .setMetaData(this)
                   .setInits(inits)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitListLiteral(this); }

    /**
     * Internal class that builds a {@link ListLiteral} object.
     */
    public static class ListLiteralBuilder extends NodeBuilder {

        /**
         * {@link ListLiteral} object we are building.
         */
        private final ListLiteral ll = new ListLiteral();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ListLiteralBuilder
         */
        public ListLiteralBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the list literal's {@link #inits}.
         * @param inits Vector of expressions containing each initial value for the list
         * @return ListLiteralBuilder
         */
        public ListLiteralBuilder setInits(Vector<Expression> inits) {
            ll.setInits(inits);
            return this;
        }

        /**
         * Creates a {@link ListLiteral} object.
         * @return {@link ListLiteral}
         */
        public ListLiteral create() {
            super.saveMetaData(ll);
            ll.addChild(ll.inits);
            return ll;
        }
    }
}
