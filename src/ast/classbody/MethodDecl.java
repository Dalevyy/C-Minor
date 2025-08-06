package ast.classbody;

import ast.AST;
import ast.misc.Modifier;
import ast.misc.Name;
import ast.misc.NameDecl;
import ast.misc.ParamDecl;
import ast.misc.ScopeDecl;
import ast.operators.Operator;
import ast.statements.BlockStmt;
import ast.topleveldecls.ClassDecl;
import ast.types.Type;
import token.Token;
import utilities.Vector;
import utilities.Visitor;
import utilities.SymbolTable;

/**
 * A {@link ClassNode} that represents a method declared in a {@link ClassDecl}.
 * @author Daniel Levy
 */
public class MethodDecl extends ClassNode implements NameDecl, ScopeDecl {

    /**
     * The scope that the method opens.
     */
    private SymbolTable scope;

    /**
     * The name of the method. This is not set if we are overloading an operator.
     */
    private Name name;

    /**
     * The {@link Operator} the method overloads. This is set only if we overload an operator.
     */
    private Operator op;

    /**
     * {@link Vector} of parameters that the method accepts.
     */
    private Vector<ParamDecl> params;

    /**
     * The return {@link Type} of the method.
     */
    private Type returnType;

    /**
     * {@link BlockStmt} representing the body of the method.
     */
    private BlockStmt body;

    /**
     * {@link Modifier} containing the access privilege of the method.
     */
    public Modifier mod;

    /**
     * Stores the signature of the method's parameters after {@link #getParamSignature()} is called.
     */
    private String paramSignature;

    /**
     * Flag that tracks if the current method was marked with the {@code override} keyword.
     */
    private boolean isOverridden;

    /**
     * Default constructor for {@link MethodDecl}.
     */
    public MethodDecl() { this(new Token(),null,null,null,new Vector<>(),null,null,false); }

    /**
     * Main constructor for {@link MethodDecl}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param mod {@link Modifier} that will be stored into {@link #mod}.
     * @param name {@link Name} that will be stored into {@link #name}.
     * @param op {@link Operator} that will be stored into {@link #op}.
     * @param params {@link Vector} of {@link ParamDecl} that will be stored into {@link #params}.
     * @param returnType {@link Type} that will be stored into {@link #returnType}.
     * @param body {@link BlockStmt} that will be stored into {@link #body}.
     * @param isOverridden Flag that will be stored into {@link #isOverridden}.
     */
    public MethodDecl(Token metaData, Modifier mod, Name name, Operator op, 
                      Vector<ParamDecl> params, Type returnType, BlockStmt body, boolean isOverridden) {
       super(metaData);
       
       this.mod = mod;
       this.name = name;
       this.op = op;
       this.params = params;
       this.returnType = returnType;
       this.body = body;
       this.isOverridden = isOverridden;
       
       if(this.name != null) 
           addChildNode(this.name);
       else 
           addChildNode(this.op);
       addChildNode(this.params);
       addChildNode(this.body);
    }

    /**
     * Generates the method's signature.
     * @return String representing the signature of the method.
     */
    public String getMethodSignature() { return this + "(" + getParamSignature() + ")"; }

    /**
     * Generates the parameter signature for the current method.
     * @return {@link #paramSignature}
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
     * Getter method for {@link #name}
     * @return {@link #name}
     */
    public Name getMethodName() { return name; }

    /**
     * Getter method for {@link #op}.
     * @return {@link #op}
     */
    public Operator getOperatorOverload() { return op; }

    /**
     * Getter method for {@link #params}.
     * @return {@link #params}
     */
    public Vector<ParamDecl> getParams() { return params; }

    /**
     * Getter method for {@link #returnType}
     * @return {@link #returnType}
     */
    public Type getReturnType() { return returnType; }

    /**
     * Getter method for {@link #body}.
     * @return {@link #body}
     */
    public BlockStmt getBody() { return body; }

