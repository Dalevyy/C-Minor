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

            if(input.equals("#quit"))
                return;
            else if(input.isEmpty())
                continue;

            // #include need to be changed to make this work
        
            var lexer = new Lexer(input);
            var parser = new Parser(lexer,false);

            AST root = parser.compilation();

            root.visit(new OutputInputRewrite());
            root.visitChildren(new NameChecker());

            root.visit(new GenerateConstructor());
            root.visitChildren(new TypeChecker());

            //root.visitChildren(new ModifierChecker());

            root.asCompilation().mainDecl().visit(new Interpreter());
            System.out.println();
        }
    }
}
