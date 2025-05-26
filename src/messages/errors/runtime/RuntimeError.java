package messages.errors.runtime;

import messages.errors.Error;
import utilities.PrettyPrint;

public class RuntimeError extends Error {

    public RuntimeError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.RED + "Runtime error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.RED + "Runtime error detected!\n\n" + PrettyPrint.RESET;
    }
}
