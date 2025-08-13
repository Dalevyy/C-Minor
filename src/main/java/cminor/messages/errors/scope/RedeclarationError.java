package cminor.messages.errors.scope;

import cminor.messages.CompilationMessage;
import cminor.utilities.SymbolTable;

/**
 * A {@link CompilationMessage} generated when a redeclaration occurs during the {@link namechecker.NameChecker}.
 * <p>
 *     This class exists to make sure we don't try to remove an {@link ast.AST} node that is redeclared since
 *     we want the user to still use the original declaration.
 * </p>
 */
public class RedeclarationError extends CompilationMessage {

    /**
     * Default constructor for {@link RedeclarationError}.
     * @param se {@link ScopeError} to store into {@link #msg}
     */
    public RedeclarationError(ScopeError se) { super(se); }

    /**
     * {@inheritDoc}
     * <p>
     *     Whenever we have a redeclaration error, we will not remove any nodes
     *     since the node that is redeclared will not have yet been added to the
     *     symbol table.
     * </p>
     */
    @Override
    public void updateGlobalScope(SymbolTable globalScope) { /* DO NOTHING */ }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRedeclarationError() { return true; }
}
