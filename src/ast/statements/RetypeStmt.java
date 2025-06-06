package ast.statements;

import ast.expressions.Expression;
import ast.expressions.NewExpr;
import token.Token;
import utilities.Visitor;

public class RetypeStmt extends Statement {
    private final Expression objName;
    private final NewExpr objStatement;

    public RetypeStmt(Token t, Expression n, NewExpr ne) {
        super(t);
        this.objName = n;
        this.objStatement = ne;

        this.addChild(n);
        this.addChild(ne);
        this.setParent();
    }

    public Expression getName() { return this.objName; }
    public NewExpr getNewObject() { return this.objStatement; }

    public boolean isRetypeStmt() { return true; }
    public RetypeStmt asRetypeStmt() { return this; }

    @Override
    public void visit(Visitor v) { v.visitRetypeStmt(this); }
}
