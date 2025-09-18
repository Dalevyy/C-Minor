package cminor.micropasses;

import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.InitDecl;
import cminor.ast.classbody.InitDecl.InitDeclBuilder;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.NameExpr.NameExprBuilder;
import cminor.ast.expressions.NewExpr;
import cminor.ast.expressions.ThisStmt;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.operators.AssignOp.AssignOpBuilder;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.ast.statements.AssignStmt;
import cminor.ast.statements.AssignStmt.AssignStmtBuilder;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.ImportDecl;
import cminor.utilities.SymbolTable;
import cminor.utilities.SymbolTable.NameIterator;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Visitor} that generates class constructors.
 * <p>
 *     C Minor does not require (nor allow) a programmer to create constructors
 *     for their classes. Instead, the compiler will generate a single constructor
 *     for each class. This will be represented as an {@link cminor.ast.classbody.InitDecl}
 *     internally. A C Minor constructor will just be a sequence of {@link AssignStmt}s
 *     that initialize every field to a class to a default value unless the user
 *     specified a different initial value within a {@link cminor.ast.expressions.NewExpr}
 * </p>
 * @author Daniel Levy
 */
public class ConstructorGenerator extends Visitor {

    /**
     * Keeps track of a list of instantiated classes that we have created constructors for.
     * This prevents us from creating multiple constructors for the same class!
     */
    private static final Vector<String> classes = new Vector<>();

    /**
     * Creates a constructor for the current {@link ClassDecl}.
     * <p>
     *     After type checking is complete, we will generate an
     *     {@link InitDecl} for each class declared in a user's program.
     * </p>
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        // Ignore template classes since objects are never instantiated from the template!
        if(cd.isTemplate())
            return;

        Vector<AssignStmt> assignStmts = new Vector<>();

        NameIterator it = new SymbolTable.NameIterator(cd.getScope());
        // For each field, create an assignment statement!
        while(it.hasNext()) {
            FieldDecl field = it.next().asClassNode().asFieldDecl();

            AssignStmt as =
                new AssignStmtBuilder()
                    .setLHS(
                        new FieldExprBuilder()
                            .setTarget(new ThisStmt())
                            .setAccessExpr(
                                new NameExprBuilder()
                                    .setName(field.getVariableName())
                                    .create()
                            )
                            .create()
                    )
                    .setRHS(field.getInitialValue())
                    .setAssignOp(
                        new AssignOpBuilder()
                            .setAssignOperator(AssignType.EQ)
                            .create()
                    )
                    .create();

            assignStmts.add(as);
        }

        InitDecl id = new InitDeclBuilder()
                          .setInits(assignStmts)
                          .create();

        cd.setConstructor(id);
    }

    /**
     * Executes the constructor generator in compilation mode.
     * <p>
     *     The constructor generator is only concerned with classes, so we will only
     *     visit the classes of the current {@link CompilationUnit} and any imported classes.
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
     * Generates a constructor for any classes that were instantiated.
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        if(ne.createsFromTemplate() && !classes.contains(ne.type.getTypeName())) {
            ne.getInstantiatedClass().visit(this);
            classes.add(ne.type.getTypeName());
        }
    }
}
