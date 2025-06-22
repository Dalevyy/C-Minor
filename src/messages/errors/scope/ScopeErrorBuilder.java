package messages.errors.scope;

import ast.AST;
import messages.errors.ErrorBuilder;
import messages.errors.ErrorFactory;

public class ScopeErrorBuilder extends ErrorBuilder {

    public ScopeErrorBuilder(ErrorFactory ef, boolean mode) { super(ef,mode); }

    public ScopeErrorBuilder addRedeclaration(AST node) {
        error.asScopeError().setRedeclarationLocation(node);
        return this;
    }

    public ScopeErrorBuilder asScopeErrorBuilder() { return this; }
}
