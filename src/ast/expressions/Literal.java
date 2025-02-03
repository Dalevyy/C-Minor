package ast.expressions;

import token.*;
import utilities.*;

// Leaf Node
public class Literal extends Expression {

    public enum ConstantKind { BOOL, INT, CHAR, STR, TEXT, REAL, LIST, ARR }

    private ConstantKind kind;

    public Literal(Token t, ConstantKind ck) {
        super(t);
        this.kind = ck;
    }

    public ConstantKind getConstantKind() { return kind; }

    public int intValue() {
        if(kind != ConstantKind.INT) {
            System.out.println(PrettyPrint.RED + "Error! " + this.type.typeName() + " is not of type Int.");
            System.exit(1);
        }
        return Integer.parseInt(this.getText());
    }

    public boolean isLiteral() { return true; }
    public Literal asLiteral() { return this; }

    public void evaluate() { System.out.println(text); }
    @Override
    public void visit(Visitor v) { v.visitLiteral(this); }
}
