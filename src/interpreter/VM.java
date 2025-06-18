package interpreter;

import ast.AST;
import ast.misc.Compilation;
import ast.misc.Var;
import ast.topleveldecls.EnumDecl;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import lexer.Lexer;
import micropasses.*;
import modifierchecker.ModifierChecker;
import namechecker.NameChecker;
import parser.Parser;
import typechecker.TypeChecker;
import utilities.Printer;
import utilities.SymbolTable;
import utilities.Vector;

public class VM {

    public static void runInterpreter() throws IOException {
        System.out.println("C Minor Interpreter");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Compilation compilationUnit = new Compilation();

        // Phases
        NameChecker nameChecker = new NameChecker(compilationUnit.globalTable);
        TypeChecker typeChecker = new TypeChecker(compilationUnit.globalTable);
        ModifierChecker modChecker = new ModifierChecker(compilationUnit.globalTable);
        Interpreter interpreter = new Interpreter(compilationUnit.globalTable);

        // Micropasses
        Printer treePrinter = new Printer();
        InOutStmtRewrite ioRewritePass = new InOutStmtRewrite(true);
        PropertyMethodGeneration generatePropertyPass = new PropertyMethodGeneration();
        VariableInitialization variableInitPass = new VariableInitialization(true);
        FieldRewrite fieldRewritePass = new FieldRewrite();
        OperatorOverloadCheck operatorOverloadPass = new OperatorOverloadCheck(true);
        LoopKeywordCheck loopCheckPass = new LoopKeywordCheck(true);
        ClassToEnumTypeRewrite classToEnumPass = new ClassToEnumTypeRewrite(compilationUnit.globalTable);
        ConstructorGeneration generateConstructorPass = new ConstructorGeneration();

        boolean printTokens = false;
        boolean printAST = false;
        boolean printST = false;
        boolean debug = true;

        while(true) {
            String input;
            StringBuilder program = new StringBuilder();
            System.out.print(">>> ");
            input = reader.readLine();

            if(input.equals("#quit"))
                System.exit(0);
            else if(input.equals("#clear")) {
                compilationUnit = new Compilation();
                nameChecker = new NameChecker(compilationUnit.globalTable);
                classToEnumPass = new ClassToEnumTypeRewrite(compilationUnit.globalTable);
                typeChecker = new TypeChecker(compilationUnit.globalTable);
                modChecker = new ModifierChecker(compilationUnit.globalTable);
                interpreter = new Interpreter(compilationUnit.globalTable);
                continue;
            }
            else if(input.equals("#show-tokens")) {
                printTokens = !printTokens;
                continue;
            }
            else if(input.equals("#show-tree")) {
                printAST = !printAST;
                continue;
            }
            else if(input.equals("#show-table")) {
                printST = !printST;
                continue;
            }
            else if(input.equals("#debug")) {
                debug = !debug;
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
            AST currNode = null;
            try {
                var lexer = new Lexer(program.toString(),true);
                var parser = new Parser(lexer, printTokens, true);

                nodes = parser.nextNode();

                for (AST node : nodes) {
                    currNode = node;
                    node.visit(ioRewritePass);
                    if(printAST)
                        node.visit(treePrinter);
                    node.visit(generatePropertyPass);
                    node.visit(nameChecker);

                    if(printST)
                        System.out.println(compilationUnit.globalTable.toString());

                    node.visit(variableInitPass);
                    if(node.isTopLevelDecl() && node.asTopLevelDecl().isClassDecl()) {
                        node.visit(fieldRewritePass);
                        node.visit(operatorOverloadPass);
                    }

                    node.visit(loopCheckPass);
                    node.visit(classToEnumPass);
                    node.visit(typeChecker);
                    node.visit(generateConstructorPass);
                    node.visit(modChecker);

                    if (node.isTopLevelDecl()) {
                        if(node.asTopLevelDecl().isClassDecl())
                            compilationUnit.addClassDecl(node.asTopLevelDecl().asClassDecl());
                        else if(node.asTopLevelDecl().isFuncDecl())
                            compilationUnit.addFuncDecl(node.asTopLevelDecl().asFuncDecl());
                        else
                            node.visit(interpreter);
                    } else {
                        node.visit(interpreter);
                        compilationUnit.mainDecl().mainBody().addStmt(node.asStatement());
                    }
                }
            }
            catch(Exception e) {
                generateError(e,currNode,compilationUnit.globalTable);
                if(debug)
                    e.printStackTrace();
            }
        }
    }

    private static void generateError(Exception e, AST location, SymbolTable st) {
        // If a lexer/parser error occurs, do not remove anything from the symbol table.
        if(location != null && !e.getMessage().equals("EOF Not Found") && !e.getMessage().equals("Redeclaration")) {
            if(location.isTopLevelDecl()) {
                if(location.asTopLevelDecl().isEnumDecl()) {
                    EnumDecl ed = location.asTopLevelDecl().asEnumDecl();
                    for(Var constant : ed.constants())
                        st.removeName(constant.toString());
                    st.removeName(ed.toString());
                }
                else if(location.asTopLevelDecl().isClassDecl() || location.asTopLevelDecl().isGlobalDecl())
                    st.removeName(location.toString());
                else if(location.asTopLevelDecl().isFuncDecl())
                    st.removeName(location.asTopLevelDecl().asFuncDecl().funcSignature());
            }
            else if(location.isParamDecl() || location.isStatement() && location.asStatement().isLocalDecl())
                st.removeName(location.toString());
        }
    }
}
