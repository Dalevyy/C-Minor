package ast.types;

import ast.*;
import token.*;
import utilities.Visitor;

// Leaf Node
public class ScalarType extends Type {

    public static enum Scalars { STR, TEXT, REAL };
    public static String[] names = { "String", "Text", "Real" };

    private Scalars sType;

    public ScalarType(Scalars s) {
        super((AST)null);
        this.sType = s;
    }

    public ScalarType(Token t, Scalars s) {
        super(t);
        this.sType = s;
    }

    public boolean isScalarType() { return true; }
    public ScalarType asScalarType() { return this; }
    public Scalars getScalarType() { return sType; }

    public String typeName() { return names[sType.ordinal()]; }

    @Override
    public String toString() { return names[sType.ordinal()]; }

    @Override
    public void visit(Visitor v) { v.visitScalarType(this); }
}
