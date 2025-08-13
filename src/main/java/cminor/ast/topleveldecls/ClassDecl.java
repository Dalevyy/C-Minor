package cminor.ast.topleveldecls;

import cminor.ast.AST;
import cminor.ast.classbody.ClassBody;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.InitDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.misc.*;
import cminor.ast.types.ClassType;
import cminor.token.Token;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link TopLevelDecl} node that represents a class.
 * @author Daniel Levy
 */
public class ClassDecl extends TopLevelDecl implements NameDecl, ScopeDecl {

    /**
     * The scope that the class opens.
     */
    private SymbolTable scope;

    /**
     * The name of the class.
     */
    private Name name;

    /**
     * The type parameters of the class. This is only set if the class is a template.
     */
    private Vector<TypeParam> typeParams;

    /**
     * The class that is inherited by the current class. This is only set if the class uses inheritance.
     */
    private ClassType superClass;

    /**
     * The {@link ClassBody} of the current class.
     */
    private ClassBody body;

    /**
     * The constructor of the current class. This will be set by the {@link cminor.micropasses.ConstructorGeneration} pass.
     */
    private InitDecl constructor;

    private final Vector<Name> inheritedClasses;    // idk lol

    /**
     * {@link Modifier} containing the meta information about the class.
     */
    public Modifier mod;

    /**
     * Default constructor for {@link ClassDecl}.
     */
    public ClassDecl() { this(new Token(),null,null,new Vector<>(),null,null); }

    /**
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param mod {@link Modifier} to store into {@link #mod}.
     * @param name {@link Name} to store into {@link #name}.
     * @param typeParams {@link Vector} of type parameters to store into {@link #typeParams}.
     * @param superClass {@link ClassType} to store into {@link #superClass}.
     * @param body {@link ClassBody} to store into {@link #body}.
     */
    public ClassDecl(Token metaData, Modifier mod, Name name,
                     Vector<TypeParam> typeParams, ClassType superClass, ClassBody body) {
        super(metaData);

        this.mod = mod;
        this.name = name;
        this.typeParams = typeParams;
        this.superClass = superClass;
        this.body = body;
        this.inheritedClasses = new Vector<>();

        addChildNode(this.typeParams);
        addChildNode(this.body);
    }

    /**
     * Checks if the current class represents a template.
     * @return {@code True} if the class is a template, {@code False} otherwise.
     */
    public boolean isTemplate() { return !typeParams.isEmpty();}

    /**
     * Getter method for {@link #name}.
     * @return {@link Name}
     */
    public Name getName() { return name; }

    /**
     * Getter method for {@link #typeParams}.
     * @return {@link Vector} of type parameters
     */
    public Vector<TypeParam> getTypeParams() { return typeParams; }

    /**
     * Getter method for {@link #superClass}.
     * @return {@link ClassType}
     */
    public ClassType getSuperClass() { return superClass; }

    /**
     * Getter method for {@link #body}.
     * @return {@link ClassBody}
     */
    public ClassBody getClassBody() { return body; }

    /**
     * Getter method for {@link #constructor}.
     * @return {@link InitDecl}
     */
    public InitDecl getConstructor() { return constructor; }

    /**
     * Sets the {@link #constructor} field during the {@link micropasses.ConstructorGeneration} pass.
     * @param init The {@link InitDecl} we generated for this class.
     */
    public void setConstructor(InitDecl init) { constructor = (constructor == null) ? init : constructor; }

    public void addBaseClass(Name n) { inheritedClasses.add(n); }
    public Vector<Name> getInheritedClasses() { return inheritedClasses; }

    public boolean inherits(String sup) {
        for(Name sub : this.inheritedClasses)
            if(sub.toString().equals(sup))
                return true;
        return false;
    }

    public void removeTypeParams(){
        for(TypeParam tp : this.typeParams);
        // scope.removeName(tp.toString());
        this.typeParams = new Vector<>();
    }

    public boolean containsMethod(MethodDecl md) {
        for(MethodDecl baseMethod : body.getMethods())
            if(baseMethod.equals(md))
                return true;
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public AST getDecl() { return this; }

    /**
     * {@inheritDoc}
     */
    public String getDeclName() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    public boolean isMethod() { return false; }

    /**
     * {@inheritDoc}
     */
    public boolean isFunction() { return false; }

    /**
     * {@inheritDoc}
     */
    public SymbolTable getScope() { return (scope != null) ? scope : null; }

    /**
     * {@inheritDoc}
     */
    public void setScope(SymbolTable newScope) { scope = (scope == null) ? newScope : scope; }

    /**
     * {@inheritDoc}
     */
    public boolean isClassDecl() { return true; }

    /**
     * {@inheritDoc}
     */
    public ClassDecl asClassDecl() { return this; }

    /**
     * Returns the name of the class.
     * @return String representation of the class name.
     */
    @Override
    public String toString() { return name.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String header() {
        StringBuilder sb = new StringBuilder();

        sb.append(getLocation().start.line)
          .append("| ")
          .append(text, 0, text.indexOf("{"));

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(int pos, AST node) { throw new RuntimeException("A class can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        ClassDeclBuilder cb = new ClassDeclBuilder();
        Vector<TypeParam> typeParams = new Vector<>();

        for(TypeParam tp : this.typeParams)
            typeParams.add(tp.deepCopy().asSubNode().asTypeParam());

        if(superClass != null)
            cb.setSuperClass(superClass.deepCopy().asType().asClassType());

        return cb.setMetaData(this)
                  .setMods(mod)
                  .setClassName(name.deepCopy().asSubNode().asName())
                  .setTypeArgs(typeParams)
                  .setClassBody(body.deepCopy().asClassNode().asClassBody())
                  .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitClassDecl(this); }

    /**
     * Internal class that builds a {@link ClassDecl} object.
     */
    public static class ClassDeclBuilder extends NodeBuilder {

        /**
         * {@link ClassDecl} object we are building.
         */
        private final ClassDecl cd = new ClassDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setMetaData(AST node) {
            super.setMetaData(cd, node);
            return this;
        }

        /**
         * Sets the class declaration's {@link #mod}.
         * @param mod List of modifiers that is applied to the current class
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setMods(Modifier mod) {
            cd.mod = mod;
            return this;
        }

        /**
         * Sets the class declaration's {@link #name}.
         * @param name Name representing the name of the class
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setClassName(Name name) {
            cd.name = name;
            return this;
        }

        /**
         * Sets the class declaration's {@link #typeParams}.
         * @param typeArgs Vector of typeifiers that the class has
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setTypeArgs(Vector<TypeParam> typeArgs) {
            cd.typeParams = typeArgs;
            return this;
        }

        /**
         * Sets the class declaration's {@link #superClass}.
         * @param ct Class type representing the super class
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setSuperClass(ClassType ct) {
            cd.superClass = ct;
            return this;
        }

        /**
         * Sets the class declaration's {@link #body}.
         * @param body Type representing the body of the class
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setClassBody(ClassBody body) {
            cd.body = body;
            if(cd.scope != null) {
                for(FieldDecl fd : body.getFields())
                    cd.scope.addName(fd.toString(),fd);
                for(MethodDecl md : body.getMethods())
                    cd.scope.addName(md+"/"+md.getParamSignature(),md);
            }
            return this;
        }

        /**
         * Creates a {@link ClassDecl} object.
         * @return {@link ClassDecl}
         * */
        public ClassDecl create() {
            cd.addChildNode(cd.typeParams);
            cd.addChildNode(cd.body);
            return cd;
        }
    }
}
