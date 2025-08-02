package ast.operators;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing a binary operation.
 * <p>
 *     A {@link BinaryOp} is generated any time a {@link ast.expressions.BinaryExpr}
 *     is used. Many of the binary operations found in C++ and other C-like languages
 *     are supported in C Minor including the bitwise operations.
 * </p>
 * @author Daniel Levy
 */
public class BinaryOp extends Operator {

    /**
     * Enum representing all available binary operations.
     */
    public enum BinaryType { EQEQ, NEQ, GT, GTEQ, LT, LTEQ, LTGT, UFO, PLUS, MINUS, MULT, DIV,
                             MOD, EXP, SLEFT, SRIGHT, INSTOF, NINSTOF, AS, BAND, XOR, BOR, AND, OR }

    /**
     * Vector containing the string representation of {@link BinaryType}.
     */
    private static final Vector<String> names =
            new Vector<>(new String[]{ "==", "!=", ">", ">=", "<", "<=", "<>", "<=>", "+", "-", "*", "/", "%", "**",
                                       "<<", ">>", "instanceof", "!instanceof", "as?", "&", "^", "|", "and", "or"});

    /**
     * {@link BinaryType} representing the binary operation.
     */
    private BinaryType binOp;

    /**
     * Default constructor for {@link BinaryOp}.
     */
    public BinaryOp() { this(new Token(),null); }

    /**
     * Main constructor for {@link BinaryOp}.
     * @param metaData Token containing metadata we want to save
     * @param binOp {@link BinaryType} to save into {@link #binOp}
     */
    public BinaryOp(Token metaData, BinaryType binOp) {
        super(metaData);
        this.binOp = binOp;
    }

    /**
     * Getter for {@link #binOp}.
     * @return {@link BinaryType}
     */
    public BinaryType getBinaryType() { return this.binOp; }

    /**
     * Setter for {@link #binOp}.
     * @param binOp {@link BinaryType}
     */
    private void setBinOp(BinaryType binOp) { this.binOp = binOp; }

    /**
     * Checks if the current AST node is a {@link BinaryOp}.
     * @return Boolean
     */
    public boolean isBinaryOp() { return true; }

    /**
     * Type cast method for {@link BinaryOp}.
     * @return BinaryOp
     */
    public BinaryOp asBinaryOp() { return this; }

    public boolean equals(String op) { return this.toString().equals(op); }

    /**
     * {@code toString} method.
     * @return String representing the binary operation.
     */
    @Override
    public String toString() { return names.get(binOp.ordinal()); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link BinaryOp}
     */
    @Override
    public AST deepCopy() {
        return new BinaryOpBuilder()
                   .setMetaData(this)
                   .setBinaryOperator(this.binOp)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitBinaryOp(this); }

    /**
     * Internal class that builds a {@link BinaryOp} object.
     */
    public static class BinaryOpBuilder extends NodeBuilder {

        /**
         * {@link BinaryOp} object we are building.
         */
        private final BinaryOp bo = new BinaryOp();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return BinaryOpBuilder
         */
        public BinaryOpBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the binary op's {@link #binOp}.
         * @param binOp Binary operation that this node represents
         * @return BinaryOpBuilder
         */
        public BinaryOpBuilder setBinaryOperator(BinaryType binOp) {
            bo.setBinOp(binOp);
            return this;
        }

        /**
         * Creates a {@link BinaryOp} object.
         * @return {@link BinaryOp}
         */
        public BinaryOp create() {
            super.saveMetaData(bo);
            return bo;
        }
    }
}
