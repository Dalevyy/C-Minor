package AST.TopLevelDecls;

import AST.*;
import AST.ClassBody.*;
import AST.Types.*;
import Token.*;
import Utilities.PokeVisitor;

// TODO: Symbol Tables for NameChecking
public class ClassDecl extends TopLevelDecl implements ID {

    Modifier mod;
    Name name;
    Vector<Type> typeParams; // Only used if using a templated class
    ClassType superClass;
    ClassBody body;

    public ClassDecl(Token t, Modifier m, Name n, ClassBody b) { this(t,m,n,null,null,b); }

    public ClassDecl(Token t, Modifier m, Name n, Vector<Type> tp, ClassType sc, ClassBody b) {
        super(t);
        this.mod = m;
        this.name = n;
        this.typeParams = tp;
        this.superClass = sc;
        this.body = b;

        addChild(this.mod);
        addChild(this.name);
        addChild(this.typeParams);
        addChild(this.superClass);
        addChild(this.body);
        setParent();
    }

    public Modifier getModifier() { return mod; }
    public Name getName() { return name; }
    public Vector<Type> getTypeParams() { return typeParams; }
    public ClassType getSuperClass() { return superClass; }
    public ClassBody getClassBody() { return body; }

    public void setID(String s) { this.name.setName(s); }
    public String getID() { return this.name.toString(); }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsClassDecl(this); }
}
