package compiler;

import ast.misc.Compilation;
import interpreter.Interpreter;
import interpreter.VM;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import lexer.Lexer;
import micropasses.*;
import modifierchecker.ModifierChecker;
import namechecker.NameChecker;
import parser.Parser;
import typechecker.TypeChecker;
import utilities.PrettyPrint;
import utilities.Printer;

/**
 * C Minor Compiler class
 * <p>
 *     This class is responsible for handling the compilation mode
 *     of C Minor. It will ensure the user correctly inputted their
 *     program alongside any compile flags and performs the compilation.
 * </p>
 * @author Daniel Levy
 */
public class Compiler {
    /** Flag that will print out C Minor tokens. */
    private static Boolean printTokens = false;

    /** Flag that will print out a C Minor AST. */
    private static Boolean printParseTree = false;

    /** Begins the C Minor compilation process. */
    public void compile(String[] args) throws IOException {
        String input = readProgram(args);
        Compilation root = syntaxAnalysis(input);
        semanticAnalysis(root,true);
    }

    /** Handles the compilation process for import statements. */
    public Compilation compile(String program) {
        Compilation root = syntaxAnalysis(program);
        semanticAnalysis(root,false);
        return root;
    }

    /**
     * Executes the syntax analysis phase of the C Minor compiler.
     * <p>
     *     During this phase, we check if a C Minor program is syntactically valid
     *     based on the grammar. If there are any errors, then the {@code Parser} will
     *     generate an error and terminate the compilation process. Additionally, the
     *     tokenization and parsing of a C Minor program will occur in parallel, so we
     *     can have better performance.
     * </p>
     * @param program C Minor program as a string
     * @return An AST node representing the {@code Compilation} unit for the program.
     */
    private Compilation syntaxAnalysis(String program) {
        Parser parser = new Parser(new Lexer(program),printTokens);
        Compilation root = parser.compilation();
        root.visit(new InOutStmtRewrite());

        if(printParseTree)
            root.visit(new Printer());

        return root;
    }

    /**
     * Executes the semantic analysis phase of the C Minor compiler.
     * <p>
     *     This method will execute all major and micro passes associated with
     *     the C Minor compiler. If no errors were found, then the compiler
     *     will currently execute the program by running it through the interpreter.
     * </p>
     * @param root Compilation unit representing the program we want to compile
     */
    private void semanticAnalysis(Compilation root, boolean execute) {
        root.visit(new PropertyMethodGeneration());
        root.visit(new NameChecker());
        root.visit(new VariableInitialization());
        root.visit(new FieldRewrite());
        root.visit(new OperatorOverloadCheck());
        root.visit(new LoopKeywordCheck());
        root.visit(new ClassToEnumTypeRewrite());
        root.visit(new TypeChecker());
        root.visit(new ConstructorGeneration());
        root.visit(new ModifierChecker());
        if(execute)
            root.visit(new Interpreter(root.globalTable));
    }

    /**
     * Reads a C Minor input file and stores the contents into a buffer.
     * <p>
     *     Once all compiler flags are valid, this method will perform
     *     file IO to read in a C Minor program for the compiler.
     * </p>
     * @param args The arguments the user passed into the compiler
     * @return String representing a C Minor program that needs to be compiled
     * @throws IOException Exception when a user incorrectly passes an argument flag
     */
    private String readProgram(String[] args) throws IOException {
        String fileName = args[inputValidation(args)];
        StringBuilder program = new StringBuilder();

        try {
            BufferedReader readInput = new BufferedReader(new FileReader(fileName));

            String currLine = readInput.readLine();
            while(currLine != null) {
                program.append(currLine).append('\n');
                currLine = readInput.readLine();
            }
        }
        // ERROR CHECK #1: An error is generated when a C Minor file could not be found in the file system.
        catch(Exception e) {
            System.out.print(PrettyPrint.RED + "Error! C Minor program file could not be found.\n");
            System.exit(-1);
        }

        return program.toString();
    }

    /**
     * Checks if a user correctly inputted compiler arguments.
     * @param args The arguments the user passed into the compiler
     * @return Integer position that denotes which argument contains the C Minor file name
     * @throws IOException Exception when a user incorrectly passes an argument flag
     */
    private int inputValidation(String[] args) throws IOException {
        // ERROR CHECK #1: We will generate an error if the user did not pass any arguments to the compiler
        if(args.length < 1) {
            System.out.print(PrettyPrint.RED + "Error! No input files were detected.\n" + PrettyPrint.RESET);
            System.exit(1);
        }
        return parseOptions(args);
    }

    /**
     * Parses the compiler arguments and checks if they are valid.
     * @param args The arguments the user passed into the compiler
     * @return Integer position that denotes which argument contains the C Minor file name
     * @throws IOException Exception when a user incorrectly passes an argument flag
     */
    private int parseOptions(String[] args) throws IOException {
        boolean inputFileFound = false;
        int fileArg = -1;

        for(int i = 0; i < args.length; i++) {
            String currArg = args[i];

            switch(currArg) {
                case "--start-vm":
                    new VM().runInterpreter();
                case "--print-tokens":
                    printTokens = true;
                    break;
                case "--print-tree":
                    printParseTree = true;
                    break;
                default:
                    if(currArg.endsWith(".cm")) {
                        inputFileFound = true;
                        fileArg = i;
                        break;
                    }
                    // ERROR CHECK #1: If an invalid compiler flag was passed, generate an error
                    System.out.print(PrettyPrint.RED + currArg + " is an invalid compiler flag." + PrettyPrint.RESET);
                    System.exit(1);
            }
        }

        // ERROR CHECK #2: Generate an error if we did not find a C Minor file and terminate the compilation process.
        if(!inputFileFound) {
            System.out.print(PrettyPrint.RED + "Error! A .cm file could not be found.\n" + PrettyPrint.RESET);
            System.exit(1);
        }

        return fileArg;
    }
}
