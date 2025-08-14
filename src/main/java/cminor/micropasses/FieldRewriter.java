package cminor.micropasses;

import cminor.ast.AST;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.FieldExpr;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.NameExpr;
import cminor.ast.expressions.ThisStmt;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.statements.CaseStmt;
import cminor.ast.statements.ChoiceStmt;
import cminor.ast.statements.DoStmt;
import cminor.ast.statements.ForStmt;
import cminor.ast.statements.IfStmt;
import cminor.ast.statements.WhileStmt;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.ImportDecl;
import cminor.utilities.SymbolTable;
import cminor.utilities.Visitor;

/**
 * A {@link Visitor} class that performs field rewrites within a class.
 * <p>
 *     To access a field within a class, a user can simply write the name
 *     of the field, and the compiler will parse this as a name expression.
 *     While this is correct syntax for the user, the compiler needs to rewrite
 *     all name expressions to be field expressions, so when we go to execute
 *     the user's code, we know the name we want to access actually refers to
 *     a field that needs to be accessed through an object. This rewrite is
 *     done by appending a {@link ThisStmt} node to the {@link NameExpr} when
 *     we create a field expression, and we will replace all instances of the
 *     {@link NameExpr} with the corresponding field expression in the AST hierarchy.
 * @author Daniel Levy
 */
public class FieldRewriter extends Visitor {

    /**
     * {@link SymbolTable} representing the current scope we are in.
     */
    private SymbolTable currentScope;

    /**
     * Sets the {@link #currentScope} to be inside a case statement.
     * @param cs {@link CaseStmt}
     */
    public void visitCaseStmt(CaseStmt cs) {
        currentScope = cs.getScope();
        super.visitCaseStmt(cs);
        currentScope = currentScope.closeScope();
    }

    /**
     * Visits and checks all the case statements for potential rewrites.
     * @param cs {@link ChoiceStmt}
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.getChoiceValue().visit(this);

        for(CaseStmt currCase : cs.getCases())
            currCase.visit(this);

        currentScope = cs.getScope();
        cs.getDefaultBody().visit(this);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the {@link #currentScope} to be inside a class.
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.getScope();
        super.visitClassDecl(cd);
    }

    /**
     * Begins the rewriting process in compilation mode.
     * <p>
     *     Note: We only care about executing this {@link Visitor} for classes,
     *     so we will only look through the program's classes and any imports.
     * </p>
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        for(ImportDecl id : cu.getImports())
            id.visit(this);

        for(ClassDecl cd : cu.getClasses())
            cd.visit(this);
    }

    /**
     * Sets the {@link #currentScope} to be inside a do while loop.
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        currentScope = ds.getScope();
        super.visitDoStmt(ds);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the {@link #currentScope} to be inside a for loop.
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.getScope();
        super.visitForStmt(fs);
        currentScope = currentScope.closeScope();
    }

    /**
     * Visits the target expression when accessing a field.
     * <p>
     *     For complex field expressions, we may need to append a {@link ThisStmt}
     *     if the initial target expression represents an object declared in a {@link cminor.ast.classbody.FieldDecl}.
     * </p>
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) { fe.getTarget().visit(this); }

    /**
     * Visits and checks all branches of an if statement for rewrites.
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        is.getCondition().visit(this);

        currentScope = is.getIfScope();
        is.getIfBody().visit(this);
        currentScope = currentScope.closeScope();

        for(IfStmt elif : is.getElifs())
            elif.visit(this);

        if(is.containsElse()) {
            currentScope = is.getElseScope();
            is.getElseBody().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Visits an {@link ImportDecl} in order to perform any field rewrites.
     * @param id {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl id) { id.getCompilationUnit().visit(this); }

    /**
     * Sets the {@link #currentScope} to be inside a method.
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.getScope();
        super.visitMethodDecl(md);
        currentScope = currentScope.closeScope();
    }

    /**
     * Checks if a name expression references a {@link cminor.ast.classbody.FieldDecl} and rewrites it.
     * <p>
     *     Since we need to make sure that an object can properly access a field at runtime, this visit
     *     will be responsible for performing any rewrites within a class.
     * </p>
     * @param ne {@link NameExpr}
     */
    public void visitNameExpr(NameExpr ne) {
        if(currentScope.hasNameInProgram(ne)) {
            AST decl = currentScope.findName(ne);
            if(decl.isClassNode() && decl.asClassNode().isFieldDecl()) {
                FieldExpr fe = new FieldExprBuilder()
                                   .setTarget(new ThisStmt())
                                   .setAccessExpr(new NameExpr(ne.toString()))
                                   .create();
                ne.replaceWith(fe);
            }
        }
    }

    /**
     * Sets the {@link #currentScope} to be inside a while loop.
     * @param ws {@link WhileStmt}
     */
    public void visitWhileStmt(WhileStmt ws) {
        currentScope = ws.getScope();
        super.visitWhileStmt(ws);
        currentScope = currentScope.closeScope();
    }
}
