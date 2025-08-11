package cminor.ast.misc;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link SubNode} type that represents an identifier in C Minor.
 * @author Daniel Levy
 */
public class Name extends SubNode {

    /**
     * The string representation of a name.
     */
    private final String ID;

    /**
     * Main constructor for {@link Name}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     */
    public Name(Token metaData) {
        super(metaData);

        this.ID = metaData.getText();
    }

    /**
     * Constructor to generate a {@link Name} using a string.
     * @param name The string we want to create a {@link Name} object out of.
     */
    public Name(String name) {
        super(new Token());

        this.ID = name;
    }

    /**
     * Deep copy constructor for {@link Name}.
     * @param name {@link Name} object we will perform a deep copy of.
     */
    private Name(Name name) {
        super(new Token());

        this.copyMetaData(name);
        this.ID = name.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isName() { return true; }

    /**
     * {@inheritDoc}
     */
    public Name asName() { return this; }

    /**
     * Returns the name as a string.
     * @return String
     */
    @Override
    public String toString() { return ID; }

    /**
     * Checks if two names are equal to each other.
     * <p>
     *     In this case, the names have to be exactly equal regardless of case sensitivity.
     * </p>
     * @param name {@link Name} object we want to compare.
     * @return {@code True} if the names are equal, {@code False} otherwise.
     */
    public boolean equals(Name name) { return toString().equals(name.toString()); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A name can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() { return new Name(this); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitName(this); }
}
