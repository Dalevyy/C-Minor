package ast.misc;

import ast.AST;
import ast.types.Type;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link SubNode} type representing a parameter.
 * <p>
 *     A C Minor parameter can be declared with either a {@link ast.classbody.MethodDecl} or
 *     a {@link ast.topleveldecls.FuncDecl}. There are 4 passing modes in C Minor:
 *     <ol>
 *         <li>In</li>
 *         <li>Out</li>
 *         <li>In Out</li>
 *         <li>Ref</li>
 *     </ol>
 * </p>
 * @author Daniel Levy
 */
public class ParamDecl extends SubNode implements NameDecl {

    /**
     * The name of the parameter.
     */
    private Name name;

    /**
     * The declared type of the parameter.
     */
    private Type type;

    /**
     * The parameter's passing mode.
     */
    public Modifier mod;

    /**
     * Default constructor for {@link ParamDecl}.
     */
    public ParamDecl() { this(new Token(),null,null,null); }

    /**
     * Main constructor for {@link ParamDecl}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param mod {@link Modifier} to store into {@link #mod}.
     * @param name {@link Name} to store into {@link #name}.
     * @param type {@link Type} to store into {@link #type}.
     */
    public ParamDecl(Token metaData, Modifier mod, Name name, Type type) {
        super(metaData);

        this.mod = mod;
        this.name = name;
        this.type = type;
    }

    /**
     * Getter method for {@link #type}.
     * @return {@link Type}
     */
    public Type getType() { return type; }

    /**
     * Setter method to reset {@link #type}.
     * <p>
     *     This will be called by the {@link micropasses.TypeValidityPass} when we are updating
     *     the type of a {@link ParamDecl}.
     * </p>
     * @param type {@link Type} we wish to replace the current type with.
     */
    public void setType(Type type) { this.type = type;}

    /**
     * Checks if the current parameter is templated.
     * @param typeParams {@link Vector} of type parameters we will check to see if they are used in {@link #type}.
     * @return {@code True} if the parameter is templated, {@code False} otherwise.
     */
    public boolean isParameterTemplated(Vector<TypeParam> typeParams) {
        for(TypeParam tp : typeParams) {
            if(tp.equals(type.typeSignature()))
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this;}

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    public boolean isParamDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public ParamDecl asParamDecl() { return this; }

    /**
     * Returns the name of the parameter.
     * @return String representation of the parameter's name.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A parameter declaration can not be updated.");}

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ParamDeclBuilder()
                   .setMetaData(this)
                   .setModifier(mod)
                   .setName(name.deepCopy().asSubNode().asName())
                   .setType(type.deepCopy().asSubNode().asType())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitParamDecl(this); }

    /**
     * Internal class that builds a {@link ParamDecl} object.
     */
    public static class ParamDeclBuilder extends NodeBuilder {

        /**
         * {@link ParamDecl} object we are building.
         */
        private final ParamDecl pd = new ParamDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ParamDeclBuilder}.
         */
        public ParamDeclBuilder setMetaData(AST node) {
            super.setMetaData(pd, node);
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #mod}.
         * @param mod {@link Modifier} representing the passing mode of the parameter
         * @return Current instance of {@link ParamDeclBuilder}.
         */
        public ParamDeclBuilder setModifier(Modifier mod) {
            pd.mod = mod;
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #name}.
         * @param name {@link Name} that represents the parameter's identification.
         * @return Current instance of {@link ParamDeclBuilder}.
         */
        public ParamDeclBuilder setName(Name name) {
            pd.name = name;
            return this;
        }

        /**
         * Sets the parameter declaration's {@link #type}.
         * @param type {@link Type} representing the data type the parameter stores
         * @return Current instance of {@link ParamDeclBuilder}.
         */
        public ParamDeclBuilder setType(Type type) {
            pd.type = type;
            return this;
        }

        /**
         * Creates a {@link ParamDecl} object.
         * @return {@link ParamDecl}
         */
        public ParamDecl create() { return pd; }
    }
}
