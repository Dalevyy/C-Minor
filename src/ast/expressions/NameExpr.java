package ast.expressions;

import ast.AST;
import ast.misc.Name;
import token.Token;
import utilities.Visitor;

/**
 * An {@link AST} node class representing a name expression.
 * <p><br>
 *     A name expression represents any time a name is used outside
 *     of a declaration. Every name needs to resolve back to a declaration
 *     which will be done by the {@link namechecker.NameChecker}.
 * </p>
 * @author Daniel Levy
 */
public class NameExpr extends Expression {

    /**
     * Name that the current {@link NameExpr} is representing.
     */
    private Name name;

    /**
     * Flag that indicates if the current {@link NameExpr} is the {@code parent} keyword.
     */
    private boolean isParent;

    /**
     * Default constructor for {@link NameExpr}
     */
    public NameExpr() { this(new Token(),null); }

    /**
     * Constructor that turns a string into a name.
     * @param name Name to save into {@link #name}
     */
    public NameExpr(String name) { this(new Token(),new Name(name)); }

    /**
     * Main constructor for {@link NameExpr}.
     * @param metaData Token containing metadata we want to save
     * @param name Name to save into {@link #name}
     */
    public NameExpr(Token metaData, Name name) {
        super(metaData);
        this.name = name;
        if(name != null && name.toString().equals("parent"))
            this.isParent = true;

        addChildNode(this.name);
    }

    /**
     * Checks if the current {@link NameExpr} represents the keyword {@code parent}.
     * @return Boolean
     */
    public boolean isParentKeyword() { return this.isParent; }

    /**
     * Getter for {@link #name}.
     * @return Name
     */
    public Name getName() { return name; }

    /**
     * Setter for {@link #name}.
     * @param name Name
     */
    private void setName(Name name) {
        this.name = name;
        if(name.toString().equals("parent"))
            setParentKeyword();
    }

    /**
     * Setter for {@link #isParent}.
     */
    private void setParentKeyword() { this.isParent = true; }

    /**
     * Checks if the current AST node is a {@link NameExpr}.
     * @return Boolean
     */
    public boolean isNameExpr() { return true; }

    /**
     * Type cast method for {@link NameExpr}.
     * @return NameExpr
     */
    public NameExpr asNameExpr() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the current name
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { name = node.asSubNode().asName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link NameExpr}
     */
    @Override
    public AST deepCopy(){
        return new NameExprBuilder()
                   .setMetaData(this)
                   .setName(this.name.deepCopy().asSubNode().asName())
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitNameExpr(this); }

    /**
     * Internal class that builds a {@link NameExpr} object.
     */
    public static class NameExprBuilder extends NodeBuilder {

        /**
         * {@link NameExpr} object we are building.
         */
        private final NameExpr ne = new NameExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return NameExprBuilder
         */
        public NameExprBuilder setMetaData(AST node) {
            super.setMetaData(ne,node);
            return this;
        }

        /**
         * Sets the name expression's {@link #name}.
         * @param name Name that the name expression refers to
         * @return NameExprBuilder
         */
        public NameExprBuilder setName(Name name) {
            ne.setName(name);
            return this;
        }

        /**
         * Creates a {@link NameExpr} object.
         * @return {@link NameExpr}
         */
        public NameExpr create() {
            ne.addChildNode(ne.name);
            return ne;
        }
    }
}
