package micropasses;

import ast.*;
import ast.class_body.*;
import ast.top_level_decls.*;
import utilities.*;
import java.util.HashMap;

/*
    After name checking is complete, we will do a micropass
    to generate a constructor for every class a user defines.
    This constructor will initialize all the DataDecls that
    belong to a single class either to a default value or a
    value provided by the user.
*/

public class GenerateConstructor extends Visitor {

    public void visitClassDecl(ClassDecl cd) {
        SymbolTable declNames = cd.symbolTable;
        HashMap<String, NameNode> fields = declNames.getVarNames();

        Vector<FieldDecl> initParams = new Vector<FieldDecl>();

        for(String key : fields.keySet()) {
            if(fields.get(key).declName().isFieldDecl()) {
                FieldDecl decl = fields.get(key).declName().asFieldDecl();
                initParams.append(decl);;
            }
        }

        // A C Minor constructor will not directly be part of the AST,
        // so only a ClassDecl may access its constructor
        InitDecl newInit = new InitDecl(initParams);
        cd.setConstructor(newInit);
    }

}
