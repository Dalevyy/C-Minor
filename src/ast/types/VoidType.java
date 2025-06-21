package ast.types;

import ast.AST;
import token.*;
import utilities.*;

/*
___________________________ VoidType ___________________________
At the bottom of the structured type hierarchy, there is a
VoidType. This type will represent any NULL values in C Minor.
________________________________________________________________
*/
public class VoidType extends Type {

    public VoidType() { this(new Token()); }
    public VoidType(Token t) { super(t); }

    public boolean isVoidType() { return true; }
    public VoidType asVoidType() { return this; }

    @Override
    public String typeName() { return "Void"; }

    @Override
    public String toString() { return "Void"; }

    @Override
    public void visit(Visitor v) { v.visitVoidType(this); }
}
