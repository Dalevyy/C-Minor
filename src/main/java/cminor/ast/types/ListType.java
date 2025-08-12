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
public class ListType extends Type {

    // A list is homogeneous in C Minor which means a list
    // only stores a single data type
    private Type baseType;
    public int numOfDims;

    public ListType() { this(new Token(),null,0); }
    public ListType(Type bt, int num) { this(new Token(),bt,num); }
    public ListType(Token metaData, Type bt, int num) {
        super(metaData);
        this.baseType = bt;
        this.numOfDims = num;
    }

    public boolean isListType() { return true; }
    public ListType asListType() { return this; }

    public Type baseType() { return baseType; }
    public int getDims() { return numOfDims; }

    public void setBaseType(Type baseType) {
        this.baseType = baseType;
    }

    public void setNumOfDims(int numOfDims) {
        this.numOfDims = numOfDims;
    }

    public boolean baseTypeCompatible(Type t) { return assignmentCompatible(baseType,t); }

    /**
     * Checks if a passed type can represent a sublist type of the current {@link ListType}.
     * <p><br>
     *     This method is primary used by the {@link typechecker.TypeChecker#visitListStmt(ListStmt)} method
     *     to type check the value that is added or removed by the list.
     * </p>
     * @param currentType Generic {@link Type}
     * @return Boolean
     */
    public boolean isSubList(Type currentType) {
        if(currentType.isListType()) {
            // If the lists are assignment compatible, then the passed list type is the same type
            // as the current list type. Thus, two lists of the same type can be sublists of each other.
            if(assignmentCompatible(this,currentType))
                return true;

            // If the lists aren't assignment compatible, then we check if the passed list type has one
            // less dimension than the current list and if their base types are the same. This means the
            // passed list type is a proper sublist if both evaluate to be true.
            return currentType.asListType().numOfDims+1 == this.numOfDims
                        && this.baseTypeCompatible(currentType.asListType().baseType);
        }
        else
            // For a 1D list, a single value can act as a sublist.
            return this.baseTypeCompatible(currentType) && this.numOfDims == 1;
    }

    public String validSublist() {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= numOfDims; i++) {
            if(i == numOfDims)
                sb.append(baseType.typeName());
            else
                sb.append("List[");
        }

        sb.append("]".repeat(Math.max(0, numOfDims-1)));
        return sb.toString();
    }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= numOfDims; i++) {
            if(i == numOfDims)
                sb.append(baseType.typeName());
            else
                sb.append("List[");
        }

        sb.append("]".repeat(Math.max(0, numOfDims)));
        return sb.toString();
    }

    @Override
    public String toString()  { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ListType}
     */
    @Override
    public AST deepCopy() {
        return new ListTypeBuilder()
                .setMetaData(this)
                .setBaseType(this.baseType.deepCopy().asType())
                .setNumOfDims(this.numOfDims)
                .create();
    }

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
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ListTypeBuilder
         */
        public ListTypeBuilder setMetaData(AST node) {
            super.setMetaData(lt,node);
            return this;
        }

        /**
         * Sets the list type's {@link #baseType}.
         * @param baseType Type that represents the values stored by the list
         * @return ListTypeBuilder
         */
        public ListTypeBuilder setBaseType(Type baseType) {
            lt.setBaseType(baseType);
            return this;
        }

        /**
         * Sets the list type's {@link #numOfDims}.
         * @param numOfDims Int representing how many dimensions the list has
         * @return ListTypeBuilder
         */
        public ListTypeBuilder setNumOfDims(int numOfDims) {
            lt.setNumOfDims(numOfDims);
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
