package ast.statements;

import ast.expressions.Expression;
import ast.expressions.NewExpr;
import ast.operators.AssignOp;
import ast.operators.AssignOp.AssignType;
import token.Token;
import utilities.Visitor;

public class RetypeStmt extends AssignStmt {

    public RetypeStmt(Token t, Expression n, NewExpr ne) { super(t,n,ne,new AssignOp(AssignType.EQ)); }

    public Expression getName() { return this.LHS(); }
    public NewExpr getNewObject() { return this.RHS().asNewExpr(); }

    public boolean isRetypeStmt() { return true; }
    public RetypeStmt asRetypeStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitRetypeStmt(this); }
}
