package ast.types;

import token.*;
import utilities.Visitor;

/*
___________________________ ArrayType ___________________________
The second structured type is an ArrayType. In C Minor, arrays
will represent continuous blocks of memory and their size must be
known at compile-time.
_________________________________________________________________
*/
public class ArrayType extends Type {

    private final Type baseType;
    public int numOfDims;

    public ArrayType() { this(new Token(),null,0); }
    public ArrayType(Token t, Type bt, int num) {
        super(t);
        this.baseType = bt;
        this.numOfDims = num;

        addChild(this.baseType);
        setParent();
    }

    public Type baseType() { return baseType; }

    public boolean isArrayType() { return true; }
    public ArrayType asArrayType() { return this; }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= numOfDims; i++) {
            if(i == numOfDims)
                sb.append(baseType.typeName());
            else
                sb.append("Array[");
        }

        sb.append("]".repeat(Math.max(0, numOfDims)));
        return sb.toString();
    }

    @Override
    public String toString() { return typeName(); }

    @Override
    public void visit(Visitor v) { v.visitArrayType(this); }
}
