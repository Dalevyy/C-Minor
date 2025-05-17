package micropasses;

import ast.expressions.BinaryExpr;
import ast.expressions.Expression;
import ast.expressions.InStmt;
import ast.expressions.OutStmt;
import messages.errors.ErrorBuilder;
import messages.errors.scope_error.ScopeErrorFactory;
import messages.MessageType;
import utilities.Vector;
import utilities.Visitor;

/**
 * Micropass #1
 * <br><br>
 * Due to the nature of the C Minor grammar, <code>InStmt</code> and <code>OutStmt</code> nodes
 * within the <code>AST</code> will not be written correctly. This is because the parser will
 * treat the <code>'<<'</code> and <code>'>>'</code> operators as binary shift operators instead
 * of the stream insertion and extraction operators. Thus, after we finish parsing, we need to
 * do a pass to rewrite these statements to ensure the <code>AST</code> is correctly written.
 * <br><br>
 * Additionally, for <code>InStmt</code> nodes, we will also check to make sure each expression
 * in the <code>InStmt</code> represents a <code>NameExpr</code> since data needs to be stored
 * in a memory location that the name points to.
 * @author Daniel Levy
 */
public class InOutStmtRewrite extends Visitor {

    private boolean insideIO = false;
    private final ScopeErrorFactory generateScopeError;
    private Vector<Expression> ioExprs;

    public InOutStmtRewrite() { generateScopeError = new ScopeErrorFactory(); }
    public InOutStmtRewrite(boolean interpretMode) {
        this();
        this.interpretMode = interpretMode;
    }

    public void visitBinaryExpr(BinaryExpr be) {
        if(insideIO) {
            switch(be.binaryOp().toString()) {
                case "<<":
                case ">>":
                    if(!be.toString().startsWith("(")) {
                        be.LHS().visit(this);
                        if(!be.LHS().isBinaryExpr()) { ioExprs.add(be.LHS()); }
                    }
                    else { ioExprs.add(be.LHS()); }

                    ioExprs.add(be.RHS());
                    break;
                default:
                    ioExprs.add(be);
            }
        }
    }

    public void visitOutStmt(OutStmt os) {
        insideIO = true;
        ioExprs = new Vector<>();
        
        if(os.outExprs().get(0).isBinaryExpr()) {
            os.removeChild(0);
            for(Expression e : os.outExprs()) { e.visit(this); }

            os.setOutExprs(ioExprs);
            os.addChild(ioExprs);
        }

        insideIO = false;
    }

    public void visitInStmt(InStmt in) {
        insideIO = true;
        ioExprs = new Vector<>();
        
        if(in.inExprs().get(0).isBinaryExpr()) {
            in.removeChild(0);
            for(Expression e : in.inExprs()) { e.visit(this); }

            in.setInExprs(ioExprs);
            in.addChild(ioExprs);
        }

        // ERROR CHECK #1: Each expression in an InStmt has to be a name or else
        //                 we can not store any input values from the user
        for(Expression e : in.inExprs()) {
            if(!e.isNameExpr()) {
                new ErrorBuilder(generateScopeError,this.interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.SCOPE_ERROR_327)
                        .error();
            }
        }
        
        insideIO = false;
    }
}
