package cminor.ast.misc;

import cminor.ast.AST;
import cminor.ast.types.Type;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link SubNode} type representing type parameters for a template class or function.
 * <p>
 *     This {@link AST} subtype represents the type parameters that appear at the
 *     beginning of a template class or function (i.e. {@code class A<discr T>} and
 *     {@code def func<scalar S, discr T>}).
 * </p>
 * @author Daniel Levy
 */
public class TypeParam extends SubNode implements NameDecl {

    /**
     * List of potential types the type parameter could represent.
     */
    public enum TypeAnnotation { DISCR, SCALAR, CLASS }

    /**
     * String representation of {@code TypeAnnotation} enum.
     */
    public static String[] names = {"discrete", "scalar", "class" };

    /**
     * The high level type that the type parameter could represent.
     * <p><
     *     In C Minor, only 3 classes of types are allowed to be used within templates:
     *     discrete, scalar, and class. The user can choose to specify if the type parameter
     *     is any of these 3 types; however, it is not a requirement. As a result, this field
     *     is not guaranteed to be set which means any type can be used as long as no type
     *     errors occur when the {@link typechecker.TypeChecker} executes.
     * </p>
     */
    private TypeAnnotation typeAnnotate;

    /**
     * The generic name for the type parameter.
     */
    private Name name;

    /**
     * Default constructor for {@code TypeParam}.
     */
    public TypeParam() { this(new Token(),null,null); }

    /**
     * Main constructor for {@code TypeParam}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param typeAnnotate The potential type that the type parameter can accept.
     * @param name The generic name for the type.
     */
    public TypeParam(Token metaData, TypeAnnotation typeAnnotate, Name name) {
        super(metaData);

        this.typeAnnotate = typeAnnotate;
        this.name = name;
    }

    /**
     * Checks if a passed type can be used with the current type parameter.
     * <p>
     *     This method will check the value of {@link #typeAnnotate} in order to
     *     determine whether a passed type can be assigned to the type parameter.
     *     If the {@link #typeAnnotate} is not set, then by default, the type
     *     can be used with the current type parameter.
     * </p>
     * @param typeArg {@link Type} argument we want to assign to the type parameter.
     * @return {@code True} if the type argument can be used with the current type parameter, {@code False} otherwise.
     */
    public boolean isValidTypeArg(Type typeArg) {
        if(typeAnnotate == null)
            return true;

        return switch (typeAnnotate) {
            case DISCR -> typeArg.isDiscrete();
            case SCALAR -> typeArg.isScalar();
            case CLASS -> typeArg.isClassOrMulti();
        };
    }

    /**
     * Getter method for {@link #typeAnnotate}.
     * @return {@link TypeAnnotation}
     */
    public TypeAnnotation getPossibleType() { return typeAnnotate; }

    /**
     * Getter method for {@link #name}.
     * @return {@link Name}
     */
    public Name getName() { return name; }

    /**
     * Returns the {@link #typeAnnotate} field as a String.
     * @return The string representation of {@link #typeAnnotate}.
     */
    public String getPossibleTypeAsString() { return names[typeAnnotate.ordinal()]; }

    /**
     * {@inheritDoc}
     */
    public boolean isTypeParam() { return true; }

    /**
     * {@inheritDoc}
     */
    public TypeParam asTypeParam() { return this; }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return name.toString(); }

    /**
     * Returns the name of the type parameter.
     * @return String representation of the type parameter.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A type parameter can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String header() { return getFullLocation().header(); }

    /**
     * Checks if two type parameters have the same name.
     * @param otherName String representing a type that might contain a type parameter.
     * @return {@code True} if the type parameters have the same name, {@code False} otherwise.
     */
    public boolean equals(String otherName) {
        String curr = name.toString();

        if(otherName.contains("<"))
            otherName = otherName.substring(otherName.indexOf("<")+1,otherName.indexOf(">"));

        return curr.equals(otherName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new TypeParamBuilder()
                   .setMetaData(this)
                   .setPossibleType(typeAnnotate)
                   .setName(name.deepCopy().asSubNode().asName())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitTypeParam(this); }

    /**
     * Internal class that builds a {@link TypeParam} object.
     */
    public static class TypeParamBuilder extends NodeBuilder {

        /**
         * {@link TypeParam} object we are building.
         */
        private final TypeParam tp = new TypeParam();

        /**
         * @see NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link TypeParamBuilder}.
         */
        public TypeParamBuilder setMetaData(AST node) {
            super.setMetaData(tp, node);
            return this;
        }

        /**
         * Sets the type parameter's {@link #typeAnnotate}.
         * @param typeAnnotate The {@link TypeAnnotation} representing the generic type of the type parameter.
         * @return Current instance of {@link TypeParamBuilder}.
         */
        public TypeParamBuilder setPossibleType(TypeAnnotation typeAnnotate) {
            tp.typeAnnotate = typeAnnotate;
            return this;
        }

        /**
         * Sets the type parameter's {@link #name}.
         * @param name {@link Name} representing the type parameter's name when used.
         * @return Current instance of {@link TypeParamBuilder}.
         */
        public TypeParamBuilder setName(Name name) {
            tp.name = name;
            return this;
        }

        /**
         * Creates a {@link TypeParam} object.
         * @return {@link TypeParam}
         */
        public TypeParam create() { return tp; }
    }
}
