package cminor.ast.topleveldecls;

import cminor.ast.AST;
import cminor.ast.misc.*;
import cminor.ast.statements.BlockStmt;
import cminor.ast.types.Type;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link TopLevelDecl} node that represents a function.
 * @author Daniel Levy
 */
public class FuncDecl extends TopLevelDecl implements NameDecl, ScopeDecl, ReturnDecl {

    /**
     * The scope of the function.
     */
    private SymbolTable scope;

    /**
     * The name of the function.
     */
    private Name name;

    /**
     * List of type parameters the function has. This is only set if the function is a template.
     */
    private Vector<TypeParam> typeParams;

    /**
     * List of parameters the function will accept.
     */
    private Vector<ParamDecl> params;

    /**
     * The return type of the function.
     */
    private Type returnType;

    /**
     * The body of the function.
     */
    private BlockStmt body;

    /**
     * {@link Modifier} that tells us if the function is {@code pure} or {@code recursive}.
     */
    public Modifier mod;

    /**
     * Signature of the current function in the form {@code <funcName>(<paramTypeSignatures>)}
     */
    private String signature;

    /**
     * The signature of the function's parameters.
     */
    private String paramSignature;

    /**
     * Flag used by {@link cminor.typechecker.TypeChecker} to determine if the function is guaranteed to return a value.
     */
    private boolean containsReturnStmt;

    /**
     * Default constructor for {@link FuncDecl}.
     */
    public FuncDecl() { this(new Token(),null,null,new Vector<>(),new Vector<>(),null,null); }

    /**
     * Main constructor for {@link FuncDecl}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param mod {@link Modifier} that will be stored into {@link #mod}.
     * @param name {@link Name} that will be stored into {@link #name}.
     * @param typeParams {@link Vector} of type parameters that will be stored into {@link #typeParams}.
     * @param params {@link Vector} of {@link ParamDecl} that will be stored into {@link #params}.
     * @param returnType {@link Type} that will be stored into {@link #returnType}.
     * @param body {@link BlockStmt} that will be stored into {@link #body}.
     */
    public FuncDecl(Token metaData, Modifier mod, Name name, Vector<TypeParam> typeParams,
                    Vector<ParamDecl> params, Type returnType, BlockStmt body) {
        super(metaData);

        this.mod = mod;
        this.name = name;
        this.typeParams = typeParams;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
        this.containsReturnStmt = false;

        addChildNode(this.typeParams);
        addChildNode(this.params);
        addChildNode(this.body);

        if(this.name != null)
            createSignature();
    }

    /**
     * Checks if the function is a template or not.
     * @return {@code True} if the function is a template, {@code False} otherwise.
     */
    public boolean isTemplate() { return !typeParams.isEmpty(); }

    /**
     * Getter method for {@link #name}.
     * @return {@link Name}
     */
    public Name getName() { return name; }

    /**
     * Getter method for {@link #typeParams}.
     * @return {@link Vector} of type parameters
     */
    public Vector<TypeParam> getTypeParams() { return typeParams; }

    /**
     * Getter method for {@link #params}.
     * @return {@link Vector} of parameters
     */
    public Vector<ParamDecl> getParams() { return params; }

    /**
     * Getter method for {@link #returnType}.
     * @return {@link Type}
     */
    public Type getReturnType() { return returnType; }

    /**
     * Getter method for {@link #body}.
     * @return {@link BlockStmt}
     */
    public BlockStmt getBody() { return body; }

    /**
     * Getter method for {@link #signature}
     * @return String representation of the function's signature.
     */
    public String getSignature() { return signature; }

