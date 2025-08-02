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

public class MethodDecl extends AST implements NameDecl {

    public SymbolTable symbolTable;

    public Modifiers mods;

    private Name methodName;       // Set if we have a MethodDecl
    private Operator op;           // Set if we have a OperatorDecl
    private Vector<ParamDecl> params;
    private Type returnType;
    private BlockStmt methodBlock;

    private boolean isOverridden;
    public boolean isOperatorOverload;

    public MethodDecl() { this(new Token(),new Vector<>(),null,null,new Vector<>(),null,null,false); }
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
       this.methodBlock = b;

       this.isOverridden = override;

       if(this.methodName != null) {
           addChild(this.methodName);
           isOperatorOverload = false;
       }
       else {
           addChild(this.op);
            isOperatorOverload = true;
       }

       addChild(this.params);
       addChild(this.methodBlock);
    }

    public AST getDecl() { return this; }
    public String getDeclName() { return methodName.toString(); }

    public Name name() { return methodName; }
    public Operator operator() { return op; }
    public Vector<ParamDecl> params() { return params; }
    public Type returnType() { return returnType; }
    public BlockStmt methodBlock() { return methodBlock; }

    private void setMods(Modifiers mods) {
        this.mods = mods;
    }

    private void setMethodName(Name methodName) {
        this.methodName = methodName;
    }

    private void setOperator(Operator op) {
        this.op = op;
        this.isOperatorOverload = true;
    }

    private void setParams(Vector<ParamDecl> params) {
        this.params = params;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    private void setMethodBlock(BlockStmt methodBlock) {
        this.methodBlock = methodBlock;
    }

    public boolean isOverridden() { return isOverridden; }

    public String paramSignature() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++)
            sb.append(params.get(i).type().typeSignature());
        return sb.toString();
    }

    public String methodSignature() { return this + "(" + paramSignature() + ")"; }

    /**
     * Checks if the current AST node is a {@link MethodDecl}.
     * @return Boolean
     */
    public boolean isMethodDecl() { return true; }

    /**
     * Type cast method for {@link MethodDecl}.
     * @return MethodDecl
     */
    public MethodDecl asMethodDecl() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the name of the method
     */
    @Override
    public String toString() { return (op != null) ? "operator" + op : methodName.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                if(this.methodName != null)
                    this.methodName = node.asName();
                else
                    this.op = node.asOperator();
                break;
            default:
                if(pos <= this.params.size()) {
                    this.params.remove(pos-1);
                    this.params.add(pos-1,node.asParamDecl());
                }
                else if(pos-1 == this.params.size())
                    this.returnType = node.asType();
                else
                    this.methodBlock = node.asStatement().asBlockStmt();
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link MethodDecl}
     */
    @Override
    public AST deepCopy() {
        MethodDeclBuilder mdb = new MethodDeclBuilder();
        Vector<ParamDecl> params = new Vector<>();
        for(ParamDecl pd : this.params)
            params.add(pd.deepCopy().asParamDecl());

        if(this.methodName != null)
            mdb.setMethodName(this.methodName.deepCopy().asName());
        else
            mdb.setOperator(this.op.deepCopy().asOperator());

        if(this.isOverridden)
            mdb.setOverridden();

        return mdb.setMetaData(this)
                  .setMods(this.mods)
                  .setParams(params)
                  .setReturnType(this.returnType.deepCopy().asType())
                  .setBlockStmt(this.methodBlock.deepCopy().asStatement().asBlockStmt())
                  .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
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
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the method declaration's {@link #mods}.
         * @param mods List of modifiers that is applied to the current method
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setMods(Modifiers mods) {
            md.setMods(mods);
            return this;
        }

        /**
         * Sets the method declaration's {@link #methodName}.
         * @param name Name representing the name of the method
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setMethodName(Name name) {
            md.setMethodName(name);
            return this;
        }

        /**
         * Sets the method declaration's {@link #op}.
         * @param op Operator that the method overloads.
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setOperator(Operator op) {
            md.setOperator(op);
            return this;
        }

        /**
         * Sets the method declaration's {@link #params}.
         * @param params Vector of parameters that the method will accept
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setParams(Vector<ParamDecl> params) {
            md.setParams(params);
            return this;
        }

        /**
         * Sets the method declaration's {@link #returnType}.
         * @param returnType Type representing the value that the method returns
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setReturnType(Type returnType) {
            md.setReturnType(returnType);
            return this;
        }

        /**
         * Sets the method declaration's {@link #methodBlock}.
         * @param methodBlock Block statement containing the code for the method to execute
         * @return MethodDeclBuilder
         */
        public MethodDeclBuilder setBlockStmt(BlockStmt methodBlock) {
            md.setMethodBlock(methodBlock);
            return this;
        }

        /**
         * Sets the method declaration's {@link #isOverridden}.
         * @return MethodDeclBuilder
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
            super.saveMetaData(md);
            md.addChild(md.methodName);
            md.addChild(md.op);
            md.addChild(md.params);
            md.addChild(md.methodBlock);
            return md;
        }
    }
}
