package ast.expressions;

import ast.AST;
import token.*;
import utilities.Visitor;

public class FieldExpr extends Expression {

    private Expression target;
    private Expression accessExpr;

    private final boolean asCheck;
    private final boolean isInvocation;

    public FieldExpr(Expression ft, Expression fn) { this(new Token(),ft,fn,false); }
    public FieldExpr(Expression ft, Expression fn, boolean ac) { this(new Token(),ft,fn,ac); }
    public FieldExpr(Token t, Expression ft, Expression fn, boolean ac) {
        super(t);
        this.target = ft;
        this.accessExpr = fn;

        this.asCheck = ac;
        this.isInvocation = this.accessExpr.isInvocation();

        addChild(this.target);
        addChild(this.accessExpr);
        setParent();
    }

    public Expression fieldTarget() { return target; }
    public Expression accessExpr() { return accessExpr; }
    public void setFieldAccess(Expression e) { this.accessExpr = e; }

    public boolean isAsCheck() { return asCheck; }
    public boolean isMethodInvocation() { return isInvocation; }
    public boolean isFieldExpr() { return true; }
    public FieldExpr asFieldExpr() { return this; }

    @Override
    public String toString() { return accessExpr.toString(); }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                target = n.asExpression();
                break;
            case 1:
                accessExpr = n.asExpression();
                break;
        }
    }

    @Override
    public void visit(Visitor v) { v.visitFieldExpr(this); }

    public static class FieldExprBuilder {
        private Expression target;
        private Expression accessExpr;
        private boolean as;

        public FieldExprBuilder setTarget(Expression target) {
            this.target = target;
            return this;
        }

        public FieldExprBuilder setAccessExpr(Expression accessExpr) {
            this.accessExpr = accessExpr;
            return this;
        }

        public FieldExprBuilder setAsOperator() {
            this.as = true;
            return this;
        }

        public FieldExpr create() {
            return new FieldExpr(
                    new Token(target.text+accessExpr.text,
                                new Location(target.location.start,accessExpr.location.end)),
                    target,accessExpr,as);
        }
    }
}
