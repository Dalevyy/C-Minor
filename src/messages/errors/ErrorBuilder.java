package messages.errors;

import ast.AST;
import messages.errors.scope.ScopeErrorBuilder;
import messages.MessageType;

public class ErrorBuilder {
    protected final Error error;

    public ErrorBuilder(ErrorFactory ef, boolean mode) {
        this.error = ef.createError();
        this.error.setInterpretMode(mode);
    }

    public String error() { return this.error.printMessage(); }

    public ErrorBuilder addFileName(String fileName) {
        this.error.setFileName(fileName);
        return this;
    }

    public ErrorBuilder addLocation(AST node) {
        this.error.setLocation(node);
        return this;
    }

    public ErrorBuilder addMessage(String msg) {
        this.error.setMsg(msg);
        return this;
    }

    public ErrorBuilder addErrorType(MessageType et) {
        this.error.setErrorType(et);
        return this;
    }

    public ErrorBuilder addSuggestType(MessageType et) {
        this.error.setSuggestType(et);
        return this;
    }

    public ErrorBuilder addArgs(Object... args) {
        this.error.setArgs(args);
        return this;
    }

    public ErrorBuilder addSuggestArgs(Object... extraArgs) {
        this.error.setArgsForSuggestions(extraArgs);
        return this;
    }

    public ScopeErrorBuilder asScopeErrorBuilder() {
        throw new RuntimeException("The current error builder is not building a scope error.");
    }
}
