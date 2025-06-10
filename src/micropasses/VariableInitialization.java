package micropasses;

import ast.statements.LocalDecl;
import ast.topleveldecls.GlobalDecl;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.Visitor;

public class VariableInitialization extends Visitor {

    private static final SemanticErrorFactory generateSemanticError = new SemanticErrorFactory();

    public VariableInitialization(boolean mode) {
        this.interpretMode = mode;
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.var().isUninit()) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                    .addLocation(gd)
                    .addErrorType(MessageType.SEMANTIC_ERROR_700)
                    .addArgs("Global",gd.toString())
                    .addSuggestType(MessageType.SEMANTIC_SUGGEST_1700)
                    .error();
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(ld.var().isUninit()) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                    .addLocation(ld)
                    .addErrorType(MessageType.SEMANTIC_ERROR_700)
                    .addArgs("Local",ld.toString())
                    .addSuggestType(MessageType.SEMANTIC_SUGGEST_1700)
                    .error();
        }
    }
}
