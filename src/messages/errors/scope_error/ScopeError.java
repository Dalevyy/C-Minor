package messages.errors.scope_error;

import messages.errors.Error;
import utilities.PrettyPrint;

public class ScopeError extends Error {

    public ScopeError() { }

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.YELLOW + "Scoping error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.YELLOW + "Scoping error detected!\n\n" + PrettyPrint.RESET;
    }
}
