package micropasses;

import ast.expressions.Expression;
import ast.expressions.InStmt;
import ast.expressions.OutStmt;
import messages.errors.scope.ScopeErrorFactory;
import messages.errors.ErrorBuilder;
import messages.MessageType;
import utilities.Visitor;

/**
 * Micropass #1
 * <p>
 * Due to the nature of the C Minor grammar, <code>InStmt</code> and <code>OutStmt</code> nodes
 * within the <code>AST</code> will not be written correctly. ThisStmt is because the parser will
 * treat the <code>'<<'</code> and <code>'>>'</code> operators as binary shift operators instead
 * of the stream insertion and extraction operators. Thus, after we finish parsing, we need to
 * do a pass to rewrite these statements to ensure the <code>AST</code> is correctly written.
 * </p>
 * <p>
 * Additionally, for <code>InStmt</code> nodes, we will also check to make sure each expression
 * in the <code>InStmt</code> represents a <code>NameExpr</code> since data needs to be stored
 * in a memory location that the name points to.
 * </p>
 * @author Daniel Levy
 */
public class InOutStmtRewrite extends Visitor {
    
    private final ScopeErrorFactory generateScopeError;

    public InOutStmtRewrite() { generateScopeError = new ScopeErrorFactory(); }
    public InOutStmtRewrite(boolean interpretMode) {
        this();
        this.interpretMode = interpretMode;
    }

    public void visitInStmt(InStmt is) {
        // ERROR CHECK #1: Each expression in an InStmt has to be a name or else
        //                 we can not store any input values from the user
        for(Expression e : is.getInExprs()) {
            if(!e.isNameExpr())
                new ErrorBuilder(generateScopeError,this.interpretMode)
                    .addLocation(is)
                    .addErrorType(MessageType.SCOPE_ERROR_327)
                    .error();
        }
    }

    public void visitOutStmt(OutStmt os) {
        // ERROR CHECK #1: Make sure the output statement does not have an input statement
        for(Expression e : os.getOutExprs()) {
            if(e.isInStmt())
                new ErrorBuilder(generateScopeError,this.interpretMode)
                    .addLocation(os)
                    .addErrorType(MessageType.SYNTAX_ERROR_101)
                    .error();
        }
    }
}
