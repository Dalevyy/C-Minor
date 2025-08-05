package messages.errors.runtime;

import messages.MessageHandler;
import messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link RuntimeError}.
 * @author Daniel Levy
 */
public class RuntimeErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link RuntimeErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public RuntimeErrorBuilder(MessageHandler handler) { super(new RuntimeError(), handler); }
}
