package messages.errors.syntax;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link SyntaxError}.
 *
 * @author Daniel Levy
 */
public class SyntaxErrorFactory extends ErrorFactory {

    /**
     * {@inheritDoc}
     */
    public Error createError() { return new SyntaxError(); }
}