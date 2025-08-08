package ast.classbody;

import ast.AST;
import ast.topleveldecls.ClassDecl;
import token.Token;

/**
 * An {@link AST} subtype representing a node that can be found inside a {@link ast.topleveldecls.ClassDecl}.
 * <p>
 *     This subtype is primarily here to organize the helper methods needed for nodes in this package.
 *     The list of {@link ClassNode} are as follows:
 *     <ol>
 *         <li>{@link ClassBody}</li>
 *         <li>{@link FieldDecl}</li>
 *         <li>{@link InitDecl}</li>
 *         <li>{@link MethodDecl}</li>
 *     </ol>
 * </p>
 * @author Daniel Levy
 */
public abstract class ClassNode extends AST {

    /**
     * Default constructor for {@link ClassNode}.
     * @param metaData {@link Token} containing all the metadata stored with the {@link AST}.
     */
    public ClassNode(Token metaData) { super(metaData); }

    /**
     * Retrieves the {@link ClassDecl} that the current {@link ClassNode} is declared in.
     * @return The {@link ClassDecl} the {@link ClassNode} is declared in.
     */
    public abstract ClassDecl getClassDecl();

    /**
     * Checks if the current AST node is a {@link ast.classbody.ClassBody}.
     * @return {@code True} if the node is a {@link ast.classbody.ClassBody}, {@code False} otherwise.
     */
    public boolean isClassBody() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isClassNode() { return true; }

    /**
     * Checks if the current AST node is a {@link ast.classbody.FieldDecl}.
     * @return {@code True} if the node is a {@link ast.classbody.FieldDecl}, {@code False} otherwise.
     */
    public boolean isFieldDecl() { return false; }

    /**
     * Checks if the current AST node is an {@link ast.classbody.InitDecl}.
     * @return {@code True} if the node is an {@link ast.classbody.InitDecl}, {@code False} otherwise.
     */
    public boolean isInitDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link ast.classbody.MethodDecl}.
     * @return {@code True} if the node is a {@link ast.classbody.MethodDecl}, {@code False} otherwise.
     */
    public boolean isMethodDecl() { return false; }

    /**
     * Explicitly casts the current node into a {@link ClassBody}.
     * @return The current node as a {@link ClassBody}.
     */
    public ClassBody asClassBody()  {
        throw new RuntimeException("The current node does not represent a class body.");
    }

    /**
     * {@inheritDoc}
     */
    public ClassNode asClassNode() { return this;}

    /**
     * Explicitly casts the current node into a {@link FieldDecl}.
     * @return The current node as a {@link FieldDecl}.
     */
    public FieldDecl asFieldDecl()  {
        throw new RuntimeException("The current node does not represent a field declaration.");
    }

    /**
     * Explicitly casts the current node into an {@link InitDecl}.
     * @return The current node as an {@link InitDecl}.
     */
    public InitDecl asInitDecl()  {
        throw new RuntimeException("The current node does not represent a constructor declaration.");
    }

    /**
     * Explicitly casts the current node into a {@link MethodDecl}.
     * @return The current node as a {@link MethodDecl}.
     */
    public MethodDecl asMethodDecl()  {
        throw new RuntimeException("The current node does not represent a method declaration.");
    }
}
