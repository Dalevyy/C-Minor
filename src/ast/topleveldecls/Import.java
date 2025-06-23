package ast.topleveldecls;

import ast.misc.Compilation;
import ast.misc.Name;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class Import extends TopLevelDecl{

    public Name fileName;
    public Vector<Compilation> compiledFiles;

    public Import(Token metaData, Name n) {
        super(metaData);
        // Removing start/end quotes from file name for cleaner error outputting.
        n.setName(n.toString().substring(1,n.toString().length()-1));
        fileName = n;
        compiledFiles = new Vector<>();

        addChild(fileName);
        setParent();
    }

    public boolean isImport() { return true; }
    public Import asImport() { return this; }

    public void addCompilationUnit(Compilation c) { compiledFiles.add(c); }

    @Override
    public String toString() { return fileName.toString(); }

    @Override
    public void visit(Visitor v) { v.visitImport(this); }
}
