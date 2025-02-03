package ast.types;

import token.*;
import utilities.*;

public class VoidType extends Type {

    public VoidType(Token t) { super(t); }
    public String typeName() { return "Void"; }

    public boolean isVoidType() { return true; }
    public VoidType asVoidType() { return this; }

    @Override
    public void visit(Visitor v) { v.visitVoidType(this); }
}
