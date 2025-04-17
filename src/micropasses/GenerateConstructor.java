package micropasses;

import ast.*;
import ast.class_body.*;
import ast.expressions.NameExpr;
import ast.operators.AssignOp;
import ast.top_level_decls.*;
import ast.statements.AssignStmt;
import token.*;
import utilities.*;
import java.util.HashMap;

/*
MICROPASS #2 : Constructor Generation after type checking
*/
public class GenerateConstructor extends Visitor {

    public void visitClassDecl(ClassDecl cd) {
        SymbolTable declNames = cd.symbolTable;
        HashMap<String, NameNode> fields = declNames.getAllNames();

        Vector<AssignStmt> initParams = new Vector<AssignStmt>();

        for(String key : fields.keySet()) {
            if(fields.get(key).decl().isFieldDecl()) {
                FieldDecl currFieldDecl = fields.get(key).decl().asFieldDecl();
                initParams.add( new AssignStmt(new Token(null,"",new Location()),new NameExpr(new Token(null,"",new Location()),currFieldDecl.var().name()),currFieldDecl.var().init(),new AssignOp(new Token(null,"",new Location()), AssignOp.AssignType.EQ)));
            }
        }

        // A C Minor constructor will not directly be part of the AST,
        // so only a ClassDecl may access its constructor
        InitDecl newInit = new InitDecl(initParams);
        cd.setConstructor(newInit);
    }

}
