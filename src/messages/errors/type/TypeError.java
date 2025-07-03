package messages.errors.type;

import messages.errors.Error;
import utilities.PrettyPrint;

public class TypeError extends Error {

    public boolean isTypeError() { return true; }
    public TypeError asTypeError() { return this; }

    @Override
    public String header() {
        return fileHeader() + PrettyPrint.GREEN + "Type Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
