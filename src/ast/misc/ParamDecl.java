package ast.misc;

import ast.*;
import ast.types.*;
import token.*;
import utilities.Vector;
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

    private Name name;
    private Type paramType;

    public ParamDecl() { this(new Token(),null,null,null); }
    public ParamDecl(Modifier m, Name n, Type paramType) { this(new Token(),m,n, paramType); }

    public ParamDecl(Token t, Modifier m, Name n, Type paramType) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.paramType = paramType;

        addChild(this.name);
    }

    public Type type() { return paramType; }

    @Override
    public String toString() { return name.toString(); }

    public void setType(Type t) { this.paramType = t;}
    public AST decl() { return this;}

    public boolean isParamDecl() { return true; }
    public ParamDecl asParamDecl() { return this; }

    public boolean isParamTypeTemplated(Vector<Typeifier> typeParams) {
        for(Typeifier typeParam : typeParams) {
            if(typeParam.toString().equals(paramType.typeSignature()))
                return true;
        }
        return false;
    }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                name = n.asName();
                break;
            case 1:
                paramType = n.asType();
                break;
        }
    }

    @Override
    public AST deepCopy() {
        return new ParamDeclBuilder()
                   .setMetaData(this)
                   .setMod(this.mod)
                   .setName(this.name.deepCopy().asName())
                   .setType(this.paramType.deepCopy().asType())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitParamDecl(this); }

    /**
     * Internal class that builds a {@link ParamDecl} object.
     */
    public static class ParamDeclBuilder extends NodeBuilder {

        /**
         * {@link ParamDecl} object we are building.
         */
        private final ParamDecl pd = new ParamDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return FieldDeclBuilder
         */
        public ParamDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #mod}.
         * @param mod Modifier representing the passing technique of the parameter
         * @return ParamDeclBuilder
         */
        public ParamDeclBuilder setMod(Modifiers mod) {
            pd.mod = mod;
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #name}.
         * @param name Name that represents the parameter
         * @return ParamDeclBuilder
         */
        public ParamDeclBuilder setName(Name name) {
            pd.name = name;
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #paramType}.
         * @param type Type representing the data type the parameter represents
         * @return ParamDeclBuilder
         */
        public ParamDeclBuilder setType(Type type) {
            pd.setType(type);
            return this;
        }

        /**
         * Creates a {@link ParamDecl} object.
         * @return {@link ParamDecl}
         */
        public ParamDecl create() {
            super.saveMetaData(pd);
            pd.addChild(pd.name);
            return pd;
        }
    }
}
