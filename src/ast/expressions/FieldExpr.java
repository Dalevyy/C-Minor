package ast.expressions;

import token.*;
import utilities.Visitor;

public class FieldExpr extends Expression {

    private Expression target;
    private Expression name;

    private boolean asCheck;

    public FieldExpr(Token t, Expression ft, Expression fn, boolean ac) {
        super(t);
        this.target = ft;
        this.name = fn;

        this.asCheck = ac;

        addChild(this.target);
        addChild(this.name);
        setParent();
    }

    public Expression fieldTarget() { return target; }
    public Expression name() { return name; }

    public boolean isAsCheck() { return asCheck; }
    public boolean isFieldExpr() { return true; }
    public FieldExpr asFieldExpr() { return this; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitFieldExpr(this); }
}
