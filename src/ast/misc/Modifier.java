package ast.misc;

import ast.AST;
import token.Token;
import utilities.Visitor;

/**
 * A {@link SubNode} type that stores all modifiers associated with a given node.
 * <p>
 *     This class serves as a helper for us to know the access privileges that
 *     various other {@link AST} nodes will have. As a result, it will simply be
 *     apart of other parse tree nodes.
 * </p>
 * @author Daniel Levy
 */
public class Modifier extends SubNode {

    /**
     * Flag that denotes if a field or method has {@code public} access.
     */
    private boolean isPublic = false;

    /**
     * Flag that denotes if a field or method has {@code protected} access.
     */
    private boolean isProtected = false;

    /**
     * Flag that denotes if a field should generate a getter and setter.
     */
    private boolean isProperty = false;

    /**
     * Flag that denotes if a class can not inherit from another class.
     */
    private boolean isFinal = false;

    /**
     * Flag that denotes if a class can not be instantiated.
     */
    private boolean isAbstract = false;

    /**
     * Flag that checks if any side effects will occur in a function or method.
     */
    private boolean isPure = false;

    /**
     * Flag that allows recursive function or method calls to occur.
     */
    private boolean isRecursive = false;

    /**
     * Flag that sets the parameter passing mode to be {@code in}.
     */
    private boolean isInMode = false;

    /**
     * Flag that sets the parameter passing mode to be {@code out}.
     */
    private boolean isOutMode = false;

    /**
     * Flag that sets the parameter passing mode to be {@code in out}.
     */
    private boolean isInOutMode = false;

    /**
     * Flag that sets the parameter passing mode to be {@code ref}.
     */
    private boolean isRefMode = false;

    /**
     * Default constructor for {@link Modifier}.
     */
    public Modifier() { super(new Token()); }

    /**
     * Returns value of {@link #isPublic}.
     * @return Boolean
     */
    public boolean isPublic() { return isPublic; }

    /**
     * Returns value of {@link #isProtected}.
     * @return Boolean
     */
    public boolean isProtected() { return isProtected; }

    /**
     * Returns value of {@link #isProperty}.
     * @return Boolean
     */
    public boolean isProperty() { return isProperty; }

    /**
     * Returns value of {@link #isFinal}.
     * @return Boolean
     */
    public boolean isFinal() { return isFinal; }

    /**
     * Returns value of {@link #isFinal}.
     * @return Boolean
     */
    public boolean isAbstract() { return isAbstract; }

    /**
     * Returns value of {@link #isPure}.
     * @return Boolean
     */
    public boolean isPure() { return isPure; }

    /**
     * Returns value of {@link #isRecursive}.
     * @return Boolean
     */
    public boolean isRecursive() { return isRecursive; }

    /**
     * Returns value of {@link #isInMode}.
     * @return Boolean
     */
    public boolean isInMode() { return isInMode; }

    /**
     * Returns value of {@link #isOutMode}.
     * @return Boolean
     */
    public boolean isOutMode() { return isOutMode; }

    /**
     * Returns value of {@link #isInOutMode}.
     * @return Boolean
     */
    public boolean isInOutMode() { return isInOutMode; }

    /**
     * Returns value of {@link #isRefMode}.
     * @return Boolean
     */
    public boolean isRefMode() { return isRefMode; }

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
