package ast.expressions;

import ast.AST;
import token.Token;
import utilities.Visitor;


/**
 * An {@link AST} node class representing a constant value.
 * <p><br>
 *     All constant values in C Minor will be represented by this
 *     class. Both {@link ArrayLiteral} and {@link ListLiteral}
 *     constants will be handled separately, so the rest of the constants
 *     will just be a generic {@link Literal} object.
 * </p>
 * @author Daniel Levy
 */
public class Literal extends Expression {

    /**
     * Enum that keeps track of all possible types a literal can represent.
     */
    public enum ConstantType { INT, CHAR, BOOL, ENUM, REAL, STR, TEXT, ARR, LIST }

    /**
     * Type denoting what the literal represents.
     */
    private ConstantType kind;

    /**
     * Default constructor for {@link Literal}.
     */
    public Literal() { this(new Token(),null); }

    /**
     * Constructor to create a default literal value in {@link typechecker.TypeChecker}.
     * @param kind {@link ConstantType} to save into {@link #kind}
     * @param value String representing a default value
     */
    public Literal(ConstantType kind, String value) {
        this(new Token(), kind);
        this.text = value;
    }

    /**
     * Main constructor for {@link Literal}.
     * @param metaData Token containing metadata we want to save
     * @param kind {@link ConstantType} to save into {@link #kind}
     */
    public Literal(Token metaData, ConstantType kind) {
        super(metaData);
        this.kind = kind;
    }

    /**
     * Gets the value of the constant as an int.
     * @return Int
     */
    public int asInt() { return this.kind == ConstantType.INT ? Integer.parseInt(this.text) : '\0'; }

    /**
     * Gets the value of the constant as a char.
     * @return Char
     */
    public char asChar() {
        if(this.kind == ConstantType.CHAR)
            return this.text.charAt(1) == '\\' ? (char) ('\\' + this.text.charAt(2)) : this.text.charAt(1);
        return '\0';
    }

    /**
     * Getter for {@link #kind}.
     * @return {@link ConstantType}
     */
    public ConstantType getConstantKind() { return kind; }

    /**
     * Setter for {@link #kind}.
     * @param kind {@link ConstantType}
     */
    private void setConstantType(ConstantType kind) { this.kind = kind; }

    /**
     * Checks if the current AST node is a {@link Literal}.
     * @return Boolean
     */
    public boolean isLiteral() { return true; }

    /**
     * Type cast method for {@link Literal}
     * @return Literal
     */
    public Literal asLiteral() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("A literal can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link Literal}
     */
    @Override
    public AST deepCopy() {
        return new LiteralBuilder()
                   .setMetaData(this)
                   .setConstantKind(this.kind)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitLiteral(this); }

    /**
     * Internal class that builds a {@link Literal} object.
     */
    public static class LiteralBuilder extends NodeBuilder {

        /**
         * {@link Literal} object we are building.
         */
        private final Literal ll = new Literal();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return LiteralBuilder
         */
        public LiteralBuilder setMetaData(AST node) {
            super.setMetaData(ll,node);
            return this;
        }

        /**
         * Sets the literal's {@link #kind}.
         * @param kind {@link ConstantType} representing the type of the constant.
         * @return LiteralBuilder
         */
        public LiteralBuilder setConstantKind(ConstantType kind) {
            ll.setConstantType(kind);
            return this;
        }

        /**
         * Sets the literal's value.
         * @param value String representing the value of the constant.
         * @return LiteralBuilder
         */
        public LiteralBuilder setValue(String value) {
            ll.text = value;
            return this;
        }

        /**
         * Creates a {@link Literal} object.
         * @return {@link Literal}
         */
        public Literal create() {
            return ll;
        }
    }
}
