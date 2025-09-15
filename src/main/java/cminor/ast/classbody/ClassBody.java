package cminor.ast.classbody;

import cminor.ast.AST;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link ClassNode} representing the body of a {@link ClassDecl}.
 * <p>
 *     This is an organizational class to help when visiting a {@link ClassDecl}.
 *     A class body contains all the fields and methods associated with a class.
 * </p>
 * @author Daniel Levy
 */
public class ClassBody extends ClassNode {

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
    public ClassBody() { this(new Token(), new Vector<>(), new Vector<>()); }

    /**
     * Main constructor for {@link ClassBody}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param fields {@link Vector<FieldDecl>} to store into {@link #fields}.
     * @param methods {@link Vector<MethodDecl>} to store into {@link #methods}.
     */
    public ClassBody(Token metaData, Vector<FieldDecl> fields, Vector<MethodDecl> methods) {
        super(metaData);

        this.fields = fields;
        this.methods = methods;

        addChildNode(this.fields);
        addChildNode(this.methods);
    }

    /**
     * Gets {@link #fields}.
     * @return {@link Vector} containing {@link FieldDecl}.
     */
    public Vector<FieldDecl> getFields() { return fields; }

    /**
     * Gets {@link #methods}.
     * @return {@link Vector} containing {@link MethodDecl}.
     */
    public Vector<MethodDecl> getMethods() { return methods; }

    /**
     * {@inheritDoc}
     */
    public ClassDecl getClassDecl() { return parent.asTopLevelDecl().asClassDecl(); }

    /**
     * {@inheritDoc}
     */
    public boolean isClassBody() { return true; }

    /**
     * {@inheritDoc}
     */
    public ClassBody asClassBody() { return this; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        if(pos < fields.size()) {
            fields.remove(pos);
            fields.add(pos, node.asClassNode().asFieldDecl());
        }
        else {
            pos -= this.methods.size();
            methods.remove(pos);
            methods.add(pos, node.asClassNode().asMethodDecl());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<FieldDecl> fields = new Vector<>();
        Vector<MethodDecl> methods = new Vector<>();

        for(FieldDecl fd : this.fields)
            fields.add(fd.deepCopy().asClassNode().asFieldDecl());
        for(MethodDecl md : this.methods)
            methods.add(md.deepCopy().asClassNode().asMethodDecl());

        return new ClassBodyBuilder()
                   .setMetaData(this)
                   .setFields(fields)
                   .setMethods(methods)
                   .create();
    }

    /**
     * {@inheritDoc}
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
         * @see cminor.ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ClassBodyBuilder}.
         */
        public ClassBodyBuilder setMetaData(AST node) {
            super.setMetaData(cb, node);
            return this;
        }

        /**
         * Sets the class body's {@link #fields}.
         * @param fields {@link Vector} of {@link FieldDecl} representing every field in the class.
         * @return Current instance of {@link ClassBodyBuilder}.
         */
        public ClassBodyBuilder setFields(Vector<FieldDecl> fields) {
            cb.fields = fields;
            return this;
        }

        /**
         * Sets the class body's {@link #methods}.
         * @param methods {@link Vector} of {@link MethodDecl} representing every method in the class.
         * @return Current instance of {@link ClassBodyBuilder}.
         */
        public ClassBodyBuilder setMethods(Vector<MethodDecl> methods) {
            cb.methods = methods;
            return this;
        }

        /**
         * Creates a {@link ClassBody} object.
         * @return {@link ClassBody}
         */
        public ClassBody create() {
            cb.addChildNode(cb.fields);
            cb.addChildNode(cb.methods);
            return cb;
        }
    }
}
