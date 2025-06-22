package messages.errors.syntax;

import messages.errors.Error;
import messages.errors.semantic.SemanticError;
import utilities.PrettyPrint;

public class SyntaxError extends Error {

    public boolean isSyntaxError() { return true; }
    public SyntaxError asSyntaxError() { return this; }

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.CYAN + "Syntax error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.CYAN + "Syntax Error\n\n" + PrettyPrint.RESET;
    }
}
