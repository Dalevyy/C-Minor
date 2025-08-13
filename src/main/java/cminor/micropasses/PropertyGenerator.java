package cminor.micropasses;

import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.classbody.MethodDecl.MethodDeclBuilder;
import cminor.ast.expressions.FieldExpr;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.NameExpr;
import cminor.ast.expressions.NameExpr.NameExprBuilder;
import cminor.ast.expressions.ThisStmt;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.Modifier;
import cminor.ast.misc.Name;
import cminor.ast.misc.ParamDecl;
import cminor.ast.misc.ParamDecl.ParamDeclBuilder;
import cminor.ast.operators.AssignOp;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.ast.statements.AssignStmt;
import cminor.ast.statements.AssignStmt.AssignStmtBuilder;
import cminor.ast.statements.BlockStmt;
import cminor.ast.statements.BlockStmt.BlockStmtBuilder;
import cminor.ast.statements.ReturnStmt;
import cminor.ast.statements.ReturnStmt.ReturnStmtBuilder;
import cminor.ast.statements.Statement;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.ImportDecl;
import cminor.ast.types.VoidType;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Visitor} class that generates getters and setters for class fields.
 * <p>
 *     C Minor supports the {@code property} attribute which allows a user to
 *     automatically generate a getter and setter for a field. The getter and
 *     setter will be named {@code get<fieldName>} and {@code set<fieldName>}
 *     respectively. This micropass executes before the {@link cminor.namechecker.NameChecker}
 *     because we need to make sure these methods are available or else we will
 *     get incorrect scope errors.
 * </p>
 * @author Daniel Levy
 */
public class PropertyGenerator extends Visitor {

    /**
     * A {@link MethodGenerator} responsible for creating a property's methods.
     */
    private final MethodGenerator generator = new MethodGenerator();

    /**
     * Generates a getter and setter for each {@link FieldDecl} in the class marked with {@code property}.
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        for(FieldDecl fd : cd.getClassBody().getFields()) {
            if(fd.mod.isProperty()) {
                cd.getClassBody().getMethods().add(generator.createGetter(fd));
                cd.getClassBody().getMethods().add(generator.createSetter(fd));
            }
        }
    }

    /**
     * Visits and checks every class associated with the current {@link CompilationUnit}.
     * <p>
     *     This is more of an optimization visit to ensure this {@link Visitor} is only executed for
     *     programs that contain classes. We also need to generate properties for any imported classes
     *     as well which this visit will handle.
     * </p>
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        for(ImportDecl id : cu.getImports())
            id.getCompilationUnit().visit(this);

        for(ClassDecl cd : cu.getClasses())
            cd.visit(this);
    }

    /**
     * An internal class that handles the generation of getters and setters.
     */
    private static class MethodGenerator {

        /**
         * Generates a {@code getter} method for the passed field.
         * @param fd The {@link FieldDecl} we will generate a getter for.
         * @return A {@link MethodDecl} representing a getter.
         */
        public MethodDecl createGetter(FieldDecl fd) {
            MethodDeclBuilder builder = new MethodDeclBuilder();

            builder.setModifier(buildModifier());
            builder.setMethodName(new Name("get" + fd));
            builder.setReturnType(fd.getDeclaredType());

            ReturnStmt retStmt = new ReturnStmtBuilder()
                                     .setReturnExpr(buildFieldExpr(fd))
                                     .create();
            builder.setBlockStmt(buildMethodBody(retStmt));

            return builder.create();
        }

        /**
         * Generates a {@code getter} method for a passed field.
         * @param fd The {@link FieldDecl} we will generate a setter for.
         * @return A {@link MethodDecl} representing a setter.
         */
        public MethodDecl createSetter(FieldDecl fd) {
            MethodDeclBuilder builder = new MethodDeclBuilder();

            builder.setModifier(buildModifier());
            builder.setMethodName(new Name("set" + fd));
            builder.setReturnType(new VoidType());

            Modifier passMode = new Modifier();
            passMode.setInMode();

            ParamDecl param = new ParamDeclBuilder()
                                  .setModifier(passMode)
                                  .setName(new Name(fd.toString()))
                                  .setType(fd.getDeclaredType())
                                  .create();
            builder.setParams(new Vector<>(param));

            AssignStmt assign = new AssignStmtBuilder()
                                    .setLHS(buildFieldExpr(fd))
                                    .setRHS(buildNameExpr(fd))
                                    .setAssignOp(new AssignOp(AssignType.EQ))
                                    .create();
            builder.setBlockStmt(buildMethodBody(assign));

            return builder.create();
        }

        /**
         * Creates a {@code public} {@link Modifier}.
         * @return {@link Modifier}
         */
        private Modifier buildModifier() {
            Modifier mod = new Modifier();
            mod.setPublic();
            return mod;
        }

        /**
         * Creates a {@link FieldExpr} for a passed field.
         * @param fd {@link FieldDecl} we will create a {@link FieldDecl} for.
         * @return {@link FieldExpr}
         */
        private FieldExpr buildFieldExpr(FieldDecl fd) {
            return new FieldExprBuilder()
                      .setTarget(new ThisStmt())
                      .setAccessExpr(buildNameExpr(fd))
                      .create();
        }

        /**
         * Creates a {@link NameExpr} representation of a field.
         * @param fd {@link FieldDecl} we will create a {@link NameExpr} for.
         * @return {@link NameExpr}
         */
        private NameExpr buildNameExpr(FieldDecl fd) {
            return new NameExprBuilder()
                      .setName(new Name(fd.toString()))
                      .create();
        }

        /**
         * Creates a {@link BlockStmt} to represent the body of a getter and setter.
         * @param stmt {@link Statement} that is contained inside the body
         * @return {@link BlockStmt} representing the body of a method.
         */
        private BlockStmt buildMethodBody(Statement stmt) {
            return new BlockStmtBuilder()
                       .setStmts(new Vector<>(stmt))
                       .create();
        }
    }
}
