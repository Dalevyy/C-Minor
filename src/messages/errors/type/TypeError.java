package messages.errors.type;

import messages.errors.Error;
import utilities.PrettyPrint;

public class TypeError extends Error {

    public TypeError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.GREEN + "Type error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.GREEN + "Type Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
