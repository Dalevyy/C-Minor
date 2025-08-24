package cminor.ast.types;

import cminor.ast.AST;
import cminor.ast.misc.Name;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * An internal representation of all the possible types an
 * object variable could be. A {@code MultiType} is only
 * created when a user executes a retype statement, and the
 * type of the object will still not be known until runtime.
 */
public class MultiType extends Type {

    private ClassType initialType;
    private Vector<ClassType> types;

    private ClassType runtimeType;      // Used by the interpreter

    /**
     * Default constructor for {@link MultiType}.
     */
    public MultiType() { this(null,new Vector<>()); }

    public MultiType(ClassType it, Vector<ClassType> ct) {
        super(new Token());
        this.initialType = it;
        this.types = ct;
    }

    /**
     * Adds another {@link ClassType} to {@link #types}.
     * @param ct A {@link ClassType} that is now represented by the current {@link MultiType}.
     */
    public void addType(ClassType ct) {
        if(!types.contains(ct))
            types.add(ct);
    }

    public ClassType getInitialType() { return initialType; }
    public Vector<ClassType> getAllTypes() { return types; }

    public void setRuntimeType(ClassType ct) { this.runtimeType = ct; }
    public ClassType getRuntimeType() { return this.runtimeType; }

    /**
     * Creates a new {@link MultiType} based on two separate class types.
     * @param base The original {@link ClassType}.
     * @param sub The possible new {@link ClassType} that could be represented.
     * @return A new instance of {@link MultiType}.
     */
    public static MultiType create(ClassType base, ClassType sub) {
        return new MultiTypeBuilder()
                .setMetaData(base)
                .setInitialType(base)
                .setAllTypes(new Vector<>(new ClassType[]{base, sub}))
                .create();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMulti() { return true; }

    /**
     * {@inheritDoc}
     */
    public MultiType asMulti() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= types.size(); i++) {
            ClassType ct = types.get(i-1);
            if(i == types.size())
                sb.append(ct);
            else
                sb.append(ct).append("/");
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<ClassType> types = new Vector<>();
        for(ClassType t : this.types)
            types.add(t.deepCopy().asType().asClass());

        return new MultiTypeBuilder()
                   .setMetaData(this)
                   .setInitialType(initialType.deepCopy().asType().asClass())
                   .setAllTypes(types)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
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
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link MultiTypeBuilder}.
         */
        public MultiTypeBuilder setMetaData(AST node) {
            super.setMetaData(mt,node);
            return this;
        }

        /**
         * Sets the multitype's {@link #initialType}.
         * @param initialType {@link ClassType} representing the original type bounded to some variable.
         * @return Current instance of {@link MultiTypeBuilder}.
         */
        public MultiTypeBuilder setInitialType(ClassType initialType) {
            mt.initialType = initialType;
            return this;
        }

        /**
         * Sets the multitype's {@link #types}.
         * @param types {@link Vector} of class types that the current multitype could represent.
         * @return Current instance of {@link MultiTypeBuilder}.
         */
        public MultiTypeBuilder setAllTypes(Vector<ClassType> types) {
            mt.types = types;
            return this;
        }

        /**
         * Creates a {@link MultiType} object.
         * @return {@link MultiType}
         */
        public MultiType create() {
            return mt;
        }
    }

}
