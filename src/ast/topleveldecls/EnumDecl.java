package ast.topleveldecls;

import ast.AST;
import ast.misc.Name;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.types.EnumType;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/**
 * This class represents the declaration of an enumeration inside C
 * Minor
 * @author Daniel Levy
 */
public class EnumDecl extends TopLevelDecl implements NameNode {

    private EnumType type;
    private final Name name;
    private final Vector<Var> constants;

    public EnumDecl(Token t, Name name, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.constants = ef;

        addChild(this.name);
        addChild(this.constants);
        setParent();
    }

    public Name name() { return name; }
    public EnumType type() { return type; }
    public Vector<Var> constants() { return constants;}

    public boolean isEnumDecl() { return true; }
    public EnumDecl asEnumDecl() { return this; }

    public AST decl() { return this; }
    public void setType(EnumType t) { this.type = t; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }
}
