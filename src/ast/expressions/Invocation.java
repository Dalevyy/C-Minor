package ast.expressions;

import ast.AST;
import ast.misc.Name;
import token.Token;
import ast.topleveldecls.FuncDecl;
import ast.types.Type;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing an invocation.
 * <p><br>
 *     ThisStmt class will represent either a function or method invocation.
 *     Method invocations will always be contained inside of a {@link FieldExpr},
 *     so we'll have to keep track of which class the method needs to be called
 *     from. Function invocations are simpler; however, functions can be templated
 *     so we have to keep track of all type parameters if a user tries to instantiates
 *     a templated function.
 *     <br><br>
 *     Additionally, C Minor currently supports a built-in {@code length} method that
 *     will return the length of an array or a list.
 * </p>
 */
public class Invocation extends Expression {

    /**
     * Name of the function or method that is called.
     */
    private Name name;

    /**
     * Vector containing any type parameters passed to the callee (only set for functions).
     */
    private Vector<Type> typeArgs;

    /**
     * Vector containing any arguments passed to the callee
     */
    private Vector<Expression> args;

    /**
     * Class in which invocation will be invoked for (only set for methods).
     */
    public Type targetType;

    /**
     * Internal string representing the invocation's signature to help with overloading.
     */
    private String signature;

    public FuncDecl templatedFunction;

    /**
     * Flag denoting if the current invocation is a call to the built-in {@code length} method.
     */
    private boolean isLengthInvocation;

    /**
     * Default constructor for {@link Invocation}.
     */
    public Invocation() { this(new Token(),null,new Vector<>(),new Vector<>()); }

    /**
     * Main constructor for {@link Invocation}.
     * @param metaData Token containing metadata we want to save
     * @param name Name to save into {@link #name}
     * @param typeParams Vector of types to save into {@link #typeArgs}
     * @param args Vector of expressions to save into {@link #args}
     */
    public Invocation(Token metaData, Name name, Vector<Type> typeParams, Vector<Expression> args) {
        super(metaData);
        this.name = name;
        this.typeArgs = typeParams;
        this.args = args;
        if(name != null)
            this.isLengthInvocation = toString().equals("length");

        addChildNode(this.name);
        addChildNode(this.args);
    }

    /**
     * Checks if the current {@link Invocation} invokes the built-in {@code length} method.
     * @return Boolean
     */
    public boolean isLengthInvocation() { return this.isLengthInvocation; }

    public boolean isTemplate() { return !typeArgs.isEmpty(); }

    /**
     * Checks if the current {@link Invocation} instantiates a templated function.
     * @return Boolean
     */
    public boolean containsTypeArgs() { return !typeArgs.isEmpty(); }

    /**
     * Getter for {@link #name}.
     * @return Name
     */
    public Name getName() { return this.name; }

    /**
     * Getter for {@link #typeArgs}.
     * @return Vector of Types
     */
    public Vector<Type> getTypeArgs() { return this.typeArgs; }

    /**
     * Getter for {@link #args}.
     * @return Vector of Expressions
     */
    public Vector<Expression> getArgs() { return this.args; }

    /**
     * Getter for {@link #signature}.
     * @return String
     */
    public String getSignature() { return this.signature; }

    /**
     * Setter for {@link #name}.
     * @param name Name
     */
    private void setName(Name name) { this.name = name; }

    /**
     * Setter for {@link #typeArgs}.
     * @param typeArgs Vector of Types
     */
    private void setTypeArgs(Vector<Type> typeArgs) { this.typeArgs = typeArgs; }

    /**
     * Setter for {@link #args}.
     * @param args Vector of Expressions
     */
    private void setArgs(Vector<Expression> args) { this.args = args; }

    /**
     * Setter for {@link #signature}, set by the {@link typechecker.TypeChecker}.
     * @param signature String
     */
    public void setSignature(String signature) { this.signature = signature; }

    /**
     * Setter for {@link #isLengthInvocation}
     */
    public void setLengthInvocation() { this.isLengthInvocation = true; }

    /**
     * Checks if the current AST node is an {@link Invocation}.
     * @return Boolean
     */
    public boolean isInvocation() { return true; }

    /**
     * Type cast method for {@link Invocation}
     * @return Invocation
     */
    public Invocation asInvocation() { return this; }

    public String templateSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(signature);

        if(!typeArgs.isEmpty()) {
            sb.append("<");
            for(Type arg : typeArgs)
                sb.append(arg);
            sb.append(">");
        }

        return sb.toString();
    }

    /**
     * {@code toString} method.
     * @return String representing the name of the function/method called.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                name = node.asSubNode().asName();
                break;
            default:
                args.remove(pos-1);
                args.add(pos-1,node.asExpression());
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link Invocation}
     */
    @Override
    public AST deepCopy() {
        InvocationBuilder inb = new InvocationBuilder();

        if(this.containsTypeArgs()) {
            Vector<Type> typeParams = new Vector<>();
            for(Type t : this.typeArgs)
                typeParams.add(t.deepCopy().asType());

            inb.setTypeParams(typeParams);
        }

        Vector<Expression> args = new Vector<>();
        for(Expression expr : this.args)
            args.add(expr.deepCopy().asExpression());

        if(this.isLengthInvocation)
            inb.setLengthInvocation();



        return inb.setName(this.name.deepCopy().asSubNode().asName())
                  .setArgs(args)
                  .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitInvocation(this); }

    /**
     * Internal class that builds an {@link Invocation} object.
     */
    public static class InvocationBuilder extends NodeBuilder {

        /**
         * {@link Invocation} object we are building.
         */
        private final Invocation in = new Invocation();
        
        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return InvocationBuilder
         */
        public InvocationBuilder setMetaData(AST node) {
            super.setMetaData(in,node);
            return this;
        }

        /**
         * Sets the invocation's {@link #name}.
         * @param name Name representing the function or method being called.
         * @return InvocationBuilder
         */
        public InvocationBuilder setName(Name name) {
            in.setName(name);
            return this;
        }

        /**
         * Sets the invocation's {@link #typeArgs}.
         * @param typeParams Vector of types representing any type parameters passed.
         * @return InvocationBuilder
         */
        public InvocationBuilder setTypeParams(Vector<Type> typeParams) {
            in.setTypeArgs(typeParams);
            return this;
        }

        /**
         * Sets the invocation's {@link #args}.
         * @param args Vector of expressions representing all passed arguments.
         * @return InvocationBuilder
         */
        public InvocationBuilder setArgs(Vector<Expression> args) {
            in.setArgs(args);
            return this;
        }

        /**
         * Sets the invocation's {@link #isLengthInvocation}.
         * @return InvocationBuilder
         */
        public InvocationBuilder setLengthInvocation() {
            in.setLengthInvocation();
            return this;
        }

        /**
         * Creates an {@link Invocation} object.
         * @return {@link Invocation}
         */
        public Invocation create() {
            in.addChildNode(in.name);
            in.addChildNode(in.args);
            return in;
        }
    }
}
