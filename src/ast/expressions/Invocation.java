package ast.expressions;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class Invocation extends Expression {

    public Type targetType;

    private Expression target;
    private Name name;
    private Vector<Expression> args;

    public Invocation(Token t, Name fn, Vector<Expression> p) { this(t,null, fn, p); }

    public Invocation(Token t, Expression e, Name fn, Vector<Expression> p) {
        super(t);
        this.target = e;
        this.name = fn;
        this.args = p;

        addChild(this.target);
        addChild(this.name);
        addChild(this.args);
        setParent();
    }

    public Expression target() { return target; }
    public Name name() { return name; }
    public Vector<Expression> arguments() { return args; }

    public boolean isInvocation() { return true; }
    public Invocation asInvocation() { return this; }

    public void evaluate() {}

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitInvocation(this); }
}
