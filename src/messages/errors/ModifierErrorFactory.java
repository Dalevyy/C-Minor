package messages.errors;

public class ModifierErrorFactory implements ErrorFactory {
    public Error createError() { return new ModifierError(); }
}
