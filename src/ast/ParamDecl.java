package ast;

import ast.types.*;
import token.*;
import utilities.Visitor;

public class ParamDecl extends AST implements NameNode {

    public Modifiers mod;

    private Name name;
    private Type type;

    public ParamDecl(Token t, Modifier m, Name n, Type type) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.type = type;

        addChild(this.name);
        addChild(this.type);
        setParent();
    }

    public Type getType() { return type; }

    @Override
    public String toString() { return name.toString(); }

    public boolean isClassType() { return type instanceof ClassType; }
    public boolean isConstant() { return false; }

    public void setID(String s) { ;}
    public AST declName() { return this;}

    public boolean isParamDecl() { return true; }
    public ParamDecl asParamDecl() { return this; }

    @Override
    public void visit(Visitor v) { v.visitParamDecl(this); }
}
