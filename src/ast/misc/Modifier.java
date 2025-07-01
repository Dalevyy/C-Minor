package ast.misc;

import ast.AST;
import token.*;
import utilities.Visitor;

/*
__________________________ Modifier __________________________
A Modifier node will contain a modifier that a user specified
for a given C Minor construct. Currently, modifiers can be
applied to ClassDecls, FieldDecls, MethodDecls, FuncDecls, and
ParamDecls.
______________________________________________________________
*/
public class Modifier extends AST {

    public enum Mods { ABSTR, FINAL, PROPERTY, PROTECTED, PUBLIC, PURE, RECURS, IN, OUT, INOUT, REF }
    public static String[] names = {"Abstract", "Final", "Property", "Protected", "Public", "Pure",
                                    "Recursive", "In", "Out", "Inout", "Ref" };
    private final Mods mod;

    public Modifier(Mods m) { this.mod = m; }

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
    public void update(int pos, AST n) { throw new RuntimeException("A modifier can not be updated."); }

    @Override
    public AST deepCopy() {
        Modifier m = new Modifier(this.mod);
        m.copyMetaData(this);
        return m;
    }

    @Override
    public void visit(Visitor v){ v.visitModifier(this); }
}
