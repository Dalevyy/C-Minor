package ast.misc;

import ast.AST;
import ast.expressions.*;
import ast.statements.ForStmt;
import ast.statements.WhileStmt;
import ast.types.*;
import token.*;
import utilities.Visitor;


/*
___________________________ Var ___________________________
A Var node is an internal representation of any variable in
C Minor. A Var is composed of 3 parts:
    1. A name representing the Var
    2. An optional expression that the Var is set to
    3. An optional type denoting what the type of the Var is
___________________________________________________________
*/
public class Var extends AST {

    private Name name;
    private Expression init;
    private Type type;

    private boolean uninit;

    public Var() { this(new Token(),null,null,null,false); }
    public Var(Token t, Name name) { this(t,name,null,null,false); }
    public Var(Token t, Name name, Expression init) { this(t,name,null,init,false); }
    public Var(Token t, Name name, Type type, boolean uninit) { this(t,name,type,null,uninit); }

    public Var(Token t, Name name, Type type, Expression init, boolean uninit) {
        super(t);
        this.name = name;
        this.type = type;
        this.init = init;
        this.uninit = uninit;

        addChild(this.name);
        addChild(this.init);
        setParent();
    }

    public Name name() { return name; }
    public Expression init() { return init;}
    public Type type() { return type; }

    public boolean isUninit() { return uninit; }

    public boolean isVar() { return true; }
    public Var asVar() { return this; }

    public void setType(Type t) { this.type = t; }
    public void setInit(Expression e) { this.init = e; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                name = n.asName();
                break;
            case 1:
                setInit(n.asExpression());
                break;
            case 2:
                setType(n.asType());
                break;
        }
    }

    @Override
    public String toString() { return this.name.toString(); }

    @Override
    public AST deepCopy() {
        VarBuilder vb = new VarBuilder();
        if(!this.uninit)
            vb.setInit(this.init.deepCopy().asExpression());

        return vb.setMetaData(this)
                 .setName(this.name.deepCopy().asName())
                 .setType(this.type.deepCopy().asType())
                 .create();
    }

    @Override
    public void visit(Visitor v) { v.visitVar(this); }

    public static class VarBuilder extends NodeBuilder {
        private final Var v = new Var();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return VarBuilder
         */
        public VarBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public VarBuilder setName(Name name) {
            v.name = name;
            return this;
        }

        public VarBuilder setInit(Expression init) {
            v.init = init;
            v.uninit = false;
            return this;
        }

        public VarBuilder setType(Type type) {
            v.type = type;
            return this;
        }

        public Var create() {
            super.saveMetaData(v);
            v.addChild(v.name);
            v.addChild(v.init);
            return v;
        }
    }
}
