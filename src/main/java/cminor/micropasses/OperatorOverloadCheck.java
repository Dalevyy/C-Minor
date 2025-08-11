package cminor.micropasses;

import cminor.ast.classbody.MethodDecl;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.ErrorBuilder;
import cminor.messages.errors.semantic.SemanticError;
import cminor.utilities.Visitor;

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

    /** Creates operator overload micropass in compilation mode. */
    public OperatorOverloadCheck() { this.handler = new MessageHandler(); }

    /** Creates operator overload micropass in interpretation mode. */
    public OperatorOverloadCheck(String fileName) {
        this.handler = new MessageHandler(fileName);
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
        if(md.isOperator()) {
            if(md.getOperatorOverload().isUnaryOp()) {
                // ERROR CHECK #1: Make sure a unary operator overload has no arguments
                if(!md.getParams().isEmpty()) {
                    handler.createErrorBuilder(SemanticError.class)
                        .addLocation(md)
                        .addErrorNumber(MessageNumber.SEMANTIC_ERROR_701)
                        .generateError();
                }
            }
            else {
                // ERROR CHECK #2: Make sure a binary operator overload has only one argument
                if(md.getParams().size() != 1) {
                    handler.createErrorBuilder(SemanticError.class)
                            .addLocation(md)
                            .addErrorNumber(MessageNumber.SEMANTIC_ERROR_702)
                            .generateError();
                }
            }
        }
    }
}
