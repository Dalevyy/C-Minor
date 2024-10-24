package AST.Types;

import AST.*;
import Token.*;

public abstract class Type extends AST {

    public Type(Token t) { super(t); }
    public Type(AST node) { super(node); }

    public abstract String typeName();

    public boolean isScalarType() { return false; }
    public boolean isDiscreteType() { return false; }
    public boolean isClassType() { return false; }
    public boolean isListType() { return false; }

    public Type asType() { return this; }

    ScalarType asScalarType(Type myType) {
        if(myType.isScalarType())
            return (ScalarType) myType;
        else throw new IllegalStateException("Error! Type can not be casted to ScalarType.");
    }

    DiscreteType asDiscreteType(Type myType) {
        if(myType.isDiscreteType())
            return (DiscreteType) myType;
        else throw new IllegalStateException("Error! Type can not be casted to DiscreteType.");
    }

    ClassType asClassType(Type myType) {
        if(myType.isClassType())
            return (ClassType) myType;
        else throw new IllegalStateException("Error! Type can not be casted to ClassType.");
    }

    ListType asListType(Type myType) {
        if(myType.isListType())
            return (ListType) myType;
        else throw new IllegalStateException("Error! Type can not be casted to ListType.");
    }
}
