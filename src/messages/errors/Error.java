package messages.errors;

import messages.Message;
import messages.errors.scope.ScopeError;

/**
 * A {@link Message} class representing a compilation error.
 * <p>
 *     An error is generated when there is an issue with a user's program that prevents
 *     the rest of the compilation process from occurring. A generated error means the
 *     compiler needs to terminate execution at its generation (if in interpretation mode)
 *     or after the end of the current phase (in compilation mode). Currently, this class
 *     is only serving as a way to organize different errors.
 * </p>
 * @author Daniel Levy
 */
public abstract class Error extends Message {
    /**
     * Explicitly casts the current instance of {@link Error} into a {@link ScopeError}.
     * @return Current instance of {@link Error} as a {@link ScopeError}.
     */
    public ScopeError asScopeError() {
        throw new RuntimeException("The current error does not represent a scope error.");
    }
}
