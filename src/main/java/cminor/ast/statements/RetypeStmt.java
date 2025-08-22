package cminor.ast.statements;

import cminor.ast.expressions.Expression;
import cminor.ast.expressions.NewExpr;
import cminor.ast.operators.AssignOp;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * An {@link AssignStmt} for objects.
 * <p>
 *     Since C Minor supports the dynamic casting of objects, this {@link Statement}
 *     acts as a special case of assigning for objects. Any time a user wishes to
 *     store child types into parent types, they need to explicitly retype an object
 *     variable first.
 * </p>
 * @author Daniel Levy
 */
public class RetypeStmt extends AssignStmt {

    /**
     * Main constructor for {@link RetypeStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param object {@link Expression} to store into {@link #LHS}.
     * @param newType {@link Expression} to store into {@link #RHS}.
     */
    public RetypeStmt(Token metaData, Expression object, NewExpr newType) {
        super(metaData, object, newType, new AssignOp(AssignType.EQ));
    }

    /**
     * Getter method for {@link #LHS}.
     * @return {@link Expression}
     */
    public Expression getName() { return this.getLHS(); }

    /**
     * Getter method for {@link #RHS}.
     * <p>
     *     This will cast the {@link #RHS} into a {@link NewExpr}. We don't need to use this
     *     portion of the retype statement until the {@link cminor.typechecker.TypeChecker}, so it's
     *     fine if we do this.
     * </p>
     * @return {@link NewExpr}
     */
    public NewExpr getNewObject() { return this.getRHS().asNewExpr(); }

    /**
     * {@inheritDoc}
     */
    public boolean isRetypeStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public RetypeStmt asRetypeStmt() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitRetypeStmt(this); }
}
