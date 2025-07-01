package ast.topleveldecls;

import ast.*;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.statements.LocalDecl;
import ast.types.*;
import token.*;
import utilities.Visitor;

/*
____________________________ GlobalDecl ___________________________
___________________________________________________________________
*/
public class GlobalDecl extends TopLevelDecl implements NameNode {

    private Var myVar;
    private Type type;

    private final boolean isConstant; // GlobalDecl can either be a global/constant

    public GlobalDecl() { this(new Token(),null,null); }
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

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link GlobalDecl}
     */
    @Override
    public AST deepCopy() {
        return new GlobalDeclBuilder()
                   .setMetaData(this)
                   .setVar(this.myVar.deepCopy().asVar())
                   .setType(this.type.deepCopy().asType())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitGlobalDecl(this); }

    public static class GlobalDeclBuilder extends NodeBuilder {
        private final GlobalDecl gd = new GlobalDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return GlobalDeclBuilder
         */
        public GlobalDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the global declaration's {@link #var}.
         * @param var Variable that represents the global declaration
         * @return GlobalDeclBuilder
         */
        public GlobalDeclBuilder setVar(Var var) {
            gd.myVar = var;
            return this;
        }

        /**
         * Sets the global declaration's {@link #type}.
         * @param type Type representing the data type the global declaration represents
         * @return GlobalDeclBuilder
         */
        public GlobalDeclBuilder setType(Type type) {
            gd.setType(type);
            return this;
        }

        /**
         * Creates a {@link GlobalDecl} object.
         * @return {@link GlobalDecl}
         */
        public GlobalDecl create() {
            super.saveMetaData(gd);
            gd.addChild(gd.myVar);
            return gd;
        }
    }
}
