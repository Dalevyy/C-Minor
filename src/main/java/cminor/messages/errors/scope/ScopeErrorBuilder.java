package cminor.messages.errors.scope;

import cminor.ast.AST;
import cminor.messages.MessageHandler;
import cminor.messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link ScopeError}.
 * @author Daniel Levy
 */
public class ScopeErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link ScopeErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public ScopeErrorBuilder(MessageHandler handler) { super(new ScopeError(),handler); }

    /**
     * Adds the original {@link AST} declaration of a construct that is redeclared.
     * @param node The {@link AST} declaration that is being redeclared.
     * @return Current instance of {@link ErrorBuilder}.
     */
    public ScopeErrorBuilder addOriginalDeclaration(AST node) {
        error.asScopeError().originalDeclaration = node;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ScopeErrorBuilder asScopeErrorBuilder() { return this; }
}
