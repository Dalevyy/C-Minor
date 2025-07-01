package ast.classbody;

import ast.AST;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An {@link AST} node class representing the body of a class.
 * <p><br>
 *     For every C Minor class, a user has to first declare a
 *     series of fields that an object will store followed by
 *     a series of methods that can operate on the object. This
 *     class is used more as internal organization when visiting
 *     {@link ast.topleveldecls.ClassDecl}s.
 * </p>
 * @author Daniel Levy
 */
public class ClassBody extends AST {

    /**
     * Vector containing every field declared in the current class.
     */
    private Vector<FieldDecl> fields;

    /**
     * Vector containing every method declared in the current class.
     */
    private Vector<MethodDecl> methods;

    /**
     * Default constructor for {@link ClassBody}.
     */
    public ClassBody() { this(new Token(),null,null); }

    /**
     * Main constructor for {@link ClassBody}.
     * @param metaData Token containing metadata we want to save
     * @param fields Vector of field declarations to store into {@link #fields}
     * @param methods Vector of method declarations to store into {@link #methods}
     */
    public ClassBody(Token metaData, Vector<FieldDecl> fields, Vector<MethodDecl> methods) {
        super(metaData);
        this.fields = fields;
        this.methods = methods;

        addChild(this.fields);
        addChild(this.methods);
    }

    /**
     * Getter for {@link #fields}.
     * @return Vector of Field Declarations
     */
    public Vector<FieldDecl> getFields() { return fields; }

    /**
     * Getter for {@link #methods}.
     * @return Vector of Method Declarations
     */
    public Vector<MethodDecl> getMethods() { return methods; }

    /**
     * Setter for {@link #fields}.
     * @param fields Vector of Field Declarations
     */
    private void setFields(Vector<FieldDecl> fields) { this.fields = fields; }

    /**
     * Setter for {@link #methods}.
     * @param methods Vector of Method Declarations
     */
    private void setMethods(Vector<MethodDecl> methods) { this.methods = methods; }

    /**
     * Checks if the current AST node is a {@link ClassBody}.
     * @return Boolean
     */
    public boolean isClassBody() { return true; }

    /**
     * Type cast method for {@link ClassBody}.
     * @return ClassBody
     */
    public ClassBody asClassBody() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        if(pos < this.fields.size()) {
            this.fields.remove(pos);
            this.fields.add(pos,node.asFieldDecl());
        }
        else {
            pos -= this.methods.size();
            this.methods.remove(pos);
            this.methods.add(pos,node.asMethodDecl());
        }
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ClassBody}
     */
    @Override
    public AST deepCopy() {
        Vector<FieldDecl> fields = new Vector<>();
        Vector<MethodDecl> methods = new Vector<>();

        for(FieldDecl fd : this.fields)
            fields.add(fd.deepCopy().asFieldDecl());
        for(MethodDecl md : this.methods)
            methods.add(md.deepCopy().asMethodDecl());

        return new ClassBodyBuilder()
                   .setMetaData(this)
                   .setFields(fields)
                   .setMethods(methods)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitClassBody(this); }

    /**
     * Internal class that builds a {@link ClassBody} object.
     */
    public static class ClassBodyBuilder extends NodeBuilder {

        /**
         * {@link ClassBody} object we are building.
         */
        private final ClassBody cb = new ClassBody();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ClassBodyBuilder
         */
        public ClassBodyBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the class body's {@link #fields}.
         * @param fields Vector of field declarations representing every field in the class
         * @return ClassBodyBuilder
         */
        public ClassBodyBuilder setFields(Vector<FieldDecl> fields) {
            cb.setFields(fields);
            return this;
        }

        /**
         * Sets the class body's {@link #methods}.
         * @param methods Vector of method declarations representing every method in the class
         * @return ClassBodyBuilder
         */
        public ClassBodyBuilder setMethods(Vector<MethodDecl> methods) {
            cb.setMethods(methods);
            return this;
        }

        /**
         * Creates a {@link ClassBody} object.
         * @return {@link ClassBody}
         */
        public ClassBody create() {
            super.saveMetaData(cb);
            cb.addChild(cb.fields);
            cb.addChild(cb.methods);
            return cb;
        }
    }
}
