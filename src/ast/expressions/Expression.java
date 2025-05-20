package ast.expressions;

import ast.*;
import ast.types.*;
import token.*;

public abstract class Expression extends AST {

    public Type type;

    public Expression(Token t) { super(t); }

    public boolean isExpression() { return true; }
    public Expression asExpression() { return this; }

    public boolean isArrayExpr() { return false; }
    public ArrayExpr asArrayExpr() { throw new RuntimeException("Expression can not be casted into an ArrayExpr.\n"); }

    public boolean isArrayLiteral() { return false; }
    public ArrayLiteral asArrayLiteral() { throw new RuntimeException("Expression can not be casted into an ArrayLiteral.\n"); }

    public boolean isBinaryExpr() { return false; }
    public BinaryExpr asBinaryExpr() { throw new RuntimeException("Expression can not be casted into a BinaryExpr.\n"); }

    public boolean isBreakStmt() { return false; }
    public BreakStmt asBreakStmt() { throw new RuntimeException("Expression can not be casted into a BreakStmt.\n"); }

    public boolean isCastExpr() { return false; }
    public CastExpr asCastExpr() { throw new RuntimeException("Expression can not be casted into a CastExpr.\n"); }

    public boolean isContinueStmt() { return false; }
    public ContinueStmt asContinueStmt() { throw new RuntimeException("Expression can not be casted into a ContinueStmt.\n"); }

    public boolean isEndl() { return false; }
    public Endl asEndl() { throw new RuntimeException("Expression can not be casted into an Endl.\n"); }

    public boolean isFieldExpr() { return false; }
    public FieldExpr asFieldExpr() { throw new RuntimeException("Expression can not be casted into a FieldExpr.\n"); }

    public boolean isInStmt() { return false; }
    public InStmt asInStmt() { throw new RuntimeException("Expression can not be casted into an InStmt.\n"); }

    public boolean isInvocation() { return false; }
    public Invocation asInvocation() { throw new RuntimeException("Expression can not be casted into an Invocation.\n"); }

    public boolean isLiteral() { return false; }
    public Literal asLiteral() { throw new RuntimeException("Expression can not be casted into a Literal.\n"); }

    public boolean isListLiteral() { return false; }
    public ListLiteral asListLiteral() { throw new RuntimeException("Expression can not be casted into a ListLiteral.\n"); }

    public boolean isNameExpr() { return false; }
    public NameExpr asNameExpr() { throw new RuntimeException("Expression can not be casted into a NameExpr.\n"); }

    public boolean isNewExpr() { return false; }
    public NewExpr asNewExpr() { throw new RuntimeException("Expression can not be casted into a NewExpr.\n"); }

    public boolean isOutStmt() { return false; }
    public OutStmt asOutStmt() { throw new RuntimeException("Expression can not be casted into an OutStmt.\n"); }

    public boolean isThis() { return false; }
    public This asThis() { throw new RuntimeException("Expression can not be casted into a This.\n"); }

    public boolean isUnaryExpr() { return false; }
    public UnaryExpr asUnaryExpr() { throw new RuntimeException("Expression can not be casted into a UnaryExpr.\n"); }

    @Override
    public String toString() { return this.text; }
}
