package ast.topleveldecls;

import ast.*;
import ast.classbody.InitDecl;
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

    private Name name;
    private Vector<Typeifier> typeParams;
    private Vector<ParamDecl> params;
    private Type retType;
    private BlockStmt block;

    public FuncDecl() { this(new Token(),null,null,new Vector<>(),new Vector<>(),null,null); }
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

    public boolean isTemplate() { return !this.typeParams.isEmpty(); }
    public void resetTypeParams() { this.typeParams = new Vector<>(); }

    public String funcSignature() { return this + "(" + paramSignature() + ")"; }

    public String paramSignature() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).type().typeSignature());
        return sb.toString();
    }

    @Override
    public String header() {
        int endColumn = retType.location.end.column;
        return  startLine() + "| " + text.substring(0,endColumn) + "\n";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name.toString());
//        sb.append("(");
//        if(!params.isEmpty()) {
//            for(int i = 0; i < params.size()-1; i++)
//                sb.append(params.get(i).getType()).append(", ");
//            sb.append(params.get(params.size()-1).getType());
//        }
//
//        sb.append(")");
        return sb.toString();
    }

    @Override
    public AST deepCopy() {
        Vector<Typeifier> typeParams = new Vector<>();
        Vector<ParamDecl> params = new Vector<>();

        for(Typeifier tp : this.typeParams)
            typeParams.add(tp.deepCopy().asTypeifier());
        for(ParamDecl pd : this.params)
            params.add(pd.deepCopy().asParamDecl());

        return new FuncDeclBuilder()
                   .setMetaData(this)
                   .setMods(this.mod)
                   .setFuncName(this.name.deepCopy().asName())
                   .setTypeArgs(typeParams)
                   .setParams(params)
                   .setReturnType(this.retType.deepCopy().asType())
                   .setBlockStmt(this.block.deepCopy().asStatement().asBlockStmt())
                   .setSymbolTable(this.symbolTable)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitFuncDecl(this); }

    /**
     * Internal class that builds a {@link FuncDecl} object.
     */
    public static class FuncDeclBuilder extends NodeBuilder {

        /**
         * {@link FuncDecl} object we are building.
         */
        private final FuncDecl fd = new FuncDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the function declaration's {@link #mod}.
         * @param mods List of modifiers that is applied to the current function
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setMods(Modifiers mods) {
            fd.mod = mods;
            return this;
        }

        /**
         * Sets the function declaration's {@link #name}.
         * @param name Name representing the name of the function
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setFuncName(Name name) {
            fd.name = name;
            return this;
        }

        /**
         * Sets the function declaration's {@link #typeParams}.
         * @param typeArgs Vector of typeifiers that the function has
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setTypeArgs(Vector<Typeifier> typeArgs) {
            fd.typeParams = typeArgs;
            return this;
        }

        /**
         * Sets the function declaration's {@link #params}.
         * @param params Vector of parameters that the function will accept
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setParams(Vector<ParamDecl> params) {
            fd.params = params;
            return this;
        }

        /**
         * Sets the function declaration's {@link #returnType}.
         * @param returnType Type representing the value that the function returns
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setReturnType(Type returnType) {
            fd.retType = returnType;
            return this;
        }

        /**
         * Sets the function declaration's {@link #block}.
         * @param funcBlock Block statement containing the code for the function to execute
         * @return FuncDeclBuilder
         */
        public FuncDeclBuilder setBlockStmt(BlockStmt funcBlock) {
            fd.block = funcBlock;
            return this;
        }

        public FuncDeclBuilder setSymbolTable(SymbolTable st) {
            fd.symbolTable = st;
            return this;
        }

        /**
         * Creates a {@link FuncDecl} object.
         * @return {@link FuncDecl}
         * */
        public FuncDecl create() {
            super.saveMetaData(fd);
            fd.addChild(fd.name);
            fd.addChild(fd.typeParams);
            fd.addChild(fd.params);
            fd.addChild(fd.block);
            return fd;
        }
    }
}
