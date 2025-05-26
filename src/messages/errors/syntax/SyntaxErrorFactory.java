package messages.errors.syntax;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class SyntaxErrorFactory implements ErrorFactory {
    public Error createError() { return new SyntaxError(); }
}