package messages.errors.semantic;

import messages.MessageHandler;
import messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link SemanticError}.
 * @author Daniel Levy
 */
public class SemanticErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link SemanticErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public SemanticErrorBuilder(MessageHandler handler) { super(new SemanticError(),handler); }
}
