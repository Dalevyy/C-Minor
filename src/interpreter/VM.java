package interpreter;

import java.io.*;
import ast.*;
import lexer.Lexer;
import micropasses.*;
import modifierchecker.ModifierChecker;
import namechecker.NameChecker;
import parser.Parser;
import typechecker.TypeChecker;
import utilities.Printer;
import utilities.Vector;

public class VM {

    public static void runInterpreter() throws IOException {
        System.out.println("C Minor Interpreter");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Compilation compilationUnit = new Compilation();

        var treePrinter = new Printer();
        var ioRewrite = new InOutStmtRewrite(true);
        var generatePropertyMethods = new PropertyMethodGeneration();
        var nameChecker = new NameChecker(compilationUnit.globalTable);
        var fieldRewrite = new FieldRewrite();
        var fieldCheck = new ValidFieldExprCheck(true);
        var classToEnum = new ClassToEnumTypeRewrite(compilationUnit.globalTable);
        var typeChecker = new TypeChecker(compilationUnit.globalTable);
        var generateConstructor = new GenerateConstructor();
        var loopKeywordCheck = new LoopKeywordCheck(true);
        var modChecker = new ModifierChecker(compilationUnit.globalTable);
        var interpreter = new Interpreter(compilationUnit.globalTable);

        boolean tokenPrint = false;
        boolean treePrint = false;
        boolean tablePrint = false;

        while(true) {
            String input;
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
            else if(input.equals("#show-tokens")) {
                tokenPrint = !tokenPrint;
                continue;
            }
            else if(input.equals("#show-tree")) {
                treePrint = !treePrint;
                continue;
            }
            else if(input.equals("#show-table")) {
                tablePrint = !tablePrint;
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
                var parser = new Parser(lexer,tokenPrint,true);

                nodes = parser.nextNode();

                for(AST node : nodes) {
                    node.visit(ioRewrite);
                    if(treePrint) { node.visit(treePrinter); }
                    node.visit(generatePropertyMethods);
                    node.visit(nameChecker);
                    if(tablePrint) { System.out.println(compilationUnit.globalTable.toString()); }
                    if(node.isTopLevelDecl() && node.asTopLevelDecl().isClassDecl()) { node.visit(fieldRewrite); }
                    node.visit(fieldCheck);
                    node.visit(loopKeywordCheck);
                    node.visit(classToEnum);
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
                }
            }
            catch(Exception e) {
                if(e.getMessage() != null) {
                    if(!e.getMessage().equals("EOF Not Found")) {
                        try { compilationUnit.globalTable.removeName(e.getMessage()); }
                        catch(Exception e2) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                /* Do nothing */
            }
        }
    }
}
