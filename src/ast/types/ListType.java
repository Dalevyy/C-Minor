package ast.types;

import token.*;
import utilities.Visitor;

/*
___________________________ ListType ___________________________
The third structured type is a ListType. In C Minor, lists are
also continuous blocks of memory like arrays, but lists may be
dynamically resized by the user during runtime.
________________________________________________________________
*/
public class ListType extends Type {

    // A list is homogeneous in C Minor which means a list
    // only stores a single data type
    private final Type lType;

    public ListType(Token t, Type mt) {
        super(t);
        this.lType = mt;
    }

    public boolean isListType() { return true; }
    public ListType asListType() { return this; }

    public Type listType() { return lType; }

    public String typeName() { return "List: " + lType.typeName(); }

    @Override
    public String toString()  { return "List: " + lType.typeName(); }

    @Override
    public void visit(Visitor v) { v.visitListType(this); }
}
