package ast.expressions;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

public class Invocation extends Expression {

    public Type targetType;

    private Expression target;
    private final Name name;
    private final Vector<Expression> args;

    private String invokeSignature;

    public Invocation(Token t, Name fn, Vector<Expression> p) {
        super(t);
        this.name = fn;
        this.args = p;

        addChild(this.name);
        addChild(this.args);
        setParent();
    }

    public Expression target() { return target; }
    public Name name() { return name; }
    public Vector<Expression> arguments() { return args; }

    public boolean isInvocation() { return true; }
    public Invocation asInvocation() { return this; }

    public void setTarget(Expression e) {
        if(this.target == null) {
            this.target = e;
            addChild(this.target());
        }
    }

    public void setInvokeSignature(String inSig) { this.invokeSignature = inSig; }
    public String invokeSignature() { return this.invokeSignature; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitInvocation(this); }
}
