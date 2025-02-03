package ast.expressions;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class NewExpr extends Expression {

    private ClassType cType;
    private Vector<Var> fields;

    public NewExpr(Token t, ClassType ct, Vector<Var> f) {
        super(t);
        this.cType = ct;
        this.fields = f;

        addChild(this.cType);
        addChild(this.fields);
        setParent();
    }

    public ClassType classType() { return cType; }
    public Vector<Var> args() { return fields; }

    public boolean isNewExpr() { return true; }
    public NewExpr asNewExpr() { return this; }

    public void evaluate() {}

    @Override
    public void visit(Visitor v) { v.visitNewExpr(this); }
}
