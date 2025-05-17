package ast.topleveldecls;

import ast.*;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.types.*;
import token.*;
import utilities.Visitor;

/*
____________________________ GlobalDecl ___________________________
___________________________________________________________________
*/
public class GlobalDecl extends TopLevelDecl implements NameNode {

    private final Var myVar;
    private Type type;

    private final boolean isConstant; // GlobalDecl can either be a global/constant

    public GlobalDecl(Token t, Var v, Type type) { this(t,v,type,false); }

    public GlobalDecl(Token t, Var v, Type type, boolean isConst) {
        super(t);
        this.myVar = v;
        this.type = type;
        this.isConstant = isConst;

        addChild(this.myVar);
        addChild(this.type);
        setParent();
    }

    public Var var() { return myVar; }

    public void setType(Type t) { this.type = t; }
    public Type type() { return type; }
    public String toString() { return myVar.toString(); }

    public boolean isClassType() { return false; }
    public boolean isConstant() { return isConstant; }

    public boolean isGlobalDecl() { return true; }
    public GlobalDecl asGlobalDecl() { return this; }

    public AST decl() { return this; }

    @Override
    public void visit(Visitor v) { v.visitGlobalDecl(this); }
}
