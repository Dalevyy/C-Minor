package messages.errors.runtime;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link RuntimeError}.
 *
 * @author Daniel Levy
 */
public class RuntimeErrorFactory extends ErrorFactory {

    /**
     * {@inheritDoc}
     */
    public Error createError() { return new RuntimeError(); }
}
