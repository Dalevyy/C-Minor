package ast.types;

import ast.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/*
___________________________ ScalarType ___________________________
The first level of C Minor's primitive types will be scalar types
denoted by the ScalarType node. These types will include String,
Text, and Real.
__________________________________________________________________
*/
public class ScalarType extends Type {

    public enum Scalars { STR, TEXT, REAL }
    private static final Vector<String> names = new Vector<>(new String[]{"String", "Text", "Real" });

    private final Scalars sType;

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

    @Override
    public String typeName() { return names.get(sType.ordinal()); }

    @Override
    public String toString() { return names.get(sType.ordinal()); }

    @Override
    public void visit(Visitor v) { v.visitScalarType(this); }
}
