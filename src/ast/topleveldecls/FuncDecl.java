package ast.topleveldecls;

import ast.*;
import ast.misc.*;
import ast.statements.*;
import ast.types.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;
import utilities.SymbolTable;

public class FuncDecl extends TopLevelDecl implements NameNode {

    public SymbolTable symbolTable;
    public Modifiers mod;

    private final Name name;
    private final Vector<Typeifier> typeParams;
    private final Vector<ParamDecl> params;
    private final Type retType;
    private final BlockStmt block;

    public FuncDecl(Token t, Modifier m, Name n, Vector<Typeifier> tp, Vector<ParamDecl> p, Type rt, BlockStmt b) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.typeParams = tp;
        this.params = p;
        this.retType = rt;
        this.block = b;

        addChild(this.name);
        addChild(this.typeParams);
        addChild(this.params);
        addChild(this.retType);
        addChild(this.block);
        setParent();
    }

    public Name name() { return name; }
    public Vector<Typeifier> typeParams() { return typeParams; }
    public Vector<ParamDecl> params() { return params; }
    public Type returnType() { return retType; }
    public BlockStmt funcBlock() { return block; }

    public AST decl() { return this; }

    public boolean isFuncDecl() { return true; }
    public FuncDecl asFuncDecl() { return this; }

    public String funcSignature() { return this + "/" + this.paramSignature(); }

    public String paramSignature() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).type().typeSignature());
        return sb.toString();
    }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitFuncDecl(this); }
}
