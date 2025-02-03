// System properties
import java.io.*;
import ast.*;
import errors.*;
import interpreter.*;
import micropasses.OutputInputRewrite;
import parser.*;
import lexer.*;
import name_checker.*;
import utilities.*;

public class Main {

    static Boolean printTokens = false;
    static Boolean printParseTree = false;

    public static void main(String[] args) throws IOException {
        String input = readProgram(args);

        AST root = syntaxAnalysis(input);
        semanticAnalysis(root);
    }

    /*
        The first step of the compilation process is syntax analysis. We must
        check whether or not a C Minor program is syntactically valid based on
        its grammar.

        In C Minor, we will tokenize the entire program before parsing it. This
        means the lexer does not generate any errors. Instead, the parser will
        generate an error when a user has written an invalid statement or expression.
    */
    private static AST syntaxAnalysis(String program) {
        Lexer lexer = new Lexer(program);
        Parser parser = new Parser(lexer,printTokens);
        AST root = parser.compilation();
        root.visit(new OutputInputRewrite());

        if(printParseTree)
            root.visit(new Printer());

        return root;
    }

    private static void semanticAnalysis(AST root) {
        root.visitChildren(new NameChecker());

//        root.whosThatNode(new GenerateConstructor());
//        root.visitChildren(new TypeChecker());
//
//        root.visitChildren(new ModifierChecker());
//        System.out.println("\nModifier Checking is complete...");
    }

    /*
        Since the compiler is not machine dependent, we are going to use Java's
        System class to handle reading a C Minor source program for us, and we
        will transfer all contents of the file into a buffer.
    */
    private static String readProgram(String[] args) throws IOException {
        String fileName = args[inputValidation(args)];
        StringBuilder programAsStr = new StringBuilder();

        try {
            File program = new File(fileName);
            BufferedReader readInput = new BufferedReader(new FileReader(program));
            BasicError.setFileName(fileName);

            String currLine = readInput.readLine();
            while(currLine != null) {
                programAsStr.append(currLine);
                programAsStr.append('\n');
                currLine = readInput.readLine();
            }
        }
        catch(Exception e) {
            System.out.print(PrettyPrint.RED + "Error! C Minor program file could not be found.\n");
            System.exit(1);
        }

        if(programAsStr.isEmpty()) {
            System.out.print(PrettyPrint.RED + "Error! Undefined reference to main function.\n");
            System.exit(1);
        }
        return programAsStr.toString();
    }


    private static int inputValidation(String[] args) throws IOException {
        if(args.length < 1) {
            System.out.print(PrettyPrint.RED + "Error! No input files were detected.\n" + PrettyPrint.RESET);
            System.exit(1);
        }
        return parseOptions(args);
    }

    private static int parseOptions(String[] args) throws IOException {
        boolean inputFileFound = false;
        int fileArg = -1;
        for(int i = 0; i < args.length; i++) {
            String curr = args[i];

            if(curr.equals("--start-vm")) {
                VM.runInterpreter();
                System.exit(0);
            }
            else if(curr.equals("--print-tokens"))
                printTokens = true;
            else if(curr.equals("--print-tree"))
                printParseTree = true;
            else if(curr.endsWith(".cm")) {
                inputFileFound = true;
                fileArg = i;
            }
        }

        if(!inputFileFound) {
            System.out.print(PrettyPrint.RED + "Error! A .cm file was not given.\n" + PrettyPrint.RESET);
            System.exit(1);
        }

        return fileArg;
    }
}
