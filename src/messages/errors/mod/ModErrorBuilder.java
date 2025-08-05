package messages.errors.mod;

import messages.MessageHandler;
import messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link ModError}.
 * @author Daniel Levy
 */
public class ModErrorBuilder extends ErrorBuilder  {

    /**
     * Default constructor for {@link ModErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public ModErrorBuilder(MessageHandler handler) { super(new ModError(),handler); }
}
