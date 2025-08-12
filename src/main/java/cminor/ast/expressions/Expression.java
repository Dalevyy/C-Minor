package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.types.Type;
import cminor.token.Token;

/**
 * An abstract {@link AST} node class representing an expression.
 * @author Daniel Levy
 */
public abstract class Expression extends AST {

    /**
     * Type that the current expression will represent.
     */
    public Type type;

    /**
     * Default constructor for {@link Expression}.
     * @param metaData Token containing metadata we want to save
     */
    public Expression(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is an {@link ArrayExpr}.
     * @return Boolean
     */
    public boolean isArrayExpr() { return false; }

    /**
     * Checks if the current AST node is an {@link ArrayLiteral}.
     * @return Boolean
     */
    public boolean isArrayLiteral() { return false; }

    /**
     * Checks if the current AST node is a {@link BinaryExpr}.
     * @return Boolean
     */
    public boolean isBinaryExpr() { return false; }

    /**
     * Checks if the current AST node is a {@link BreakStmt}.
     * @return Boolean
     */
    public boolean isBreakStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link CastExpr}.
     * @return Boolean
     */
    public boolean isCastExpr() { return false; }

    /**
     * Checks if the current AST node is a {@link ContinueStmt}.
     * @return Boolean
     */
    public boolean isContinueStmt() { return false; }

    /**
     * Checks if the current AST node is an {@link EndlStmt}.
     * @return Boolean
     */
    public boolean isEndl() { return false; }

    /**
     * Checks if the current AST node is an {@link Expression}.
     * @return Boolean
     */
    public boolean isExpression() { return true; }

    /**
     * Checks if the current AST node is a {@link FieldExpr}.
     * @return Boolean
     */
    public boolean isFieldExpr() { return false; }

    /**
     * Checks if the current AST node is an {@link InStmt}.
     * @return Boolean
     */
    public boolean isInStmt() { return false; }

    /**
     * Checks if the current AST node is an {@link Invocation}.
     * @return Boolean
     */
    public boolean isInvocation() { return false; }

    /**
     * Checks if the current AST node is a {@link Literal}.
     * @return Boolean
     */
    public boolean isLiteral() { return false; }

    /**
     * Checks if the current AST node is a {@link ListLiteral}.
     * @return Boolean
     */
    public boolean isListLiteral() { return false; }

    /**
     * Checks if the current AST node is a {@link NameExpr}.
     * @return Boolean
     */
    public boolean isNameExpr() { return false; }

    /**
     * Checks if the current AST node is a {@link NewExpr}.
     * @return Boolean
     */
    public boolean isNewExpr() { return false; }

    /**
     * Checks if the current AST node is an {@link OutStmt}.
     * @return Boolean
     */
    public boolean isOutStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link ThisStmt}.
     * @return Boolean
     */
    public boolean isThisStmt() { return false; }

    /**
     * Checks if the current AST node is a {@link UnaryExpr}.
     * @return Boolean
     */
    public boolean isUnaryExpr() { return false; }

    /**
     * Type cast method for {@link ArrayExpr}.
     * @return ArrayExpr
     */
    public ArrayExpr asArrayExpr() { throw new RuntimeException("Expression can not be casted into an ArrayExpr.\n"); }

    /**
     * Type cast method for {@link ArrayLiteral}.
     * @return ArrayLiteral
     */
    public ArrayLiteral asArrayLiteral() { throw new RuntimeException("Expression can not be casted into an ArrayLiteral.\n"); }

    /**
     * Type cast method for {@link BinaryExpr}.
     * @return BinaryExpr
     */
    public BinaryExpr asBinaryExpr() { throw new RuntimeException("Expression can not be casted into a BinaryExpr.\n"); }

    /**
     * Type cast method for {@link BreakStmt}.
     * @return BreakStmt
     */
    public BreakStmt asBreakStmt() { throw new RuntimeException("Expression can not be casted into a BreakStmt.\n"); }

    /**
     * Type cast method for {@link CastExpr}.
     * @return CastExpr
     */
    public CastExpr asCastExpr() { throw new RuntimeException("Expression can not be casted into a CastExpr.\n"); }

    /**
     * Type cast method for {@link ContinueStmt}.
     * @return ContinueStmt
     */
    public ContinueStmt asContinueStmt() { throw new RuntimeException("Expression can not be casted into a ContinueStmt.\n"); }

    /**
     * Type cast method for {@link EndlStmt}.
     * @return EndlStmt
     */
    public EndlStmt asEndl() { throw new RuntimeException("Expression can not be casted into an EndlStmt.\n"); }

    /**
     * Type cast method for {@link Expression}.
     * @return Expression
     */
    public Expression asExpression() { return this; }

    /**
     * Type cast method for {@link FieldExpr}.
     * @return FieldExpr
     */
    public FieldExpr asFieldExpr() { throw new RuntimeException("Expression can not be casted into a FieldExpr.\n"); }

    /**
     * Type cast method for {@link InStmt}.
     * @return InStmt
     */
    public InStmt asInStmt() { throw new RuntimeException("Expression can not be casted into an InStmt.\n"); }

    /**
     * Type cast method for {@link Invocation}.
     * @return Invocation
     */
    public Invocation asInvocation() { throw new RuntimeException("Expression can not be casted into an Invocation.\n"); }

    /**
     * Type cast method for {@link Literal}.
     * @return Literal
     */
    public Literal asLiteral() { throw new RuntimeException("Expression can not be casted into a Literal.\n"); }

    /**
     * Type cast method for {@link ListLiteral}.
     * @return ListLiteral
     */
    public ListLiteral asListLiteral() { throw new RuntimeException("Expression can not be casted into a ListLiteral.\n"); }

    /**
     * Type cast method for {@link NameExpr}.
     * @return NameExpr
     */
    public NameExpr asNameExpr() { throw new RuntimeException("Expression can not be casted into a NameExpr.\n"); }

    /**
     * Type cast method for {@link NewExpr}.
     * @return NewExpr
     */
    public NewExpr asNewExpr() { throw new RuntimeException("Expression can not be casted into a NewExpr.\n"); }

    /**
     * Type cast method for {@link OutStmt}.
     * @return OutStmt
     */
    public OutStmt asOutStmt() { throw new RuntimeException("Expression can not be casted into an OutStmt.\n"); }

    /**
     * Type cast method for {@link ThisStmt}.
     * @return ThisStmt
     */
    public ThisStmt asThisStmt() { throw new RuntimeException("Expression can not be casted into a ThisStmt.\n"); }

    /**
     * Type cast method for {@link UnaryExpr}.
     * @return UnaryExpr
     */
    public UnaryExpr asUnaryExpr() { throw new RuntimeException("Expression can not be casted into a UnaryExpr.\n"); }

    /**
     * {@code toString} method.
     * @return String representing the part of the program the current {@link Expression} represents.
     */
    @Override
    public String toString() { return this.text; }
}
