package ast;

import token.*;
import utilities.*;

public class Typeifier extends AST {

    public static enum Tyfiers { DISCR, SCALAR, CLASS }
    public static String[] names = {"Discrete", "Scalar", "Class" };

    private Tyfiers typef;
    private Name name;

    public Typeifier(Token t, Tyfiers m, Name n) {
        super(t);
        this.typef = m;
        this.name = n;

        addChild(this.name);
        setParent();
    }

    public Tyfiers getTypeifier() { return typef; }
    public Name getName() { return name; }

    public boolean isTypeifier() { return true; }
    public Typeifier asTypeifier() { return this; }

    @Override
    public void visit(Visitor v) { v.visitTypeifier(this); }
}
