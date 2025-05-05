package interpreter;

import java.io.*;
import ast.*;
import lexer.Lexer;
import micropasses.*;
import modifier_checker.ModifierChecker;
import name_checker.NameChecker;
import parser.Parser;
import type_checker.TypeChecker;
import utilities.Vector;

public class VM {

    public static void runInterpreter() throws IOException {
        System.out.println("C Minor Interpreter");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Compilation compilationUnit = new Compilation();

        var ioRewrite = new OutputInputRewrite(true);
        var generatePropertyMethods = new GeneratePropertyMethods();
        var nameChecker = new NameChecker(compilationUnit.globalTable);
        var fieldRewrite = new FieldRewrite();
        var typeChecker = new TypeChecker(compilationUnit.globalTable);
        var generateConstructor = new GenerateConstructor();
        var loopKeywordCheck = new LoopKeywordCheck(true);
        var modChecker = new ModifierChecker(compilationUnit.globalTable);
        var interpreter = new Interpreter(compilationUnit.globalTable);

        while(true) {
            String input = "";
            StringBuilder program = new StringBuilder();
            System.out.print(">>> ");
            input = reader.readLine();

            if(input.equals("#quit")) { System.exit(1); }
            else if(input.equals("#clear")) {
                compilationUnit = new Compilation();
                nameChecker = new NameChecker(compilationUnit.globalTable);
                typeChecker = new TypeChecker(compilationUnit.globalTable);
                modChecker = new ModifierChecker(compilationUnit.globalTable);
                interpreter = new Interpreter(compilationUnit.globalTable);
                continue;
            }
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
            Vector<? extends AST> nodes;
            try {
                var lexer = new Lexer(program.toString());
                var parser = new Parser(lexer,false,true);

                nodes = parser.parseVM();

                for(AST node : nodes) {
                    node.visit(ioRewrite);
                    node.visit(generatePropertyMethods);
                    node.visit(nameChecker);
                    if(node.isTopLevelDecl() && node.asTopLevelDecl().isClassDecl()) { node.visit(fieldRewrite); }
                    node.visit(loopKeywordCheck);
                    node.visit(typeChecker);
                    node.visit(generateConstructor);
                    node.visit(modChecker);

                    if(node.isTopLevelDecl()) {
                        if(node.asTopLevelDecl().isClassDecl()) { compilationUnit.addClassDecl(node.asTopLevelDecl().asClassDecl()); }
                        else if(node.asTopLevelDecl().isFuncDecl()) { compilationUnit.addFuncDecl(node.asTopLevelDecl().asFuncDecl()); }
                        else { node.visit(interpreter); }
                    }
                    else {
                        node.visit(interpreter);
                        if(node.isStatement()) { compilationUnit.mainDecl().mainBody().addStmt(node.asStatement()); }
                    }
                    System.out.println();
                }
            }
            catch(Exception e) {
                // if(e.getMessage() != null) { compilationUnit.globalTable.removeName(e.getMessage()); }
                /* Do nothing */
            }
        }
    }
}
