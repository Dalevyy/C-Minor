package messages.errors.syntax;

import messages.errors.Error;
import utilities.PrettyPrint;

public class SyntaxError extends Error {
    public SyntaxError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.CYAN + "Syntax error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.CYAN + "Syntax error detected!" + PrettyPrint.RESET;
    }
}
