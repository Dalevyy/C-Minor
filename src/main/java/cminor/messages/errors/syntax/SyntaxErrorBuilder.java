package cminor.messages.errors.syntax;

import cminor.lexer.Lexer;
import cminor.messages.MessageHandler;
import cminor.messages.errors.ErrorBuilder;
import cminor.token.Token;

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

    public SyntaxErrorBuilder addLocation(Token error, Lexer input) {
        this.error.asSyntaxError().location = error.getStartPos().line + "| " + input.getText();
        return this;
    }

    public SyntaxErrorBuilder addLocation(String loc) {
        this.error.asSyntaxError().location = loc;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public SyntaxErrorBuilder asSyntaxErrorBuilder() { return this; }
}


