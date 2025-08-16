package cminor.ast.operators;

import cminor.ast.AST;
import cminor.ast.expressions.UnaryExpr;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing a unary operation.
 * <p>
 *     A {@link UnaryOp} is generated any time a {@link UnaryExpr}
 *     is used. There are only two available operations: a binary negation and
 *     the 'not' operation.
 * </p>
 * @author Daniel Levy
 */
public class UnaryOp extends Operator {

    /**
     * Enum representing all available unary operations.
     */
    public enum UnaryType { BNOT, NOT }

    /**
     * Vector containing the string representation of {@link UnaryType}.
     */
    private static final Vector<String> names = new Vector<>(new String[]{"~", "not"});

    /**
     * {@link UnaryType} representing the unary operation.
     */
    private UnaryType unOp;

    /**
     * Default constructor for {@link UnaryOp}.
     */
    public UnaryOp() { this(new Token(),null); }

    /**
     * Main constructor for {@link UnaryOp}.
     * @param metaData Token containing metadata we want to save
     * @param unOp {@link UnaryType} to save into {@link #unOp}
     */
    public UnaryOp(Token metaData, UnaryType unOp) {
        super(metaData);
        this.unOp = unOp;
    }

    /**
     * Getter for {@link #unOp}.
     * @return {@link UnaryType}
     */
    public UnaryType getUnaryType() { return this.unOp; }

    /**
     * Setter for {@link #unOp}.
     * @param unOp {@link UnaryType}
     */
    private void setUnOp(UnaryType unOp) { this.unOp = unOp; }

    /**
     * Checks if the current AST node is a {@link UnaryOp}.
     * @return Boolean
     */
    public boolean isUnaryOp() { return true; }

    /**
     * Type cast method for {@link UnaryOp}.
     * @return UnaryOp
     */
    public UnaryOp asUnaryOp() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the unary operation.
     */
    @Override
    public String toString() { return names.get(unOp.ordinal()); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link UnaryOp}
     */
    @Override
    public AST deepCopy() {
        return new UnaryOpBuilder()
                   .setMetaData(this)
                   .setUnaryOperator(this.unOp)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitUnaryOp(this); }

    /**
     * Internal class that builds a {@link UnaryOp} object.
     */
    public static class UnaryOpBuilder extends NodeBuilder {

        /**
         * {@link UnaryOp} object we are building.
         */
        private final UnaryOp uo = new UnaryOp();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return UnaryOpBuilder
         */
        public UnaryOpBuilder setMetaData(AST node) {
            super.setMetaData(uo,node);
            return this;
        }

        /**
         * Sets the unary op's {@link #unOp}.
         * @param unOp Unary operation that this node represents
         * @return UnaryOpBuilder
         */
        public UnaryOpBuilder setUnaryOperator(UnaryType unOp) {
            uo.setUnOp(unOp);
            return this;
        }

        /**
         * Creates a {@link UnaryOp} object.
         * @return {@link UnaryOp}
         */
        public UnaryOp create() {
            return uo;
        }
    }
}
