package ast.misc;

import ast.AST;
import token.*;
import utilities.*;

/*
___________________________ Typeifier ___________________________
A typeifier node is used to keep track of templated types when
working with templated classes/functions. (NOT YET IMPLEMENTED)
_________________________________________________________________
*/
public class Typeifier extends AST {

    public enum Typfiers { DISCR, SCALAR, CLASS }
    public static String[] names = {"Discrete", "Scalar", "Class" };

    private final Typfiers typef;
    private Name name;

    public Typeifier(Token t, Typfiers m, Name n) {
        super(t);
        this.typef = m;
        this.name = n;

        addChild(this.name);
        setParent();
    }

    public Typfiers getTypeifier() { return typef; }
    public Name getName() { return name; }

    public boolean isTypeifier() { return true; }
    public Typeifier asTypeifier() { return this; }

    @Override
    public void update(int pos, AST n) { name = n.asName(); }

    @Override
    public void visit(Visitor v) { v.visitTypeifier(this); }
}
