package ast.operators;

import ast.AST;
import token.Token;

public abstract class Operator extends AST {

    /**
     * Default constructor for {@link Operator}.
     * @param metaData Token containing metadata we want to save
     */
    public Operator(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is an {@link AssignOp}.
     * @return Boolean
     */
    public boolean isAssignOp() { return false; }

    /**
     * Checks if the current AST node is a {@link BinaryOp}.
     * @return Boolean
     */
    public boolean isBinaryOp() { return false; }

    /**
     * Checks if the current AST node is a {@link LoopOp}.
     * @return Boolean
     */
    public boolean isLoopOp() { return false; }

    /**
     * Checks if the current AST node is an {@link Operator}.
     * @return Boolean
     */
    public boolean isOperator() { return true; }

    /**
     * Checks if the current AST node is a {@link UnaryOp}.
     * @return Boolean
     */
    public boolean isUnaryOp() { return false; }

    /**
     * Type cast method for {@link AssignOp}.
     * @return AssignOp
     */
    public AssignOp asAssignOp() { throw new RuntimeException("Expression can not be casted into an AssignOp.\n"); }

    /**
     * Type cast method for {@link BinaryOp}.
     * @return BinaryOp
     */
    public BinaryOp asBinaryOp() { throw new RuntimeException("Expression can not be casted into a BinaryOp.\n"); }

    /**
     * Type cast method for {@link LoopOp}.
     * @return LoopOp
     */
    public LoopOp asLoopOp() { throw new RuntimeException("Expression can not be casted into a LoopOp.\n"); }

    /**
     * Type cast method for {@link Operator}.
     * @return Operator
     */
    public Operator asOperator() { return this; }

    /**
     * Type cast method for {@link UnaryOp}.
     * @return UnaryOp
     */
    public UnaryOp asUnaryOp() { throw new RuntimeException("Expression can not be casted into a UnaryOp.\n"); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("An operator can not be updated."); }
}
