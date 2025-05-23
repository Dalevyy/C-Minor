package micropasses;

import ast.expressions.FieldExpr;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.expressions.This;
import ast.topleveldecls.ClassDecl;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Micropass #3
 * <br><br>
 * Once name checking is complete, we want to go back through each class
 * in a C Minor program and make sure all <code>NameExpr</code> nodes that
 * represent fields are rewritten to be <code>FieldExpr</code> nodes instead.
 * This is needed as we have to internally keep track of whether or not the
 * <code>NameExpr</code> refers to a field since during execution, we have to
 * be able to evaluate the value of the field based on the current object. This
 * will be done by setting the target to be <code>this</code> when we generate
 * the replacement <code>FieldExpr</code>
 * @author Daniel Levy
 */
public class FieldRewrite extends Visitor {

    private SymbolTable currentScope;

    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    public void visitNameExpr(NameExpr ne) {
        if(currentScope.hasNameSomewhere(ne.toString())) {
            if(currentScope.findName(ne.toString()).decl().isFieldDecl()) {
                FieldExpr fe = new FieldExprBuilder()
                                        .setTarget(new This())
                                        .setAccessExpr(ne)
                                        .createFieldExpr();
                fe.copyAndRemove(ne);
            }
        }
    }
}
