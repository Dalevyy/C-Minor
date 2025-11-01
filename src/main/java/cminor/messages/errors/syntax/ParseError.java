package cminor.messages.errors.syntax;

import cminor.messages.CompilationMessage;
import cminor.messages.Message;

public class ParseError extends CompilationMessage {
    /**
     * Default constructor for {@link CompilationMessage}.
     * @param msg {@link Message} to store into {@link #msg}.
     */
    public ParseError(Message msg) {
        super(msg);
    }
}
