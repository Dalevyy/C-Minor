package messages.warnings;

import ast.AST;
import messages.MessageHandler;
import messages.MessageNumber;

/**
 * A builder class designed to create a {@link Warning}.
 * <p>
 *     This class is similar to the {@link messages.errors.ErrorBuilder}, but it works
 *     on a {@link Warning} object instead of an {@link Error} object. I wrote it like
 *     this, so we don't need to worry about type casting.
 * </p>
 * @author Daniel Levy
 */
public class WarningBuilder {

    /**
     * The {@link Warning} we will be creating.
     */
    private final Warning warning;

    /**
     * The current instance of {@link MessageHandler} that {@link #warning} is stored in.
     */
    private final MessageHandler handler;

    /**
     * Main constructor for {@link WarningBuilder}.
     * @param handler {@link MessageHandler} that will store {@link #warning}.
     */
    public WarningBuilder(MessageHandler handler) {
        this.warning = new Warning();
        this.handler = handler;
    }

    /**
     * Finalizes the creation of the warning object and passes the {@link Warning} to {@link #handler}.
     */
    public void generateWarning() { handler.storeMessage(warning); }

    /**
     * Adds the {@link AST} location to denote where the warning was found.
     * @param node The {@link AST} node in which the warning is generated from.
     * @return Current instance of {@link WarningBuilder}.
     */
    public WarningBuilder addLocation(AST node) {
        warning.setLocation(node);
        return this;
    }

    /**
     * Adds the specific message this warning will print out.
     * @param warningNumber The {@link MessageNumber} associated with this warning.
     * @return Current instance of {@link WarningBuilder}.
     */
    public WarningBuilder addWarningNumber(MessageNumber warningNumber) {
        warning.setMessageNumber(warningNumber);
        return this;
    }

    /**
     * Adds a list of arguments to allow personalization of the warning message.
     * @param args An array of objects that will be used within the warning message.
     * @return Current instance of {@link WarningBuilder}.
     */
    public WarningBuilder addWarningArgs(Object... args) {
        warning.setArgs(args);
        return this;
    }
}
