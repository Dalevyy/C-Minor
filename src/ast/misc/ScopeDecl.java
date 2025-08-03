package ast.misc;

import ast.AST;
import utilities.SymbolTable;

/**
 * An interface designed to keep track of {@link AST} types that will generate new scopes.
 * <p>
 *     Here is a list of {@link AST} types that will store a {@link SymbolTable}.
 *     <ol>
 *         <li>{@link ast.classbody.MethodDecl}</li>
 *         <li>{@link ast.statements.CaseStmt}</li>
 *         <li>{@link ast.statements.ChoiceStmt}</li>
 *         <li>{@link ast.statements.DoStmt}</li>
 *         <li>{@link ast.statements.ForStmt}</li>
 *         <li>{@link ast.statements.IfStmt}</li>
 *         <li>{@link ast.statements.WhileStmt}</li>
 *         <li>{@link ast.topleveldecls.ClassDecl}</li>
 *         <li>{@link CompilationUnit}</li>
 *         <li>{@link ast.topleveldecls.FuncDecl}</li>
 *         <li>{@link ast.topleveldecls.MainDecl}</li>
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
