package errors.TypeError;

import ast.*;
import errors.DefaultError;
import utilities.*;

public class TypeError extends Error {

    private static String mainError = "Type Error Detected.";

    public static void header(AST n) {
        System.out.println(PrettyPrint.GREEN + mainError + PrettyPrint.RESET + "\n");
        DefaultError.printProgramLine(n);

    }

    public static void GenericTypeError(AST n) {
        System.out.println(PrettyPrint.RED + n.getStartPosition() + ": Type error has been detected." + PrettyPrint.RESET);
        System.exit(1);
    }

}
