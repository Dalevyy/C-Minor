package ast;

import token.*;
import utilities.Visitor;


/*
___________________________ Name ___________________________
A Name represents an identifier in C Minor, and any
construct that can be named will have a Name attached to it.
____________________________________________________________
*/
public class Name extends AST {

    // A name only contains an identifier
    private String ID;

    public Name(Token t) {
        super(t);
        this.ID = t.getText();
    }
    public Name(String s) { this.ID = s; }

    public void setName(String newID) { ID = newID; }
    public String getName() { return ID; }

    public boolean isName() { return true; }
    public Name asName() { return this; }

    @Override
    public String toString() { return ID; }

    @Override
    public void visit(Visitor v) { v.visitName(this); }
}
