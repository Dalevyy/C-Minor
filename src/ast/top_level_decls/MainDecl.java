package ast.top_level_decls;

import ast.*;
import ast.statements.*;
import ast.types.*;
import token.*;
import utilities.*;

/*
____________________________ MainDecl ____________________________
__________________________________________________________________
*/
public class MainDecl extends TopLevelDecl {

    public SymbolTable symbolTable;

    private Vector<ParamDecl> args;
    private Type retType;
    private BlockStmt body;

    public MainDecl() {
        this.body = new BlockStmt();
    }

    public MainDecl(Token t, Vector<ParamDecl> a, Type rt, BlockStmt b) {
        super(t);
        this.args = a;
        this.retType = rt;
        this.body = b;

        addChild(this.args);
        addChild(this.retType);
        addChild(this.body);
        setParent();
    }

    public Vector<ParamDecl> args() { return args; }
    public Type returnType() { return retType; }
    public BlockStmt mainBody() { return body; }

    public boolean isMainDecl() { return true; }
    public MainDecl asMainDecl() { return this; }

    @Override
    public String toString() { return "Main"; }

    @Override
    public void visit(Visitor v) { v.visitMainDecl(this); }
}
