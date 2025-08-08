package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing an input statement.
 * <p><br>
 *     C Minor will mimic the input statements found in C++. Thus,
 *     a user must write 'cin >> ... ' whenever they wish to take
 *     in user input from the console. In this case, the user input
 *     can only be stored in variables of {@link ast.types.ScalarType},
 *     and we will have additional semantic checks to make sure the
 *     user wrote their input statements correctly.
 * </p>
 * @author Daniel Levy
 */
public class InStmt extends Expression {

    /**
     * Vector that *should* contain all name expressions representing input variables.
     */
    private Vector<Expression> inputExprs;

    /**
     * Default constructor for {@link InStmt}.
     */
    public InStmt() { this(new Token(),new Vector<>()); }

    /**
     * Main constructor for {@link InStmt}.
     * @param metaData Token containing metadata we want to save
     * @param inputExprs Vector of expressions to save into {@link #inputExprs}
     */
    public InStmt(Token metaData, Vector<Expression> inputExprs) {
        super(metaData);
        this.inputExprs = inputExprs;

        addChildNode(this.inputExprs);
    }

    /**
     * Getter for {@link #inputExprs}
     * @return Vector of Expressions
     */
    public Vector<Expression> getInExprs() { return this.inputExprs; }

    /**
     * Setter for {@link #inputExprs}
     * @param exprs Vector of Expressions
     */
    private void setInExprs(Vector<Expression> exprs) { this.inputExprs = exprs; }

    /**
     * Checks if the current AST node is an {@link InStmt}.
     * @return Boolean
     */
    public boolean isInStmt() { return true; }

    /**
     * Type cast method for {@link InStmt}
     * @return InStmt
     */
    public InStmt asInStmt() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        inputExprs.remove(pos);
        inputExprs.add(pos,node.asExpression());
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link InStmt}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> inputExprs = new Vector<>();
        for(Expression expr : this.inputExprs)
            inputExprs.add(expr.deepCopy().asExpression());

        return new InStmtBuilder()
                   .setMetaData(this)
                   .setInExprs(inputExprs)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitInStmt(this); }

    /**
     * Internal class that builds an {@link InStmt} object.
     */
    public static class InStmtBuilder extends NodeBuilder {

        /**
         * {@link InStmt} object we are building.
         */
        private final InStmt in = new InStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return InStmtBuilder
         */
        public InStmtBuilder setMetaData(AST node) {
            super.setMetaData(in,node);
            return this;
        }

        /**
         * Sets the input statement's {@link #inputExprs}.
         * @param inputExprs Vector of expressions containing each input variable
         * @return InStmtBuilder
         */
        public InStmtBuilder setInExprs(Vector<Expression> inputExprs) {
            in.setInExprs(inputExprs);
            return this;
        }

        /**
         * Creates an {@link InStmt} object.
         * @return {@link InStmt}
         */
        public InStmt create() {
            in.addChildNode(in.inputExprs);
            return in;
        }
    }
}
