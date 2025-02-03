package errors.TypeError;

import ast.statements.*;
import ast.types.*;
import errors.*;
import utilities.PrettyPrint;

public class StmtError extends DefaultError {

    public static void ifConditionError(IfStmt is) {
        TypeError.header(is);
        System.out.println(PrettyPrint.RED + is.getStartPosition() + ": Error! Expression evaluates to \'" + is.condition().type.typeName() + "\'.");
        System.out.println("The condition for an if Statement must be of type Boolean." + PrettyPrint.RESET);
        System.exit(1);
    }

    public static void whileConditionError(WhileStmt ws) {
        TypeError.header(ws);
        System.out.println(PrettyPrint.RED + ws.getStartPosition() + ": Error! Expression evaluates to \'" + ws.condition().type.typeName() + "\'.");
        System.out.println("The condition for a while Statement must be of type Boolean." + PrettyPrint.RESET);
        System.exit(1);
    }

    /*
        Return Statement Errors
    */
    public static void VoidReturnError(ReturnStmt rs) {
        TypeError.header(rs);
        System.out.println(PrettyPrint.RED + rs.getStartPosition() + ": Error! A function with a Void return type can not return an expression " +
                "of type \'" + rs.expr().type.typeName() + "\'.");
        System.out.println("A Void function can not return anything." + PrettyPrint.RESET);
        System.exit(1);
    }

    public static void InvalidReturnError(ReturnStmt rs, Type rt) {
        TypeError.header(rs);
        System.out.println(PrettyPrint.RED + rs.getStartPosition() + ": Error! Return statement has type \'" + rs.expr().type.typeName() +
                "\', but function has return type \'" + rt.typeName() +"\'.");
        System.out.println("A function of type \'" + rt.typeName() + "\' must return type \'" + rt.typeName() + "\'.");
        System.exit(1);
    }
}
