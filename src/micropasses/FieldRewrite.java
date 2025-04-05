package micropasses;

import ast.*;
import ast.expressions.FieldExpr;
import ast.expressions.NameExpr;
import ast.top_level_decls.ClassDecl;
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
                FieldExpr fe = new FieldExpr(new NameExpr(new Name("this")), ne);
                fe.copy(ne);
                fe.setParent();
                for(int i = 0; i < ne.getParent().children.size(); i++) {
                    AST currNode = ne.getParent().children.get(i);
                    if(currNode.isExpression() && currNode.asExpression().isNameExpr()) {
                        ne.getParent().children.set(i,fe);
                        break;
                    }
                }
            }
        }
    }
}
