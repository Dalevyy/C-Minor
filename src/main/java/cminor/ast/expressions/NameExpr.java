package cminor.ast.expressions;

import cminor.ast.AST;
import cminor.ast.misc.Name;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link Expression} representing the usage of a name.
 * <p>
 *     Every name needs to resolve back to a declaration, and
 *     this will be done by the {@link cminor.namechecker.NameChecker}.
 * </p>
 * @author Daniel Levy
 */
public class NameExpr extends Expression {

    /**
     * Name that the current {@link NameExpr} is representing.
     */
    private Name name;

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
    }

    /**
     * Getter for {@link #name}.
     * @return Name
     */
    public Name getName() { return name; }

    /**
     * {@inheritDoc}
     */
    public boolean isNameExpr() { return true; }

    /**
     * {@inheritDoc}
     */
    public NameExpr asNameExpr() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the current name
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        throw new RuntimeException("A name expression can not be updated.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy(){
        return new NameExprBuilder()
                   .setMetaData(this)
                   .setName(name.deepCopy().asSubNode().asName())
                   .create();
    }

    /**
     * {@inheritDoc}
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
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link NameExprBuilder}.
         */
        public NameExprBuilder setMetaData(AST node) {
            super.setMetaData(ne,node);
            return this;
        }

        /**
         * Sets the name expression's {@link #name}.
         * @param name {@link Name} that the name expression refers to
         * @return Current instance of {@link NameExprBuilder}.
         */
        public NameExprBuilder setName(Name name) {
            ne.name = name;
            return this;
        }

        /**
         * Creates a {@link NameExpr} object.
         * @return {@link NameExpr}
         */
        public NameExpr create() { return ne; }
    }
}
