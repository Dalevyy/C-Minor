package ast.topleveldecls;

import ast.*;
import ast.classbody.*;
import ast.misc.*;
import ast.types.*;
import token.*;
import utilities.*;

/*
____________________________ ClassDecl ____________________________
___________________________________________________________________
*/
public class ClassDecl extends TopLevelDecl implements NameDecl {

    public SymbolTable symbolTable;
    public Modifiers mod;

    private Name name;
    private Vector<Typeifier> typeParams; // Only used if using a templated class
    private ClassType superClass;
    private ClassBody body;
    private InitDecl constructor;

    private Vector<Name> inheritedClasses;

    public ClassDecl() { this(new Token(),null,null,new Vector<>(),null,null); }
    public ClassDecl(Token t, Modifier m, Name n, ClassBody b) { this(t,m,n,null,null,b); }

    public ClassDecl(Token t, Modifier m, Name n, Vector<Typeifier> tp, ClassType sc, ClassBody b) {
        super(t);
        this.mod = new Modifiers(m);
        this.name = n;
        this.typeParams = tp;
        this.superClass = sc;
        this.body = b;
        this.inheritedClasses = new Vector<>();

        addChild(this.name);
        addChild(this.typeParams);
        addChild(this.superClass);
        addChild(this.body);
        setParent();
    }

    public Name name() { return name; }
    public Vector<Typeifier> typeParams() { return typeParams; }
    public ClassType superClass() { return superClass; }
    public ClassBody classBlock() { return body; }

    public AST getDecl() { return this; }
    public String getDeclName() { return name.toString(); }

    public void setConstructor(InitDecl ind) { this.constructor = ind; }
    public InitDecl constructor() { return this.constructor; }

    public void addBaseClass(Name n) { this.inheritedClasses.add(n); }
    public Vector<Name> getInheritedClasses() { return this.inheritedClasses; }

    public boolean inherits(String sup) {
        for(Name sub : this.inheritedClasses)
            if(sub.toString().equals(sup))
                return true;
        return false;
    }

    /**
     * Checks if the current class represents a template.
     * @return Boolean
     */
    public boolean isTemplate() { return !typeParams.isEmpty();}

    public boolean isClassDecl() { return true; }
    public ClassDecl asClassDecl() { return this; }

    public void removeTypeParams(){
        for(Typeifier tp : this.typeParams)
            symbolTable.removeName(tp.toString());
        this.typeParams = new Vector<>();
    }

    @Override
    public String header() {
        int endColumn;
        if(!typeParams.isEmpty())
            endColumn = typeParams.top().location.end.column;
        else
            endColumn = name.location.end.column;
        return  startLine() + "| " + text.substring(0,endColumn) + "\n";
    }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public AST deepCopy() {
        ClassDeclBuilder cdb = new ClassDeclBuilder();
        Vector<Typeifier> typeParams = new Vector<>();

        for(Typeifier tp : this.typeParams)
            typeParams.add(tp.deepCopy().asTypeifier());

        if(this.superClass != null)
            cdb.setSuperClass(this.superClass.deepCopy().asType().asClassType());

        return cdb.setMetaData(this)
                  .setMods(this.mod)
                  .setClassName(this.name.deepCopy().asName())
                  .setTypeArgs(typeParams)
                  .setClassBody(this.body.deepCopy().asClassBody())
                  .create();
    }

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
            super.setMetaData(node);
            return this;
        }

        /**
         * Sets the class declaration's {@link #mod}.
         * @param mods List of modifiers that is applied to the current class
         * @return ClassDeclBuilder
         */
        public ClassDeclBuilder setMods(Modifiers mods) {
            cd.mod = mods;
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
        public ClassDeclBuilder setTypeArgs(Vector<Typeifier> typeArgs) {
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
            if(cd.symbolTable != null) {
                for(FieldDecl fd : body.getFields())
                    cd.symbolTable.addName(fd.toString(),fd);
                for(MethodDecl md : body.getMethods())
                    cd.symbolTable.addName(md+"/"+md.paramSignature(),md);
            }
            return this;
        }

        /**
         * Creates a {@link ClassDecl} object.
         * @return {@link ClassDecl}
         * */
        public ClassDecl create() {
            super.saveMetaData(cd);
            cd.addChild(cd.name);
            cd.addChild(cd.typeParams);
            cd.addChild(cd.body);
            return cd;
        }
    }
}
