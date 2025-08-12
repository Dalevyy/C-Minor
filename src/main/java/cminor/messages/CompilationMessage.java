package cminor.messages;

import cminor.ast.AST;
import cminor.ast.misc.Var;
import cminor.utilities.SymbolTable;

/**
 * A {@link RuntimeException} generated any time an {@link messages.errors.Error} needs to be displayed.
 * <p>
 *     A {@link CompilationMessage} will only be generated when we are executing the compiler in interpretation
 *     mode. This is needed for us to both display the error message alongside updating the global scope in order
 *     to prevent the use of an invalid construct.
 * </p>
 */
public class CompilationMessage extends RuntimeException {

    /**
     * The {@link Message} that will be outputted to the user during interpretation mode.
     */
    protected final Message msg;

    /**
     * Shared flag that determines whether or not we should print out a stack trace (only for debugging purposes).
     */
    protected static boolean debugMode = false;

    /**
     * Default constructor for {@link CompilationMessage}.
     * @param msg {@link Message} to store into {@link #msg}.
     */
    public CompilationMessage(Message msg) { this.msg = msg; }

    /**
     * Prints out the {@link #msg} for the user.
     */
    public void printMessage() {
        System.out.println(msg.getMessage());

        if(debugMode) {
            printStackTrace();
            throw this;
        }
    }

    /**
     * Removes any construct from the global symbol table based on the message generated.
     * <p>
     *     This method will be called when we are in interpretation mode. Since we are storing constructs
     *     within the VM's {@link ast.misc.CompilationUnit} during name checking, we have to make sure these
     *     same constructs are properly removed from the global scope if a future compilation error occurs.
     * </p>
     * @param globalScope {@link SymbolTable} representing the global scope of the {@link interpreter.VM}.
     */
    public boolean updateGlobalScope(SymbolTable globalScope) {
        AST node = msg.getLocation();

        if(node == null)
            return false;

        // Only remove any top level declaration or local declaration.
        if(!(node.isClassNode() || node.isTopLevelDecl() || (node.isStatement() && node.asStatement().isLocalDecl())))
            return false;

        // Special Case: For enums, we have to also remove their constants.
        if(node.isTopLevelDecl() && node.asTopLevelDecl().isEnumDecl())
            for(Var v : node.asTopLevelDecl().asEnumDecl().getConstants())
                globalScope.removeName(v);

        globalScope.removeName(node);
        return true;
    }

    /**
     * Checks if the current message represents a redeclaration error.
     * @return {@code True} if the error is a {@link messages.errors.scope.RedeclarationError}, {@code False} otherwise.
     */
    public boolean isRedeclarationError() { return false; }

    /**
     * Sets {@link #debugMode} for all instances of a {@link CompilationMessage}.
     */
    public static void setDebugMode() { debugMode = !debugMode; }
}
