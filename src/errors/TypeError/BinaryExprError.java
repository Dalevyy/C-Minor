package errors.TypeError;

import ast.expressions.*;
import ast.types.*;
import errors.*;
import utilities.PrettyPrint;

public class BinaryExprError extends DefaultError {

    // Error for when 2 operands
    public static void BinaryOpsNonMatchingError(BinaryExpr be, Type left, Type right) {
        TypeError.header(be);

        String op = be.BinaryOp().toString();
        System.out.println(PrettyPrint.RED + be.getStartPosition() + ": LHS has type \'" + left.typeName() + "\' and RHS has type \'" + right.typeName() + "\'.");
        System.out.println("Both types need to be the same for binary operator \'" + op + "\'." + PrettyPrint.RESET);
        System.exit(1);
    }

    public static void BinaryOpInvalidTypeError(BinaryExpr be, Type t, boolean side) {
        TypeError.header(be);

        String op = be.BinaryOp().toString();
        System.out.print(PrettyPrint.RED + be.getStartPosition());

        String currOp = null;
        if(side)
            currOp = "LHS";
        else
            currOp = "RHS";

        System.out.println(": " + currOp + " has type \'" + t.typeName() + "\'.");

        switch(op) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "<>":
            case "<=>": {
                System.out.println("\'" + op + "\' can only compare types of \'Real\', \'Int\', and \'Char\'.");
                break;
            }
            case "<<":
            case ">>": {
                System.out.println("\'" + op + "\' can only operate on operands of type Int.");
                break;
            }
            case "&":
            case "|": {
                System.out.println("\'" + op + "\' can only operate on operands of Discrete type.");
                break;
            }
            case "and":
            case "or": {
                System.out.println("\'" + op + "\' can only compare operands of type Bool.");
                break;
            }
        }
        System.exit(1);
    }

    public static void BinaryOpInvalidOpError(BinaryExpr be) {
        TypeError.header(be);

        String op = be.BinaryOp().toString();
        System.out.println(PrettyPrint.RED + be.getStartPosition() + ": \'" + op);
        switch(op) {
            case "+": {
                System.out.print("\' can not be used with Strings.");
                System.out.println("The only valid binary operator for two String types is '+'." + PrettyPrint.RESET);
                break;
            }
            case "&":
            case "|": {
                System.out.print(": can not be used with Scalars.");
                System.out.println("ONly Discrete types may be used with \'" + op + "\'.");
                break;
            }
        }

        System.exit(1);
    }
}
