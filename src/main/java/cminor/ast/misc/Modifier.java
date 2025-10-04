package cminor.ast.misc;

import cminor.ast.AST;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link SubNode} type that stores all modifiers associated with a given node.
 * <p>
 *     This class serves as a helper for us to know the access privileges that
 *     various other {@link AST} nodes will have. As a result, it will simply be
 *     a part of other parse tree nodes.
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
     * A {@link Vector} containing each modifier as a string (for debug printing).
     */
    private final Vector<String> allModifiers;

    /**
     * Default constructor for {@link Modifier}.
     */
    public Modifier() {
        super(new Token());
        this.allModifiers = new Vector<>();
    }

    /**
     * Copies the true attributes of a {@link Modifier} to the current {@link Modifier}.
     * This is slow... but it won't be called often so it's fine for now... 0_0
     * @param mod The {@link Modifier} we wish to combine with the current one.
     */
    public void add(Modifier mod) {
        if(mod.isPublic)
            this.isPublic = true;
        if(mod.isProtected)
            this.isProtected = true;
        if(mod.isProperty)
            this.isProperty = true;
        if(mod.isFinal)
            this.isFinal = true;
        if(mod.isAbstract)
            this.isAbstract = true;
        if(mod.isPure)
            this.isPure = true;
        if(mod.isRecursive)
            this.isRecursive = true;
        if(mod.isInMode)
            this.isInMode = true;
        if(mod.isOutMode)
            this.isOutMode = true;
        if(mod.isInOutMode)
            this.isInOutMode = true;
        if(mod.isRefMode)
            this.isRefMode = true;
    }

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
    public void setPublic() {
        if(!isPublic) {
            isPublic = true;
            allModifiers.add("public");
        }
    }

    /**
     * Sets {@link #isProtected} to be {@code True}.
     */
    public void setProtected() {
        if(!isProperty) {
            isProtected = true;
            allModifiers.add("protected");
        }
    }

    /**
     * Sets {@link #isProperty} to be {@code True}.
     */
    public void setProperty() {
        if(!isProperty) {
            isProperty = true;
            allModifiers.add("property");
        }
    }

    /**
     * Sets {@link #isFinal} to be {@code True}.
     */
    public void setFinal() {
        if(!isFinal) {
            isFinal = true;
            allModifiers.add("final");
        }
    }

    /**
     * Sets {@link #isAbstract} to be {@code True}.
     */
    public void setAbstract() {
        if(!isAbstract) {
            isAbstract = true;
            allModifiers.add("abstract");
        }
    }

    /**
     * Sets {@link #isPure} to be {@code True}.
     */
    public void setPure() {
        if(!isPure) {
            isPure = true;
            allModifiers.add("pure");
        }
    }

    /**
     * Sets {@link #isRecursive} to be {@code True}.
     */
    public void setRecursive() {
        if(!isRecursive) {
            isRecursive = true;
            allModifiers.add("recursive");
        }
    }

    /**
     * Sets {@link #isInMode} to be {@code True}.
     */
    public void setInMode() {
        if(!isInMode) {
            isInMode = true;
            allModifiers.add("in");
        }
    }

    /**
     * Sets {@link #isOutMode} to be {@code True}.
     */
    public void setOutMode() {
        if(!isOutMode) {
            isOutMode = true;
            allModifiers.add("out");
        }
    }

    /**
     * Sets {@link #isInOutMode} to be {@code True}.
     */
    public void setInOutMode() {
        if(!isInOutMode) {
            isInOutMode = true;
            allModifiers.add("inout");
        }
    }

    /**
     * Sets {@link #isRefMode} to be {@code True}.
     */
    public void setRefMode() {
        if(!isRefMode) {
            isRefMode = true;
            allModifiers.add("ref");
        }
    }

    /**
     * Checks if there are any modifiers present.
     * @return {@code True} if there are modifiers, {@code False} otherwise.
     */
    public boolean isEmpty() { return allModifiers.isEmpty(); }

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

    /**
     * Creates a string representation of all modifiers being used.
     * @return String with each modifier name listed.
     */
    @Override
    public String toString() {
        if(allModifiers.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < allModifiers.size()-1; i++)
            sb.append(allModifiers.get(i)).append(", ");

        sb.append(allModifiers.getLast());
        return sb.toString();
    }
}
