package ast.types;

import token.*;
import utilities.Visitor;

public class ArrayType extends Type {

    private Type baseType;
    private int numOfDims;

    public ArrayType(Type bt, int num) { this(null,bt,num); }
    public ArrayType(Token t, Type bt, int num) {
        super(t);
        this.baseType = bt;
        this.numOfDims = num;

        addChild(this.baseType);
        setParent();
    }

    public Type getBaseType() { return baseType; }
    public int getArrayDims() { return numOfDims; }

    public String typeName() {
        return "ArrayType";
    }

    public boolean isArrayType() { return true; }
    public ArrayType asArrayType() { return this; }

    public static boolean arrayAssignmentCompatible(Type LHS, Type RHS) {
        return true;
    }

    @Override
    public void visit(Visitor v) { v.visitArrayType(this); }
}
