package ast.misc;

import ast.AST;
import ast.types.Type;
import token.*;
import utilities.*;

/*
___________________________ Typeifier ___________________________
A typeifier node is used to keep track of templated types when
working with templated classes/functions. (NOT YET IMPLEMENTED)
_________________________________________________________________
*/
public class Typeifier extends AST implements NameNode {

    /**
     * List of potential types the typefier could represent.
     */
    public enum TypeAnnotation { DISCR, SCALAR, CLASS }

    /**
     * String representation of {@code TypeAnnotation} enum.
     */
    public static String[] names = {"discrete", "scalar", "class" };

    /**
     * The high level type that the typefier could represent.
     * <p>
     *     In C Minor, only 3 classes of types are allowed to be used
     *     within templates: discrete, scalar, and class. The user can
     *     choose to specify if the typefier is any of these 3 types;
     *     however, it is not a requirement. As a result, this field
     *     will be empty, and it will be our job to make sure the user
     *     correctly uses the appropriate type.
     * </p>
     */
    private TypeAnnotation typeAnnotate;

    /**
     * The generic name for the type parameter.
     */
    private Name name;

    public Typeifier() { this(new Token(),null,null); }
    /**
     * Default constructor for {@code Typeifier}
     * @param metaData Metadata token
     * @param typeAnnotate The type the typefier represents
     * @param n The generic name for the type
     */
    public Typeifier(Token metaData, TypeAnnotation typeAnnotate, Name n) {
        super(metaData);
        this.typeAnnotate = typeAnnotate;
        this.name = n;

        addChild(this.name);
    }

    public TypeAnnotation getPossibleType() { return typeAnnotate; }
    public Name getName() { return name; }

    public boolean isTypeifier() { return true; }
    public Typeifier asTypeifier() { return this; }

    public boolean hasTypeAnnotation() { return typeAnnotate != null; }

    public boolean isValidTypeArg(Type t) {
        switch(possibleTypeToString()) {
            case "discrete":
                if(t.isDiscreteType())
                    return true;
                break;
            case "scalar":
                if(t.isScalarType())
                    return true;
                break;
            case "class":
                if(t.isClassOrMultiType())
                    return true;
        }
        return false;
    }

    public String possibleTypeToString() { return names[typeAnnotate.ordinal()]; }

    @Override
    public AST decl() { return this; }

    @Override
    public void update(int pos, AST n) { name = n.asName(); }

    @Override
    public String header() { return getParent().header(); }

    @Override
    public String toString() { return name.toString(); }

    public boolean equals(String otherName) {
        String curr = name.toString();
        if(otherName.contains("<")) {
            otherName = otherName.substring(otherName.indexOf("<")+1,otherName.indexOf(">"));
        }
        return curr.equals(otherName);
    }

    @Override
    public AST deepCopy() {
        return new TypeifierBuilder()
                   .setMetaData(this)
                   .setPossibleType(this.typeAnnotate)
                   .setName(this.name.deepCopy().asName())
                   .create();
    }

    /**
     * Visit method.
     * @param v Current visitor
     */
    @Override
    public void visit(Visitor v) { v.visitTypeifier(this); }

    public static class TypeifierBuilder extends NodeBuilder {
        private final Typeifier tp = new Typeifier();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return TypeifierBuilder
         */
        public TypeifierBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public TypeifierBuilder setPossibleType(TypeAnnotation pt) {
            tp.typeAnnotate = pt;
            return this;
        }

        public TypeifierBuilder setName(Name name) {
            tp.name = name;
            return this;
        }

        public Typeifier create() {
            super.saveMetaData(tp);
            tp.addChild(tp.name);
            return tp;
        }
    }
}
