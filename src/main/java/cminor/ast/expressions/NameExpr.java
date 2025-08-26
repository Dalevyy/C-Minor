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
     * Deep copy constructor for {@link NameExpr}.
     * @param ne The {@link NameExpr} we wish to create a copy of.
     */
    public NameExpr(NameExpr ne) {
        super(new Token());
        this.name = ne.getName().deepCopy().asSubNode().asName();
        this.text = ne.text;
    }

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
     * Checks if a {@link NameExpr} is found inside a complex field expression.
     * @return {@code True} if the name is in a complex field expression, {@code False} otherwise.
     */
    public boolean inComplexFieldExpr() {
        if(!inFieldExpr())
            return false; // this.x

        // To return true, the name has to be found AFTER the first target!
        AST target = parent;

        if(target.getParent() != null &&
                target.getParent().isExpression() && target.getParent().asExpression().isFieldExpr())
            return true;
        System.out.println(!this.equals(target.asExpression().asFieldExpr().getTarget()));
        return !this.equals(target.asExpression().asFieldExpr().getTarget());
    }

    /**
     * Checks if the current {@link NameExpr} is found inside a {@link FieldExpr}.
     * <p>
     *     This will be used to correctly resolve names.
     * </p>
     * @return {@code True} if the name is in a {@link FieldExpr}, {@code False} otherwise.
     */
    private boolean inFieldExpr() {return parent.isExpression() && parent.asExpression().isFieldExpr();}

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
