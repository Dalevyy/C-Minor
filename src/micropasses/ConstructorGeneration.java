package micropasses;

import ast.classbody.*;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.operators.AssignOp.AssignType;
import ast.topleveldecls.ClassDecl;
import ast.statements.AssignStmt;
import ast.statements.AssignStmt.AssignStmtBuilder;
import utilities.Vector;
import utilities.Visitor;

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
