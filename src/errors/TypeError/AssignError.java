package errors.TypeError;

import ast.*;
import ast.statements.*;
import ast.types.*;
import errors.*;
import utilities.PrettyPrint;

public class AssignError extends DefaultError {

    public static void InvalidAssignError(AssignStmt as, Type lType, Type rType) {
        TypeError.header(as);

        System.out.println(PrettyPrint.RED + as.getStartPosition() + ": The LHS has type \'" + lType.typeName() + "\' and the RHS has type \'" +rType.typeName() + "\'.");
        System.out.println("The RHS type must be the same type as the LHS for an assignment statement." + PrettyPrint.RESET);

        System.exit(1);
    }

    public static void varAssignError(Var v) {
        TypeError.header(v);
        AST n = null;
        String varType = "";
        if(v.getParent().isTopLevelDecl()) {
            n = v.getParent().asTopLevelDecl().asGlobalDecl();
            varType = ": Global Variable \'";
        }
        else {
            n = v.getParent().asStatement().asLocalDecl();
            varType = ": Local Variable \'";
        }

        System.out.println(PrettyPrint.RED + v.getStartPosition() + ": " + varType + v.name() + "\' has type \'" + v.type().typeName() + "\'.");
        System.out.println("Cannot assign a \'" + v.init().type.typeName() + "\' to type \'" + v.type().typeName() + "\'." + PrettyPrint.RESET);

        System.exit(-1);
    }
}
