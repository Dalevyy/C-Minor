package micropasses;

import ast.*;
import ast.expressions.FieldExpr;
import ast.expressions.NameExpr;
import ast.Modifier.Mods;
import ast.classbody.*;
import ast.operators.AssignOp;
import ast.statements.*;
import ast.topleveldecls.ClassDecl;
import ast.types.VoidType;
import utilities.Vector;
import utilities.Visitor;

/*
    Micropass #2: Generating Class Property Methods

    This micropass will create a getter and a setter for all fields
    denoted with the `property` modifier. This will be done before
    name checking to ensure we know a field has a valid getter/setter.
*/
public class GeneratePropertyMethods extends Visitor {

    // This method is responsible for creating a setter for the field
    // A setter will just be a MethodDecl with an AssignStmt
    public MethodDecl createSetter(FieldDecl fd) {
        Vector<Modifier> mods = new Vector<>();
        mods.add(new Modifier(Mods.PUBLIC));
        Name n = new Name("set_"+fd);
        Vector<ParamDecl> param = new Vector<>();
        param.add(new ParamDecl(new Modifier(Mods.IN),new Name("param" + fd),fd.type()));
        BlockStmt b = new BlockStmt();

        MethodDecl setter = new MethodDecl(mods,n,param,new VoidType(),b);

        AssignStmt as = new AssignStmt(new FieldExpr(new NameExpr("this"), new NameExpr(fd.toString())),
                                       new NameExpr("param" + fd),
                                       new AssignOp(AssignOp.AssignType.EQ));

        b.addStmt(as);

        return setter;
    }

    // This method is responsible for creating a getter for the field.
    // A getter will just be a MethodDecl with a ReturnStmt
    public MethodDecl createGetter(FieldDecl fd) {
        Vector<Modifier> mods = new Vector<>();
        mods.add(new Modifier(Mods.PUBLIC));
        Name n = new Name("get_"+fd);
        Vector<ParamDecl> param = new Vector<>();

        ReturnStmt rs = new ReturnStmt(new FieldExpr(new NameExpr("this"),
                                                     new NameExpr(fd.toString())));
        Vector<Statement> stmt = new Vector<>();
        stmt.add(rs);

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
                cd.classBlock().methodDecls().add(0,createSetter(currField));
                // Then, create the getter method
                cd.classBlock().methodDecls().add(0,createGetter(currField));
            }
        }
    }
}
