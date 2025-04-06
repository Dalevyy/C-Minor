package micropasses;

import ast.expressions.*;
import ast.statements.*;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.scope_error.ScopeErrorFactory;
import utilities.Visitor;

import java.util.ArrayList;

/*
________________________________ Micropass #1 ________________________________
This micropass is designed to check if the keywords 'break' and 'continue'
were found in a C Minor program. This will occur after the name resolution
pass is completed.


If either keyword is used, then we are going to check to see if these keywords
are found inside a C Minor's loop constructs (DoStmts, ForStmts, and
WhileStmts). If these keywords were found, but not inside a loop construct,
then we are going to create a ScopeError.
______________________________________________________________________________
*/
public class LoopKeywordCheck extends Visitor {

    private boolean insideLoop;
    private ScopeErrorFactory generateScopeError;
    private ArrayList<String> errors;

    public LoopKeywordCheck() {
        insideLoop = false;
        generateScopeError = new ScopeErrorFactory();
        errors = new ArrayList<>();
        this.interpretMode = false;
    }

    public LoopKeywordCheck(boolean interpret) {
        this();
        this.interpretMode = interpret;
    }

    public void visitBreakStmt(BreakStmt bs) {
        if(!insideLoop) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(bs)
                    .addErrorType(MessageType.SCOPE_ERROR_323)
                    .error());
        }
    }

    public void visitContinueStmt(ContinueStmt cs) {
        if(!insideLoop) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(cs)
                    .addErrorType(MessageType.SCOPE_ERROR_324)
                    .error());
        }
    }

    public void visitDoStmt(DoStmt ds) {
        boolean prevInsideLoop = insideLoop;
        insideLoop = true;
        super.visitDoStmt(ds);
        insideLoop = prevInsideLoop;
    }

    public void visitForStmt(ForStmt fs) {
        boolean prevInsideLoop = insideLoop;
        insideLoop = true;
        super.visitForStmt(fs);
        insideLoop = prevInsideLoop;
    }

    public void visitWhileStmt(WhileStmt ws) {
        boolean prevInsideLoop = insideLoop;
        insideLoop = true;
        super.visitWhileStmt(ws);
        insideLoop = prevInsideLoop;
    }

}
