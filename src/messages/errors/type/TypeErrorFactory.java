package messages.errors.type;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class TypeErrorFactory implements ErrorFactory {
    public Error createError() { return new TypeError(); }
}