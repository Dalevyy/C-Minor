package errors;

import ast.*;
import ast.expressions.*;
import utilities.*;

public class ScopeError extends DefaultError {

    private static String mainError = "Scoping Error Detected.";

    public static void InvokeError(Invocation in) {
        System.out.println(PrettyPrint.BLUE + mainError + PrettyPrint.RESET);
        System.out.println();
        DefaultError.printProgramLine(in);
        System.out.println(PrettyPrint.RED + in.getStartPosition() + ": Scoping Error! "
                + in.name().toString() + " has not yet been declared in the program." + PrettyPrint.RESET);
        System.exit(-1);
    }

    public static void NameExprError(NameExpr ne) {
        System.out.println(PrettyPrint.BLUE + mainError + PrettyPrint.RESET);
        System.out.println();
        DefaultError.printProgramLine(ne);
        System.out.println(PrettyPrint.RED + ne.getStartPosition() + ": Scoping Error! "
                + ne.getName().toString() + " has not yet been declared in the current scope." + PrettyPrint.RESET);
        System.exit(-1);
    }

    public static void LocalDeclError(AST ld) {
        System.out.println(PrettyPrint.BLUE + mainError + PrettyPrint.RESET);
        System.out.println();
        DefaultError.printProgramLine(ld);
        System.out.println(PrettyPrint.RED + ld.getStartPosition() + ": Scoping Error! "
                + ld.toString() + " has already been declared." + PrettyPrint.RESET);
        System.exit(-1);
    }

    public static void ParamDeclError(AST ld) {
        System.out.println(PrettyPrint.BLUE + mainError + PrettyPrint.RESET);
        System.out.println();
        DefaultError.printProgramLine(ld);
        System.out.println(PrettyPrint.RED + ld.getStartPosition() + ": Scoping Error! "
                + ld.toString() + " has already been declared." + PrettyPrint.RESET);
        System.exit(-1);
    }

    public static void RedeclError(NameNode i) {
        AST node = i.declName();
        Name redeclName = null;
        if(node.isStatement()) {
            if(node.asStatement().isLocalDecl())
                redeclName = node.asStatement().asLocalDecl().var().name();
        }
        System.out.println(PrettyPrint.BLUE + mainError + PrettyPrint.RESET);
        System.out.println();
        DefaultError.printProgramLine(node);
        System.out.println(PrettyPrint.RED + node.getStartPosition() +
                ": Redeclaration of " + redeclName.toString() + "." + PrettyPrint.RESET);
        System.exit(-1);
    }
}
