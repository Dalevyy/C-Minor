package messages.errors.semantic;

import messages.errors.Error;
import utilities.PrettyPrint;

public class SemanticError extends Error {

    public boolean isSemanticError() { return true; }
    public SemanticError asSemanticError() { return this; }

    @Override
    public String header() {
        return fileHeader() + PrettyPrint.CYAN + "Semantic Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
