package micropasses;

import ast.expressions.*;
import ast.statements.*;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeErrorFactory;
import utilities.Visitor;

import java.util.ArrayList;

/**
 * Micropass #4
 * <br><br>
 *
 * This pass is a continuation of name checking and concerns the usage of <code>break</code>
 * and <code>continue</code> inside of a C Minor program. In order to use these keywords,
 * they must be found inside a loop statement and if they are not, we have to generate a
 * scoping error.
 * <br><br>
 * The following is a list of the loop statements we check during this pass.
 * <ol>
 *     <li><code>DoStmt</code></li>
 *     <li><code>ForStmt</code></li>
 *     <li><code>WhileStmt</code></li>
 * </ol>
 * @author Daniel Levy
 */
public class LoopKeywordCheck extends Visitor {

    private boolean insideLoop;
    private final ScopeErrorFactory generateScopeError;
    private final ArrayList<String> errors;

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
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(bs)
                        .addErrorType(MessageType.SCOPE_ERROR_323)
                        .error()
            );
        }
    }

    public void visitContinueStmt(ContinueStmt cs) {
        if(!insideLoop) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(cs)
                        .addErrorType(MessageType.SCOPE_ERROR_324)
                        .error()
            );
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
