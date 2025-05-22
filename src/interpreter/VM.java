package interpreter;

import java.io.*;
import ast.*;
import ast.misc.Compilation;
import ast.misc.Var;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
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

        var treePrinter = new Printer();
        var ioRewrite = new InOutStmtRewrite(true);
        var generatePropertyMethods = new PropertyMethodGeneration();
        var nameChecker = new NameChecker(compilationUnit.globalTable);
        var fieldRewrite = new FieldRewrite();
        var classToEnum = new ClassToEnumTypeRewrite(compilationUnit.globalTable);
        var typeChecker = new TypeChecker(compilationUnit.globalTable);
        var generateConstructor = new ConstructorGeneration();
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
            AST currNode = null;
            try {
                var lexer = new Lexer(program.toString());
                var parser = new Parser(lexer,tokenPrint,true);

                nodes = parser.nextNode();

                for(AST node : nodes) {
                    currNode = node;
                    node.visit(ioRewrite);
                    if(treePrint) { node.visit(treePrinter); }
                    node.visit(generatePropertyMethods);
                    node.visit(nameChecker);
                    if(tablePrint) { System.out.println(compilationUnit.globalTable.toString()); }
                    if(node.isTopLevelDecl() && node.asTopLevelDecl().isClassDecl()) { node.visit(fieldRewrite); }
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

                    if(node.isExpression() && node.asExpression().isOutStmt()) { System.out.println(); }
                }
            }
            catch(Exception e) {
                if(currNode.isTopLevelDecl()) {
                    if (currNode.asTopLevelDecl().isClassDecl())
                        compilationUnit.globalTable.removeName(currNode.toString());
                }
                if(e.getMessage() != null) {
                    if(!e.getMessage().equals("EOF Not Found")) {
                        try {
                            if(currNode.isTopLevelDecl()) {
                                if(currNode.asTopLevelDecl().isEnumDecl())
                                    removeEnumDecl(currNode.asTopLevelDecl().asEnumDecl(), compilationUnit.globalTable);
                                else if(currNode.asTopLevelDecl().isClassDecl())
                                    compilationUnit.globalTable.removeName(currNode.toString());
                                else if(currNode.asTopLevelDecl().isFuncDecl())
                                    removeFuncDecl(currNode.asTopLevelDecl().asFuncDecl(),compilationUnit.globalTable);
                            }
                            else
                                compilationUnit.globalTable.removeName(e.getMessage());
                        }
                        catch(Exception e2) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void removeEnumDecl(EnumDecl ed, SymbolTable st) {
        for(Var constant : ed.constants()) { st.removeName(constant.toString()); }
    }

    private static void removeFuncDecl(FuncDecl fd, SymbolTable st) {
        st.removeName(fd.funcSignature());
    }
}
