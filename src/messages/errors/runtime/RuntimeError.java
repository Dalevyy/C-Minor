package messages.errors.runtime;

import messages.errors.Error;
import utilities.PrettyPrint;

public class RuntimeError extends Error {

    public boolean isRuntimeError() { return true; }
    public RuntimeError asRuntimeError() { return this; }

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.RED + "Runtime error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.RED + "Runtime Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
