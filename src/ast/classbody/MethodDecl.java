package ast.classbody;

import ast.*;
import ast.misc.*;
import ast.operators.*;
import ast.statements.*;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;
import utilities.SymbolTable;

public class MethodDecl extends AST implements NameNode {

    public SymbolTable symbolTable;
    public Modifiers mods;

    private final Name methodName;       // Set if we have a MethodDecl
    private final Operator op;           // Set if we have a OperatorDecl
    private final Vector<ParamDecl> params;
    private final Type returnType;
    private final BlockStmt block;

    private final boolean isOverridden;

    public MethodDecl(Vector<Modifier> m, Name n, Vector<ParamDecl> p, Type rt, BlockStmt b) {
        this(new Token(),m,n,null,p,rt,b,false);
    }
    public MethodDecl(Token t, Vector<Modifier> m, Name n, Operator o, Vector<ParamDecl> p,
                      Type rt, BlockStmt b, boolean override) {
       super(t);
       this.mods = new Modifiers(m);
       this.methodName = n;
       this.op = o;
       this.params = p;
       this.returnType = rt;
       this.block = b;

       this.isOverridden = override;

       addChild(this.methodName);
       addChild(this.op);
       addChild(this.params);
       addChild(this.returnType);
       addChild(this.block);
       setParent();
    }

    public AST decl() { return this; }

    public Name name() { return methodName; }
    public Operator operator() { return op; }
    public Vector<ParamDecl> params() { return params; }
    public Type returnType() { return returnType; }
    public BlockStmt methodBlock() { return block; }

    public boolean isOverridden() { return isOverridden; }

    public boolean isMethodDecl() { return true; }
    public MethodDecl asMethodDecl() { return this; }

    public String paramSignature() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).type().typeSignature());
        return sb.toString();
    }

    public String methodSignature() {
        return this + "(" + paramSignature() + ")" + returnType.typeSignature();
    }

    @Override
    public String toString() { return methodName.toString(); }

    @Override
    public void visit(Visitor v) { v.visitMethodDecl(this); }

    public static class MethodDeclBuilder {
        public Vector<Modifier> mods;
        private Name methodName;
        private Vector<ParamDecl> params;
        private Type returnType;
        private BlockStmt block;

        public MethodDeclBuilder setMods(Vector<Modifier> mods) {
            this.mods = mods;
            return this;
        }

        public MethodDeclBuilder setName(String s) {
            this.methodName = new Name(s);
            return this;
        }

        public MethodDeclBuilder setParams(Vector<ParamDecl> pd) {
            this.params = pd;
            return this;
        }

        public MethodDeclBuilder setReturnType(Type rt) {
            this.returnType = rt;
            return this;
        }

        public MethodDeclBuilder setBlockStmt(BlockStmt bs) {
            this.block = bs;
            return this;
        }

        public MethodDecl createMethodDecl() { return new MethodDecl(mods,methodName,params,returnType,block); }
    }
}
