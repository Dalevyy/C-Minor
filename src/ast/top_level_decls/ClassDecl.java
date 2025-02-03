package ast.top_level_decls;

import ast.*;
import ast.class_body.*;
import ast.types.*;
import token.*;
import utilities.*;

public class ClassDecl extends TopLevelDecl implements NameNode {

    public SymbolTable symbolTable;
    public Modifiers mod;

    private Name name;
    private Vector<Type> typeParams; // Only used if using a templated class
    private ClassType superClass;
    private ClassBody body;
    private InitDecl constructor;

    public ClassDecl(Token t, Modifier m, Name n, ClassBody b) { this(t,m,n,null,null,b); }

    public ClassDecl(Token t, Modifier m, Name n, Vector<Type> tp, ClassType sc, ClassBody b) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.typeParams = tp;
        this.superClass = sc;
        this.body = b;

        addChild(this.name);
        addChild(this.typeParams);
        addChild(this.superClass);
        addChild(this.body);
        setParent();
    }

    public Name name() { return name; }
    public Vector<Type> typeParams() { return typeParams; }
    public ClassType superClass() { return superClass; }
    public ClassBody clalssBlock() { return body; }

    public void setID(String s) { this.name.setName(s); }
    public AST declName() { return this; }

    public void setConstructor(InitDecl ind) { this.constructor = ind; }
    public InitDecl getConstructor() { return this.constructor; }

    public boolean isClassDecl() { return true; }
    public ClassDecl asClassDecl() { return this; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitClassDecl(this); }
}
