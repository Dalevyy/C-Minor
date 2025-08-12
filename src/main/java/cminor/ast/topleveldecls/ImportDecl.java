package cminor.ast.topleveldecls;

import cminor.ast.AST;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.Name;
import cminor.token.Token;
import cminor.utilities.Visitor;

/**
 * A {@link TopLevelDecl} node representing an imported file.
 * <p>
 *     C Minor supports the ability to import code from other C Minor files. This means
 *     we will have to keep track of several {@link CompilationUnit} to account for each
 *     imported file we could have, and this will be done through an {@link ImportDecl}.
 * </p>
 * @author Daniel Levy
 */
public class ImportDecl extends TopLevelDecl{

    /**
     * The name of the file we are importing.
     */
    private Name fileName;

    /**
     * The {@link CompilationUnit} associated with the imported file.
     */
    private CompilationUnit compilationUnit;

    /**
     * Default constructor for {@link ImportDecl}.
     */
    public ImportDecl() { this(new Token(),null); }

    /**
     * Main constructor for {@link ImportDecl}
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param fileName {@link Name} to store into {@link #fileName}.
     */
    public ImportDecl(Token metaData, Name fileName) {
        super(metaData);
        this.fileName = fileName;
    }

    /**
     * Getter method for {@link #compilationUnit}.
     * @return {@link CompilationUnit}
     */
    public CompilationUnit getCompilationUnit() { return compilationUnit; }

    /**
     * Sets the {@link #compilationUnit} value after the {@link micropasses.ImportHandler} parses the imported file.
     * @param cu The {@link CompilationUnit} that is saved into the program.
     */
    public void setCompilationUnit(CompilationUnit cu) {
        compilationUnit = (compilationUnit == null) ? cu : compilationUnit;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isImport() { return true; }

    /**
     * {@inheritDoc}
     */
    public ImportDecl asImport() { return this; }

    /**
     * Returns the name of the imported file as a string.
     * @return String representation of the imported file name.
     */
    @Override
    public String toString() { return fileName.toString(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) { throw new RuntimeException("An import statement can not be updated."); }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        return new ImportDeclBuilder()
                   .setMetaData(this)
                   .setFileName(fileName.deepCopy().asSubNode().asName())
                   .setCompilationUnit(compilationUnit.deepCopy().asSubNode().asCompilationUnit())
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitImportDecl(this); }

    /**
     * Internal class that builds an {@link ImportDecl} object.
     */
    public static class ImportDeclBuilder extends NodeBuilder {

        /**
         * {@link ImportDecl} object we are building.
         */
        private final ImportDecl id = new ImportDecl();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ImportDeclBuilder}.
         */
        public ImportDeclBuilder setMetaData(AST node) {
            super.setMetaData(id, node);
            return this;
        }

        /**
         * Sets the import declaration's {@link #fileName}.
         * @param name {@link Name} representing the name of the imported file.
         * @return Current instance of {@link ImportDeclBuilder}.
         */
        public ImportDeclBuilder setFileName(Name name) {
            id.fileName = name;
            return this;
        }

        /**
         * Sets the import declarations {@link #compilationUnit}.
         * @param compilationUnit The {@link CompilationUnit} of the imported file.
         * @return Current instance of {@link ImportDeclBuilder}.
         */
        public ImportDeclBuilder setCompilationUnit(CompilationUnit compilationUnit) {
            id.compilationUnit = compilationUnit;
            return this;
        }

        /**
         * Creates an {@link ImportDecl} object.
         * @return {@link ImportDecl}
         */
        public ImportDecl create() { return id; }
    }
}
