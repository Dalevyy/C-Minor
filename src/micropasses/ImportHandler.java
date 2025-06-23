package micropasses;

import ast.misc.Compilation;
import ast.topleveldecls.Import;
import compiler.Compiler;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.Visitor;

import java.io.BufferedReader;
import java.io.FileReader;

public class ImportHandler extends Visitor {

    private final SemanticErrorFactory generateSemanticError;

    public ImportHandler(boolean mode) {
        generateSemanticError = new SemanticErrorFactory();
        interpretMode = mode;
    }

    public void visitImport(Import im) {
        // ERROR CHECK #1: Make sure the imported file is a C Minor program
        if(!im.toString().endsWith(".cm")) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                .addLocation(im)
                .addErrorType(MessageType.SEMANTIC_ERROR_703)
                .addArgs(im)
                .addSuggestType(MessageType.SEMANTIC_SUGGEST_1701)
                .error();
        }

        StringBuilder program = new StringBuilder();
        // ERROR CHECK #2: Make sure the import file does exist and read in its data
        try {
            BufferedReader readInput = new BufferedReader(new FileReader(im.toString()));

            String currLine = readInput.readLine();
            while(currLine != null) {
                program.append(currLine).append('\n');
                currLine = readInput.readLine();
            }
        }
        catch(Exception e) {
            new ErrorBuilder(generateSemanticError,interpretMode)
                    .addLocation(im)
                    .addErrorType(MessageType.SEMANTIC_ERROR_704)
                    .addArgs(im)
                    .error();
        }


        Compiler c = new Compiler();
        Compilation root = c.compile(program.toString());
        im.addCompilationUnit(root);
    }
}
