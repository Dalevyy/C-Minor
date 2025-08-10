package micropasses;

import ast.classbody.MethodDecl;
import ast.expressions.FieldExpr;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.expressions.ThisStmt;
import ast.misc.CompilationUnit;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.ImportDecl;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * A {@link Visitor} class that performs field rewrites within a class.
 * <p><br>
 *     To access a field within a class, a user can simply write the name
 *     of the field, and the compiler will parse this as a name expression.
 *     While this is correct syntax for the user, the compiler needs to rewrite
 *     all name expressions to be field expressions, so when we go to execute
 *     the user's code, we know the name we want to access actually refers to
 *     a field that we need to access from an object. This field rewrite will
 *     be done by appending a {@link ThisStmt} node to the {@link NameExpr} when
 *     we create a field expression, and we will replace all instances of the
 *     {@link NameExpr} with the corresponding field expression in the AST hierarchy.
 * @author Daniel Levy
 */
public class FieldRewrite extends Visitor {
//
//    /**
//     * Current scope we are in (either globally or within a class).
//     */
//    private SymbolTable currentScope;
//
//    // Temp solution!!!!!!!!!!!!!!!!! Fix when you review this class later.
//    private boolean insideClass;
//
//    /**
//     * Sets the current scope to be inside of a case statement.
//     * @param cs Case Statement
//     */
//    public void visitCaseStmt(CaseStmt cs) {
//        currentScope = cs.scope;
//        super.visitCaseStmt(cs);
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Sets the current scope to be inside of a choice statement.
//     * @param chs Choice Statement
//     */
//    public void visitChoiceStmt(ChoiceStmt chs) {
//        currentScope = chs.scope;
//        super.visitChoiceStmt(chs);
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Sets the current scope to be inside of a class.
//     * @param cd Class declaration
//     */
//    public void visitClassDecl(ClassDecl cd) {
//        insideClass = true;
//        currentScope = cd.getScope();
//        super.visitClassDecl(cd);
//        currentScope = currentScope.closeScope();
//        insideClass = false;
//    }
//
//    /**
//     * Begins the field rewrite pass in compilation mode.
//     * <p><br>
//     *     Since field rewrites are only needed for classes, we want
//     *     to make sure this micropass executes for all classes found
//     *     in the main compilation unit alongside any imported classes.
//     * </p>
//     * @param c Compilation Unit
//     */
//    public void visitCompilationUnit(CompilationUnit c) {
//        currentScope = c.getScope();
//
//        for(ImportDecl id : c.getImports())
//            id.visit(this);
//
//        for(ClassDecl cd : c.getClasses())
//            cd.visit(this);
//    }
//
//    /**
//     * Sets the current scope to be inside of a do while loop.
//     * @param ds Do Statement
//     */
//    public void visitDoStmt(DoStmt ds) {
//        currentScope = ds.scope;
//        super.visitDoStmt(ds);
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Visits and rewrites the target of a field expression.
//     * <p><br>
//     *     Since an object can be a valid field within a class, we need
//     *     to make sure the object itself is rewritten to be `this.objName`.
//     *     Thus, we are only going to visit the target expression of the
//     *     current field expression.
//     * </p>
//     * @param fe Field Expression
//     */
//    public void visitFieldExpr(FieldExpr fe) { fe.getTarget().visit(this); }
//
//    /**
//     * Sets the current scope to be inside of a for loop.
//     * @param fs For Statement
//     */
//    public void visitForStmt(ForStmt fs) {
//        currentScope = fs.scope;
//        super.visitForStmt(fs);
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Sets the current scope to be inside of an if statement.
//     * @param is If Statement
//     */
//    public void visitIfStmt(IfStmt is) {
//        is.getCondition().visit(this);
//        currentScope = is.ifScope;
//        is.getIfBody().visit(this);
//
//        for(IfStmt elifStmt : is.getElifs())
//            elifStmt.visit(this);
//
//        if(is.getElseBody() != null) {
//            currentScope = is.elseScope;
//            is.getElseBody().visit(this);
//        }
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Looks through any imported classes and performs a field rewrite.
//     * @param im Import Declaration
//     */
//    public void visitImportDecl(ImportDecl im) {
//        SymbolTable oldScope = currentScope;
//
//        im.getCompilationUnit().visit(this);
//
//        currentScope = oldScope;
//    }
//
//    /**
//     * Sets the current scope to be inside of a method declaration.
//     * @param md Method Declaration
//     */
//    public void visitMethodDecl(MethodDecl md) {
//        currentScope = md.getScope();
//        super.visitMethodDecl(md);
//        currentScope = currentScope.closeScope();
//    }
//
//    /**
//     * Visits and rewrites all name expressions that correspond to field declarations
//     * @param ne Name Expression
//     */
//    public void visitNameExpr(NameExpr ne) {
//        if(!insideClass)
//            return;
//        if(!ne.isParentKeyword() && currentScope.findName(ne.toString()).getDecl().asClassNode().isFieldDecl()) {
//            FieldExpr fe = new FieldExprBuilder()
//                               .setTarget(new ThisStmt())
//                               .setAccessExpr(new NameExpr(ne.toString()))
//                               .create();
//            ne.replaceWith(fe);
//        }
//    }
//
//    /**
//     * Sets the current scope to be inside of a while loop.
//     * @param ws While Statement
//     */
//    public void visitWhileStmt(WhileStmt ws) {
//        currentScope = ws.scope;
//        super.visitWhileStmt(ws);
//        currentScope = currentScope.closeScope();
//    }
}
