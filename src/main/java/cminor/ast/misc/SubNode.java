package cminor.ast.misc;

import cminor.ast.AST;
import cminor.token.Token;

/**
 * An {@link AST} subtype that represents all miscellaneous nodes in the parse tree.
 * <p>
 *     This is simply an organizational class for all nodes that do not belong to a more
 *     specific {@link AST} subtype. This class stores all the helper methods for these
 *     nodes, so we don't have clog the {@link AST} class with helpers.
 * </p>
 * @author Daniel Levy
 */
public abstract class SubNode extends AST {

    /**
     * Default constructor for {@link SubNode}
     * @param metaData {@link Token} containing all the metadata stored with the {@link AST}.
     */
    public SubNode(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is a {@link CompilationUnit}.
     * @return {@code True} if the node is a {@link CompilationUnit}, {@code False} otherwise.
     */
    public boolean isCompilationUnit() { return false; }

    /**
     * Checks if the current AST node is a {@link Label}.
     * @return {@code True} if the node is a {@link Label}, {@code False} otherwise.
     */
    public boolean isLabel() { return false; }

    /**
     * Checks if the current AST node is a {@link Modifier}.
     * @return {@code True} if the node is a {@link Modifier}, {@code False} otherwise.
     */
    public boolean isModifier() { return false; }

    /**
     * Checks if the current AST node is a {@link Name}.
     * @return {@code True} if the node is a {@link Name}, {@code False} otherwise.
     */
    public boolean isName() { return false; }

    /**
     * Checks if the current AST node is a {@link ParamDecl}.
     * @return {@code True} if the node is a {@link ParamDecl}, {@code False} otherwise.
     */
    public boolean isParamDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link TypeParam}.
     * @return {@code True} if the node is a {@link TypeParam}, {@code False} otherwise.
     */
    public boolean isTypeParam() { return false; }

    /**
     * Checks if the current AST node is a {@link Var}.
     * @return {@code True} if the node is a {@link Var}, {@code False} otherwise.
     */
    public boolean isVar() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isSubNode() { return true; }

    /**
     * Explicitly casts the current node into a {@link CompilationUnit}.
     * @return The current node as a {@link CompilationUnit}.
     */
    public CompilationUnit asCompilationUnit() {
        throw new RuntimeException("The current node does not represent a type parameter.");
    }

    /**
     * Explicitly casts the current node into a {@link Label}.
     * @return The current node as a {@link Label}.
     */
    public Label asLabel() {
        throw new RuntimeException("The current node does not represent a label.");
    }

    /**
     * Explicitly casts the current node into a {@link Modifier}.
     * @return The current node as a {@link Modifier}.
     */
    public Modifier asModifier() {
        throw new RuntimeException("The current node does not represent a modifier.");
    }

    /**
     * Explicitly casts the current node into a {@link Name}.
     * @return The current node as a {@link Name}.
     */
    public Name asName() {
        throw new RuntimeException("The current node does not represent a name.");
    }

    /**
     * Explicitly casts the current node into a {@link ParamDecl}.
     * @return The current node as a {@link ParamDecl}.
     */
    public ParamDecl asParamDecl() {
        throw new RuntimeException("The current node does not represent a parameter declaration.");
    }

    /**
     * {@inheritDoc}
     */
    public SubNode asSubNode() { return this; }

    /**
     * Explicitly casts the current node into a {@link TypeParam}.
     * @return The current node as a {@link TypeParam}.
     */
    public TypeParam asTypeParam() {
        throw new RuntimeException("The current node does not represent a type parameter.");
    }

    /**
     * Explicitly casts the current node into a {@link Var}.
     * @return The current node as a {@link Var}.
     */
    public Var asVar() {
        throw new RuntimeException("The current node does not represent a variable.");
    }
}
