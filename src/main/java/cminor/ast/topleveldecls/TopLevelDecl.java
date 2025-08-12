package cminor.ast.topleveldecls;

import cminor.ast.AST;
import cminor.token.Token;

/**
 * An {@link AST} subtype representing a node that appears in the global scope of a C Minor program.
 * <p>
 *     A top level declaration will declare any construct that can be accessed anywhere. Here is a list
 *     of all top level declarations in C Minor:
 *     <ol>
 *         <li>{@link ImportDecl}</li>
 *         <li>{@link EnumDecl}</li>
 *         <li>{@link GlobalDecl}</li>
 *         <li>{@link ClassDecl}</li>
 *         <li>{@link FuncDecl}</li>
 *         <li>{@link MainDecl}</li>
 *     </ol>
 *      If we are in interpretation mode, then any of these constructs can be declared at any time.
 *      However, if we are in compilation mode, then the order the constructs appear in the list has to be the
 *      order in which the user writes them in their program or else we will generate a parsing error.
 * </p>
 * @author Daniel Levy
 */
public abstract class TopLevelDecl extends AST {

    /**
     * Default constructor for {@link TopLevelDecl}.
     * @param metaData {@link Token} containing all the metadata stored with the {@link AST}.
     */
    public TopLevelDecl(Token metaData) { super(metaData); }

    /**
     * Checks if the current AST node is a {@link ClassDecl}.
     * @return {@code True} if the node is a {@link ClassDecl}, {@code False} otherwise.
     */
    public boolean isClassDecl() { return false; }

    /**
     * Checks if the current AST node is an {@link EnumDecl}.
     * @return {@code True} if the node is an {@link EnumDecl}, {@code False} otherwise.
     */
    public boolean isEnumDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link FuncDecl}.
     * @return {@code True} if the node is a {@link FuncDecl}, {@code False} otherwise.
     */
    public boolean isFuncDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link GlobalDecl}.
     * @return {@code True} if the node is a {@link GlobalDecl}, {@code False} otherwise.
     */
    public boolean isGlobalDecl() { return false; }

    /**
     * Checks if the current AST node is an {@link ImportDecl}.
     * @return {@code True} if the node is an {@link ImportDecl}, {@code False} otherwise.
     */
    public boolean isImport() { return false; }

    /**
     * Checks if the current AST node is a {@link MainDecl}.
     * @return {@code True} if the node is a {@link MainDecl}, {@code False} otherwise.
     */
    public boolean isMainDecl() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isTopLevelDecl() { return true; }

    /**
     * Explicitly casts the current node into a {@link ClassDecl}.
     * @return The current node as a {@link ClassDecl}.
     */
    public ClassDecl asClassDecl() { throw new RuntimeException("Expression can not be casted into a ClassDecl.\n"); }

    /**
     * Explicitly casts the current node into an {@link EnumDecl}.
     * @return The current node as an {@link EnumDecl}.
     */
    public EnumDecl asEnumDecl() { throw new RuntimeException("Expression can not be casted into an EnumDecl.\n"); }

    /**
     * Explicitly casts the current node into a {@link FuncDecl}.
     * @return The current node as a {@link FuncDecl}.
     */
    public FuncDecl asFuncDecl() { throw new RuntimeException("Expression can not be casted into a FuncDecl.\n"); }

    /**
     * Explicitly casts the current node into a {@link GlobalDecl}.
     * @return The current node as a {@link GlobalDecl}.
     */
    public GlobalDecl asGlobalDecl() { throw new RuntimeException("Expression can not be casted into a GlobalDecl.\n"); }

    /**
     * Explicitly casts the current node into an {@link ImportDecl}.
     * @return The current node as an {@link ImportDecl}.
     */
    public ImportDecl asImport() { throw new RuntimeException("Expression can not be casted into an ImportDecl.\n"); }

    /**
     * Explicitly casts the current node into a {@link MainDecl}.
     * @return The current node as a {@link MainDecl}.
     */
    public MainDecl asMainDecl() { throw new RuntimeException("Expression can not be casted into a MainDecl.\n"); }

    /**
     * {@inheritDoc}
     */
    public TopLevelDecl asTopLevelDecl() { return this; }
}
