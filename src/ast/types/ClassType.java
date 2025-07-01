package ast.types;

import ast.AST;
import ast.misc.Name;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/*
___________________________ ClassType ___________________________
The first structured type is a ClassType. These types represent
any custom types that a C Minor uses defines when they create a
class. Any name that is used as a type will be parsed as a
ClassType as well.
_________________________________________________________________
*/
public class ClassType extends Type {
    private Name className;                        // Only for single type
    private Vector<Name> inheritedTypes = new Vector<>();
    private Vector<Type> typeArgs;

    public ClassType() { this(new Token(),null,null,null); }
    public ClassType(String s) { this(new Token(),new Name(s),new Vector<>()); }
    public ClassType(Name n) { this(new Token(),n,new Vector<>()); }
    public ClassType(Token t, Name n) { this(t,n,new Vector<>(),new Vector<>()); }
    public ClassType(Name n, Vector<Type> ct) { this(new Token(),n,new Vector<>(),ct); }
    public ClassType(Token t, Name n, Vector<Type> tt) { this(t,n,new Vector<>(),tt); }
    public ClassType(Token t, Name n, Vector<Name> it, Vector<Type> tt) {
        super(t);
        this.className = n;
        this.typeArgs = tt;

        addChild(this.className);
    }

    public Name getClassName() { return className; }
    public Vector<Type> typeArgs() { return typeArgs; }

    public void setInheritedTypes(Vector<Name> it) { this.inheritedTypes = it; }
    public Vector<Name> getInheritedTypes() { return this.inheritedTypes; }

    public void setClassName(Name className) {
        this.className = className;
    }

    public void setTypeArgs(Vector<Type> typeArgs) {
        this.typeArgs = typeArgs;
    }

    public boolean isClassType() { return true; }
    public ClassType asClassType() { return this; }

    /**
     * Checks if the current {@code ClassType} represents a templated class.
     * @return Boolean
     */
    public boolean isTemplatedType() { return !typeArgs.isEmpty(); }

    public static boolean classAssignmentCompatibility(Type ct1, ClassType ct2) {
        if(ct1.isMultiType())
            return ClassType.isSuperClass(ct2,ct1.asMultiType().getInitialType());
        else if(ct1.asClassType().getInheritedTypes().size() > ct2.getInheritedTypes().size())
            return ClassType.isSuperClass(ct1.asClassType(),ct2);
        else
            return ClassType.isSuperClass(ct2,ct1.asClassType());
    }

    public static boolean isSuperClass(ClassType subClass, ClassType superClass) {
        if(subClass.toString().equals(superClass.toString()))
            return true;

        for(Name n : subClass.getInheritedTypes()) {
            if(n.toString().equals(superClass.toString()))
                return true;
        }
        return false;
    }

    @Override
    public String typeName() { return className.toString(); }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ClassType}
     */
    @Override
    public AST deepCopy() {
        Vector<Type> typeArgs = new Vector<>();
        for(Type t : this.typeArgs)
            typeArgs.add(t.deepCopy().asType());

        return new ClassTypeBuilder()
                .setMetaData(this)
                .setClassName(this.className.deepCopy().asName())
                .setTypeArgs(typeArgs)
                .create();
    }

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
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ClassTypeBuilder
         */
        public ClassTypeBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the class type's {@link #className}.
         * @param className Name representing the class the type refers to
         * @return ClassTypeBuilder
         */
        public ClassTypeBuilder setClassName(Name className) {
            ct.setClassName(className);
            return this;
        }

        /**
         * Sets the class type's {@link #typeArgs}.
         * @param typeArgs Vector of types containing any passed type arguments
         * @return ClassTypeBuilder
         */
        public ClassTypeBuilder setTypeArgs(Vector<Type> typeArgs) {
            ct.setTypeArgs(typeArgs);
            return this;
        }

        /**
         * Creates a {@link ClassType} object.
         * @return {@link ClassType}
         */
        public ClassType create() {
            super.saveMetaData(ct);
            ct.addChild(ct.className);
            return ct;
        }
    }
}
