package ast.statements;

import ast.expressions.*;
import ast.types.*;
import token.*;
import utilities.Visitor;


public class ReturnStmt extends Statement {

    public Type type;
    private Expression expr;

    public ReturnStmt(Expression e) { this(new Token(),e); }
    public ReturnStmt(Token t, Expression e) {
        super(t);
        this.type = null;
        this.expr = e;

        addChild(this.type);
        addChild(this.expr);
        setParent();
    }

    public Type returnType() { return type; }
    public Expression expr() { return expr; }

    public boolean isReturnStmt() { return true; }
    public ReturnStmt asReturnStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitReturnStmt(this); }
}
