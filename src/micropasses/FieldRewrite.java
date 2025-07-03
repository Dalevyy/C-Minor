package micropasses;

import ast.expressions.FieldExpr;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.expressions.ThisStmt;
import ast.misc.Compilation;
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

    /**
     * Current scope we are in (either globally or within a class).
     */
    private SymbolTable currentScope;

    /**
     * Sets the current scope to be inside of a class.
     * @param cd Class declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    /**
     * Begins the field rewrite pass in compilation mode.
     * <p><br>
     *     Since field rewrites are only needed for classes, we want
     *     to make sure this micropass executes for all classes found
     *     in the main compilation unit alongside any imported classes.
     * </p>
     * @param c Compilation Unit
     */
    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;

        for(ImportDecl id : c.imports())
            id.visit(this);

        for(ClassDecl cd : c.classDecls())
            cd.visit(this);
    }

    /**
     * Visits and rewrites the target of a field expression.
     * <p><br>
     *     Since an object can be a valid field within a class, we need
     *     to make sure the object itself is rewritten to be `this.objName`.
     *     Thus, we are only going to visit the target expression of the
     *     current field expression.
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) { fe.getTarget().visit(this); }

    /**
     * Looks through any imported classes and performs a field rewrite.
     * @param im Import Declaration
     */
    public void visitImportDecl(ImportDecl im) {
        SymbolTable oldScope = currentScope;

        im.getCompilationUnit().visit(this);

        currentScope = oldScope;
    }

    /**
     * Visits and rewrites all name expressions that correspond to field declarations
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        if(currentScope.findName(ne.toString()).decl().isFieldDecl()) {
            FieldExpr fe = new FieldExprBuilder()
                               .setTarget(new ThisStmt())
                               .setAccessExpr(new NameExpr(ne.toString()))
                               .create();
            fe.replace(ne);
        }
    }
}
