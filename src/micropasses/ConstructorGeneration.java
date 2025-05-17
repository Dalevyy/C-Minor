package micropasses;

import ast.classbody.FieldDecl;
import ast.classbody.InitDecl;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.operators.AssignOp.AssignType;
import ast.topleveldecls.ClassDecl;
import ast.statements.AssignStmt;
import ast.statements.AssignStmt.AssignStmtBuilder;
import utilities.Vector;
import utilities.Visitor;

/**
 * Micropass #6
 * <br><br>
 * In C Minor, we do not allow a user to create class constructors. Instead, we
 * will provide a constructor for the user, and this pass will generate a constructor
 * after type checking is completed. In C Minor, a constructor is represented by an
 * <code>InitDecl</code> node and is not directly part of an <code>AST</code>. The
 * constructor will also only contain <code>AssignStmt</code> nodes.
 *
 * @author Daniel Levy
 */
public class ConstructorGeneration extends Visitor {

    public void visitClassDecl(ClassDecl cd) {
        Vector<AssignStmt> initParams = new Vector<>();

        for(String key : cd.symbolTable.getAllNames().keySet()) {
            if(cd.symbolTable.findName(key).decl().isFieldDecl()) {
                FieldDecl currFieldDecl = cd.symbolTable.findName(key).decl().asFieldDecl();
                initParams.add(
                    new AssignStmtBuilder()
                            .setLHS(
                                new FieldExprBuilder()
                                        .setTarget(new NameExpr("this"))
                                        .setAccessExpr(new NameExpr(currFieldDecl.toString()))
                                        .createFieldExpr()
                            )
                            .setRHS(currFieldDecl.var().init())
                            .setAssignOp(AssignType.EQ)
                            .createAssignStmt()
                );
            }
        }
        cd.setConstructor(new InitDecl(initParams));
    }
}
