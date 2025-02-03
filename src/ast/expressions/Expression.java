package ast.expressions;

import ast.*;
import ast.types.*;
import token.*;
import utilities.PrettyPrint;
import utilities.SymbolTable;

public abstract class Expression extends AST {

    public Type type;
    private Object value; // Only for interpretation

    public Expression(Token t) { super(t); }

    public boolean isExpression() { return true; }
    public Expression asExpression() { return this; }

    public void setValue(Object val) { value = val; }

    public Object getValue(SymbolTable ss) {
        if(this.isFieldExpr())
            return value;
        Object found = ss.getValueInRuntimeStack(this.text);
        if(found != null) { return found; }
        return value;
    }

    public boolean isArrayExpr() { return false; }
    public ArrayExpr asArrayExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an ArrayExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isArrayLiteral() { return false; }
    public ArrayLiteral asArrayLiteral() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an ArrayLiteral.\n");
        System.exit(1);
        return null;
    }

    public boolean isBinaryExpr() { return false; }
    public BinaryExpr asBinaryExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a BinaryExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isBreakStmt() { return false; }
    public BreakStmt asBreakStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a BreakStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isCastExpr() { return false; }
    public CastExpr asCastExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a CastExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isContinueStmt() { return false; }
    public ContinueStmt asContinueStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ContinueStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isEndl() { return false; }
    public Endl asEndl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an Endl.\n");
        System.exit(1);
        return null;
    }

    public boolean isFieldExpr() { return false; }
    public FieldExpr asFieldExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a FieldExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isInvocation() { return false; }
    public Invocation asInvocation() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an Invocation.\n");
        System.exit(1);
        return null;
    }

    public boolean isLiteral() { return false; }
    public Literal asLiteral() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Literal.\n");
        System.exit(1);
        return null;
    }

    public boolean isListLiteral() { return false; }
    public ListLiteral asListLiteral() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ListLiteral.\n");
        System.exit(1);
        return null;
    }

    public boolean isNameExpr() { return false; }
    public NameExpr asNameExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a NameExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isNewExpr() { return false; }
    public NewExpr asNewExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a NewExpr.\n");
        System.exit(1);
        return null;
    }

    public boolean isInStmt() { return false; }
    public InStmt asInStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an InStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isOutStmt() { return false; }
    public OutStmt asOutStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an OutStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isUnaryExpr() { return false; }
    public UnaryExpr asUnaryExpr() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a UnaryExpr.\n");
        System.exit(1);
        return null;
    }

    @Override
    public String toString() { return this.text; }
}
