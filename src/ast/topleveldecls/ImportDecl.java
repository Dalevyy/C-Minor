package ast.topleveldecls;

import ast.AST;
import ast.misc.Compilation;
import ast.misc.Name;
import token.Token;
import utilities.Visitor;

public class ImportDecl extends TopLevelDecl{

    public Name fileName;
    private Compilation compilationUnit;

    public ImportDecl() { this(new Token(),null); }
    public ImportDecl(Token metaData, Name n) {
        super(metaData);
        // Removing start/end quotes from file name for cleaner error outputting.
        n.setName(n.toString().substring(1,n.toString().length()-1));
        fileName = n;

        addChild(fileName);
        setParent();
    }

    public boolean isImport() { return true; }
    public ImportDecl asImport() { return this; }

    public void addCompilationUnit(Compilation c) { this.compilationUnit = c; }
    public Compilation getCompilationUnit() { return this.compilationUnit; }

    @Override
    public String toString() { return fileName.toString(); }

    @Override
    public AST deepCopy() {
        return new ImportDeclBuilder()
                   .setMetaData(this)
                   .setFileName(this.fileName.deepCopy().asName())
                   .setCompilationUnit(this.compilationUnit.deepCopy().asCompilation())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitImportDecl(this); }

    public static class ImportDeclBuilder extends NodeBuilder {
        private final ImportDecl id = new ImportDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ImportDeclBuilder
         */
        public ImportDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public ImportDeclBuilder setFileName(Name name) {
            id.fileName = name;
            return this;
        }

        public ImportDeclBuilder setCompilationUnit(Compilation c) {
            id.compilationUnit = c;
            return this;
        }

        public ImportDecl create() {
            super.saveMetaData(id);
            id.addChild(id.fileName);
            return id;
        }
    }
}
