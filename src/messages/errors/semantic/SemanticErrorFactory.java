package messages.errors.semantic;

import messages.errors.Error;
import messages.errors.ErrorFactory;

public class SemanticErrorFactory implements ErrorFactory {
    public Error createError() { return new SemanticError(); }
}
