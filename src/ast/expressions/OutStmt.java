package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * This class represents an output statement. In C Minor, we are going
 * to mimic a C++ 'cout statement' to provide a user with more flexibility
 * and control over how output will be displayed.
 */
public class OutStmt extends Expression {

    /**
     * <p>
     *     A vector containing all expressions that will be printed out.
     *     An expression in this context is anything in between either
     *     two {@code <<} operators or an expression that follows a single
     *     {@code <<} operator.
     * </p>
     * */
    private Vector<Expression> exprs;

    /**
     * Creates {@code OutStmt} node.
     * @param t Metadata token
     * @param exprs Vector of expressions
     */
    public OutStmt(Token t, Vector<Expression> exprs) {
        super(t);
        this.exprs = exprs;

        addChild(this.exprs);
        setParent();
    }

    public Vector<Expression> outExprs() { return exprs; }
    public void setOutExprs(Vector<Expression> exprs) { this.exprs = exprs; }

    public boolean isOutStmt() { return true; }
    public OutStmt asOutStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        exprs.remove(pos);
        exprs.add(pos,n.asExpression());
    }

    @Override
    public void visit(Visitor v) { v.visitOutStmt(this); }
}
