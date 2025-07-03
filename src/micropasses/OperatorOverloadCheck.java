package micropasses;

import ast.classbody.MethodDecl;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.Visitor;

/**
 * Micropass #??
 * <p>
 *     ThisStmt micropass is designed to validate if operator overloads
 *     were correctly written by the user prior to typechecking.
 * </p>
 *
 * @author Daniel Levy
 */
public class OperatorOverloadCheck extends Visitor {

    /** Factory that generates a generic semantic error. */
    private final SemanticErrorFactory generateSemanticError;

    /** Creates operator overload micropass in compilation mode. */
    public OperatorOverloadCheck() { generateSemanticError = new SemanticErrorFactory(); }

    /** Creates operator overload micropass in interpretation mode. */
    public OperatorOverloadCheck(boolean mode) {
        this();
        this.interpretMode = mode;
    }

    /** Checks if the operator overload was written correctly.
     * <p>
     *     When we visit a {@code MethodDecl} that corresponds to an operator
     *     overload, we want to make sure the user correctly wrote the overload.
     *     In this case, we will check if the correct number of arguments was
     *     given to the overload and if not, we are going to generate an error.
     * </p>
     * @param md Method declaration
     */
    public void visitMethodDecl(MethodDecl md) {
        if(md.isOperatorOverload) {
            if(md.operator().isUnaryOp()) {
                // ERROR CHECK #1: Make sure a unary operator overload has no arguments
                if(!md.params().isEmpty()) {
                    new ErrorBuilder(generateSemanticError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.SEMANTIC_ERROR_701)
                        .error();
                }
            }
            else {
                // ERROR CHECK #2: Make sure a binary operator overload has only one argument
                if(md.params().size() != 1) {
                    new ErrorBuilder(generateSemanticError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.SEMANTIC_ERROR_702)
                        .error();
                }
            }
        }
    }
}
