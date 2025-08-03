package ast.misc;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * An {@link AST} node that stores all modifiers associated with a given node.
 * <p>
 *     This class serves as a helper for us to know the access privileges that
 *     various other {@link AST} nodes will have. As a result, it will not be
 *     stored within the parse tree itself.
 * </p>
 * @author Daniel Levy
 */
public class Modifier extends SubNode {

    public boolean isPublic = false;
    public boolean isProtected = false;
    public boolean isProperty = false;
    public boolean isFinal = false;
    public boolean isAbstract = false;
    public boolean isPure = false;
    public boolean isRecursive = false;
    public boolean isInMode = false;
    public boolean isOutMode = false;
    public boolean isInOutMode = false;
    public boolean isRefMode = false;

    public Modifier() { super(new Token()); }

    /**
     * Sets {@link #isPublic} to be {@code True}.
     */
    public void setPublic() { isPublic = true; }

    /**
     * Sets {@link #isProtected} to be {@code True}.
     */
    public void setProtected() { isProtected = true; }

    /**
     * Sets {@link #isProperty} to be {@code True}.
     */
    public void setProperty() { isProperty = true; }

    /**
     * Sets {@link #isFinal} to be {@code True}.
     */
    public void setFinal() { isFinal = true; }

    /**
     * Sets {@link #isAbstract} to be {@code True}.
     */
    public void setAbstract() { isAbstract = true; }

    /**
     * Sets {@link #isPure} to be {@code True}.
     */
    public void setPure() { isPure = true; }

    /**
     * Sets {@link #isRecursive} to be {@code True}.
     */
    public void setRecursive() { isRecursive = true; }

    /**
     * Sets {@link #isInMode} to be {@code True}.
     */
    public void setInMode() { isInMode = true; }

    /**
     * Sets {@link #isOutMode} to be {@code True}.
     */
    public void setOutMode() { isOutMode = true; }

    /**
     * Sets {@link #isInOutMode} to be {@code True}.
     */
    public void setInOutMode() { isInOutMode = true; }

    /**
     * Sets {@link #isRefMode} to be {@code True}.
     */
    public void setRefMode() { isRefMode = true; }

    /**
     * {@inheritDoc}
     */
    public boolean isModifier() { return true; }

    /**
     * {@inheritDoc}
     */
    public Modifier asModifier() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A modifier can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Modifier copy = new Modifier();
        copy.copyMetaData(this);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v){ v.visitModifier(this); }
}
