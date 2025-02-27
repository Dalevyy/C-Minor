package messages.errors;

public class TypeErrorFactory implements ErrorFactory {
    public Error createError() { return new TypeError(); }
}