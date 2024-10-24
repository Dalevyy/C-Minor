package AST.TopLevelDecls;

import AST.*;
import AST.Statements.*;
import AST.Types.*;
import Token.*;
import Utilities.PokeVisitor;

public class MainDecl extends TopLevelDecl {

    private Vector<ParamDecl> args;
    private Type retType;
    private BlockStmt body;

    public MainDecl(Token t, Vector<ParamDecl> a, Type rt, BlockStmt b) {
        super(t);
        this.args = a;
        this.retType = rt;
        this.body = b;

        addChild(this.args);
        addChild(this.retType);
        addChild(this.body);
        setParent();
        setDebugInfo();
    }

    public Vector<ParamDecl> getArgs() { return args; }
    public Type getReturnType() { return retType; }
    public BlockStmt getMainBody() { return body; }


    public void setDebugInfo() {
        this.location.end = this.body.location.end;
        //this.text +=
    }


    @Override
    public String toString() { return "Main"; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsMainDecl(this); }
}
