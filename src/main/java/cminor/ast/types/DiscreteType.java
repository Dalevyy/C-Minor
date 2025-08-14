package cminor.ast.types;

import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link ScalarType} representing a type that contains a discrete value.
 * <p>
 *     A discrete value is any value that is fixed in size. In this case, the types
 *     {@code Int}, {@code Char}, and {@code Bool} are considered discrete types and
 *     are a subset of the {@link ScalarType}.
 * </p>
 * @author Daniel Levy
 */
public class DiscreteType extends ScalarType {

    /**
     * Default constructor for {@link DiscreteType}.
     */
    public DiscreteType() { this(new Token(),null); }

    /**
     * Constructor to generate a {@link DiscreteType} when given a specific type.
     * @param discrete {@link Scalars} to store into {@link #scalar}.
     */
    public DiscreteType(Scalars discrete) { this(new Token(), discrete); }

    /**
     * Main constructor for {@link DiscreteType}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param discrete {@link Scalars} to store into {@link #scalar}.
     */
    public DiscreteType(Token metaData, Scalars discrete) { super(metaData,discrete); }

    /**
     * {@inheritDoc}
     */
    public boolean isInt() { return scalar == Scalars.INT; }

    /**
     * {@inheritDoc}
     */
    public boolean isChar() { return scalar == Scalars.CHAR; }

    /**
     * {@inheritDoc}
     */
    public boolean isBool() { return scalar == Scalars.BOOL; }

    /**
     * {@inheritDoc}
     */
    public boolean isDiscrete() { return true; }

    /**
     * {@inheritDoc}
     */
    public DiscreteType asDiscrete() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitDiscreteType(this); }
}
