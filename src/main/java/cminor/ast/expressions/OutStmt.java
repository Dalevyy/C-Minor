package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;


/**
 * An {@link AST} node class representing an output statement.
 * <p><br>
 *     C Minor will mimic the output statements found in C++. Thus,
 *     a user must write 'cout << ... ' whenever they wish to print
 *     something to the console.
 * </p>
 * @author Daniel Levy
 */
public class OutStmt extends Expression {

    /**
     * Vector containing all expressions that will be printed out.
     * <p><br>
     *     An expression in this context is anything in between either
     *     two {@code <<} operators or an expression that follows a
     *     single {@code <<} operator.
     * </p>
     */
    private Vector<Expression> outputExprs;

    /**
     * Default constructor for {@link OutStmt}.
     */
    public OutStmt() { this(new Token(),new Vector<>()); }

    /**
     * Main constructor for {@link OutStmt}.
     * @param metaData Token containing metadata we want to save
     * @param outputExprs Vector of expressions to save into {@link #outputExprs}
     */
    public OutStmt(Token metaData, Vector<Expression> outputExprs) {
        super(metaData);
        this.outputExprs = outputExprs;

        addChildNode(this.outputExprs);
    }

    /**
     * Getter for {@link #outputExprs}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getOutExprs() { return outputExprs; }

    /**
     * Setter for {@link #outputExprs}.
     * @param exprs Vector of Expressions
     */
    private void setOutExprs(Vector<Expression> exprs) { this.outputExprs = exprs; }

    /**
     * Checks if the current AST node is an {@link OutStmt}.
     * @return Boolean
     */
    public boolean isOutStmt() { return true; }

    /**
     * Type cast method for {@link OutStmt}.
     * @return OutStmt
     */
    public OutStmt asOutStmt() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        this.outputExprs.remove(pos);
        this.outputExprs.add(pos,node.asExpression());
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link OutStmt}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> outputExprs = new Vector<>();
        for(Expression expr : this.outputExprs)
            outputExprs.add(expr.deepCopy().asExpression());

        return new OutStmtBuilder()
                   .setMetaData(this)
                   .setOutExprs(outputExprs)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitOutStmt(this); }

    /**
     * Internal class that builds an {@link OutStmt} object.
     */
    public static class OutStmtBuilder extends NodeBuilder {

        /**
         * {@link OutStmt} object we are building.
         */
        private final OutStmt os = new OutStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return OutStmtBuilder
         */
        public OutStmtBuilder setMetaData(AST node) {
            super.setMetaData(os,node);
            return this;
        }

        /**
         * Sets the input statement's {@link #outputExprs}.
         * @param outputExprs Vector of expressions that will be outputted to the terminal
         * @return OutStmtBuilder
         */
        public OutStmtBuilder setOutExprs(Vector<Expression> outputExprs) {
            os.setOutExprs(outputExprs);
            return this;
        }

        /**
         * Creates an {@link OutStmt} object.
         * @return {@link OutStmt}
         */
        public OutStmt create() {
            os.addChildNode(os.outputExprs);
            return os;
        }
    }
}