    /**
     * Getter method for {@link #paramSignature}.
     * <p>
     *     This method also generates the parameter signature and prevents it from being generated again.
     * </p>
     * @return String representation of the function's parameter signature.
     */
    public String getParamSignature() {
        if(paramSignature != null)
            return paramSignature;

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).getType().typeSignature());

        paramSignature = sb.toString();
        return paramSignature;
    }

    /**
     * Setter for {@link #returnType}. This should only be called by {@link cminor.micropasses.TypeValidator}.
     * @param returnType {@link Type} to save into {@link #returnType}.
     */
    public void setReturnType(Type returnType) { this.returnType = returnType; }

    // Come back later to make sure this is good
    public void removeTypeParams(){
        for(TypeParam tp : this.typeParams)
            //scope.removeName(tp.toString());
        this.typeParams = new Vector<>();
    }

    /**
     * Updates the {@link #signature} of the current function.
     * <p>
     *     This method will be used by {@link cminor.micropasses.TypeValidator} in order to
     *     change the function's signature when a template function is instantiated.
     * </p>
     */
    public void resetSignature() { createSignature(); }

    /**
     * Creates the function signature.
     * <p>
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
                if(param.isParameterTemplated(typeParams))
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
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return toString(); }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() { return (scope != null) ? scope : null; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable st) { scope = (scope == null) ? st : scope; }

    /**
     * {@inheritDoc}
     */
    public void setIfReturnStmtFound() { containsReturnStmt = true; }

    /**
     * {@inheritDoc}
     */
    public boolean containsReturnStmt() { return containsReturnStmt; }

    /**
     * {@inheritDoc}
     */
    public boolean isFuncDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public FuncDecl asFuncDecl() { return this; }

    /**
     * Returns the name of the function.
     * @return String representation of the function name.
     */
    @Override
    public String toString() {
        if(typeParams.isEmpty())
            return name.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(name).append("<");
        for(int i = 0; i < typeParams.size()-1; i++)
            sb.append(typeParams.get(i)).append(", ");

        sb.append(typeParams.getLast()).append(">");
        return sb.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String header() { return getLocation().start.line + "| " + text.substring(0, text.indexOf("{")); }

    @Override
    protected void update(int pos, AST newNode) { throw new RuntimeException("A function can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<TypeParam> typeParams = new Vector<>();
        Vector<ParamDecl> params = new Vector<>();

        for(TypeParam tp : this.typeParams)
            typeParams.add(tp.deepCopy().asSubNode().asTypeParam());

        for(ParamDecl pd : this.params)
            params.add(pd.deepCopy().asSubNode().asParamDecl());

        return new FuncDeclBuilder()
                   .setMetaData(this)
                   .setMods(mod)
                   .setFuncName(name.deepCopy().asSubNode().asName())
                   .setTypeArgs(typeParams)
                   .setParams(params)
                   .setReturnType(returnType.deepCopy().asType())
                   .setBlockStmt(body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
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
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setMetaData(AST node) {
            super.setMetaData(fd, node);
            return this;
        }

        /**
         * Sets the function declaration's {@link #mod}.
         * @param mod {@link Modifier} that is applied to the current function.
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setMods(Modifier mod) {
            fd.mod = mod;
            return this;
        }

        /**
         * Sets the function declaration's {@link #name}.
         * @param name {@link Name} representing the name of the function
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setFuncName(Name name) {
            fd.name = name;
            return this;
        }

        /**
         * Sets the function declaration's {@link #typeParams}.
         * @param typeParams {@link Vector} of type parameters the function might have.
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setTypeArgs(Vector<TypeParam> typeParams) {
            fd.typeParams = typeParams;
            return this;
        }

        /**
         * Sets the function declaration's {@link #params}.
         * @param params {@link Vector} of parameters that the function will accept
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setParams(Vector<ParamDecl> params) {
            fd.params = params;
            return this;
        }

        /**
         * Sets the function declaration's {@link #returnType}.
         * @param returnType {@link Type} representing the value that the function returns.
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setReturnType(Type returnType) {
            fd.returnType = returnType;
            return this;
        }

        /**
         * Sets the function declaration's {@link #body}.
         * @param body {@link BlockStmt} containing the code for the function to execute.
         * @return Current instance of {@link FuncDeclBuilder}.
         */
        public FuncDeclBuilder setBlockStmt(BlockStmt body) {
            fd.body = body;
            return this;
        }

        /**
         * Creates a {@link FuncDecl} object.
         * @return {@link FuncDecl}
         * */
        public FuncDecl create() {
            fd.addChildNode(fd.typeParams);
            fd.addChildNode(fd.params);
            fd.addChildNode(fd.body);
            fd.createSignature();
            return fd;
        }
    }
}
