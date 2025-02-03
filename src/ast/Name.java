package ast;

import token.*;
import utilities.Visitor;

/*
 +-----------------------------------------------------------+
 +                           Name                            +
 +-----------------------------------------------------------+

A Name has 1 component:
    1. A String representation of the Name

Parent Node: Compilation
This is also a leaf node.
*/
public class Name extends AST {

    // A name only contains an identifier
    private String ID;

    public Name(Token t) {
        super(t);
        this.ID = t.getText();
    }

    public void setName(String newID) { ID = newID; }
    public String getName() { return ID; }

    public boolean isName() { return true; }
    public Name asName() { return this; }

    @Override
    public String toString() { return ID; }

    @Override
    public void visit(Visitor v) { v.visitName(this); }
}
