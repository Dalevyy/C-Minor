package messages.errors.runtime;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class RuntimeErrorFactory implements ErrorFactory {
    public Error createError() { return new RuntimeError(); }
}
