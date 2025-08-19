package cminor.ast.misc;

import cminor.ast.AST;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.statements.LocalDecl;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.EnumDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.GlobalDecl;

/**
 * An interface that denotes which {@link AST} nodes can be inserted into the {@link utilities.SymbolTable}.
 * <p>
 *     All {@link AST} nodes representing declarations will implement this interface. The list of declarations
 *     can be seen below:
 *     <ul>
 *         <li> Top Level Declarations </li>
 *         <ol>
 *             <li> {@link ClassDecl} </li>
 *             <li> {@link EnumDecl} </li>
 *             <li> {@link FuncDecl} </li>
 *             <li> {@link GlobalDecl} </li>
 *         </ol>
 *         <li> Class Body Declarations </li>
 *         <ol>
 *             <li> {@link FieldDecl} </li>
 *             <li> {@link MethodDecl} </li>
 *         </ol>
 *         <li> Other Declarations </li>
 *         <ol>
 *             <li> {@link LocalDecl} </li>
 *             <li> {@link ParamDecl} </li>
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
}
