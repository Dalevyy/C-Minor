package ast.top_level_decls;

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

    private final Name name;
    public Type constantType;
    private final Vector<Var> eVars;

    public EnumDecl(Token t, Name name, Vector<Var> ef) { this(t,name, null, ef); }

    public EnumDecl(Token t, Name name, Type constantType, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.constantType = constantType;
        this.eVars = ef;

        addChild(this.name);
        addChild(this.constantType);
        addChild(this.eVars);
        setParent();
    }

    public Name name() { return name; }
    public Type constantType() { return constantType; }
    public Vector<Var> enumVars() { return eVars;}

    public boolean isEnumDecl() { return true; }
    public EnumDecl asEnumDecl() { return this; }

    public AST decl() { return this; }
    public void setType(Type t) { this.constantType = t; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }
}
