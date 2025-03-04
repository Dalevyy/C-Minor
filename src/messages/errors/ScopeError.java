package messages.errors;

import ast.AST;
import utilities.PrettyPrint;
import utilities.SymbolTable;

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

    @Override
    public String buildSuggestion() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");

        return sb.toString();
    }
}
