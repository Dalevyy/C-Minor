package AST;

import Token.*;
import Utilities.PokeVisitor;

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

    @Override
    public String toString() { return ID; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsName(this); }
}
