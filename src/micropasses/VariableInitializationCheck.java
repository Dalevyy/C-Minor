package micropasses;

import ast.statements.ForStmt;
import ast.statements.LocalDecl;
import ast.topleveldecls.GlobalDecl;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.Visitor;

/**
 * A micropass checking if variables are initialized in a C Minor program.
 * <p>
 *     In C Minor, it will be a requirement for the user to initialize all
 *     variables declared in a program. This prevents a programmer from
 *     accidentally using garbage data due to the use of uninitialized variables.
 *     If a user does not care what initial value a variable has, then they should
 *     use the {@code uninit} keyword which will create a default value that will
 *     be assigned to the variable depending on its declared data type.
 * </p>
 * @author Daniel Levy
 */
public class VariableInitializationCheck extends Visitor {

    /**
     * An error factory that creates {@link messages.errors.semantic.SemanticError} objects.
     */
    private static final SemanticErrorFactory generateSemanticError = new SemanticErrorFactory();

    /**
     * Constructor for executing {@link VariableInitializationCheck} in compilation mode.
     */
    public VariableInitializationCheck() { this(false); }

    /**
     * Constructor for executing {@link VariableInitializationCheck} in interpretation mode.
     * @param compileMode {@code True} if we are compiling code with the {@link interpreter.VM},
     * {@code False} if we are compiling with the {@link compiler.Compiler}.
     */
    public VariableInitializationCheck(boolean compileMode) { this.interpretMode = compileMode; }

    /**
     * Visits the {@link ast.statements.BlockStmt} corresponding to the current {@link ForStmt}.
     * <p>
     *     Since we are storing the loop control variable as a local declaration, we do not need to
     *     check if it's initialized since its initial value will be based on the starting value of
     *     the loop control statement. This means we can ignore it and just visit the for loop's
     *     body directly.
     * </p>
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) { fs.forBlock().visit(this); }

    /**
     * Visits a {@link GlobalDecl} and checks if it was initialized.
     * @param gd {@link GlobalDecl}.
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // ERROR CHECK #1: For all global variables and constants, a user must initialize
        //                 the global using a value or the `uninit` keyword.
        if(!gd.hasInitialValue()) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                .addLocation(gd)
                .addErrorType(MessageType.SEMANTIC_ERROR_700)
                .addArgs("Global", gd)
                .addSuggestType(MessageType.SEMANTIC_SUGGEST_1700)
                .error();
        }
    }

    /**
     * Visits a {@link LocalDecl} and checks if it was initialized.
     * @param ld {@link LocalDecl}.
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: For all local variables, a user must initialize
        //                 the local using a value or the `uninit` keyword.
        if(!ld.hasInitialValue()) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                .addLocation(ld)
                .addErrorType(MessageType.SEMANTIC_ERROR_700)
                .addArgs("Local", ld)
                .addSuggestType(MessageType.SEMANTIC_SUGGEST_1700)
                .error();
        }
    }
}
