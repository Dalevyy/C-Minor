package cminor.ast.misc;

import cminor.ast.AST;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.MainDecl;
import cminor.utilities.SymbolTable;

/**
 * An interface designed to keep track of {@link AST} types that will generate new scopes.
 * <p>
 *     Here is a list of {@link AST} types that will store a {@link SymbolTable}.
 *     <ol>
 *         <li>{@link MethodDecl}</li>
 *         <li>{@link CaseStmt}</li>
 *         <li>{@link ChoiceStmt}</li>
 *         <li>{@link DoStmt}</li>
 *         <li>{@link ForStmt}</li>
 *         <li>{@link IfStmt}</li>
 *         <li>{@link WhileStmt}</li>
 *         <li>{@link ClassDecl}</li>
 *         <li>{@link CompilationUnit}</li>
 *         <li>{@link FuncDecl}</li>
 *         <li>{@link MainDecl}</li>
 *     </ol>
 * </p>
 * @author Daniel Levy
 */
public interface ScopeDecl {

    /**
     * Sets the scope for the current {@link AST} node.
     * <p>
     *     This will be set by the {@link namechecker.NameChecker}.
     * </p>
     * @param newScope The {@link SymbolTable} representing the scope of the current {@link AST} node.
     */
    void setScope(SymbolTable newScope);

    /**
     * Getter method that retrieves the current scope we are in for a specified {@link AST} node.
     * @return The {@link SymbolTable} representing the scope of the current {@link AST} node.
     */
    SymbolTable getScope();
}
