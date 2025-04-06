package ast.types;

import ast.*;
import token.*;
import utilities.Visitor;

/*
___________________________ DiscreteType ___________________________
The second level of C Minor's primitive types will be discrete types
denoted by the DiscreteType node. These types will include Int, Char,
Bool, and also Enum.
____________________________________________________________________
*/
public class DiscreteType extends Type {

    public enum Discretes { INT, CHAR, BOOL, ENUM }
    public static String[] names = { "Int", "Char", "Bool", "Enum" };

    private final Discretes dType;

    public DiscreteType(Discretes d) {
        super((AST)null);
        this.dType = d;
    }


    public DiscreteType(Token t, Discretes d) {
        super(t);
        this.dType = d;
    }

    public boolean isDiscreteType() { return true; }
    public DiscreteType asDiscreteType() { return this; }
    public Discretes getDiscreteType() { return dType; }

    public String typeName() { return names[dType.ordinal()]; }

    @Override
    public String toString() { return names[dType.ordinal()]; }

    @Override
    public void visit(Visitor v) { v.visitDiscreteType(this); }
}
