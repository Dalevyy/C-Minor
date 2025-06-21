package ast.types;

import ast.*;
import ast.expressions.Expression;
import token.*;
import utilities.Vector;

/*
__________________________ Type __________________________
Type is an abstract class that will be inherited by all C
Minor type nodes. This class contains a bunch of helper
methods to aid in type checking/interpretation including
type equality and assignment compatibility.

C Minor will have two main forms of types: Structured and
Primitive types.
__________________________________________________________
*/
public abstract class Type extends AST {
    public Type(Token t) { super(t); }
    public Type(AST node) { super(node); }

    public abstract String typeName();

    public boolean isType() { return true; }
    public Type asType() { return this; }

    public String typeSignature() {
        if(this.isBool()) return "B";
        else if(this.isInt()) return "I";
        else if(this.isChar()) return "C";
        else if(this.isString()) return "S";
        else if(this.isReal()) return "R";
        else if(this.isEnumType()) return this.toString();
        else if(this.isListType()) return "L";
        else if(this.isArrayType()) return "A";
        else if(this.isClassType()) return this.toString();
        else return "V";
    }

    public static String createTypeString(Vector<Expression> args) {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= args.size(); i++) {
            if(i == args.size())
                sb.append(args.get(i - 1).type.typeName());
            else
                sb.append(args.get(i-1).type.typeName()).append(", ");
        }

        return sb.toString();
    }

    public static boolean sameTypeName(Type LHS, Type RHS) {
        return LHS.typeName().equals(RHS.typeName());
    }

    public static boolean typeEqual(Type LHS, Type RHS) {
        return LHS.typeSignature().equals(RHS.typeSignature()) && Type.sameTypeName(LHS,RHS);
    }

    public static boolean assignmentCompatible(Type LHS, Type RHS) {
        if(Type.typeEqual(LHS,RHS))
            return true;
        else if(LHS.isClassType() && RHS.isClassType())
            return LHS.asClassType().toString().equals(RHS.asClassType().toString());
        else if(LHS.isEnumType() && RHS.isEnumType())
            return LHS.asEnumType().toString().equals(RHS.asEnumType().toString());
        else if(LHS.isListType() && RHS.isListType()) {
            ListType lType = LHS.asListType();
            ListType rType = RHS.asListType();
            return lType.numOfDims == rType.numOfDims && lType.baseType().equals(rType.baseType());
        }
        else if(LHS.isArrayType() && RHS.isArrayType()) {
            ArrayType lType = LHS.asArrayType();
            ArrayType rType = RHS.asArrayType();
            return lType.numOfDims == rType.numOfDims && lType.baseType().equals(rType.baseType());
        }
        else if((LHS.isMultiType() || RHS.isMultiType()) && (LHS.isClassType() || RHS.isClassType())) {
            Vector<ClassType> possibleTypes;
            if(LHS.isMultiType()) {
                possibleTypes = LHS.asMultiType().getAllTypes();
                for(ClassType ct : possibleTypes) {
                    if(ct.toString().equals(RHS.asClassType().toString()))
                        return true;
                }
            }
            else {
                possibleTypes = RHS.asMultiType().getAllTypes();
                for(ClassType ct : possibleTypes) {
                    if(ct.toString().equals(LHS.asClassType().toString()))
                        return true;
                }
            }
            return false;
        }
        else
            return false;
    }


    /*
        Discrete Type Predicates
    */
    public boolean isBool() {
        return this.isDiscreteType()
                && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.BOOL)
                && !this.isEnumType();
    }
    public boolean isInt() {
        return this.isDiscreteType()
                && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.INT)
                && !this.isEnumType();
    }
    public boolean isChar() {
        return this.isDiscreteType()
                && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.CHAR)
                && !this.isEnumType();
    }

    /*
        Scalar Type Predicates
    */
    public boolean isString() {
        return this.isScalarType() && (this.asScalarType().getScalarType() == ScalarType.Scalars.STR);
    }
    public boolean isReal() {
        return this.isScalarType() && (this.asScalarType().getScalarType() == ScalarType.Scalars.REAL);
    }

    public boolean isList() { return this.isListType(); }
    public boolean isArray() { return this.isArrayType(); }
    public boolean isNumeric() { return this.isInt() || this.isChar() || this.isReal(); }
    public boolean isEnumType() { return false; }
    public boolean isScalarType() { return false; }
    public boolean isDiscreteType() { return false; }
    public boolean isClassType() { return false; }
    public boolean isMultiType() { return false; }
    public boolean isClassOrMultiType() { return this.isClassType() || this.isMultiType(); }
    public boolean isListType() { return false; }
    public boolean isArrayType() { return false; }
    public boolean isVoidType() { return false; }

    public ScalarType asScalarType() { throw new RuntimeException("Expression can not be casted into a ScalarType.\n"); }
    public DiscreteType asDiscreteType() { throw new RuntimeException("Expression can not be casted into a DiscreteType.\n"); }
    public ClassType asClassType() { throw new RuntimeException("Expression can not be casted into a ClassType.\n"); }
    public MultiType asMultiType() { throw new RuntimeException("Expression can not be casted into a MultiType.\n"); }
    public ListType asListType() { throw new RuntimeException("Expression can not be casted into a ListType.\n"); }
    public ArrayType asArrayType() { throw new RuntimeException("Expression can not be casted into an ArrayType.\n"); }
    public VoidType asVoidType() { throw new RuntimeException("Expression can not be casted into a VoidType.\n"); }
    public EnumType asEnumType() { throw new RuntimeException("Expression can not be casted into an EnumType.\n"); }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A type can not be updated."); }
}
