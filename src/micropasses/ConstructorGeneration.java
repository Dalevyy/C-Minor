package micropasses;

import ast.classbody.FieldDecl;
import ast.classbody.InitDecl.InitDeclBuilder;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr.NameExprBuilder;
import ast.expressions.NewExpr;
import ast.expressions.ThisStmt;
import ast.operators.AssignOp.AssignOpBuilder;
import ast.operators.AssignOp.AssignType;
import ast.topleveldecls.ClassDecl;
import ast.statements.AssignStmt;
import ast.statements.AssignStmt.AssignStmtBuilder;
import utilities.Vector;
import utilities.Visitor;

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
