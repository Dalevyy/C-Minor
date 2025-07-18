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

    private Name name;
    private Vector<Typeifier> typeParams;
    private Vector<ParamDecl> params;
    private Type retType;
    private BlockStmt block;

    /**
     * Signature of the current function in the form {@code <funcName>(<paramTypeSignatures>)}
     */
    private String signature;

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

        if(this.name != null)
            createSignature();
    }

    public Name name() { return name; }
    public Vector<Typeifier> typeParams() { return typeParams; }
    public Vector<ParamDecl> params() { return params; }
    public Type returnType() { return retType; }
    public BlockStmt funcBlock() { return block; }

    public AST decl() { return this; }

    public boolean isFuncDecl() { return true; }
    public FuncDecl asFuncDecl() { return this; }

    public void setReturnType(Type returnType) { this.retType = returnType; }

    public boolean isTemplate() { return !this.typeParams.isEmpty(); }

    public void removeTypeParams(){
        for(Typeifier tp : this.typeParams)
            symbolTable.removeName(tp.toString());
        this.typeParams = new Vector<>();
    }

    public String getSignature() { return signature; }

    /**
     * Creates the function signature.
     * <p><br>
     *     This method will create the signature for the current function when it is initialized.
     *     We do this at initialization since the signature will not change, so we can improve the
     *     compiler's performance. If we have a template function, then all parameters that use a
     *     type parameter will be marked with a type of {@code <.>}. This is done in order to prevent
     *     the user from redeclaring the same template function, but with a different type parameter
     *     name alongside not causing an edge case error.
     * </p>
     */
    private void createSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(this);
        sb.append("(");

        if(isTemplate()) {
            for(ParamDecl param : params) {
                if(param.isParamTypeTemplated(typeParams))
                    sb.append(".");
                else
                    sb.append(param.getType().typeSignature());
            }
        }
        else
            for(ParamDecl param : params)
                sb.append(param.getType().typeSignature());

        sb.append(")");
        signature = sb.toString();
    }

    /**
     * Updates the {@link #signature} of the current function.
     * <p><br>
     *     This method will be used by {@link micropasses.TypeValidityPass} in order to
     *     change the function's signature when a template function is instantiated.
     * </p>
     */
    public void resetSignature() { createSignature(); }

    @Override
    public String header() {
        int endColumn = retType.location.end.column;
        return  startLine() + "| " + text.substring(0,endColumn) + "\n";
    }

    @Override
    public String toString() { return name.toString(); }

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
            fd.createSignature();
            return fd;
        }
    }
}
