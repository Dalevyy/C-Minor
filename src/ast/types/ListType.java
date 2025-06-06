package ast.types;

import token.Token;
import utilities.Visitor;

/**
 * The third structured type is a ListType. In C Minor, lists are
 * also continuous blocks of memory like arrays, but lists may be
 * dynamically resized by the user during runtime.
 */
public class ListType extends Type {

    // A list is homogeneous in C Minor which means a list
    // only stores a single data type
    private final Type baseType;
    public int numOfDims;

    public ListType(Type bt, int num) { this(new Token(),bt,num); }
    public ListType(Token t, Type bt, int num) {
        super(t);
        this.baseType = bt;
        this.numOfDims = num;

        addChild(this.baseType);
        setParent();
    }

    public boolean isListType() { return true; }
    public ListType asListType() { return this; }

    public Type baseType() { return baseType; }
    public int getDims() { return numOfDims; }

    public boolean baseTypeCompatible(Type t) { return Type.assignmentCompatible(baseType,t); }

    public boolean isSubList(Type ct) {
        if(!ct.isListType()) {
            if(this.numOfDims == 1)
                return this.baseTypeCompatible(ct);
            return false;
        }
        else if(this.numOfDims-ct.asListType().numOfDims < 0 || this.numOfDims-ct.asListType().numOfDims > 1)
            return false;
        else
            return this.baseTypeCompatible(ct.asListType().baseType);
    }

    public String typeName() { return "List: " + baseType.typeName(); }

    @Override
    public String toString()  { return "List: " + baseType.typeName(); }

    @Override
    public void visit(Visitor v) { v.visitListType(this); }
}
