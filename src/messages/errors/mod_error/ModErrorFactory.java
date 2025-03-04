package messages.errors.mod_error;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class ModErrorFactory implements ErrorFactory {
    public Error createError() { return new ModError(); }
}
