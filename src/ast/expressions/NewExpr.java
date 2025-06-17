package ast.expressions;

import ast.AST;
import ast.misc.Var;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;

public class NewExpr extends Expression {

    private ClassType cType;
    private final Vector<Var> fields;

    public NewExpr(String ct) { this(new Token(),new ClassType(ct),new Vector<>());}
    public NewExpr(Token t, ClassType ct, Vector<Var> f) {
        super(t);
        this.cType = ct;
        this.fields = f;

        addChild(this.cType);
        addChild(this.fields);
        setParent();
    }

    public ClassType classType() { return cType; }
    public Vector<Var> args() { return fields; }

    public boolean isNewExpr() { return true; }
    public NewExpr asNewExpr() { return this; }

    public String getObjectName() { return this.getParent().toString(); }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                cType = n.asType().asClassType();
                break;
            default:
                fields.remove(pos-1);
                fields.add(pos-1,n.asVar());
        }
    }

    @Override
    public void visit(Visitor v) { v.visitNewExpr(this); }
}
