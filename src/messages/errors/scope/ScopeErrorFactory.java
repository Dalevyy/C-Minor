package messages.errors.scope;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class ScopeErrorFactory implements ErrorFactory {
    public Error createError() { return new ScopeError(); }
}
