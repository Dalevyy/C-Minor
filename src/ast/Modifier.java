package ast;

import token.*;
import utilities.Visitor;

// Leaf Node
public class Modifier extends AST {

    public static enum Mods { ABSTR, FINAL, PROPERTY, PROTECTED, PUBLIC, PURE,
                              RECURS, IN, OUT, INOUT, REF }
    public static String[] names = {"Abstract", "Final", "Property", "Protected", "Public", "Pure",
                                    "Recursive", "In", "Out", "Inout", "Ref" };
    private Mods mod;

    public Modifier(Token t, Mods m) {
        super(t);
        this.mod = m;
    }

    public Mods getModifier() { return mod; }

    public boolean isModifier() { return true; }
    public Modifier asModifier() { return this; }

    @Override
    public String toString() { return names[mod.ordinal()]; }

    @Override
    public void visit(Visitor v){ v.visitModifier(this); }
}
