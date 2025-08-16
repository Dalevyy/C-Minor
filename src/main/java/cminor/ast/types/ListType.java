package cminor.ast.types;

import cminor.ast.AST;
import cminor.ast.statements.ListStmt;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * The third structured type is a ListType. In C Minor, lists are
 * also continuous blocks of memory like arrays, but lists may be
 * dynamically resized by the user during runtime.
 */
public class ListType extends ArrayType {

    /**
     * Default constructor for {@link ListType}.
     */
    public ListType() { this(new Token(),null,0); }

    /**
     * Main constructor for {@link ListType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param baseType {@link Type} to store into {@link #baseType}.
     * @param dims {@code int} to store into {@link #dims}.
     */
    public ListType(Token metaData, Type baseType, int dims) { super(metaData,baseType,dims); }

    /**
     * {@inheritDoc}
     * <p>
     *     Overriding the parent method since a list is different from an array in C Minor.
     * </p>
     */
    @Override
    public boolean isArray() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isList() { return true; }

    /**
     * {@inheritDoc}
     */
    public ListType asList() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= dims; i++) {
            if(i == dims)
                sb.append(baseType.getTypeName());
            else
                sb.append("List[");
        }

        sb.append("]".repeat(Math.max(0, dims)));
        return sb.toString();
    }

    public boolean baseTypeCompatible(Type t) { return assignmentCompatible(baseType,t); }

    /**
     * Checks if a passed type can represent a sublist type of the current {@link ListType}.
     * <p><br>
     *     This method is primarily used by the {@link cminor.typechecker.TypeChecker#visitListStmt(ListStmt)}
     *     method to type check the value that is added or removed by the list.
     * </p>
     * @param currentType Generic {@link Type}
     * @return Boolean
     */
    public boolean isSubList(Type currentType) {
        if(currentType.isList()) {
            // If the lists are assignment compatible, then the passed list type is the same type
            // as the current list type. Thus, two lists of the same type can be sublists of each other.
            if(assignmentCompatible(this,currentType))
                return true;

            // If the lists aren't assignment compatible, then we check if the passed list type has one
            // less dimension than the current list and if their base types are the same. This means the
            // passed list type is a proper sublist if both evaluate to be true.
            return currentType.asList().dims+1 == this.dims
                        && this.baseTypeCompatible(currentType.asList().baseType);
        }
        else
            // For a 1D list, a single value can act as a sublist.
            return this.baseTypeCompatible(currentType) && this.dims == 1;
    }

    public String validSublist() {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= dims; i++) {
            if(i == dims)
                sb.append(baseType.getTypeName());
            else
                sb.append("List[");
        }

        sb.append("]".repeat(Math.max(0, dims-1)));
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitListType(this); }

    /**
     * Internal class that builds a {@link ListType} object.
     */
    public static class ListTypeBuilder extends NodeBuilder {

        /**
         * {@link ListType} object we are building.
         */
        private final ListType lt = new ListType();

        /**
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ListTypeBuilder}.
         */
        public ListTypeBuilder setMetaData(AST node) {
            super.setMetaData(lt, node);
            return this;
        }

        /**
         * Sets the list type's {@link #baseType}.
         * @param baseType {@link Type} that represents the values stored by the list.
         * @return Current instance of {@link ListTypeBuilder}.
         */
        public ListTypeBuilder setBaseType(Type baseType) {
            lt.baseType = baseType;
            return this;
        }

        /**
         * Sets the list type's {@link #dims}.
         * @param dims {@code Int} representing how many dimensions the list type has.
         * @return Current instance of {@link ListTypeBuilder}.
         */
        public ListTypeBuilder setNumOfDims(int dims) {
            lt.dims = dims;
            return this;
        }

        /**
         * Creates a {@link ListType} object.
         * @return {@link ListType}
         */
        public ListType create() {
            return lt;
        }
    }
}
