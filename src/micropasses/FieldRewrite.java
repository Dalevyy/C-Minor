package micropasses;

import ast.*;
import ast.expressions.FieldExpr;
import ast.expressions.NameExpr;
import ast.topleveldecls.ClassDecl;
import utilities.Visitor;

public class FieldRewrite extends Visitor {

    private ClassDecl currClass;

    public void visitClassDecl(ClassDecl cd) {
        currClass = cd;
        super.visitClassDecl(cd);
        currClass = null;
    }

    public void visitNameExpr(NameExpr ne) {
        if(currClass.symbolTable.hasNameSomewhere(ne.toString())) {
            NameNode name = currClass.symbolTable.findName(ne.toString());
            if(name.decl().isFieldDecl()) {
                FieldExpr fe = new FieldExpr(new NameExpr("this"), ne);
                fe.copy(ne);
                fe.setParent();
            }
        }
    }
}
