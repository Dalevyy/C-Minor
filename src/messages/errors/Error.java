package messages.errors;

import messages.Message;
import messages.errors.scope.ScopeError;

/**
 * A {@link Message} class representing a compilation error.
 * <p>
 *     As it stands, this class serves as an organizational unit instead
 *     of any truly functional unit.
 * </p>
 * @author Daniel Levy
 */
public abstract class Error extends Message {

    /**
     * Generates a string that contains the specific error number.
     * @return
     */
    protected String errorNumber() {
        return messageType.toString().substring(messageType.toString().lastIndexOf("_")+1);
    }

    /**
     * Explicitly casts the current instance of {@link Error} into a {@link ScopeError}.
     * @return Current instance of {@link Error} as a {@link ScopeError}.
     */
    public ScopeError asScopeError() {
        throw new RuntimeException("The current error does not represent a scope error.");
    }
}
