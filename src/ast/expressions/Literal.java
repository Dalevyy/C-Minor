package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;

// Leaf Node
public class Literal extends Expression {

    public enum ConstantKind { BOOL, INT, CHAR, STR, TEXT, REAL, LIST, ARR, ENUM }

    private final ConstantKind kind;

    public Literal(ConstantKind ck, String val) {
        this(new Token(), ck);
        this.text = val;
    }

    public Literal(Token t, ConstantKind ck) {
        super(t);
        this.kind = ck;
    }

    public ConstantKind getConstantKind() { return kind; }

    public boolean isLiteral() { return true; }
    public Literal asLiteral() { return this; }

    public int asInt() {
        if(this.kind == ConstantKind.INT)
            return Integer.parseInt(this.toString());
        else
            return '\0';
    }

    public char asChar() {
        if(this.kind == ConstantKind.CHAR) {
            if(this.getText().charAt(1) == '\\') { return (char) ('\\' + this.getText().charAt(2));}
            return this.getText().charAt(1);
        }
        return '\0';
    }

    @Override
    public String toString() { return this.getText(); }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A literal can not be updated."); }

    @Override
    public void visit(Visitor v) { v.visitLiteral(this); }

    public static class LiteralBuilder {
        private ConstantKind kind;
        private String value;

        public LiteralBuilder setConstantKind(ConstantKind ck) {
            this.kind = ck;
            return this;
        }

        public LiteralBuilder setValue(String s) {
            this.value = s;
            return this;
        }

        public Literal createLiteral() { return new Literal(kind,value); }
    }
}
