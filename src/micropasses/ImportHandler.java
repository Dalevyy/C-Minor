package micropasses;

import ast.misc.Compilation;
import ast.topleveldecls.ImportDecl;
import compiler.Compiler;
import java.io.BufferedReader;
import java.io.FileReader;

import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.semantic.SemanticError;
import utilities.Vector;
import utilities.Visitor;

/**
 * Class that handles import statement error checking.
 * <p>
 *     The {@code ImportHandler} class can be considered as the
 *     first micropass for the C Minor compiler. The goal of this
 *     class is to ensure a user correctly wrote all import statements
 *     before each import file is parsed by the compiler. Internally,
 *     we will be using a queue to keep track of which import statement
 *     needs to be checked next alongside a list of all imported files
 *     that were already parsed. ThisStmt ensures that all import files are
 *     valid and will not cause any trouble for the compiler. Once all import
 *     statements have been properly checked and parsed for a given file, we
 *     will return a list of import statements that will then be stored by the
 *     current file's compilation unit.
 * </p>
 * @author Daniel Levy
 */
public class ImportHandler extends Visitor {

    /**
     * File we were initially parsing (only set during compilation mode)
     */
    private String currFile;

    /**
     * Semantic error generator, all import errors will be
     * treated as general semantic errors to the user
     */
    private MessageHandler handler;

    /**
     * Queue of import statements we need to perform syntax analysis for.
     */
    private final static Vector<ImportDecl> q = new Vector<>();

    /**
     * List of all imports we have performed syntax analysis for
     * including the original file. ThisStmt is needed to prevent circular
     * imports causing an infinite loop in the parser.
     */
    private final static Vector<String> seenImports = new Vector<>();

    /**
     * Creates a default {@code ImportHandler} object.
     * @param mode ThisStmt represents the compiler mode we are in.
     */
    public ImportHandler(boolean mode) {
        this.handler = new MessageHandler();
    }

    /**
     * Creates an {@code ImportHandler} object.
     * @param mainFile ThisStmt is a String that represents the original file we were compiling
     * @param mode ThisStmt represents the compiler mode we are in.
     */
    public ImportHandler(String mainFile, boolean mode) {
        this(mode);
        this.currFile = mainFile;
        seenImports.add(mainFile);
        this.handler = new MessageHandler(mainFile);
    }

    /**
     * Adds an import statement to the queue.
     * @param im New import statement that needs to be handled
     */
    public void enqueue(ImportDecl im) { q.add(im); }

    /**
     * Removes an import statement from the queue.
     * <p>
     *     The import statement that is removed represents the next
     *     import statement we will perform syntax analysis on.
     * </p>
     * @return ImportDecl Statement
     */
    private ImportDecl dequeue() {
        if(!q.isEmpty())
            return q.remove(0);
        return null;
    }

    /**
     * Clears all seen imports (only during interpretation)
     * <p>
     *     ThisStmt method removes all the seen imports whenever the
     *     {@code #clear} flag is used inside of the VM. ThisStmt prevents
     *     any unwarranted errors to show up when a user tries to reimport
     *     a file after all of its contents have been cleared.
     * </p>
     * */
    public static void clear() { seenImports.clear(); }

    /**
     * Analyzes every import statement currently in the queue.
     * <p>
     *     ThisStmt method will iterate through the queue in order
     *     to check if each import statement was written correctly.
     *     If an import statement has no problem, it will then proceed
     *     to be parsed. Once all import statements have been checked
     *     for a single file, we will return a list of imports to be
     *     stored into the current file's AST.
     * </p>
     * @return Vector of {@code ImportDecl} for the current file
     */
    public Vector<ImportDecl> analyzeImports() {
        Vector<ImportDecl> imports = new Vector<>();

        while(!q.isEmpty()) {
            ImportDecl currImport = dequeue();
            currImport.visit(this);
            seenImports.add(currImport.toString());
            imports.add(currImport);
        }

        return imports;
    }

    /**
     * Performs import statement error checking.
     * <p>
     *     ThisStmt visit is designed to perform all semantic checks on an
     *     {@code ImportDecl}. If there are no issues with the import statement,
     *     then we will go ahead and execute syntax analysis on the file.
     * </p>
     * @param im Current import statement we want to parse
     */
    public void visitImportDecl(ImportDecl im) {
        /* ERROR CHECK #1: ThisStmt checks to make sure the file we are importing represents a C Minor program. */
        if(!im.toString().endsWith(".cm")) {
            handler.createErrorBuilder(SemanticError.class)
                .addLocation(im)
                .addErrorNumber(MessageNumber.SEMANTIC_ERROR_703)
                .addErrorArgs(im)
                .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1701)
                .generateError();
        }

        /* ERROR CHECK #2: ThisStmt checks to make sure the current file does not import itself. */
        if(im.toString().equals(currFile)) {
            handler.createErrorBuilder(SemanticError.class)
                .addLocation(im)
                .addErrorNumber(MessageNumber.SEMANTIC_ERROR_705)
                .addErrorArgs(im)
                .generateError();
        }

        /* ERROR CHECK #3: ThisStmt checks if we are reimporting any files in order to avoid circular imports. */
        for(String importName : seenImports) {
            if(importName.equals(im.toString())) {
                handler.createErrorBuilder(SemanticError.class)
                    .addLocation(im)
                    .addErrorNumber(MessageNumber.SEMANTIC_ERROR_706)
                    .addErrorArgs(im)
                    .generateError();
            }
        }

        StringBuilder program = new StringBuilder();
        /* ERROR CHECK #4: The final check makes sure the file does exist somewhere in the user's file system. */
        try {
            BufferedReader readInput = new BufferedReader(new FileReader(im.toString()));

            String currLine = readInput.readLine();
            while(currLine != null) {
                program.append(currLine).append('\n');
                currLine = readInput.readLine();
            }
        }
        catch(Exception e) {
            handler.createErrorBuilder(SemanticError.class)
                .addLocation(im)
                .addErrorNumber(MessageNumber.SEMANTIC_ERROR_704)
                .addErrorArgs(im)
                .generateError();
        }

        Compiler c = new Compiler(im.toString());
        Compilation root = c.syntaxAnalysis(program.toString(),false,true);
        im.addCompilationUnit(root);
    }
}
