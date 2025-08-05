package micropasses;

import ast.expressions.*;
import ast.statements.*;
import com.sun.source.tree.Scope;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeError;
import utilities.Visitor;

import java.util.ArrayList;

/**
 * Micropass #4
 * <br><br>
 *
 * ThisStmt pass is a continuation of name checking and concerns the usage of <code>break</code>
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

    private boolean insideLoop = false;

    public LoopKeywordCheck() {
        this.handler = new MessageHandler();
    }

    public LoopKeywordCheck(String fileName) {
        this.handler = new MessageHandler(fileName);
    }

    public void visitBreakStmt(BreakStmt bs) {
        if(!insideLoop) {
            handler.createErrorBuilder(ScopeError.class)
                        .addLocation(bs)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_323)
                        .generateError();
        }
    }

    public void visitContinueStmt(ContinueStmt cs) {
        if(!insideLoop) {
            handler.createErrorBuilder(ScopeError.class)
                        .addLocation(cs)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_324)
                        .generateError();
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
