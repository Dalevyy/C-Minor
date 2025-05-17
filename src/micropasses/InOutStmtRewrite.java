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
    private Vector<Expression> exprs;

    public InOutStmtRewrite() { generateScopeError = new ScopeErrorFactory(); }
    public InOutStmtRewrite(boolean interpretMode) {
        this();
        this.interpretMode = interpretMode;
    }

    public void visitBinaryExpr(BinaryExpr be) {
        if(insideIO) {
            if(be.binaryOp().toString().equals("<<") || be.binaryOp().toString().equals(">>")) {
                if(!be.toString().startsWith("(")) {
                    be.LHS().visit(this);
                    if(!be.LHS().isBinaryExpr())
                        exprs.add(be.LHS());
                }
                else { exprs.add(be.LHS()); }

                exprs.add(be.RHS());
            }
            else
                exprs.add(be);
        }
    }

    public void visitOutStmt(OutStmt os) {
        insideIO = true;
        exprs = new Vector<>();

        if(!os.outExprs().get(0).isBinaryExpr())
            return;

        os.removeChild(0);
        for(Expression e : os.outExprs()) { e.visit(this); }

        os.setOutExprs(exprs);
        os.addChild(exprs);
        insideIO = false;
    }

    public void visitInStmt(InStmt in) {
        insideIO = true;
        exprs = new Vector<>();

        // ERROR CHECK #1: If we have a single input expression, then
        //                 make sure it refers to some name
        if(!in.inExprs().get(0).isBinaryExpr()) {
            if(!in.inExprs().get(0).isNameExpr()) {
                new ErrorBuilder(generateScopeError,this.interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.SCOPE_ERROR_327)
                        .error();
            }
            return;
        }

        in.removeChild(0);
        for(Expression e : in.inExprs()) { e.visit(this); }

        in.setInExprs(exprs);
        in.addChild(exprs);

        // ERROR CHECK #2: Same as error check #1, make sure each input
        //                 expression represents a name
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
