package ast;

import ast.Modifier.Mods;
import ast.types.*;
import token.*;
import utilities.Visitor;

/*
___________________________ ParamDecl ___________________________
A ParamDecl represents a parameter that is declared for either a
FuncDecl or a MethodDecl. Parameters will take the form of
'<mod> <name>:<type>`.
_________________________________________________________________
*/
public class ParamDecl extends AST implements NameNode {

    public Modifiers mod;

    private final Name name;
    private Type type;

    public ParamDecl(Modifier m, Name n, Type type) { this(new Token(),m,n,type); }

    public ParamDecl(Token t, Modifier m, Name n, Type type) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.type = type;

        addChild(this.name);
        addChild(this.type);
        setParent();
    }

    public Type type() { return type; }

    @Override
    public String toString() { return name.toString(); }

    public void setType(Type t) { this.type = t;}
    public AST decl() { return this;}

    public boolean isParamDecl() { return true; }
    public ParamDecl asParamDecl() { return this; }

    @Override
    public void visit(Visitor v) { v.visitParamDecl(this); }

    public static class ParamDeclBuilder {
        private Modifier mod;
        private Name name;
        private Type type;

        public ParamDeclBuilder setModifier(Mods mod) {
            this.mod = new Modifier(mod);
            return this;
        }

        public ParamDeclBuilder setName(String s) {
            this.name = new Name(s);
            return this;
        }

        public ParamDeclBuilder setType(Type t) {
            this.type = t;
            return this;
        }

        public ParamDecl createParamDecl() { return new ParamDecl(mod,name,type); }

    }
}
