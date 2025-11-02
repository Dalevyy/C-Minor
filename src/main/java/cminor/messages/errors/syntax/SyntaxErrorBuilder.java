package cminor.messages.errors.syntax;

import cminor.lexer.Lexer;
import cminor.messages.MessageHandler;
import cminor.messages.errors.ErrorBuilder;
import cminor.token.Location;
import cminor.token.Token;
import cminor.utilities.PrettyPrint;
import cminor.utilities.Vector;

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
     * Generates an error location when an error is thrown from the {@link Lexer}.
     * @param loc The {@link Location} denoting where the error token was generated from.
     * @param text The text to display to the user.
     * @return {@link SyntaxErrorBuilder}
     */
    public SyntaxErrorBuilder addLocation(Location loc, String text) {
        error.asSyntaxError().location = loc.start.line + "| ";

        // We are going to ignore any new lines when a token spans multiple lines.
        // This means we will print where the token begins in this edge case!
        int end = (text.contains("\n")) ? text.indexOf("\n") : text.length();
        error.asSyntaxError().location += text.substring(0,end) + "\n";

        // This gets the offset for where we want to highlight the error token.
        int offset = (Integer.toString(loc.start.line).length() + 2) + (loc.start.column-1);

        for(int i = 0; i < offset; i++)
            error.asSyntaxError().location += " ";
        error.asSyntaxError().location += PrettyPrint.RED;

        while(offset <= loc.end.column) {
            error.asSyntaxError().location += "^";
            offset += 1;
        }
        error.asSyntaxError().location += "^";
        error.asSyntaxError().location += PrettyPrint.RESET;

        return this;
    }

    public SyntaxErrorBuilder setLexerMode() {
        this.error.asSyntaxError().setLexerMode();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public SyntaxErrorBuilder asSyntaxErrorBuilder() { return this; }
}


