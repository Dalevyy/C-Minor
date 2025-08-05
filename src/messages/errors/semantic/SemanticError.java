package messages.errors.semantic;

import messages.errors.Error;
import utilities.PrettyPrint;

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
                + PrettyPrint.CYAN
                + "Semantic Error "
                + errorNumber()
                + "\n\n"
                + PrettyPrint.RESET;
    }
}
