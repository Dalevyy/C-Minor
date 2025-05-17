package micropasses;

import ast.expressions.*;
import messages.errors.ErrorBuilder;
import messages.errors.scope_error.ScopeErrorFactory;
import messages.MessageType;
import utilities.Vector;
import utilities.Visitor;

/*
    Micropass #1: OutStmt and InStmt Rewriting

    This micro pass will rewrite any OutStmt and InStmt
    nodes in the current AST. The parser will not generate
    the correct AST nodes since it will parse these expressions
    as if they were binary expressions.

    This pass is designed to take all binary expressions written
    with '<<' and '>>' in an OutStmt/InStmt and change them into
    individual expressions, so we can individually print out everything.

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
