package micropasses;

import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.classbody.MethodDecl.MethodDeclBuilder;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.NameExpr;
import ast.Modifier;
import ast.Modifier.Mods;
import ast.operators.AssignOp.AssignType;
import ast.ParamDecl.ParamDeclBuilder;
import ast.statements.AssignStmt.AssignStmtBuilder;
import ast.statements.BlockStmt.BlockStmtBuilder;
import ast.statements.ReturnStmt.ReturnStmtBuilder;
import ast.topleveldecls.ClassDecl;
import ast.types.VoidType;
import utilities.Vector;
import utilities.Visitor;

/**
 * Micropass #2
 * <br><br>
 * Before we start name checking, we need to generate <code>MethodDecl</code> nodes
 * for all fields declared with the <code>property</code> modifier. This automatically
 * generates a setter,<code> set_x</code>, and a getter, <code>get_x</code> for a field
 * <i>x</i>. We need to make sure this is done prior to name checking or else we will
 * produce incorrect scope errors.
 * @author Daniel Levy
 */
public class PropertyMethodGeneration extends Visitor {

    public MethodDecl createSetter(FieldDecl fd) {
        return new MethodDeclBuilder()
                .setMods(new Vector<>(new Modifier(Mods.PUBLIC)))
                .setName("set_"+fd)
                .setParams(new Vector<>(
                    new ParamDeclBuilder()
                        .setModifier(Mods.IN)
                        .setName("param"+fd)
                        .setType(fd.type())
                        .createParamDecl())
                )
                .setReturnType(new VoidType())
                .setBlockStmt(
                    new BlockStmtBuilder()
                        .addStmt(
                            new AssignStmtBuilder()
                                .setLHS(
                                    new FieldExprBuilder()
                                            .setTarget(new NameExpr("this"))
                                            .setAccessExpr(new NameExpr(fd.toString()))
                                            .createFieldExpr()
                                )
                                .setRHS(new NameExpr("param"+fd))
                                .setAssignOp(AssignType.EQ)
                                .createAssignStmt()
                        )
                        .createBlockStmt()
                )
                .createMethodDecl();
    }

    public MethodDecl createGetter(FieldDecl fd) {
        return new MethodDeclBuilder()
                        .setMods(new Vector<>(new Modifier(Mods.PUBLIC)))
                        .setName("get_"+fd)
                        .setParams(new Vector<>())
                        .setReturnType(fd.type())
                        .setBlockStmt(
                            new BlockStmtBuilder()
                                .addStmt(
                                    new ReturnStmtBuilder()
                                        .setReturnExpr(new FieldExprBuilder()
                                                .setTarget(new NameExpr("this"))
                                                .setAccessExpr(new NameExpr(fd.toString()))
                                                .createFieldExpr()
                                        )
                                        .createReturnStmt()
                                )
                                .createBlockStmt()
                        )
                        .createMethodDecl();
    }

    public void visitClassDecl(ClassDecl cd) {
        Vector<FieldDecl> fields = cd.classBlock().fieldDecls();

        for(FieldDecl fd : cd.classBlock().fieldDecls()) {
            if(fd.mod.isProperty()) {
                cd.classBlock().methodDecls().add(createSetter(fd));
                cd.classBlock().methodDecls().add(createGetter(fd));
            }
        }
    }
}
