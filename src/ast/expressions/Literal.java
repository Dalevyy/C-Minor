package ast.expressions;

import token.*;
import utilities.*;

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

    public char asChar() {
      if(this.kind == ConstantKind.CHAR) {
          if(this.getText().charAt(1) == '\\') { return (char) ('\\' + this.getText().charAt(2));}
          return this.getText().charAt(1);
      }
      return '\0';
    }

    @Override
    public void visit(Visitor v) { v.visitLiteral(this); }
}
