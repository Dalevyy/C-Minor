package ast.operators;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing a loop range operation.
 * <p>
 *     A {@link LoopOp} is generated any time a {@link ast.statements.ForStmt}
 *     is used. A loop operator denotes the range for which a for loop will
 *     iterate over. Additionally, this operator also denotes whether or not
 *     the starting and ending values are included in the range to loop over.
 * </p>
 * @author Daniel Levy
 */
public class LoopOp extends Operator {

    /**
     * Enum representing all available loop range operations.
     */
    public enum LoopType { INCL, EXCL, EXCL_L, EXCL_R }

    /**
     * Vector containing the string representation of {@link LoopType}.
     */
    private static final Vector<String> names = new Vector<>(new String[]{"..", "<..<", "<..", "..<"});

    /**
     * {@link LoopType} representing the loop range operation.
     */
    private LoopType loopOp;

    /**
     * Default constructor for {@link LoopOp}.
     */
    public LoopOp() { this(new Token(),null); }

    /**
     * Main constructor for {@link LoopOp}.
     * @param metaData Token containing metadata we want to save
     * @param loopOp {@link LoopType} to save into {@link #loopOp}
     */
    public LoopOp(Token metaData, LoopType loopOp) {
        super(metaData);
        this.loopOp = loopOp;
    }

    /**
     * Getter for {@link #loopOp}.
     * @return {@link LoopType}
     */
    public LoopType getLoopOp() { return this.loopOp; }

    /**
     * Setter for {@link #loopOp}.
     * @param loopOp {@link LoopType}
     */
    private void setLoopOp(LoopType loopOp) { this.loopOp = loopOp; }

    /**
     * Checks if the current AST node is a {@link LoopOp}.
     * @return Boolean
     */
    public boolean isLoopOp() { return true; }

    /**
     * Type cast method for {@link LoopOp}.
     * @return LoopOp
     */
    public LoopOp asLoopOp() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the loop range.
     */
    @Override
    public String toString() { return names.get(loopOp.ordinal()); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link LoopOp}
     */
    @Override
    public AST deepCopy() {
        return new LoopOpBuilder()
                   .setMetaData(this)
                   .setLoopOperator(this.loopOp)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitLoopOp(this); }

    /**
     * Internal class that builds a {@link LoopOp} object.
     */
    public static class LoopOpBuilder extends NodeBuilder {

        /**
         * {@link LoopOp} object we are building.
         */
        private final LoopOp lo = new LoopOp();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return LoopOpBuilder
         */
        public LoopOpBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the loop op's {@link #loopOp}.
         * @param loopOp Loop operation that represents the range to loop on
         * @return LoopOpBuilder
         */
        public LoopOpBuilder setLoopOperator(LoopType loopOp) {
            lo.setLoopOp(loopOp);
            return this;
        }

        /**
         * Creates a {@link LoopOp} object.
         * @return {@link LoopOp}
         */
        public LoopOp create() {
            super.saveMetaData(lo);
            return lo;
        }
    }
}
