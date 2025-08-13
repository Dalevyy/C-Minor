package cminor.messages.errors.semantic;

import cminor.messages.errors.Error;
import cminor.utilities.PrettyPrint;

/**
 * An {@link Error} generated for any non-specific semantic-related issues.
 * @author Daniel Levy
 */
public class SemanticError extends Error {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMessageHeader(String fileName) {
        return super.buildMessageHeader(fileName)
                + PrettyPrint.RED
                + "Semantic Error "
                + messageNumber()
                + ": ";
    }
}
