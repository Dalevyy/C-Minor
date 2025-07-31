package messages.errors.type;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link TypeError}.
 *
 * @author Daniel Levy
 */
public class TypeErrorFactory extends ErrorFactory {

    /**
     * {@inheritDoc}
     */
    public Error createError() { return new TypeError(); }
}
