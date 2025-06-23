package ast.expressions;

import ast.AST;
import ast.misc.Name;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

public class Invocation extends Expression {

    public Type targetType;

    private Vector<Type> templateTypes;
    private Name name;
    private final Vector<Expression> args;

    private String invokeSignature;

    public Invocation(Token t, Name fn, Vector<Type> tt, Vector<Expression> p) {
        super(t);
        this.name = fn;
        this.templateTypes = tt;
        this.args = p;

        addChild(this.name);
        addChild(this.args);
        setParent();
    }

    public Name name() { return name; }
    public Vector<Expression> arguments() { return args; }

    public boolean isInvocation() { return true; }
    public Invocation asInvocation() { return this; }

    public void setInvokeSignature(String inSig) { this.invokeSignature = inSig; }
    public String invokeSignature() { return this.invokeSignature; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                name = n.asName();
                break;
            default:
                args.remove(pos-1);
                args.add(pos+1,n.asExpression());
        }
    }

    @Override
    public void visit(Visitor v) { v.visitInvocation(this); }

    public static class InvocationBuilder {
        private Token metaData;
        private Name name;
        private Vector<Expression> args;

        public InvocationBuilder setName(Name n) {
            this.name = n;
            return this;
        }

        public InvocationBuilder setArgs(Vector<Expression> args) {
            this.args = args;
            return this;
        }

        public Invocation create() {
            metaData = new Token();
            metaData.appendText(name.text);
            for(Expression e : args)
                metaData.appendText(e.text);
            metaData.setStartLocation(this.name.location.start);
            if(args.isEmpty())
                metaData.setEndLocation(this.name.location.end);
            else
                metaData.setEndLocation(this.args.top().location.end);

            return new Invocation(metaData,name,new Vector<>(),args);
        }
    }
}
