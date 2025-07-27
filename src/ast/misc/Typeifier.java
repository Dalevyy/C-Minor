package ast.misc;

import ast.AST;
import ast.types.Type;
import token.Token;
import utilities.Visitor;

/**
 * Type parameters for a template class or function.
 * <p><br>
 *     This {@link AST} subtype represents the type parameters that appear at the
 *     beginning of a template class or function (i.e. {@code class A<discr T>} and
 *     {@code def func<scalar S, discr T>}).
 * </p>
 * @author Daniel Levy
 */
public class Typeifier extends AST implements NameDecl {

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
     * <p><br>
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
     * Default constructor for {@code Typeifier}.
     */
    public Typeifier() { this(new Token(),null,null); }

    /**
     * Main constructor for {@code Typeifier}.
     * @param metaData Metadata token
     * @param typeAnnotate The potential type that the type parameter can accept
     * @param n The generic name for the type
     */
    public Typeifier(Token metaData, TypeAnnotation typeAnnotate, Name n) {
        super(metaData);
        this.typeAnnotate = typeAnnotate;
        this.name = n;

        addChild(this.name);
    }

    /**
     * Checks if a passed type can be used with the current type parameter.
     * <p><br>
     *     This method will check the value of {@link #typeAnnotate} in order to
     *     determine whether a passed type can be assigned to the type parameter.
     *     If the {@link #typeAnnotate} is not set, then by default, the type
     *     can be used with the current type parameter.
     * </p>
     * @param typeArg {@link Type} argument we want to assign to the type parameter
     * @return True if the type argument can be used with the current type parameter, False otherwise
     */
    public boolean isValidTypeArg(Type typeArg) {
        if(typeAnnotate == null)
            return true;

        return switch (typeAnnotate) {
            case DISCR -> typeArg.isDiscreteType();
            case SCALAR -> typeArg.isScalarType();
            case CLASS -> typeArg.isClassOrMultiType();
        };
    }

    /**
     * Returns the type annotation of the current type parameter.
     * @return The value of {@link #typeAnnotate} as a {@link TypeAnnotation}.
     */
    public TypeAnnotation getPossibleType() { return typeAnnotate; }

    /**
     * Returns the name of the type parameter.
     * @return The {@link Name} of the current type parameter.
     */
    public Name getName() { return name; }

    /**
     * Returns the {@link #typeAnnotate} field as a String.
     * @return The string representation of {@link #typeAnnotate}.
     */
    public String possibleTypeToString() { return names[typeAnnotate.ordinal()]; }

    /**
     * {@inheritDoc}
     */
    public boolean isTypeifier() { return true; }

    /**
     * {@inheritDoc}
     */
    public Typeifier asTypeifier() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST getDecl() { return this; }

    public String getDeclName() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST n) { name = n.asName(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String header() { return getParent().header(); }

    /**
     * {@inheritDoc}
     * @return String representation of the type parameter's {@link #name}.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * Checks if two type parameters have the same name.
     * @param otherName String representing a type that might contain a type parameter
     * @return True if the type parameters have the same name, False otherwise
     */
    public boolean equals(String otherName) {
        String curr = name.toString();
        if(otherName.contains("<")) {
            otherName = otherName.substring(otherName.indexOf("<")+1,otherName.indexOf(">"));
        }
        return curr.equals(otherName);
    }

    /**
     * {@inheritDoc}
     * @return A deep copy of the current {@link Typeifier}.
     */
    @Override
    public AST deepCopy() {
        return new TypeifierBuilder()
                   .setMetaData(this)
                   .setPossibleType(this.typeAnnotate)
                   .setName(this.name.deepCopy().asName())
                   .create();
    }

    /**
     * {@inheritDoc}
     * @param v Current {@code Visitor} we are visiting.
     */
    @Override
    public void visit(Visitor v) { v.visitTypeifier(this); }

    /**
     * Internal class that builds a {@link Typeifier} object.
     */
    public static class TypeifierBuilder extends NodeBuilder {

        /**
         * {@link Typeifier} object we are building.
         */
        private final Typeifier typeParam = new Typeifier();

        /**
         * {@inheritDoc}
         * @param node {@link AST} node we want to copy the metadata of.
         * @return Current instance of {@link TypeifierBuilder}.
         */
        public TypeifierBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the typeifier's {@link #typeAnnotate}.
         * @param typeAnnotate The generic type that the type parameter can be set to.
         * @return Current instance of {@link TypeifierBuilder}.
         */
        public TypeifierBuilder setPossibleType(TypeAnnotation typeAnnotate) {
            typeParam.typeAnnotate = typeAnnotate;
            return this;
        }

        /**
         * Sets the typeifier's {@link #name}.
         * @param name The name that will be given to use the type parameter.
         * @return Current instance of {@link TypeifierBuilder}.
         */
        public TypeifierBuilder setName(Name name) {
            typeParam.name = name;
            return this;
        }

        /**
         * Creates a {@link Typeifier} object.
         * @return {@link Typeifier} object.
         */
        public Typeifier create() {
            super.saveMetaData(typeParam);
            typeParam.addChild(typeParam.name);
            return typeParam;
        }
    }
}
