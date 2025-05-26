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

    public ListType() { this(new Token(),null,0); }
    public ListType(Token t, Type mt, int num) {
        super(t);
        this.baseType = mt;
        this.numOfDims = num;

        addChild(this.baseType);
        setParent();
    }

    public boolean isListType() { return true; }
    public ListType asListType() { return this; }

    public Type baseType() { return baseType; }

    public boolean baseTypeCompatible(Type t) { return Type.assignmentCompatible(baseType,t); }

    public String typeName() { return "List: " + baseType.typeName(); }

    @Override
    public String toString()  { return "List: " + baseType.typeName(); }

    @Override
    public void visit(Visitor v) { v.visitListType(this); }
}
