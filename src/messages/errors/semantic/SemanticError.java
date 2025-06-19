package messages.errors.semantic;

import messages.errors.Error;
import utilities.PrettyPrint;

public class SemanticError extends Error {
    public SemanticError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.CYAN + "Semantic error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.CYAN + "Semantic Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
