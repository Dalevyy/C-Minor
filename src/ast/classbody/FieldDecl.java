package ast.classbody;

import ast.AST;
import ast.misc.Modifier;
import ast.misc.Modifiers;
import ast.misc.NameNode;
import ast.misc.Var;
import ast.types.*;
import token.*;
import utilities.Visitor;

public class FieldDecl extends AST implements NameNode {

    private void setMod(Modifiers mod) {
        this.mod = mod;
    }

    public Modifiers mod;

    private void setVar(Var var) {
        this.var = var;
    }

    private Var var;
    private Type type;

    public FieldDecl() { this(new Token(),null,null,null); }
    public FieldDecl(Token t, Modifier m, Var v, Type type) {
        super(t);
        this.mod = new Modifiers(m);
        this.var = v;
        this.type = type;

        addChild(this.var);
        setParent();
    }

    public AST decl() { return this; }

    public Var var() { return var; }
    public void setType(Type t) { this.type = t; }
    public Type type() { return type; }

    /**
     * Checks if the current AST node is a {@link FieldDecl}.
     * @return Boolean
     */
    public boolean isFieldDecl() { return true; }

    /**
     * Type cast method for {@link FieldDecl}.
     * @return FieldDecl
     */
    public FieldDecl asFieldDecl() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the name of the field
     */
    @Override
    public String toString() { return var().toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { this.var = node.asVar(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link FieldDecl}
     */
    @Override
    public AST deepCopy() {
        return new FieldDeclBuilder()
                .setMetaData(this)
                .setMod(mod)
                .setVar(this.var.deepCopy().asVar())
                .setType(this.type.deepCopy().asType())
                .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitFieldDecl(this); }

    /**
     * Internal class that builds a {@link FieldDecl} object.
     */
    public static class FieldDeclBuilder extends NodeBuilder {

        /**
         * {@link FieldDecl} object we are building.
         */
        private final FieldDecl fd = new FieldDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the field declaration's {@link #mod}.
         * @param mod Modifier representing the access ability of the modifier.
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setMod(Modifiers mod) {
            fd.setMod(mod);
            return this;
        }

        /**
         * Sets the field declaration's {@link #var}.
         * @param var Variable that represents the field
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setVar(Var var) {
            fd.setVar(var);
            return this;
        }

        /**
         * Sets the field declaration's {@link #type}.
         * @param type Type representing the data type the field represents
         * @return FieldDeclBuilder
         */
        public FieldDeclBuilder setType(Type type) {
            fd.setType(type);
            return this;
        }

        /**
         * Creates a {@link FieldDecl} object.
         * @return {@link FieldDecl}
         */
        public FieldDecl create() {
            super.saveMetaData(fd);
            fd.addChild(fd.var);
            return fd;
        }
    }
}
