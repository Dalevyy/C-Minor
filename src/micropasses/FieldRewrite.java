package micropasses;

import ast.expressions.FieldExpr;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.topleveldecls.ClassDecl;
import utilities.SymbolTable;
import utilities.Visitor;

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
                                        .setTarget(new NameExpr("this"))
                                        .setAccessExpr(ne)
                                        .createFieldExpr();
                fe.copy(ne);
                fe.setParent();
            }
        }
    }
}
