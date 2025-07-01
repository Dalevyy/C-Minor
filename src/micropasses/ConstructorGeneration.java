package micropasses;

import ast.classbody.FieldDecl;
import ast.classbody.InitDecl.InitDeclBuilder;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr.NameExprBuilder;
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

    /**
     * Creates an {@link ast.classbody.InitDecl} for the current class.
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        Vector<AssignStmt> initParams = new Vector<>();

        // For every field declared in the current class, we will create an
        // assignment statement that will be stored in the generated constructor.
        for(String key : cd.symbolTable.getAllNames().keySet()) {
            if(cd.symbolTable.findName(key).decl().isFieldDecl()) {
                FieldDecl currFieldDecl = cd.symbolTable.findName(key).decl().asFieldDecl();
                initParams.add(
                    new AssignStmtBuilder()
                        .setLHS(
                            new FieldExprBuilder()
                                .setTarget(new ThisStmt())
                                .setAccessExpr(
                                    new NameExprBuilder()
                                        .setName(currFieldDecl.var().name())
                                        .create()
                                )
                                .create()
                        )
                        .setRHS(currFieldDecl.var().init())
                        .setAssignOp(
                            new AssignOpBuilder()
                                .setAssignOperator(AssignType.EQ)
                                .create()
                        )
                        .create());
            }
        }

        cd.setConstructor(
            new InitDeclBuilder()
                .setInits(initParams)
                .create()
        );
    }
}
