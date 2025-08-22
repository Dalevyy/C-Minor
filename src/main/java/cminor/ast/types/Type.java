package cminor.ast.types;

import cminor.ast.*;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.Name;
import cminor.token.*;
import cminor.utilities.Vector;

/*
__________________________ Type __________________________
Type is an abstract class that will be inherited by all C
Minor type nodes. ThisStmt class contains a bunch of helper
methods to aid in type checking/interpretation including
type equality and assignment compatibility.

C Minor will have two main forms of types: Structured and
Primitive types.
__________________________________________________________
*/
public abstract class Type extends AST {

    /**
     * Default constructor for {@link Type}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public Type(Token metaData) { super(metaData); }

    /**
     * Returns the String representation of the current {@link Type} node.
     * @return String representation of the {@link Type}.
     */
    public abstract String getTypeName();

    public String typeSignature() {
        if(this.isBool()) return "B";
        else if(this.isInt()) return "I";
        else if(this.isChar()) return "C";
        else if(this.isString()) return "S";
        else if(this.isReal()) return "R";
        else if(this.isEnum()) return this.toString();
        else if(this.isList()) return "L";
        else if(this.isArray()) return "A";
        else if(this.isClass()) return this.toString();
        else return "V";
    }

    public static String createTypeString(Vector<Expression> args) {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= args.size(); i++) {
            if(i == args.size())
                sb.append(args.get(i - 1).type.getTypeName());
            else
                sb.append(args.get(i-1).type.getTypeName()).append(", ");
        }

        return sb.toString();
    }


    public boolean equals(Type RHS) {
        return this.typeSignature().equals(RHS.typeSignature()) && this.getTypeName().equals(RHS.getTypeName());
    }

    public static boolean assignmentCompatible(Type LHS, Type RHS) {
        if(LHS.isStructured() && RHS.isStructured()) {
            if(LHS.isArray() && RHS.isArray())
                return ArrayType.isArrayAssignmentCompatible(LHS.asArray(), RHS.asArray());
            else if(LHS.isList() && RHS.isList())
                return ListType.isArrayAssignmentCompatible(LHS.asList(), RHS.asList());
            else if(LHS.isClass() && RHS.isClass())
                return LHS.asClass().equals(RHS.asClass());
        }
        return LHS.equals(RHS);
//        else if(LHS.isClassOrMultiType() && RHS.isClassOrMultiType()) {
//            Vector<ClassType> possibleTypes;
//            if(LHS.isMultiType()) {
//                possibleTypes = LHS.asMultiType().getAllTypes();
//                for(ClassType ct : possibleTypes) {
//                    if(ct.toString().equals(RHS.asClassType().toString()))
//                        return true;
//                }
//            }
//            else {
//                possibleTypes = RHS.asMultiType().getAllTypes();
//                for(ClassType ct : possibleTypes) {
//                    if(ct.toString().equals(LHS.asClassType().toString()))
//                        return true;
//                }
//            }
//            return false;
    }

    /**
     * Generates a new type based on a provided type parameter.
     * <p><br>
     *     When we are instantiating a template class or function, we need to replace every
     *     type parameter with the appropriate type argument the user specified. This method
     *     will be responsible for creating all the new types that will be saved into the
     *     instantiated class or function.
     * </p>
     * <p>
     *     In some cases, the user could specify generic types within generic types. For example, if
     *     a user declares a generic {@code Vector<T>} class, then the user will be allowed to use
     *     the type {@code Vector<T>} within the class itself. This is important to keep in mind as
     *     we need to make sure the type argument only replaces the {@code T} type parameter and not
     *     the entire {@code Vector<T>} type in order to preserve the user's semantics. This is why
     *     we are checking whether or not the given template type has a {@code '<'} to denote whether
     *     the template type is simply a type parameter or a class type that contains a type parameter.
     * </p>
     * <p>
     *     Note: Remember, a user can not instantiate a class with a List or an Array type.
     * </p>
     * @param templateType Type parameter we wish to replace
     * @param typeArg Type we want to now use in place of the type parameter
     * @return Newly created {@link Type} when we instantiate a template class or function
     */
