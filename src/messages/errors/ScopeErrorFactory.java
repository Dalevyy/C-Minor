package messages.errors;

public class ScopeErrorFactory implements ErrorFactory {
    public Error createError() { return new ScopeError(); }
}
