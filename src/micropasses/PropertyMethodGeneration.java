package micropasses;

import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.classbody.MethodDecl.MethodDeclBuilder;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr.NameExprBuilder;
import ast.expressions.ThisStmt;
import ast.misc.Modifier;
import ast.misc.Modifier.Mods;
import ast.misc.Modifiers;
import ast.misc.Name;
import ast.operators.AssignOp;
import ast.operators.AssignOp.AssignType;
import ast.misc.ParamDecl.ParamDeclBuilder;
import ast.statements.AssignStmt.AssignStmtBuilder;
import ast.statements.BlockStmt.BlockStmtBuilder;
import ast.statements.ReturnStmt.ReturnStmtBuilder;
import ast.topleveldecls.ClassDecl;
import ast.types.VoidType;
import utilities.Vector;
import utilities.Visitor;

/**
 * A {@link Visitor} class that generates getters and setters for class fields.
 * <p><br>
 *     C Minor supports the {@code property} attribute which allows a user to
 *     automatically generate a getter and setter for a field. The getter and
 *     setter will be named {@code get_<fieldName>} and {@code set_<fieldName>}
 *     respectively. This will be done prior to name checking to prevent incorrect
 *     scope errors.
 * </p>
 * @author Daniel Levy
 */
public class PropertyMethodGeneration extends Visitor {

    /**
     * Generates a setter method for the current field.
     * @param fd Field Declaration
     * @return Method Declaration
     */
    public MethodDecl createSetter(FieldDecl fd) {
        return new MethodDeclBuilder()
                   .setMods(new Modifiers(new Modifier(Mods.PUBLIC)))
                   .setMethodName(new Name("set_" + fd))
                   .setParams(
                       new Vector<>(
                           new ParamDeclBuilder()
                               .setMod(new Modifiers(new Modifier(Mods.IN)))
                               .setName(new Name("param" + fd))
                               .setType(fd.type())
                               .create()
                       )
                   )
                   .setReturnType(new VoidType())
                   .setBlockStmt(
                        new BlockStmtBuilder()
                            .setStmts(
                                new Vector<>(
                                    new AssignStmtBuilder()
                                        .setLHS(
                                            new FieldExprBuilder()
                                                .setTarget(new ThisStmt())
                                                .setAccessExpr(
                                                    new NameExprBuilder()
                                                        .setName(new Name(fd.toString()))
                                                        .create()
                                                )
                                                .create()
                                        )
                                        .setRHS(
                                            new NameExprBuilder()
                                                .setName(new Name("param" + fd))
                                                .create()
                                        )
                                        .setAssignOp(new AssignOp(AssignType.EQ))
                                        .create()
                                )
                            )
                            .create()
                   )
                   .create();
    }

    /**
     * Generates a getter method for the current field.
     * @param fd Field Declaration
     * @return Method Declaration
     */
    public MethodDecl createGetter(FieldDecl fd) {
        return new MethodDeclBuilder()
                   .setMods(new Modifiers(new Modifier(Mods.PUBLIC)))
                   .setMethodName(new Name("get_" + fd))
                   .setReturnType(fd.type())
                   .setBlockStmt(
                       new BlockStmtBuilder()
                           .setStmts(
                               new Vector<>(
                                   new ReturnStmtBuilder()
                                       .setReturnExpr(
                                           new FieldExprBuilder()
                                               .setTarget(new ThisStmt())
                                               .setAccessExpr(
                                                   new NameExprBuilder()
                                                       .setName(new Name(fd.toString()))
                                                       .create()
                                               )
                                               .create()
                                       )
                                       .create()
                               )
                           )
                           .create()
                   )
                   .create();
    }

    /**
     * Generates a getter and setter for each field marked as {@code property} in the current class.
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        Vector<FieldDecl> fields = cd.classBlock().getFields();

        for(FieldDecl fd : cd.classBlock().getFields()) {
            if(fd.mod.isProperty()) {
                cd.classBlock().getMethods().add(createSetter(fd));
                cd.classBlock().getMethods().add(createGetter(fd));
            }
        }
    }
}
