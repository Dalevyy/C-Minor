package messages.errors.semantic;

import messages.errors.Error;
import messages.errors.ErrorFactory;

/**
 * An {@link ErrorFactory} responsible for generating {@link SemanticError}.
 *
 * @author Daniel Levy
 */
public class SemanticErrorFactory extends ErrorFactory {

    /**
     * {@inheritDoc}
     */
    public Error createError() { return new SemanticError(); }
}
