package ast.expressions;

import ast.AST;
import token.Token;
import ast.types.Type;
import utilities.Visitor;

/**
 * An {@link AST} node class representing a cast expression.
 * <p><br>
 *     In C Minor, there are 3 primary cast expressions: {@code Int()},
 *     {@code Real()}, and {@code Char()}. {@code Bool()} and {@code String()}
 *     are also supported in a limited way. Since C Minor does not support
 *     type coercion, a user has to explicitly type cast values when working
 *     with mixed type expressions. All of the potential type errors will be
 *     handled by the {@link typechecker.TypeChecker}.
 * </p>
 * @author Daniel Levy
 */
public class CastExpr extends Expression {

    /**
     * Type that the {@link #castExpr} will be casted into.
     */
    private Type castType;

    /**
     * Expression that will be casted.
     */
    private Expression castExpr;

    /**
     * Default constructor for {@link CastExpr}.
     */
    public CastExpr() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link BinaryExpr}.
     * @param metaData Token containing metadata we want to save
     * @param castType Type to save into {@link #castType}
     * @param castExpr Expression to save into {@link #castExpr}
     */
    public CastExpr(Token metaData, Type castType, Expression castExpr) {
        super(metaData);
        this.castType = castType;
        this.castExpr = castExpr;

        addChild(this.castExpr);
    }

    /**
     * Getter for {@link #castType}.
     * @return Type
     */
    public Type getCastType() { return castType; }

    /**
     * Getter for {@link #castExpr}.
     * @return Expression
     */
    public Expression getCastExpr() { return castExpr; }

    /**
     * Setter for {@link #castType}.
     * @param castType Type
     */
    private void setCastType(Type castType) { this.castType = castType; }

    /**
     * Setter for {@link #castExpr}.
     * @param castExpr Expression
     */
    private void setCastExpr(Expression castExpr) { this.castExpr = castExpr; }

    /**
     * Checks if the current AST node is a {@link CastExpr}.
     * @return Boolean
     */
    public boolean isCastExpr() { return true; }

    /**
     * Type cast method for {@link CastExpr}
     * @return CastExpr
     */
    public CastExpr asCastExpr() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                castType = node.asType();
                break;
            case 1:
                castExpr = node.asExpression();
                break;
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link CastExpr}
     */
    @Override
    public AST deepCopy() {
        return new CastExprBuilder()
                   .setMetaData(this)
                   .setCastType(this.getCastType().deepCopy().asType())
                   .setCastExpr(this.getCastExpr().deepCopy().asExpression())
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitCastExpr(this); }

    /**
     * Internal class that builds a {@link CastExpr} object.
     */
    public static class CastExprBuilder extends NodeBuilder {

        /**
         * {@link CastExpr} object we are building.
         */
        private final CastExpr ce = new CastExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return CastExprBuilder
         */
        public CastExprBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the cast expression's {@link #castType}.
         * @param castType Type that the {@link #castExpr} will be casted into
         * @return CastExprBuilder
         */
        public CastExprBuilder setCastType(Type castType) {
            ce.setCastType(castType);
            return this;
        }

        /**
         * Sets the cast expression's {@link #castExpr}.
         * @param castExpr Expression that will be type casted
         * @return CastExprBuilder
         */
        public CastExprBuilder setCastExpr(Expression castExpr) {
            ce.setCastExpr(castExpr);
            return this;
        }

        /**
         * Creates a {@link CastExpr} object.
         * @return {@link CastExpr}
         */
        public CastExpr create() {
            super.saveMetaData(ce);
            ce.addChild(ce.castExpr);
            return ce;
        }
    }
}
