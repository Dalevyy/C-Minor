package interpreter;

import java.io.*;
import ast.AST;
import lexer.Lexer;
import micropasses.GenerateConstructor;
import micropasses.OutputInputRewrite;
import modifier_checker.ModifierChecker;
import name_checker.NameChecker;
import parser.Parser;
import type_checker.TypeChecker;

public class VM {

    public static void runInterpreter() throws IOException {
        System.out.println("C Minor Interpreter");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String input = "";
            StringBuilder program = new StringBuilder();
            System.out.print(">>> ");
            input = reader.readLine();

            if(input.equals("#quit")) { System.exit(1); }
            else if(input.isEmpty()) { continue; }
            else {
                int tabs = 0;
                program.append(input);
                tabs += input.length() - input.replace("{","").length();
                tabs -= input.length() - input.replace("}", "").length();
                while(tabs > 0) {
                    System.out.print("... ");
                    input = reader.readLine();
                    tabs += input.length() - input.replace("{","").length();
                    tabs -= input.length() - input.replace("}", "").length();
                    program.append(input).append("\n");
                }
            }

            try {
                var lexer = new Lexer(program.toString());
                var parser = new Parser(lexer,false,true);

                AST root = parser.compilation();

                root.visit(new OutputInputRewrite());
                root.visitChildren(new NameChecker(true));

                root.visitChildren(new TypeChecker());
                root.visit(new GenerateConstructor());

                //root.visitChildren(new ModifierChecker());

                root.asCompilation().mainDecl().visit(new Interpreter());
                System.out.println();
            }
            catch(Exception e) { /* Do nothing */ }
        }
    }
}
