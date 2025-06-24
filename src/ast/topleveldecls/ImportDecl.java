package ast.topleveldecls;

import ast.misc.Compilation;
import ast.misc.Name;
import token.Token;
import utilities.Visitor;

public class ImportDecl extends TopLevelDecl{

    public Name fileName;
    private Compilation compilationUnit;

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
    public void visit(Visitor v) { v.visitImport(this); }
}
