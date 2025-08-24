package cminor.ast.misc;

/**
 * An interface to track whether a return statement is found in a function, method, or {@code Main}.
 * <p>
 *     Currently, the C Minor compiler does not have a phase designed to analyze the control flow of
 *     a program. Thus, we will not necessarily have access to some information we will need during
 *     compilation. One such piece of info is determining whether a function or method has a return
 *     statement. In this context, we are referring to a valid return statement i.e. a return statement
 *     not found in a control flow structure. We have to find a workaround to ensure a non-Void function
 *     or method guarantees a return value. This is the point of this class as the {@link cminor.typechecker.TypeChecker}
 *     will determine if a function or method has a valid return value.
 * </p>
 * @author Daniel Levy
 */
public interface ReturnDecl {

    /**
     * Sets the {@link ReturnDecl} flag associated with whether or not a {@link cminor.ast.statements.ReturnStmt}
     * was found.
     */
    void setIfReturnStmtFound();

    /**
     * Checks if the current method has a valid {@link cminor.ast.statements.ReturnStmt}.
     * <p>
     *     By "valid", we mean the method is guaranteed to return a value. :)
     * </p>
     * @return {@code True} if the method contains a valid return statement, {@code False} otherwise.
     */
    boolean containsReturnStmt();
}
