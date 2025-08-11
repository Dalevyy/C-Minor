package cminor.ast.operators;

import cminor.ast.AST;
import cminor.ast.statements.AssignStmt;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * An {@link AST} node class representing an assignment operation.
 * <p>
 *     An {@link AssignOp} is generated any time an {@link AssignStmt}
 *     is used. Many of the assignments operations found in C++ and other C-like languages
 *     are supported in C Minor.
 * </p>
 * @author Daniel Levy
 */
public class AssignOp extends Operator {

    /**
     * Enum representing all available assignment operations.
     */
    public enum AssignType { EQ, PLUSEQ, MINUSEQ, MULTEQ, DIVEQ, MODEQ, EXPEQ }

    /**
     * Vector containing the string representation of {@link AssignType}.
     */
    private static final Vector<String> names = new Vector<>(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "**="});

    /**
     * {@link AssignType} representing the assignment operation.
     */
    private AssignType assignOp;

    /**
     * Default constructor for {@link AssignOp}.
     */
    public AssignOp() { this(new Token(),null); }

    /**
     * Constructor to create a new {@link AssignOp} for an assignment and retype statement.
     * @param assignOp {@link AssignType} to save into {@link #assignOp}
     */
    public AssignOp(AssignType assignOp) { this(new Token(),assignOp); }

    /**
     * Main constructor for {@link AssignOp}.
     * @param metaData Token containing metadata we want to save
     * @param assignOp {@link AssignType} to save into {@link #assignOp}
     */
    public AssignOp(Token metaData, AssignType assignOp) {
        super(metaData);
        this.assignOp = assignOp;
    }

    /**
     * Getter for {@link #assignOp}.
     * @return {@link AssignType}
     */
    public AssignType getAssignOp() { return this.assignOp; }

    /**
     * Setter for {@link #assignOp}.
     * @param assignOp {@link AssignType}
     */
    private void setAssignOp(AssignType assignOp) { this.assignOp = assignOp; }

    /**
     * Checks if the current AST node is an {@link AssignOp}.
     * @return Boolean
     */
    public boolean isAssignOp() { return true; }

    /**
     * Type cast method for {@link AssignOp}.
     * @return AssignOp
     */
    public AssignOp asAssignOp() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the assignment operation.
     */
    @Override
    public String toString() { return names.get(assignOp.ordinal()); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link AssignOp}
     */
    @Override
    public AST deepCopy() {
        return new AssignOpBuilder()
                   .setMetaData(this)
                   .setAssignOperator(this.assignOp)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitAssignOp(this); }

    /**
     * Internal class that builds a {@link AssignOp} object.
     */
    public static class AssignOpBuilder extends NodeBuilder {

        /**
         * {@link AssignOp} object we are building.
         */
        private final AssignOp ao = new AssignOp();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return AssignOpBuilder
         */
        public AssignOpBuilder setMetaData(AST node) {
            super.setMetaData(ao,node);
            return this;
        }

        /**
         * Sets the binary op's {@link #assignOp}.
         * @param assignOp Assignment operation that this node represents
         * @return AssignOpBuilder
         */
        public AssignOpBuilder setAssignOperator(AssignType assignOp) {
            ao.setAssignOp(assignOp);
            return this;
        }

        /**
         * Creates a {@link AssignOp} object.
         * @return {@link AssignOp}
         */
        public AssignOp create() {
            return ao;
        }
    }
}
