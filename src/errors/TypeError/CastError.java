package errors.TypeError;

import ast.expressions.*;
import errors.*;
import utilities.PrettyPrint;

public class CastError extends DefaultError {

    public static void InvalidIntCastError(CastExpr ce) {
        TypeError.header(ce);

        System.out.println(PrettyPrint.RED + ce.getStartPosition() + ": An expression of type \'Int\' can not " +
                "be casted to type \'" + ce.castType().typeName() + "\'.");
        System.out.println("An expression of type Int can only be casted as a Real or Char type.");
        System.exit(1);
    }
}
