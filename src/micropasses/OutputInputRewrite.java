package micropasses;

import ast.*;
import ast.expressions.*;
import utilities.Visitor;

/*
    Micro pass #1: OutStmt and InStmt Rewriting

    This micro pass will rewrite any OutStmt and InStmt
    nodes in the current AST. The parser will not generate
    the correct AST nodes since it will parse these expressions
    as if they were binary expressions.

    This pass is designed to take all binary expressions written
    with '<<' and '>>' in an OutStmt/InStmt and change them into
    individual expressions, so we can individually print out everything.

*/

public class OutputInputRewrite extends Visitor {

    private boolean insideIO = false;
    private Vector<Expression> exprs;

    public void visitBinaryExpr(BinaryExpr be) {
        if(insideIO) {
            if(be.binaryOp().toString().equals("<<") || be.binaryOp().toString().equals(">>")) {
                if(!be.toString().startsWith("(")) {
                    be.LHS().visit(this);
                    if(!be.LHS().isBinaryExpr())
                        exprs.addChild(be.LHS());
                }
                else { exprs.addChild(be.LHS()); }

                exprs.addChild(be.RHS());
            }
            else
                exprs.addChild(be);
        }
    }

    public void visitOutStmt(OutStmt os) {
        insideIO = true;
        exprs = new Vector<>();

        if(!os.outExprs().get(0).isBinaryExpr())
            return;

        os.removeChild(0);
        os.outExprs().visit(this);

        os.setOutExprs(exprs);
        os.addChild(exprs);
        insideIO = false;
    }

    public void visitInStmt(InStmt in) {
        insideIO = true;
        exprs = new Vector<>();
        in.removeChild(0);

        in.inExprs().visit(this);

        in.setInExprs(exprs);
        in.addChild(exprs);
        insideIO = false;
    }
}
