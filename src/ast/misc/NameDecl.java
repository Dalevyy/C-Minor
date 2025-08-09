package ast.misc;

import ast.AST;

/**
 * An interface that denotes which {@link AST} nodes can be inserted into the {@link utilities.SymbolTable}.
 * <p>
 *     All {@link AST} nodes representing declarations will implement this interface. The list of declarations
 *     can be seen below:
 *     <ul>
 *         <li> Top Level Declarations </li>
 *         <ol>
 *             <li> {@link ast.topleveldecls.ClassDecl} </li>
 *             <li> {@link ast.topleveldecls.EnumDecl} </li>
 *             <li> {@link ast.topleveldecls.FuncDecl} </li>
 *             <li> {@link ast.topleveldecls.GlobalDecl} </li>
 *         </ol>
 *         <li> Class Body Declarations </li>
 *         <ol>
 *             <li> {@link ast.classbody.FieldDecl} </li>
 *             <li> {@link ast.classbody.MethodDecl} </li>
 *         </ol>
 *         <li> Other Declarations </li>
 *         <ol>
 *             <li> {@link ast.statements.LocalDecl} </li>
 *             <li> {@link ast.misc.ParamDecl} </li>
 *             <li> {@link TypeParam} </li>
 *         </ol>
 *     </ul>
 * </p>
 * @author Daniel Levy
 */
public interface NameDecl {

    /**
     * Returns the current {@link NameDecl} as an {@link AST} type.
     * @return Current {@link NameDecl} as an {@link AST} type.
     */
    AST getDecl();

    /**
     * Returns the name of the current {@link NameDecl}.
     * @return String representation of the declaration's name.
     */
    String getDeclName();

    /**
     * Checks if the current {@link NameDecl} represents a method.
     * @return {@code True} if the {@link NameDecl} is a {@link ast.classbody.MethodDecl}, {@code False} otherwise.
     */
    boolean isMethod();

    /**
     * Checks if the current {@link NameDecl} represents a function.
     * @return {@code True} if the {@link NameDecl} is a {@link ast.topleveldecls.FuncDecl}, {@code False} otherwise.
     */
    boolean isFunction();
}
