package ast.class_body;

import ast.*;
import ast.operators.*;
import ast.statements.*;
import ast.types.*;
import token.*;
import utilities.Visitor;
import utilities.SymbolTable;

public class MethodDecl extends AST implements NameNode {

    public SymbolTable symbolTable;
    public Modifiers mods;

    private Name methodName;       // Set if we have a MethodDecl
    private Operator op;     // Set if we have a OperatorDecl
    private Vector<ParamDecl> params;
    private Type returnType;
    private BlockStmt block;

    private boolean isOverrode;

    public MethodDecl(Token t, Vector<Modifier> m, Name n, Operator o, Vector<ParamDecl> p, Type rt, BlockStmt b, boolean override) {
       super(t);
       this.mods = new Modifiers(m);
       this.methodName = n;
       this.op = o;
       this.params = p;
       this.returnType = rt;
       this.block = b;

       this.isOverrode = override;

       addChild(this.methodName);
       addChild(this.op);
       addChild(this.params);
       addChild(this.returnType);
       addChild(this.block);
       setParent();
    }

    public AST declName() { return this; }

    public Name name() { return methodName; }
    public Operator operator() { return op; }
    public Vector<ParamDecl> params() { return params; }
    public Type returnType() { return returnType; }
    public BlockStmt methodBlock() { return block; }

    public boolean isOverriden() { return isOverrode; }

    public boolean isMethodDecl() { return true; }
    public MethodDecl asMethodDecl() { return this; }

    public String paramSignature() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).getType().typeSignature());
        return sb.toString();
    }

    public String methodSignature() {
        return toString() + "(" + paramSignature() + ")" + returnType.typeSignature();
    }

    @Override
    public String toString() { return methodName.toString(); }

    @Override
    public void visit(Visitor v) { v.visitMethodDecl(this); }
}
