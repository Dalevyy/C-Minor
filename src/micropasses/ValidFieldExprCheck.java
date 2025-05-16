package micropasses;

import ast.expressions.FieldExpr;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.scope_error.ScopeErrorFactory;
import utilities.Visitor;

public class ValidFieldExprCheck extends Visitor {

    private final ScopeErrorFactory generateScopeError;

    public ValidFieldExprCheck(){ generateScopeError = new ScopeErrorFactory(); }
    public ValidFieldExprCheck(boolean mode) {
        this();
        this.interpretMode = mode;
    }

    public void visitFieldExpr(FieldExpr fe) {
        if(fe.accessExpr().isFieldExpr()) { fe.accessExpr().visit(this); }
        else if(!(fe.accessExpr().isNameExpr()
                || fe.accessExpr().isInvocation()
                || fe.accessExpr().isArrayExpr())) {
            new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(fe)
                    .addErrorType(MessageType.SCOPE_ERROR_330)
                    .error();
        }
    }
}
