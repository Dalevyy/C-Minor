package ast.statements;

import ast.*;
import ast.classbody.FieldDecl;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class LocalDecl extends Statement implements NameNode {

    private Var myVar;
    private Type type;

    public LocalDecl(){ this(new Token(),null,null);}
    public LocalDecl(Token metaData, Var v, Type type) {
        super(metaData);
        this.myVar = v;
        this.type = type;

        addChild(this.myVar);
    }

    public Var var() { return myVar; }

    public Type type() { return type; }
    public String toString() { return myVar.toString(); }

    public AST decl() { return this; }
    public void setType(Type t) { this.type = t; }

    public boolean isLocalDecl() { return true; }
    public LocalDecl asLocalDecl() { return this; }

    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                myVar = node.asVar();
                break;
            case 1:
                type = node.asType();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link LocalDecl}
     */
    @Override
    public AST deepCopy() {
        return new LocalDeclBuilder()
                   .setMetaData(this)
                   .setVar(this.myVar.deepCopy().asVar())
                   .setType(this.type.deepCopy().asType())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitLocalDecl(this); }

    public static class LocalDeclBuilder extends NodeBuilder {
        private final LocalDecl ld = new LocalDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return LocalDeclBuilder
         */
        public LocalDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the local declaration's {@link #var}.
         * @param var Variable that represents the local declaration
         * @return LocalDeclBuilder
         */
        public LocalDeclBuilder setVar(Var var) {
            ld.myVar = var;
            return this;
        }

        /**
         * Sets the local declaration's {@link #type}.
         * @param type Type representing the data type the local declaration represents
         * @return LocalDeclBuilder
         */
        public LocalDeclBuilder setType(Type type) {
            ld.setType(type);
            return this;
        }

        /**
         * Creates a {@link LocalDecl} object.
         * @return {@link LocalDecl}
         */
        public LocalDecl create() {
            super.saveMetaData(ld);
            ld.addChild(ld.myVar);
            return ld;
        }
    }
}
