package ast.misc;

import ast.AST;
import ast.classbody.FieldDecl;
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

    private Literal lConstant;
    private Literal rConstant;

    public Label() { this(new Token(),null,null); }
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
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                lConstant = n.asExpression().asLiteral();
                break;
            case 1:
                rConstant = n.asExpression().asLiteral();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link Label}
     */
    @Override
    public AST deepCopy() {
        LabelBuilder lb = new LabelBuilder();
        if(this.rConstant != null)
            lb.setRightLabel(this.rConstant.deepCopy().asExpression().asLiteral());

        return lb.setMetaData(this)
                 .setLeftLabel(this.lConstant.deepCopy().asExpression().asLiteral())
                 .create();
    }

    @Override
    public void visit(Visitor v) { v.visitChoiceLabel(this); }

    public static class LabelBuilder extends NodeBuilder {
        private final Label l = new Label();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return LabelBuilder
         */
        public LabelBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public LabelBuilder setLeftLabel(Literal li) {
            l.lConstant = li;
            return this;
        }

        public LabelBuilder setRightLabel(Literal li) {
            l.rConstant = li;
            return this;
        }

        public Label create() {
            super.saveMetaData(l);
            l.addChild(l.lConstant);
            l.addChild(l.rConstant);
            return l;
        }
    }
}
