package ast.types;

import ast.*;
import token.*;
import utilities.Visitor;

// Leaf Node
public class DiscreteType extends Type {

    public static enum Discretes { INT, CHAR, BOOL, ENUM };
    public static String[] names = { "Int", "Char", "Bool", "ENUM" };

    private Discretes dType;

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
    public String toString() { return names[dType.ordinal()]; };

    @Override
    public void visit(Visitor v) { v.visitDiscreteType(this); }
}
