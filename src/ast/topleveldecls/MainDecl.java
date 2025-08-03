package ast.topleveldecls;

import ast.AST;
import ast.misc.ParamDecl;
import ast.misc.ScopeDecl;
import ast.statements.BlockStmt;
import ast.types.Type;
import token.Token;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link TopLevelDecl} node that represents the main function of a C Minor program.
 * <p>
 *     Every C Minor program will have a single {@link MainDecl}, and it will represent where
 *     the program will begin its execution. If we are in interpretation mode, then the user
 *     will not be able to create a {@code Main} function at all.
 * </p>
 * @author Daniel Levy
 */
public class MainDecl extends TopLevelDecl implements ScopeDecl {

    /**
     * The scope of the main function.
     */
    private SymbolTable scope;

    /**
     * List of parameters that the main function accepts.
     */
    private Vector<ParamDecl> params;

    /**
     * The return {@link Type} (should be {@code Void} by default).
     */
    private Type returnType;

    /**
     * The body of the main function.
     */
    private BlockStmt body;

    /**
     * Default constructor for {@link MainDecl}.
     */
    public MainDecl() { this(new Token(),new Vector<>(),null,null); }

    /**
     * Main constructor for {@link MainDecl}.
     * @param metaData {@link Token} containing all the metadata stored with the {@link AST}.
     * @param params {@link Vector} of parameters to store into {@link #params}.
     * @param returnType {@link Type} to store into {@link #returnType}.
     * @param body {@link BlockStmt} to store into {@link #body}.
     */
    public MainDecl(Token metaData, Vector<ParamDecl> params, Type returnType, BlockStmt body) {
        super(metaData);

        this.params = params;
        this.returnType = returnType;
        this.body = body;

        addChildNode(this.params);
        addChildNode(this.body);
    }

    /**
     * Getter method for {@link #params}.
     * @return {@link Vector} of {@link ParamDecl}
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
    public boolean isMainDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public MainDecl asMainDecl() { return this; }

    /**
     * Returns the string "Main".
     * @return String
     */
    @Override
    public String toString() { return "Main"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        if(pos < params.size()) {
            params.remove(pos-1);
            params.add(pos-1, node.deepCopy().asSubNode().asParamDecl());
        }
        else
            body = node.deepCopy().asStatement().asBlockStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<ParamDecl> params = new Vector<>();

        for(ParamDecl pd : this.params)
            params.add(pd.deepCopy().asSubNode().asParamDecl());

        return new MainDeclBuilder()
                   .setMetaData(this)
                   .setParams(params)
                   .setReturnType(returnType.deepCopy().asType())
                   .setBlockStmt(body.deepCopy().asStatement().asBlockStmt())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitMainDecl(this); }

    /**
     * Internal class that builds a {@link MainDecl} object.
     */
    public static class MainDeclBuilder extends NodeBuilder {

        /**
         * {@link MainDecl} object we are building.
         */
        private final MainDecl md = new MainDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link MainDeclBuilder}.
         */
        public MainDeclBuilder setMetaData(AST node) {
            super.setMetaData(md, node);
            return this;
        }

        /**
         * Sets the main function's {@link #params}.
         * @param params {@link Vector} of parameters that the main function accepts.
         * @return Current instance of {@link MainDeclBuilder}.
         */
        public MainDeclBuilder setParams(Vector<ParamDecl> params) {
            md.params = params;
            return this;
        }

        /**
         * Sets the main function's {@link #returnType}.
         * @param returnType {@link Type} that the main function will return.
         * @return Current instance of {@link MainDeclBuilder}.
         */
        public MainDeclBuilder setReturnType(Type returnType) {
            md.returnType = returnType;
            return this;
        }

        /**
         * Sets the main function's {@link #body}.
         * @param body {@link BlockStmt} representing the main function's body.
         * @return Current instance of {@link MainDeclBuilder}.
         */
        public MainDeclBuilder setBlockStmt(BlockStmt body) {
            md.body = body;
            return this;
        }

        /**
         * Creates a {@link MainDecl} object.
         * @return {@link MainDecl}
         * */
        public MainDecl create() {
            md.addChildNode(md.params);
            md.addChildNode(md.body);
            return md;
        }
    }
}
