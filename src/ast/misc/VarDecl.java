package ast.misc;

import ast.AST;
import ast.expressions.Expression;
import ast.types.Type;

/**
 * An interface that implements the helper methods when working with a {@link Var} object.
 * <p>
 *     Since Java does not support multiple inheritance, this was "my" solution to trying to
 *     make my code simpler to maintain when working with the {@link Var} type. "My" is in quotes
 *     since I realized this interface is similar (arguably nearly identical) to the one found in
 *     the Espresso compiler, so the credit goes to Dr. Pedersen here. I didn't really understand
 *     the point of this interface until I couldn't extend a class using multiple classes...
 * </p>
 * @author Daniel Levy
 */
public interface VarDecl {

    /**
     * Checks if the current variable was initialized. A variable is considered initialized if
     * it has an initial value or was marked as {@code uninit}.
     * @return {@code True} if the variable is initialized, {@code False} otherwise.
     */
    boolean hasInitialValue();

    /**
     * Getter method to return the name of the variable.
     * @return The name of the variable as a {@link Name}
     */
    Name getVariableName();

    /**
     * Getter method to return the initial value of the variable (if applicable).
     * @return {@link Expression} representing the initial value.
     * If no initial value was given, return {@code null}.
     */
    Expression getInitialValue();

    /**
     * Getter method to return the variable's declared type (if applicable).
     * @return {@link Type} representing the variable's type. If no type was given
     * (for field and enum constants), return {@code null}.
     */
    Type getDeclaredType();

    /**
     * {@code toString} method that returns the name of the variable as a string.
     * @return String representation of the variable's name.
     */
    String toString();

    /**
     * Cast method returning the current {@link VarDecl} as an {@link AST} type.
     * @return The {@link VarDecl} object as an {@link AST} type.
     */
    AST asAST();
}
