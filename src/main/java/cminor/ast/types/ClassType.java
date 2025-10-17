package cminor.ast.types;

import cminor.ast.AST;
import cminor.ast.misc.Name;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A structured {@link Type} representing a user-defined type.
 * <p>
 *     This type corresponds directly to any types the user defines using
 *     the {@code class} keyword. The parser will automatically create a
 *     {@link ClassType} any time a name is used as a type!
 * </p>
 * @author Daniel Levy
 */
public class ClassType extends Type {

    /**
     * The {@link Name} referencing the class this type represents.
     */
    private Name className;

    /**
     * A list of type arguments. Only set when working with template classes!
     */
    private Vector<Type> typeArgs;

    /**
     * Default constructor for {@link ClassType}.
     */
    public ClassType() { this(new Token(),null,new Vector<>()); }

    /**
     * Creates a {@link ClassType} based on a passed string name.
     * @param className String to store into {@link #className}.
     */
    public ClassType(String className) { this(new Token(),new Name(className),new Vector<>()); }

    /**
     * Creates a {@link ClassType} based on a passed {@link Name} and {@link Vector} of types.
     * @param className {@link Name} to store into {@link #className}.
     * @param typeArgs {@link Vector} of {@link Type} to store into {@link #typeArgs}.
     */
    public ClassType(Name className, Vector<Type> typeArgs) { this(null,className,typeArgs); }

    /**
     * Main constructor for {@link ClassType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param className {@link Name} to store into {@link #className}.
     * @param typeArgs {@link Vector} of {@link Type} to store into {@link #typeArgs}.
     */
    public ClassType(Token metaData, Name className, Vector<Type> typeArgs) {
        super(metaData);

        this.className = className;
        this.typeArgs = typeArgs;
    }

    public static boolean isSuperClass(SymbolTable globalScope, ClassType LHS, ClassType RHS) {
        SymbolTable classTable = globalScope.getGlobalScope();
        ClassDecl subClass = classTable.findName(RHS.getClassName()).asTopLevelDecl().asClassDecl();

        // We will look through the class hierarchy until the class matches the LHS class.
        while(subClass.getSuperClass() != null) {
            if(subClass.getName().equals(LHS.getClassName()))
                return true;
            subClass = classTable.findName(subClass.getSuperClass().getClassName()).asTopLevelDecl().asClassDecl();
        }
        return subClass.getName().equals(LHS.getClassName());
    }

    public static boolean temporaryName(SymbolTable globalScope, ClassType obj, ClassType className) {
        SymbolTable classTable = globalScope.getGlobalScope();
        ClassDecl subClass = classTable.findName(obj.getClassName()).asTopLevelDecl().asClassDecl();

        // We will look through the class hierarchy until the class matches the LHS class.
        while(subClass.getSuperClass() != null) {
            if(subClass.getName().equals(className.getClassName()))
                return true;
            subClass = classTable.findName(subClass.getSuperClass().getClassName()).asTopLevelDecl().asClassDecl();
        }
        return subClass.getName().equals(className.getClassName());
    }

    /**
     * Getter method for {@link #className}.
     * @return {@link Name}
     */
    public Name getClassName() { return className; }

    /**
     * Getter method for {@link #typeArgs}.
     * @return {@link Vector} of types
     */
    public Vector<Type> getTypeArgs() { return typeArgs; }

    /**
     * {@inheritDoc}
     */
    public boolean isClass() { return true; }

    /**
     * {@inheritDoc}
     */
    public ClassType asClass() { return this; }

    /**
     * Checks if the current {@code ClassType} represents a templated class.
     * @return Boolean
     */
    public boolean isTemplatedType() { return !typeArgs.isEmpty(); }

    /**
     * Checks if a {@link ClassType} is equal to another {@link ClassType}
     * @param ct The RHS {@link ClassType}
     * @return {@code True} if they are equal, {@code False} otherwise.
     */
    public boolean equals(ClassType ct) {
        if(this.isTemplatedType() && ct.isTemplatedType()) {
            if(!this.className.equals(ct.className) || this.typeArgs.size() != ct.typeArgs.size())
                return false;

            for(int i = 0; i < this.typeArgs.size(); i++) {
                Type LHS = this.typeArgs.get(i);
                Type RHS = ct.typeArgs.get(i);
                if(!LHS.equals(RHS))
                    return false;
            }
            return true;
        }
        else if(this.isTemplatedType() || ct.isTemplatedType())
            return false;
        else
            return this.className.equals(ct.className);
    }

    /**
     * Returns the class name.
     * @return String representation of the class name.
     */
    public String toString() { return className.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.className);

        if(!this.typeArgs.isEmpty()) {
            sb.append("<");
            for(int i = 0; i < this.typeArgs.size()-1; i++)
                sb.append(this.typeArgs.get(i)).append(", ");
            sb.append(this.typeArgs.top());
            sb.append(">");
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<Type> typeArgs = new Vector<>();
        for(Type t : this.typeArgs)
            typeArgs.add(t.deepCopy().asType());

        return new ClassTypeBuilder()
                .setMetaData(this)
                .setClassName(this.className.deepCopy().asSubNode().asName())
                .setTypeArgs(typeArgs)
                .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitClassType(this); }

    /**
     * Internal class that builds a {@link ClassType} object.
     */
    public static class ClassTypeBuilder extends NodeBuilder {

        /**
         * {@link ClassType} object we are building.
         */
        private final ClassType ct = new ClassType();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ClassTypeBuilder}.
         */
        public ClassTypeBuilder setMetaData(AST node) {
            super.setMetaData(ct, node);
            return this;
        }

        /**
         * Sets the class type's {@link #className}.
         * @param className {@link Name} representing the class the type refers to
         * @return Current instance of {@link ClassTypeBuilder}.
         */
        public ClassTypeBuilder setClassName(Name className) {
            ct.className = className;
            return this;
        }

        /**
         * Sets the class type's {@link #typeArgs}.
         * @param typeArgs Vector of types containing any passed type arguments
         * @return Current instance of {@link ClassTypeBuilder}.
         */
        public ClassTypeBuilder setTypeArgs(Vector<Type> typeArgs) {
            ct.typeArgs = typeArgs;
            return this;
        }

        /**
         * Creates a {@link ClassType} object.
         * @return {@link ClassType}
         */
        public ClassType create() { return ct; }
    }
}
