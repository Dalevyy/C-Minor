package ast.expressions;

import token.*;
import utilities.*;

// Leaf Node
public class Literal extends Expression {

    public enum ConstantKind { BOOL, INT, CHAR, STR, TEXT, REAL, LIST, ARR }

    private ConstantKind kind;

    public Literal(ConstantKind ck, String val) { this(new Token(), ck); }
    public Literal(Token t, ConstantKind ck) {
        super(t);
        this.kind = ck;
    }

    public ConstantKind getConstantKind() { return kind; }

    public boolean isLiteral() { return true; }
    public Literal asLiteral() { return this; }

    @Override
    public String toString() {
        switch(kind) {
            case INT:
                if(this.getText().startsWith("~")) {return "-" + this.getText().substring(1); }
                else { return this.getText(); }
            default:
                return this.getText();
        }
    }

    @Override
    public void visit(Visitor v) { v.visitLiteral(this); }
}
