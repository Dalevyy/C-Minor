package cminor.messages.errors.type;

import cminor.messages.MessageHandler;
import cminor.messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link TypeError}.
 * @author Daniel Levy
 */
public class TypeErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link TypeErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public TypeErrorBuilder(MessageHandler handler) { super(new TypeError(),handler); }
}
