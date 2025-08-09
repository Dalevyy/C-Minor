package ast.topleveldecls;

import ast.AST;
import ast.misc.Name;
import ast.misc.NameDecl;
import ast.misc.Var;
import ast.types.EnumType;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link TopLevelDecl} node that represents an enumeration.
 * @author Daniel Levy
 */
public class EnumDecl extends TopLevelDecl implements NameDecl {

    /**
     * The name of the enumeration.
     */
    private Name name;

    /**
     * The list of constants associated with this enumeration.
     */
    private Vector<Var> constants;

    /**
     * The type of the enumeration.
     */
    private EnumType constantType;

    /**
     * Default constructor for {@link EnumDecl}.
     */
    public EnumDecl() { this(new Token(),null,new Vector<>()); }

    /**
     * Main constructor for {@link EnumDecl}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param name {@link Name} to store into {@link #name}.
     * @param constants {@link Vector} of variables to store into {@link #constants}.
     */
    public EnumDecl(Token metaData, Name name, Vector<Var> constants) {
        super(metaData);

        this.name = name;
        this.constants = constants;

        addChildNode(this.constants);
    }

    /**
     * Getter method for {@link #name}.
     * @return {@link Name}
     */
    public Name getName() { return name; }

    /**
     * Getter method for {@link #constantType}
     * @return {@link EnumType}
     */
    public EnumType getConstantType() { return constantType; }

    /**
     * Getter method for {@link #constants}.
     * @return {@link Vector} of variables.
     */
    public Vector<Var> getConstants() { return constants;}

    /**
     * Setter that sets the value of {@link #constantType}.
     * <p>
     *     The {@link #constantType} will be set by the {@link typechecker.TypeChecker}.
     * </p>
     * @param constantType The {@link EnumType} we will set for this enumeration.
     */
    public void setConstantType(EnumType constantType) { this.constantType = constantType; }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return name.toString(); };

    /**
     * {@inheritDoc}
     */
    public boolean isMethod() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isFunction() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isEnumDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public EnumDecl asEnumDecl() { return this; }

    /**
     * Returns the name of the enumeration.
     * @return String representation of the enumeration's name.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(int pos, AST node) {
        constants.remove(pos);
        constants.add(pos, node.asSubNode().asVar());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<Var> constants = new Vector<>();

        for(Var v : this.constants)
            constants.add(v.deepCopy().asSubNode().asVar());

        return new EnumDeclBuilder()
                   .setMetaData(this)
                   .setName(name.deepCopy().asSubNode().asName())
                   .setConstants(constants)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }

    /**
     * Internal class that builds an {@link EnumDecl} object.
     */
    public static class EnumDeclBuilder extends NodeBuilder {

        /**
         * {@link EnumDecl} object we are building.
         */
        private final EnumDecl ed = new EnumDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link EnumDeclBuilder}.
         */
        public EnumDeclBuilder setMetaData(AST node) {
            super.setMetaData(ed, node);
            return this;
        }

        /**
         * Sets the enumeration's {@link #name}.
         * @param name {@link Name} representing the name of the enum.
         * @return Current instance of {@link EnumDeclBuilder}.
         */
        public EnumDeclBuilder setName(Name name) {
            ed.name = name;
            return this;
        }

        /**
         * Sets the enumeration's {@link #constants}.
         * @param constants {@link Vector} of variables that are created with the enumeration.
         * @return Current instance of {@link EnumDeclBuilder}.
         */
        public EnumDeclBuilder setConstants(Vector<Var> constants) {
            ed.constants = constants;
            return this;
        }

        /**
         * Creates an {@link EnumDecl} object.
         * @return {@link EnumDecl}
         * */
        public EnumDecl create() {
            ed.addChildNode(ed.constants);
            return ed;
        }
    }
}
