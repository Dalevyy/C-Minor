package ast.topleveldecls;

import ast.*;
import ast.classbody.*;
import ast.misc.Modifier;
import ast.misc.Modifiers;
import ast.misc.Name;
import ast.misc.NameNode;
import ast.types.*;
import token.*;
import utilities.*;

/*
____________________________ ClassDecl ____________________________
___________________________________________________________________
*/
public class ClassDecl extends TopLevelDecl implements NameNode {

    public SymbolTable symbolTable;
    public Modifiers mod;

    private final Name name;
    private final Vector<Type> typeParams; // Only used if using a templated class
    private final ClassType superClass;
    private final ClassBody body;
    private InitDecl constructor;

    private Vector<Name> inheritedClasses;

    public ClassDecl(Token t, Modifier m, Name n, ClassBody b) { this(t,m,n,null,null,b); }

    public ClassDecl(Token t, Modifier m, Name n, Vector<Type> tp, ClassType sc, ClassBody b) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.typeParams = tp;
        this.superClass = sc;
        this.body = b;
        this.inheritedClasses = new Vector<>();

        addChild(this.name);
        addChild(this.typeParams);
        addChild(this.superClass);
        addChild(this.body);
        setParent();
    }

    public Name name() { return name; }
    public Vector<Type> typeParams() { return typeParams; }
    public ClassType superClass() { return superClass; }
    public ClassBody classBlock() { return body; }

    public AST decl() { return this; }

    public void setConstructor(InitDecl ind) { this.constructor = ind; }
    public InitDecl constructor() { return this.constructor; }

    public void addBaseClass(Name n) { this.inheritedClasses.add(n); }
    public Vector<Name> getInheritedClasses() { return this.inheritedClasses; }

    public boolean inherits(String sup) {
        for(Name sub : this.inheritedClasses)
            if(sub.toString().equals(sup))
                return true;
        return false;
    }

    public boolean isClassDecl() { return true; }
    public ClassDecl asClassDecl() { return this; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitClassDecl(this); }
}
