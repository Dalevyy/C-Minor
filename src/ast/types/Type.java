package ast.types;

import ast.*;
import token.*;
import utilities.PrettyPrint;

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
        else if(this.isEnum()) return "E";
        else if(this.isString()) return "S";
        else if(this.isReal()) return "R";
        else if(this.isListType()) return "L";
        else if(this.isArrayType()) return "A";
        else if(this.isClassType()) return "O";
        else return "V";
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
            ClassType.isSuperClass(LHS.asClassType(),RHS.asClassType());
        else if(LHS.isArrayType() && RHS.isArrayType()) {
            return true;
        }
        return false;
    }


    /*
        Discrete Type Predicates
    */
    public boolean isBool() {
        return this.isDiscreteType() && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.BOOL);
    }
    public boolean isInt() {
        return this.isDiscreteType() && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.INT);
    }
    public boolean isChar() {
        return this.isDiscreteType() && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.CHAR);
    }

    public boolean isEnum() {
        return this.isDiscreteType() && (this.asDiscreteType().getDiscreteType() == DiscreteType.Discretes.ENUM);
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

    public boolean isArray() {
        return this.isArrayType();
    }

    public boolean isNumeric() { return this.isInt() || this.isChar() || this.isReal(); }

    public boolean isScalarType() { return false; }
    public boolean isDiscreteType() { return false; }
    public boolean isClassType() { return false; }
    public boolean isListType() { return false; }
    public boolean isArrayType() { return false; }
    public boolean isVoidType() { return false; }

    public ScalarType asScalarType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ScalarType.\n");
        System.exit(1);
        return null;
    }

    public DiscreteType asDiscreteType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a DiscreteType.\n");
        System.exit(1);
        return null;
    }

    public ClassType asClassType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ClassType.\n");
        System.exit(1);
        return null;
    }

    public ListType asListType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ListType.\n");
        System.exit(1);
        return null;
    }

    public ArrayType asArrayType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an ArrayType.\n");
        System.exit(1);
        return null;
    }

    public VoidType asVoidType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a VoidType.\n");
        System.exit(1);
        return null;
    }
}
