
import java.io.*;
import AST.*;
import Parser.*;
import Lexer.*;
import Utilities.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1)
            throw new IllegalArgumentException("Error! Program to compile not found");

        String input = readProgram(args[0]);

        var lexer = new Lexer(input);
        var parser = new Parser(lexer);

        AST root = parser.compilation();
        root.visitChildren(new Printer());

        System.out.println("\nParsing is complete...");
    }

    // This will read in a C Minor source program into a buffer, and it will return a
    private static String readProgram(String fileName) throws Exception {
        File program = new File("/home/dalev/C_Minor/UnitTests/GoodTests/"+fileName);
        BufferedReader readInput = new BufferedReader(new FileReader(program));
        StringBuilder programAsStr = new StringBuilder();

        String currLine = readInput.readLine();
        while(currLine != null) {
            programAsStr.append(currLine);
            programAsStr.append('\n');
            currLine = readInput.readLine();
        }

        return programAsStr.toString();
    }
}
