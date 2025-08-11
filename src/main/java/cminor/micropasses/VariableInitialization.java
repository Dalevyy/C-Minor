package cminor.micropasses;

import cminor.ast.statements.ForStmt;
import cminor.ast.statements.LocalDecl;
import cminor.ast.topleveldecls.GlobalDecl;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.semantic.SemanticError;
import cminor.utilities.Visitor;

public class VariableInitialization extends Visitor {

    public VariableInitialization() { this.handler = new MessageHandler(); }
    public VariableInitialization(String fileName) {
        this.handler = new MessageHandler(fileName);
    }

    public void visitForStmt(ForStmt fd) { fd.getBody().visit(this); }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(!gd.hasInitialValue()) {
            handler.createErrorBuilder(SemanticError.class)
                    .addLocation(gd)
                    .addErrorNumber(MessageNumber.SEMANTIC_ERROR_700)
                    .addErrorArgs("Global",gd.toString())
                    .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1700)
                    .generateError();
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(!ld.hasInitialValue()) {
            handler.createErrorBuilder(SemanticError.class)
                    .addLocation(ld)
                    .addErrorNumber(MessageNumber.SEMANTIC_ERROR_700)
                    .addErrorArgs("Local",ld.toString())
                    .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1700)
                    .generateError();
        }
    }
}
