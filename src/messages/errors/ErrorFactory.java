package messages.errors;

import ast.misc.Compilation;

/**
 * An abstract class that sets up the creation of {@link Error} objects.
 * @author Daniel Levy
 */
public abstract class ErrorFactory {

    /**
     * Flag that denotes which mode the compiler runs in.
     * <ul>
     *     <li> Set to {@code True} if we are compiling with the {@link interpreter.VM}.</li>
     *     <li> Set to {@code False} if we are compiling with the {@link compiler.Compiler}.</li>
     * </ul>
     */
    protected boolean interpretationMode = false;

    /**
     * The name of the file we are generating an error for (if we are in compilation mode).
     */
    protected String fileName = "";

    /**
     * Generates a new {@link Error} object based on the factory type.
     * @return An {@link Error} dependent on the current factory.
     */
    protected abstract Error createError();

    /**
     * Generates and prints out an error message to the user.
     * @param eb {@link ErrorBuilder} that has an error ready to be generated for the user.
     * @return An {@link Error} object that will be stored during compilation. Nothing will be returned if we
     * are in interpretation mode as we will throw an exception to the {@link interpreter.VM}.
     */
    public Error createCompileError(ErrorBuilder eb) {
        Error error = eb.error();

        if(interpretationMode)
            error.printMessage();

        return error;
    }

    /**
     * Setter responsible for setting {@link #interpretationMode} to be {@code True} when called.
     * <p>
     *     This setter is only called when we are in interpretation mode.
     * </p>
     */
    public void setInterpretationExecutionMode() { this.interpretationMode = true; }

    /**
     * Setter responsible for setting the {@link #fileName}.
     * <p>
     *     This setter is only called during a {@link utilities.Visitor#visitCompilation(Compilation)} execution
     *     whenever the compiler is in compilation mode for each respective visitor.
     * </p>
     * @param fileName String representing the file name that will be used when generating an error.
     */
    public void setFileName(String fileName) { this.fileName = fileName; }
}
