package messages.errors.syntax;

import messages.errors.Error;
import messages.errors.semantic.SemanticError;
import utilities.PrettyPrint;

public class SyntaxError extends Error {

    public boolean isSyntaxError() { return true; }
    public SyntaxError asSyntaxError() { return this; }

    @Override
    public String header() {
        return fileHeader() + PrettyPrint.CYAN + "Syntax Error 100 " + "\n\n" + PrettyPrint.RESET;
    }
}
