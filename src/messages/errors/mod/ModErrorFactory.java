package messages.errors.mod;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link ModError}.
 *
 * @author Daniel Levy
 */
public class ModErrorFactory extends ErrorFactory {
    public Error createError() { return new ModError(); }
}
