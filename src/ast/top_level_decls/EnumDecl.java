package ast.top_level_decls;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Visitor;

/*
 +-----------------------------------------------------------+
 +                        EnumDecl                           +
 +-----------------------------------------------------------+

An EnumDecl is composed of 3 parts:
    1. A name given to the Enum
    2. An optional type denoting what type the Enum evaluates to
    3. A vector of Vars that store the enum values. All Vars
       will have a name, but have an optional expression.

Parent Node: Compilation
*/
public class EnumDecl extends TopLevelDecl implements NameNode {

    private Name name;
    public Type type;
    private Vector<Var> eVars;

    public EnumDecl(Token t, Name name, Vector<Var> ef) { this(t,name, null, ef); }

    public EnumDecl(Token t, Name name, Type type, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.type = type;
        this.eVars = ef;

        addChild(this.name);
        addChild(this.type);
        addChild(this.eVars);
        setParent();
    }

    public Name name() { return name; }
    public Type type() { return type; }
    public Vector<Var> enumVars() { return eVars;}

    public boolean isEnumDecl() { return true; }
    public EnumDecl asEnumDecl() { return this; }

    public AST declName() { return this; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }
}
