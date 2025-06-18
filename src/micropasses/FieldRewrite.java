package micropasses;

import ast.expressions.FieldExpr;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.expressions.This;
import ast.misc.Compilation;
import ast.topleveldecls.ClassDecl;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Micropass #3
 * <br><br>
 * <p>
 *     Once name checking is complete, we want to go back through each class
 *     in a C Minor program and make sure all {@code NameExpr} nodes that
 *     represent fields are rewritten to be {@code FieldExpr} nodes instead.
 *     This is needed as we have to internally keep track of whether or not the
 *     {@code NameExpr} refers to a field since during execution, we have to
 *     be able to evaluate the value of the field based on the current object. This
 *     will be done by setting the target to be {@code this} when we generate
 *     the replacement {@code FieldExpr}
 * </p>
 * @author Daniel Levy
 */
public class FieldRewrite extends Visitor {

    /** Current class scope we are in */
    private SymbolTable currentScope;

    /**
     * Sets the current scope to be the class we are doing a field rewrite for
     * @param cd Class declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;
        super.visitCompilation(c);
    }

    /**
     * Visits and rewrites all name expressions that correspond to field declarations
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        /*
            If the current name expression can be traced back to a field declaration,
            then we need to turn the name expression into a field expression.
        */
        if(currentScope.hasName(ne.toString()) && currentScope.findName(ne.toString()).decl().isFieldDecl()) {
            FieldExpr fe = new FieldExprBuilder()
                               .setTarget(new This())
                               .setAccessExpr(new NameExpr(ne.toString()))
                               .create();
            fe.replace(ne);
        }
    }
}
