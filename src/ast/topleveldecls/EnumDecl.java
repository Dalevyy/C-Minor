package ast.topleveldecls;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/*
____________________________ EnumDecl ____________________________
An EnumDecl is composed of 3 parts:
    1. A name given to the Enum
    2. An optional type denoting what type the Enum evaluates to
    3. A vector of Vars that store the enum values. All Vars
       will have a name, but have an optional expression.
__________________________________________________________________
*/
public class EnumDecl extends TopLevelDecl implements NameNode {

    private Type type;

    private final Name name;
    private final Vector<Var> constants;

    public EnumDecl(Token t, Name name, Vector<Var> ef) { this(t,name, null, ef); }
    public EnumDecl(Token t, Name name, Type constantType, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.type = constantType;
        this.constants = ef;

        addChild(this.name);
        addChild(this.type);
        addChild(this.constants);
        setParent();
    }

    public Name name() { return name; }
    public Type type() { return type; }
    public Vector<Var> constants() { return constants;}

    public boolean isEnumDecl() { return true; }
    public EnumDecl asEnumDecl() { return this; }

    public AST decl() { return this; }
    public void setType(Type t) { this.type = t; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }
}