//    public static Type instantiateType(ClassType templateType, Type typeArg) {
//        if(templateType.toString().contains("<"))
//            return new ClassType(new Name(templateType.getClassNameAsString()), new Vector<>(typeArg));
//        else
//            return typeArg;
//    }

    /**
     * Checks if the current AST node is an {@link ArrayType}.
     * @return {@code True} if the node is an {@link ArrayType}, {@code False} otherwise.
     */
    public boolean isArray() { return false; }

    /**
     * Checks if the current AST node is a {@code Bool} type.
     * @return {@code True} if the node is a {@code Bool} type, {@code False} otherwise.
     */
    public boolean isBool() { return isDiscrete() && asDiscrete().isBool(); }

    /**
     * Checks if the current AST node is a {@code Char} type.
     * @return {@code True} if the node is a {@code Char} type, {@code False} otherwise.
     */
    public boolean isChar() { return isDiscrete() && asDiscrete().isChar(); }

    /**
     * Checks if the current AST node is a {@link ClassType}.
     * @return {@code True} if the node is a {@link ClassType}, {@code False} otherwise.
     */
    public boolean isClass() { return false; }

    /**
     * Checks if the current AST node is either a {@link ClassType} or a {@link MultiType}.
     * @return {@code True} if the node is a {@link ClassType} or a {@link MultiType}, {@code False} otherwise.
     */
    public boolean isClassOrMulti() { return isClass() || isMulti(); }

    /**
     * Checks if the current AST node is a {@link DiscreteType}.
     * @return {@code True} if the node is a {@link DiscreteType}, {@code False} otherwise.
     */
    public boolean isDiscrete() { return false; }

    /**
     * Checks if the current AST node is an {@link EnumType}.
     * @return {@code True} if the node is an {@link EnumType}, {@code False} otherwise.
     */
    public boolean isEnum() { return false; }

    /**
     * Checks if the current AST node is an {@code Int} type.
     * @return {@code True} if the node is an {@code Int} type, {@code False} otherwise.
     */
    public boolean isInt() { return isDiscrete() && asDiscrete().isInt(); }

    /**
     * Checks if the current AST node is a {@link ListType}.
     * @return {@code True} if the node is a {@link ListType}, {@code False} otherwise.
     */
    public boolean isList() { return false; }

    /**
     * Checks if the current AST node is a {@link MultiType}.
     * @return {@code True} if the node is a {@link MultiType}, {@code False} otherwise.
     */
    public boolean isMulti() { return false; }

    /**
     * Checks if the current AST node is a numeric type.
     * @return {@code True} if the node is a numeric type, {@code False} otherwise.
     */
    public boolean isNumeric() { return isInt() || isReal() || isChar(); }

    /**
     * Checks if the current AST node is a {@code Real} type.
     * @return {@code True} if the node is a {@code Real} type, {@code False} otherwise.
     */
    public boolean isReal() { return isScalar() && asScalar().isReal(); }

    /**
     * Checks if the current AST node is a {@link ScalarType}.
     * @return {@code True} if the node is a {@link ScalarType}, {@code False} otherwise.
     */
    public boolean isScalar() { return false; }

    /**
     * Checks if the current AST node is a {@code String} type.
     * @return {@code True} if the node is a {@code String} type, {@code False} otherwise.
     */
    public boolean isString() { return isScalar() && asScalar().isString(); }

    /**
     * Checks if the current AST node is a structured type.
     * @return {@code True} if the node is a structured type, {@code False} otherwise.
     */
    public boolean isStructured() { return isArray() || isClassOrMulti() || isList(); }

    /**
     * Checks if the current AST node is a {@code Text} type.
     * @return {@code True} if the node is a {@code Text} type, {@code False} otherwise.
     */
    public boolean isText() { return isScalar() && asScalar().isText(); }

    /**
     * {@inheritDoc}
     */
    public boolean isType() { return true; }

    /**
     * Checks if the current AST node is a {@link VoidType}.
     * @return {@code True} if the node is a {@link VoidType}, {@code False} otherwise.
     */
    public boolean isVoid() { return false; }

    /**
     * Explicitly casts the current node into an {@link ArrayType}.
     * @return The current node as an {@link ArrayType}.
     */
    public ArrayType asArray() {
        throw new RuntimeException("The current node does not represent an array type.\n");
    }

    /**
     * Explicitly casts the current node into a {@link ClassType}.
     * @return The current node as a {@link ClassType}.
     */
    public ClassType asClass() {
        throw new RuntimeException("The current node does not represent a class type.\n");
    }

    /**
     * Explicitly casts the current node into a {@link DiscreteType}.
     * @return The current node as a {@link DiscreteType}.
     */
    public DiscreteType asDiscrete() {
        throw new RuntimeException("The current node does not represent a discrete type.\n");
    }

    /**
     * Explicitly casts the current node into an {@link EnumType}.
     * @return The current node as an {@link EnumType}.
     */
    public EnumType asEnum() {
        throw new RuntimeException("The current node does not represent an Enum type.\n");
    }

    /**
     * Explicitly casts the current node into a {@link ListType}.
     * @return The current node as a {@link ListType}.
     */
    public ListType asList() {
        throw new RuntimeException("The current node does not represent a list type.\n");
    }

    /**
     * Explicitly casts the current node into a {@link MultiType}.
     * @return The current node as a {@link MultiType}.
     */
    public MultiType asMulti() {
        throw new RuntimeException("Expression can not be casted into a multi-type.\n");
    }

    /**
     * Explicitly casts the current node into a {@link ScalarType}.
     * @return The current node as a {@link ScalarType}.
     */
    public ScalarType asScalar() {
        throw new RuntimeException("The current node does not represent a scalar type.\n");
    }

    /**
     * {@inheritDoc}
     */
    public Type asType() { return this; }

    /**
     * Explicitly casts the current node into a {@link VoidType}.
     * @return The current node as a {@link VoidType}.
     */
    public VoidType asVoidType() {
        throw new RuntimeException("The current node does not represent a Void type.\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A type can not be updated."); }

    /**
     * Returns the type name of the current {@link Type}.
     * @return String representing the name of the {@link Type}.
     */
    @Override
    public String toString() { return getTypeName(); }
}
