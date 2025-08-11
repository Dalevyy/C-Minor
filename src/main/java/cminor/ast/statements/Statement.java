package cminor.ast.statements;

import cminor.ast.AST;
import cminor.token.Token;

/**
 * An {@link AST} subtype that represents all statements that are executed at runtime.
 * @author Daniel Levy
 */
public abstract class Statement extends AST {

    /**
     * Default constructor for {@link Statement}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public Statement (Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is an {@link AssignStmt}.
     * @return {@code True} if the node is an {@link AssignStmt}, {@code False} otherwise.
     */
    public boolean isAssignStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link BlockStmt}.
     * @return {@code True} if the node is a {@link BlockStmt}, {@code False} otherwise.
     */
    public boolean isBlockStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link CaseStmt}.
     * @return {@code True} if the node is a {@link CaseStmt}, {@code False} otherwise.
     */
    public boolean isCaseStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link ChoiceStmt}.
     * @return {@code True} if the node is a {@link ChoiceStmt}, {@code False} otherwise.
     */
    public boolean isChoiceStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link DoStmt}.
     * @return {@code True} if the node is a {@link DoStmt}, {@code False} otherwise.
     */
    public boolean isDoStmt() { return false; }

    /**
     * Checks if the current AST node is an {@link ExprStmt}.
     * @return {@code True} if the node is an {@link ExprStmt}, {@code False} otherwise.
     */
    public boolean isExprStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link ForStmt}.
     * @return {@code True} if the node is a {@link ForStmt}, {@code False} otherwise.
     */
    public boolean isForStmt() { return false; }

    /**
     * Checks if the current AST node is an {@link IfStmt}.
     * @return {@code True} if the node is an {@link IfStmt}, {@code False} otherwise.
     */
    public boolean isIfStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link ListStmt}.
     * @return {@code True} if the node is a {@link ListStmt}, {@code False} otherwise.
     */
    public boolean isListStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link LocalDecl}.
     * @return {@code True} if the node is a {@link LocalDecl}, {@code False} otherwise.
     */
    public boolean isLocalDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link ReturnStmt}.
     * @return {@code True} if the node is a {@link ReturnStmt}, {@code False} otherwise.
     */
    public boolean isReturnStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link RetypeStmt}.
     * @return {@code True} if the node is a {@link RetypeStmt}, {@code False} otherwise.
     */
    public boolean isRetypeStmt() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isStatement() { return true; }

    /**
     * Checks if the current AST node is a {@link StopStmt}.
     * @return {@code True} if the node is a {@link StopStmt}, {@code False} otherwise.
     */
    public boolean isStopStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link WhileStmt}.
     * @return {@code True} if the node is a {@link WhileStmt}, {@code False} otherwise.
     */
    public boolean isWhileStmt() { return false; }

    /**
     * Explicitly casts the current node into an {@link AssignStmt}.
     * @return The current node as an {@link AssignStmt}.
     */
    public AssignStmt asAssignStmt() {
        throw new RuntimeException("The current node does not represent an assignment statement.");
    }

    /**
     * Explicitly casts the current node into a {@link BlockStmt}.
     * @return The current node as a {@link BlockStmt}.
     */
    public BlockStmt asBlockStmt() {
        throw new RuntimeException("The current node does not represent a block statement.");
    }

    /**
     * Explicitly casts the current node into a {@link CaseStmt}.
     * @return The current node as a {@link CaseStmt}.
     */
    public CaseStmt asCaseStmt() {
        throw new RuntimeException("The current node does not represent a case statement.");
    }

    /**
     * Explicitly casts the current node into a {@link ChoiceStmt}.
     * @return The current node as a {@link ChoiceStmt}.
     */
    public ChoiceStmt asChoiceStmt() {
        throw new RuntimeException("The current node does not represent a choice statement.");
    }

    /**
     * Explicitly casts the current node into a {@link DoStmt}.
     * @return The current node as a {@link DoStmt}.
     */
    public DoStmt asDoStmt() {
        throw new RuntimeException("The current node does not represent a do statement.");
    }

    /**
     * Explicitly casts the current node into an {@link ExprStmt}.
     * @return The current node as an {@link ExprStmt}.
     */
    public ExprStmt asExprStmt() {
        throw new RuntimeException("The current node does not represent an expression statement.");
    }

    /**
     * Explicitly casts the current node into a {@link ForStmt}.
     * @return The current node as a {@link ForStmt}.
     */
    public ForStmt asForStmt() {
        throw new RuntimeException("The current node does not represent a for statement.");
    }

    /**
     * Explicitly casts the current node into an {@link IfStmt}.
     * @return The current node as an {@link IfStmt}.
     */
    public IfStmt asIfStmt() {
        throw new RuntimeException("The current node does not represent an if statement.");
    }

    /**
     * Explicitly casts the current node into a {@link ListStmt}.
     * @return The current node as a {@link ListStmt}.
     */
    public ListStmt asListStmt() {
        throw new RuntimeException("The current node does not represent a list statement.");
    }

    /**
     * Explicitly casts the current node into a {@link LocalDecl}.
     * @return The current node as a {@link LocalDecl}.
     */
    public LocalDecl asLocalDecl() {
        throw new RuntimeException("The current node does not represent a local declaration.");
    }

    /**
     * Explicitly casts the current node into a {@link ReturnStmt}.
     * @return The current node as a {@link ReturnStmt}.
     */
    public ReturnStmt asReturnStmt() {
        throw new RuntimeException("The current node does not represent a return statement.");
    }

    /**
     * Explicitly casts the current node into a {@link RetypeStmt}.
     * @return The current node as a {@link RetypeStmt}.
     */
    public RetypeStmt asRetypeStmt() {
        throw new RuntimeException("The current node does not represent a retype statement.");
    }

    /**
     * {@inheritDoc}
     */
    public Statement asStatement() { return this; }

    /**
     * Explicitly casts the current node into a {@link StopStmt}.
     * @return The current node as a {@link StopStmt}.
     */
    public StopStmt asStopStmt() {
        throw new RuntimeException("The current node does not represent a stop statement.");
    }

    /**
     * Explicitly casts the current node into a {@link WhileStmt}.
     * @return The current node as a {@link WhileStmt}.
     */
    public WhileStmt asWhileStmt() {
        throw new RuntimeException("The current node does not represent a while statement.");
    }
}
