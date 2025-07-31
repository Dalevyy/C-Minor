package messages.errors.scope;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link ScopeError}.
 *
 * @author Daniel Levy
 */
public class ScopeErrorFactory extends ErrorFactory {

    /**
     * {@inheritDoc}
     */
    public Error createError() { return new ScopeError(); }
}
