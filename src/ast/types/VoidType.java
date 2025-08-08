package ast.types;

import ast.AST;
import token.*;
import utilities.*;

/*
___________________________ VoidType ___________________________
At the bottom of the structured type hierarchy, there is a
VoidType. ThisStmt type will represent any NULL values in C Minor.
________________________________________________________________
*/
public class VoidType extends Type {

    public VoidType() { this(new Token()); }
    public VoidType(Token t) { super(t); }

    public boolean isVoidType() { return true; }
    public VoidType asVoidType() { return this; }

    @Override
    public String typeName() { return "Void"; }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link VoidType}
     */
    @Override
    public AST deepCopy() {
        return new VoidTypeBuilder()
                   .setMetaData(this)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitVoidType(this); }

    /**
     * Internal class that builds a {@link VoidType} object.
     */
    public static class VoidTypeBuilder extends NodeBuilder {

        /**
         * {@link VoidType} object we are building.
         */
        private final VoidType vt = new VoidType();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return VoidTypeBuilder
         */
        public VoidTypeBuilder setMetaData(AST node) {
            super.setMetaData(vt,node);
            return this;
        }

        /**
         * Creates a {@link VoidType} object.
         * @return {@link VoidType}
         */
        public VoidType create() {
            return vt;
        }
    }
}
