package messages.errors.mod;

import messages.errors.Error;
import utilities.PrettyPrint;

public class ModError extends Error {

    public ModError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.YELLOW + "Modifier error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.YELLOW + "Modifier Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
