package messages.errors.type_error;

import messages.errors.Error;
import utilities.PrettyPrint;

public class TypeError extends Error {

    public TypeError() {}

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.GREEN + "Typing error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.GREEN + "Typing error detected!\n\n" + PrettyPrint.RESET;
    }
}
