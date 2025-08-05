package messages.errors;

import ast.AST;
import messages.MessageHandler;
import messages.errors.scope.ScopeErrorBuilder;
import messages.MessageNumber;

/**
 * An abstract builder class that creates an {@link Error} object.
 * <p>
 *     Dr. C shared this design with me as a way to dynamically create errors without
 *     hardcoding the specific error messages directly into the compiler. I have reworked
 *     the initial design to support the generation of more specific errors (hence why the
 *     class is abstract) and to interface with the {@link MessageHandler} to have it decide
 *     on when the error should be displayed.
 * </p>
 * @author Daniel Levy
 */
public abstract class ErrorBuilder {

    /**
     * The error we will be creating.
     */
    protected final Error error;

    /**
     * The current instance of {@link MessageHandler} that {@link #error} is stored in.
     */
    private MessageHandler handler;

    /**
     * Main constructor for {@link ErrorBuilder}.
     * @param e {@link Error} object we would like to build.
     */
    public ErrorBuilder(Error e, MessageHandler handler) {
        this.error = e;
        this.handler = handler;
    }

    /**
     * Finalizes the creation of the error object and passes the {@link Error} to {@link #handler}.
     */
    public void generateError() { handler.storeMessage(error); }

    /**
     * Adds the {@link AST} location to denote where the error was found.
     * @param node The {@link AST} node in which the error is generated from.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ErrorBuilder addLocation(AST node) {
        error.setLocation(node);
        return this;
    }

    /**
     * Adds the specific error message this error will print out.
     * @param errorNumber The {@link MessageNumber} associated with this error.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ErrorBuilder addErrorNumber(MessageNumber errorNumber) {
        error.setMessageNumber(errorNumber);
        return this;
    }

    /**
     * Adds a list of arguments to allow personalization of the error message.
     * @param args An array of objects that will be used within the error message.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ErrorBuilder addErrorArgs(Object... args) {
        error.setArgs(args);
        return this;
    }

    /**
     * Adds a suggestion that can aid in understanding the error message.
     * @param suggestionNumber {@link MessageNumber} denoting the suggestion we will attach to the error.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ErrorBuilder addSuggestionNumber(MessageNumber suggestionNumber) {
        error.setSuggestionNumber(suggestionNumber);
        return this;
    }

    /**
     * Adds a list of arguments to allow personalization of the suggestion.
     * @param suggestionArgs An array of objects that will be used within the suggestion.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ErrorBuilder addSuggestionArgs(Object... suggestionArgs) {
        error.setSuggestionArgs(suggestionArgs);
        return this;
    }

    /**
     * Explicitly casts the current instance of {@link ErrorBuilder} into a {@link ScopeErrorBuilder}.
     * @return Current instance of {@link ErrorBuilder} as a {@link ScopeErrorBuilder}.
     */
    public ScopeErrorBuilder asScopeErrorBuilder() {
        throw new RuntimeException("The current builder is not generating a scope-related error.");
    }
}
