package ast.types;

import token.*;
import utilities.Visitor;

// Leaf Node
public class ListType extends Type {

    // A list is homogeneous in C Minor which means a list
    // only stores a single data type
    private Type memberType;

    public ListType(Token t, Type mt) {
        super(t);
        this.memberType = mt;
    }

    public boolean isListType() { return true; }
    public ListType asListType() { return this; }

    public Type getMemberType() { return memberType; }

    public String typeName() { return "List: " + memberType.typeName(); }

    @Override
    public String toString()  { return "List: " + memberType.typeName(); }

    @Override
    public void visit(Visitor v) { v.visitListType(this); }
}
