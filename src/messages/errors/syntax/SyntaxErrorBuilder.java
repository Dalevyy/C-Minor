package messages.errors.syntax;

import messages.MessageHandler;
import messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link SyntaxError}.
 * @author Daniel Levy
 */
public class SyntaxErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link SyntaxErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public SyntaxErrorBuilder(MessageHandler handler) { super(new SyntaxError(),handler); }
}
