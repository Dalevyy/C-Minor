package ast.misc;

import ast.AST;
import token.*;
import utilities.*;

/*
___________________________ Typeifier ___________________________
A typeifier node is used to keep track of templated types when
working with templated classes/functions. (NOT YET IMPLEMENTED)
_________________________________________________________________
*/
public class Typeifier extends AST implements NameNode {

    /**
     * List of potential types the typefier could represent.
     */
    public enum PossibleType { DISCR, SCALAR, CLASS }

    /**
     * String representation of {@code PossibleType} enum.
     */
    public static String[] names = {"Discrete", "Scalar", "Class" };

    /**
     * The high level type that the typefier could represent.
     * <p>
     *     In C Minor, only 3 classes of types are allowed to be used
     *     within templates: discrete, scalar, and class. The user can
     *     choose to specify if the typefier is any of these 3 types;
     *     however, it is not a requirement. As a result, this field
     *     will be empty, and it will be our job to make sure the user
     *     correctly uses the appropriate type.
     * </p>
     */
    private final PossibleType pt;

    /**
     * The generic name for the type that is written by the user.
     */
    private Name name;

    /**
     * Default constructor for {@code Typeifier}
     * @param metaData Metadata token
     * @param pt The type the typefier represents
     * @param n The generic name for the type
     */
    public Typeifier(Token metaData, PossibleType pt, Name n) {
        super(metaData);
        this.pt = pt;
        this.name = n;

        addChild(this.name);
        setParent();
    }

    public PossibleType getPossibleType() { return pt != null ? pt : null; }
    public Name getName() { return name; }

    public boolean isTypeifier() { return true; }
    public Typeifier asTypeifier() { return this; }

    @Override
    public AST decl() { return this; }

    @Override
    public void update(int pos, AST n) { name = n.asName(); }

    @Override
    public String toString() { return name.toString(); }

    /**
     * Visit method.
     * @param v Current visitor
     */
    @Override
    public void visit(Visitor v) { v.visitTypeifier(this); }
}
