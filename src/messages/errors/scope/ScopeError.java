package messages.errors.scope;

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
        return PrettyPrint.YELLOW + "Scope Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