    /**
     * Getter method for {@link #isOverridden}
     * @return {@link #isOverridden}
     */
    public boolean isOverridden() { return isOverridden; }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     * @return
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
    public ClassDecl getClassDecl() { return parent.asClassNode().getClassDecl(); }

    /**
     * {@inheritDoc}
     */
    public boolean isMethodDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public MethodDecl asMethodDecl() { return this; }

    /**
     * Checks if two {@link MethodDecl}'s are equal to each other.
     * @param md The {@link MethodDecl} we want to compare with the current method.
     * @return {@code True} if the methods share the same parameter signature, {@code False} otherwise.
     */
    public boolean equals(MethodDecl md) { return paramSignature.equals(md.paramSignature); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return (op != null) ? "operator" + op : name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                if(name != null)
                    name = node.asSubNode().asName();
                else
                    op = node.asOperator();
                break;
            default: 
                if(pos < params.size()) { 
                    params.remove(pos-1);
                    params.add(pos-1, node.asSubNode().asParamDecl());
                }
                else if(pos-1 == params.size())
                    returnType = node.asType();
                else
                    body = node.asStatement().asBlockStmt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        MethodDeclBuilder mb = new MethodDeclBuilder();
        Vector<ParamDecl> params = new Vector<>();
        
        for(ParamDecl pd : this.params)
            params.add(pd.deepCopy().asSubNode().asParamDecl());

        if(name != null)
            mb.setMethodName(name.deepCopy().asSubNode().asName());
        else
            mb.setOperator(op.deepCopy().asOperator());

        if(isOverridden)
            mb.setOverridden();

        return mb.setMetaData(this)
                 .setModifier(mod)
                 .setParams(params)
                 .setReturnType(returnType.deepCopy().asType())
                 .setBlockStmt(body.deepCopy().asStatement().asBlockStmt())
                 .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitMethodDecl(this); }

    /**
     * Internal class that builds a {@link MethodDecl} object.
     */
    public static class MethodDeclBuilder extends NodeBuilder {

        /**
         * {@link InitDecl} object we are building.
         */
        private final MethodDecl md = new MethodDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setMetaData(AST node) {
            super.setMetaData(md, node);
            return this;
        }

        /**
         * Sets the method declaration's {@link #mod}.
         * @param mod {@link Modifier} storing the access privilege of the method.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setModifier(Modifier mod) {
            md.mod = mod;
            return this;
        }

        /**
         * Sets the method declaration's {@link #name}.
         * @param name {@link Name} representing the name of the method
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setMethodName(Name name) {
            if(md.op != null)
                md.name = name;
            return this;
        }

        /**
         * Sets the method declaration's {@link #op}.
         * @param op {@link Operator} that is overloaded by the method.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setOperator(Operator op) {
            if(md.name != null)
                md.op = op;
            return this;
        }

        /**
         * Sets the method declaration's {@link #params}.
         * @param params {@link Vector} of {@link ParamDecl} that the method will accept.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setParams(Vector<ParamDecl> params) {
            md.params = params;
            return this;
        }

        /**
         * Sets the method declaration's {@link #returnType}.
         * @param returnType {@link Type} representing the value that the method returns.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setReturnType(Type returnType) {
            md.returnType = returnType;
            return this;
        }

        /**
         * Sets the method declaration's {@link #body}.
         * @param body {@link BlockStmt} containing the code for the method to execute.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setBlockStmt(BlockStmt body) {
            md.body = body;
            return this;
        }

        /**
         * Sets the method declaration's {@link #isOverridden}.
         * @return Current instance of {@link MethodDeclBuilder}.
         */
        public MethodDeclBuilder setOverridden() {
            md.isOverridden = true;
            return this;
        }

        /**
         * Creates a {@link MethodDecl} object.
         * @return {@link MethodDecl}
         * */
        public MethodDecl create() {
            if(md.name != null)
                md.addChildNode(md.name);
            else
                md.addChildNode(md.op);
            md.addChildNode(md.params);
            md.addChildNode(md.body);
            return md;
        }
    }
}
