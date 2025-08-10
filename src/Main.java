
import compiler.Compiler;

import java.io.IOException;

/**
 * C Minor Main Class
 * <p>
 *     ThisStmt is the {@code Main} class for the C Minor compiler
 *     which begins the compilation process.
 * </p>
 * @author Daniel Levy
 */
public class Main {

    /**
     * {@code Main} method.
     * <p>
     *     ThisStmt is the entrance method for the C Minor compiler. It will
     *     call the {@code compile} method from the {@link Compiler} class
     *     to begin the compilation process.
     * </p>
     * @param args The arguments the user passes into the compiler
     */
    public static void main(String[] args) throws IOException { new Compiler().compile(args); }
}
