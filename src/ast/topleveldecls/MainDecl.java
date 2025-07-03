package ast.topleveldecls;

import ast.AST;
import ast.misc.ParamDecl;
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
    public AST deepCopy() {
        Vector<ParamDecl> args = new Vector<>();
        for(ParamDecl pd : this.args)
            args.add(pd.deepCopy().asParamDecl());

        return new MainDeclBuilder()
                   .setMetaData(this)
                   .setArgs(args)
                   .setReturnType(this.retType.deepCopy().asType())
                   .setBlockStmt(this.body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitMainDecl(this); }

    public static class MainDeclBuilder extends NodeBuilder {
        private final MainDecl md = new MainDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return MainDeclBuilder
         */
        public MainDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public MainDeclBuilder setArgs(Vector<ParamDecl> args) {
            md.args = args;
            return this;
        }

        public MainDeclBuilder setReturnType(Type type) {
            md.retType = type;
            return this;
        }

        public MainDeclBuilder setBlockStmt(BlockStmt body) {
            md.body = body;
            return this;
        }

        public MainDecl create() {
            super.saveMetaData(md);
            md.addChild(md.args);
            md.addChild(md.body);
            return md;
        }
    }
}
