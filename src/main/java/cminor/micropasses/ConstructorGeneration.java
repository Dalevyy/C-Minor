package cminor.micropasses;

import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.InitDecl.InitDeclBuilder;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.NameExpr.NameExprBuilder;
import cminor.ast.expressions.NewExpr;
import cminor.ast.expressions.ThisStmt;
import cminor.ast.operators.AssignOp.AssignOpBuilder;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.statements.AssignStmt;
import cminor.ast.statements.AssignStmt.AssignStmtBuilder;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Visitor} class designed to generate class constructors.
 * <p><br>
 *     C Minor does not require (nor allow) a programmer to create constructors
 *     for their classes. Instead, the compiler will generate a single constructor
 *     for each class. This will be represented as an {@link ast.classbody.InitDecl}
 *     internally. A C Minor constructor will just be a sequence of {@link AssignStmt}s
 *     that will initialize every field to a class to a default value unless the user
 *     specified a different initial value within a {@link ast.expressions.NewExpr}
 * </p>
 * @author Daniel Levy
 */
public class ConstructorGeneration extends Visitor {
//
//    /**
//     * Creates an {@link ast.classbody.InitDecl} for the current class.
//     * @param cd Class Declaration
//     */
//    public void visitClassDecl(ClassDecl cd) {
//        Vector<AssignStmt> initParams = new Vector<>();
//
//        // For every field declared in the current class, we will create an
//        // assignment statement that will be stored in the generated constructor.
//        for(String key : cd.getScope().getAllNames().keySet()) {
//            if(cd.getScope().findName(key).getDecl().asClassNode().isFieldDecl()) {
//                FieldDecl currFieldDecl = cd.getScope().findName(key).getDecl().asClassNode().asFieldDecl();
//                initParams.add(
//                    new AssignStmtBuilder()
//                        .setLHS(
//                            new FieldExprBuilder()
//                                .setTarget(new ThisStmt())
//                                .setAccessExpr(
//                                    new NameExprBuilder()
//                                        .setName(currFieldDecl.getVariableName())
//                                        .create()
//                                )
//                                .create()
//                        )
//                        .setRHS(currFieldDecl.getInitialValue())
//                        .setAssignOp(
//                            new AssignOpBuilder()
//                                .setAssignOperator(AssignType.EQ)
//                                .create()
//                        )
//                        .create());
//            }
//        }
//
//        cd.setConstructor(
//            new InitDeclBuilder()
//                .setInits(initParams)
//                .create()
//        );
//    }
//
//    public void visitNewExpr(NewExpr ne) {
//        if(ne.createsFromTemplate())
//            ne.getInstantiatedClass().visit(this);
//    }
}
