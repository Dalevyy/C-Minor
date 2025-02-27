package messages.errors;

public class SyntaxErrorFactory implements ErrorFactory {
    public Error createError() { return new SyntaxError(); }
}