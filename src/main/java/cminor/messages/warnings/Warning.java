package cminor.messages.warnings;

import cminor.messages.Message;
import cminor.utilities.PrettyPrint;

/**
 * A {@link Message} that displays a warning to the user.
 * <p>
 *     A warning is an issue that was found in compilation that should be drawn to the user's
 *     attention. Unlike with errors, a warning does not terminate the compilation process.
 * </p>
 * @author Daniel Levy
 */
public class Warning extends Message {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMessageHeader(String fileName) {
        return super.buildMessageHeader(fileName)
                + PrettyPrint.PINK
                + "Warning "
                + messageNumber()
                + "\n\n"
                + PrettyPrint.RESET;
    }
}
