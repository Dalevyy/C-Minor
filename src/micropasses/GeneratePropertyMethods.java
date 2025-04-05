package micropasses;

import ast.*;
import ast.expressions.FieldExpr;
import ast.expressions.NameExpr;
import ast.Modifier.Mods;
import ast.class_body.*;
import ast.operators.AssignOp;
import ast.statements.*;
import ast.top_level_decls.ClassDecl;
import ast.types.VoidType;
import utilities.Visitor;

public class GeneratePropertyMethods extends Visitor {

    // This method is responsible for creating a setter for the field
    // A setter will just be a MethodDecl with an AssignStmt
    public MethodDecl createSetter(FieldDecl fd) {
        Vector<Modifier> mods = new Vector<>();
        mods.append(new Modifier(Mods.PUBLIC));
        Name n = new Name("set_"+fd);
        Vector<ParamDecl> param = new Vector<>();
        param.append(new ParamDecl(new Modifier(Mods.IN),new Name("param" + fd),fd.type()));
        BlockStmt b = new BlockStmt();

        MethodDecl setter = new MethodDecl(mods,n,param,new VoidType(),b);

        AssignStmt as = new AssignStmt(new FieldExpr(new NameExpr(new Name("this")), new NameExpr(new Name(fd.toString()))),
                                       new NameExpr(new Name("param" + fd)),
                                       new AssignOp(AssignOp.AssignType.EQ));

        b.addStmt(as);

        return setter;
    }

    // This method is responsible for creating a getter for the field.
    // A getter will just be a MethodDecl with a ReturnStmt
    public MethodDecl createGetter(FieldDecl fd) {
        Vector<Modifier> mods = new Vector<>();
        mods.append(new Modifier(Mods.PUBLIC));
        Name n = new Name("get_"+fd);
        Vector<ParamDecl> param = new Vector<>();

        ReturnStmt rs = new ReturnStmt(new FieldExpr(new NameExpr(new Name("this")),
                                                     new NameExpr(new Name(fd.toString()))));
        Vector<Statement> stmt = new Vector<>();
        stmt.append(rs);

        BlockStmt b = new BlockStmt(stmt);

        MethodDecl getter = new MethodDecl(mods,n,param,fd.type(),b);

        return getter;
    }

    public void visitClassDecl(ClassDecl cd) {
        Vector<FieldDecl> fields = cd.classBlock().fieldDecls();

        for(int i = 0; i < fields.size(); i++) {
            FieldDecl currField = fields.get(i);
            if(currField.mod.isProperty()) {
                // First, create the setter method
                cd.classBlock().methodDecls().insert(0,createSetter(currField));
                // Then, create the getter method
                cd.classBlock().methodDecls().insert(0,createGetter(currField));
            }
        }
    }
}
