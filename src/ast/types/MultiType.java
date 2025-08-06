package ast.types;

import ast.AST;
import ast.misc.Name;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An internal representation of all the possible types an
 * object variable could be. A {@code MultiType} is only
 * created when a user executes a retype statement, and the
 * type of the object will still not be known until runtime.
 */
public class MultiType extends Type {

    private ClassType initialType;
    private Vector<ClassType> allTypes;

    private ClassType runtimeType;      // Used by the interpreter

    public MultiType() { this(null,new Vector<>()); }
    public MultiType(ClassType it, Vector<ClassType> ct) {
        super(new Token());
        this.initialType = it;
        this.allTypes = ct;
    }

    public void addType(ClassType ct) {
        if(!allTypes.contains(ct))
            allTypes.add(ct);
    }

    public ClassType getInitialType() { return initialType; }
    public Vector<ClassType> getAllTypes() { return allTypes; }

    private void setInitialType(ClassType initialType) {
        this.initialType = initialType;
    }

    private void setAllTypes(Vector<ClassType> allTypes) {
        this.allTypes = allTypes;
    }

    public void setRuntimeType(ClassType ct) { this.runtimeType = ct; }
    public ClassType getRuntimeType() { return this.runtimeType; }

    public boolean isMultiType() { return true; }
    public MultiType asMultiType() { return this; }

    public static MultiType create(ClassType base, ClassType sub) {
        return new MultiType(base,new Vector<>(new ClassType[]{base, sub}));
    }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= allTypes.size(); i++) {
            ClassType ct = allTypes.get(i-1);
            if(i == allTypes.size())
                sb.append(ct);
            else
                sb.append(ct).append("/");
        }

        return sb.toString();
    }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link MultiType}
     */
    @Override
    public AST deepCopy() {
        Vector<ClassType> allTypes = new Vector<>();
        for(ClassType t : this.allTypes)
            allTypes.add(t.deepCopy().asType().asClassType());

        return new MultiTypeBuilder()
                   .setMetaData(this)
                   .setInitialType(this.initialType.deepCopy().asType().asClassType())
                   .setAllTypes(allTypes)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitMultiType(this); }

    /**
     * Internal class that builds a {@link MultiType} object.
     */
    public static class MultiTypeBuilder extends NodeBuilder {

        /**
         * {@link MultiType} object we are building.
         */
        private final MultiType mt = new MultiType();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return MultiTypeBuilder
         */
        public MultiTypeBuilder setMetaData(AST node) {
            super.setMetaData(mt,node);
            return this;
        }

        public MultiTypeBuilder setInitialType(ClassType initialType) {
            mt.setInitialType(initialType);
            return this;
        }

        public MultiTypeBuilder setAllTypes(Vector<ClassType> allTypes) {
            mt.setAllTypes(allTypes);
            return this;
        }

        public MultiType create() {
            return mt;
        }
    }

}
