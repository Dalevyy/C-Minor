package ast.expressions;
import ast.topleveldecls.ClassDecl;
import ast.AST;
import ast.misc.Var;
import token.Token;
import ast.types.ClassType;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the creation of an object.
 * <p><br>
 *     In C Minor, objects will be created the same way as they are in
 *     Java. Since C Minor automatically provides a constructor for all
 *     classes, the user does not need to worry about writing any constructors.
 *     A user simply needs to write which fields they want to initialize when
 *     creating a new object, and the compiler will handle it for them.
 * </p>
 *
 * @author Daniel Levy
 */
public class NewExpr extends Expression {

    public ClassDecl templatedClass;

    /**
     * {@link ClassType} representing the type of the created object.
     */
    private ClassType objectType;

    /**
     * Vector of variables representing every field the user initializes for the object.
     */
    private Vector<Var> initialFields;

    /**
     * Default constructor for {@link NewExpr}.
     */
    public NewExpr() { this(new Token(),null,new Vector<>()); }

    /**
     * Main constructor for {@link NewExpr}.
     * @param metaData Token containing metadata we want to save
     * @param objectType {@link ClassType} to save into {@link #objectType}
     * @param initialFields Vector of variables to save into {@link #initialFields}
     */
    public NewExpr(Token metaData, ClassType objectType, Vector<Var> initialFields) {
        super(metaData);
        this.objectType = objectType;
        this.initialFields = initialFields;

        addChild(this.objectType);
        addChild(this.initialFields);
    }

    /**
     * Checks if the object is created from a templated class.
     * @return Boolean
     */
    public boolean createsFromTemplate() { return objectType.isTemplatedType(); }

    /**
     * Getter for {@link #objectType}.
     * @return {@link ClassType}
     */
    public ClassType getClassType() { return this.objectType; }

    /**
     * Getter for {@link #initialFields}.
     * @return Vector of Variables
     */
    public Vector<Var> getInitialFields() { return this.initialFields; }

    /**
     * Setter for {@link #objectType}.
     * @param objectType {@link ClassType}
     */
    private void setClassType(ClassType objectType) { this.objectType = objectType; }

    /**
     * Setter for {@link #initialFields}.
     * @param initialFields Vector of Variables
     */
    private void setInitialFields(Vector<Var> initialFields) { this.initialFields = initialFields; }

    /**
     * Checks if the current AST node is a {@link NewExpr}.
     * @return Boolean
     */
    public boolean isNewExpr() { return true; }

    /**
     * Type cast method for {@link NewExpr}.
     * @return NewExpr
     */
    public NewExpr asNewExpr() { return this; }

    /**
     * {@code toString} method.
     * @return String representing the name of the class being instantiated.
     */
    @Override
    public String toString() { return this.objectType.toString(); }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        switch(pos) {
            case 0:
                this.objectType = node.asType().asClassType();
                break;
            default:
                this.initialFields.remove(pos-1);
                this.initialFields.add(pos-1,node.asVar());
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link NewExpr}
     */
    @Override
    public AST deepCopy() {
        Vector<Var> initialFields = new Vector<>();
        for(Var v : this.initialFields)
            initialFields.add(v.deepCopy().asVar());

        return new NewExprBuilder()
                   .setMetaData(this)
                   .setClassType(this.objectType.deepCopy().asType().asClassType())
                   .setInitialFields(initialFields)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitNewExpr(this); }

    /**
     * Internal class that builds a {@link NewExpr} object.
     */
    public static class NewExprBuilder extends NodeBuilder {

        /**
         * {@link NewExpr} object we are building.
         */
        private final NewExpr ne = new NewExpr();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return NewExprBuilder
         */
        public NewExprBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the new expression's {@link #objectType}.
         * @param objectType ClassType representing the object being instantiated.
         * @return NewExprBuilder
         */
        public NewExprBuilder setClassType(ClassType objectType) {
            ne.setClassType(objectType);
            return this;
        }

        /**
         * Sets the new expression's {@link #initialFields}.
         * @param initialFields Vector of variables representing every field that is initialized by the user
         * @return NewExprBuilder
         */
        public NewExprBuilder setInitialFields(Vector<Var> initialFields) {
            ne.setInitialFields(initialFields);
            return this;
        }

        /**
         * Creates a {@link NewExpr} object.
         * @return {@link NewExpr}
         */
        public NewExpr create() {
            super.saveMetaData(ne);
            ne.addChild(ne.objectType);
            ne.addChild(ne.initialFields);
            return ne;
        }
    }
}
