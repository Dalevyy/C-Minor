package ast;

import ast.expressions.*;
import token.*;
import utilities.Visitor;

/*
__________________________ Label __________________________
A Label node represents a label found in each CaseStmt of
a ChoiceStmt node. Labels are comprised of either a single
constant (ex. 1) or two constants (ex. 1..3).
___________________________________________________________
*/
public class Label extends AST {

    private final Literal lConstant;
    private final Literal rConstant;

    public Label(Token t, Literal l) { this(t, l, null); }

    public Label(Token t, Literal l, Literal r) {
        super(t);
        this.lConstant = l;
        this.rConstant = r;

        addChild(this.lConstant);
        addChild(this.rConstant);
        setParent();
    }

    public Literal leftLabel() { return lConstant; }
    public Literal rightLabel() { return rConstant; }

    public boolean isLabel() { return true; }
    public Label asLabel() { return this; }

    @Override
    public void visit(Visitor v) { v.visitChoiceLabel(this); }
}
