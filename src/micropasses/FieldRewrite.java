package micropasses;

import ast.AST;
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

//    public void check(AST curr, ClassDecl cd) {
////        for(int i = 0; i < curr.children.size(); i++) {
////            AST n = curr.children.get(i);
////            if(n.isExpression() && n.asExpression().isNameExpr()) {
////                if(cd.symbolTable.hasNameSomewhere(n.toString())) {
////                    if(cd.symbolTable.findName(n.toString()).decl().isFieldDecl()) {
////                        FieldExpr fe = new FieldExprBuilder()
////                                        .setTarget(new This())
////                                        .setAccessExpr(n.asExpression())
////                                        .createFieldExpr();
////                        curr.children.set(i,fe);
////                    }
////                }
////            }
////            check(n,cd);
////       }
//// }

    private SymbolTable currentScope;

    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    public void visitFieldExpr(FieldExpr fe) {}

    public void visitNameExpr(NameExpr ne) {
        if(currentScope.hasNameSomewhere(ne.toString())) {
            if(currentScope.findName(ne.toString()).decl().isFieldDecl()) {
                //ne.setNameInClass();
                AST parent = ne.getParent();
                FieldExpr fe = new FieldExprBuilder()
                                        .setTarget(new This())
                                        .setAccessExpr(ne)
                                        .createFieldExpr();

                fe.copyAndRemove(ne);
            }
        }
    }
}
