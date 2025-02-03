package name_checker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.*;
import ast.top_level_decls.*;
import errors.*;
import utilities.*;

import java.util.HashSet;

public class NameChecker extends Visitor {

    private SymbolTable currentScope;

    public NameChecker() { this.currentScope = new SymbolTable(); }

    public AST retrieveName(String name) {
        AST prevDecl = currentScope.findName(name).declName();

        if(prevDecl.isFieldDecl())
            return prevDecl.asFieldDecl();
        else if(prevDecl.isTopLevelDecl()) {
            if(prevDecl.asTopLevelDecl().isGlobalDecl())
                return prevDecl.asTopLevelDecl().asGlobalDecl();
            else if(prevDecl.asTopLevelDecl().isEnumDecl())
                return prevDecl.asTopLevelDecl().asEnumDecl();
            else
                return prevDecl.asTopLevelDecl().asClassDecl();
        }
        else if(prevDecl.isStatement()) {
            if (prevDecl.asStatement().isLocalDecl())
                return prevDecl.asStatement().asLocalDecl();
        }
        else
            return prevDecl.asParamDecl();

        return null;
    }

    public void printScopeError(String varName, AST currDecl) {
        System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);

        currDecl.printLine();
        AST prevDecl = retrieveName(varName);

        System.out.println(PrettyPrint.RED + currDecl.getStartPosition() + ": Scoping Error! \'"
                + currDecl.toString() + "\' has already been declared on line " + prevDecl.startLine() +
                ".\n" + PrettyPrint.RESET);

        prevDecl.printLine();
        System.out.println(PrettyPrint.RED + "Redeclaration of \'" + currDecl.toString() + "\' in the " +
                "same scope is not allowed.");
        System.exit(1);
    }

    public void visitBlockStmt(BlockStmt bs) {
        currentScope = currentScope.openNewScope();

        bs.decls().visit(this);
        bs.stmts().visit(this);
    }

    // CaseStmt: Opens a new scope
    public void visitCaseStmt(CaseStmt cs) {

        cs.choiceLabel().visit(this);
        currentScope = currentScope.openNewScope();

        cs.caseBlock().decls().visit(this);
        cs.caseBlock().stmts().visit(this);

        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    // ChoiceStmt: Opens a new scope
    public void visitChoiceStmt(ChoiceStmt cs) {

        cs.choiceExpr().visit(this);
        cs.caseStmts().visit(this);

        BlockStmt otherBlock = cs.choiceBlock();
        currentScope = currentScope.openNewScope();

        cs.choiceBlock().decls().visit(this);
        cs.choiceBlock().stmts().visit(this);

        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
        When a user defines a class, we want to make sure the
        name binded to the class is not already used by some
        other construct in the current file or an import.
    */
    public void visitClassDecl(ClassDecl cd) {
        String className = cd.name().toString();

        if(currentScope.isNameUsedAnywhere(className))
            printScopeError(className,cd);

        if(cd.superClass() != null)
            className += "_" + cd.superClass().getName().toString();

        currentScope.addName(className,cd);

        currentScope = currentScope.openNewScope();

        cd.clalssBlock().fieldDecls().visit(this);
        cd.clalssBlock().methodDecls().visit(this);

        cd.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
    ------------------- DoStmt OPENS a scope -------------------

        A DoStmt will store 1 symbol table.
    */
    public void visitDoStmt(DoStmt ds) {

        ds.doBlock().visit(this);

        ds.symbolTable = currentScope;
        currentScope = currentScope.closeScope();

        Statement nextExpr = ds.nextExpr();
        if(nextExpr != null)
            nextExpr.visit(this);

        ds.condition().visit(this);
    }

    /*
        An EnumDecl will be the first construct a user can define
        in a C Minor program. Thus, we want to check if the name
        binded to an Enum isn't already used by another Enum in
        the same file OR by some other construct located in an
        import file.
    */
    public void visitEnumDecl(EnumDecl ed) {
        String enumName = ed.name().toString();

        if(currentScope.isNameUsedAnywhere(enumName))
            printScopeError(enumName,ed);

        currentScope.addName(enumName,ed);

        // Every field in an enum will be added to the current scope, so it
        // may be used elsewhere in the program
        for(int i = 0; i < ed.enumVars().size(); i++)
            currentScope.addName(ed.enumVars().get(i).asVar().toString(),ed);
    }

    /*
    Inside a class, it is fine if we have a field declaration's
    name be binded to some other construct outside the class
    since we will treat the class as its own namespace. :)
    */
    public void visitFieldDecl(FieldDecl fd) {
        String fieldName = fd.var().name().toString();

        if(currentScope.hasName(fieldName))
            printScopeError(fieldName,fd);

        currentScope.addName(fieldName, fd);
    }

    public void visitFieldExpr(FieldExpr fe) {

    }

    /*
    ------------------- ForStmt OPENS a scope -------------------

        A ForStmt will store 1 symbol table.
    */
    public void visitForStmt(ForStmt fs) {
        currentScope = currentScope.openNewScope();

        fs.forInits().visit(this);
        fs.condition().visit(this);

        Statement nextExpr = fs.nextExpr();

        if(nextExpr != null)
            nextExpr.visit(this);

        fs.forBlock().decls().visit(this);
        fs.forBlock().stmts().visit(this);

        fs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    // FuncDecl: Opens a new scope
    public void visitFuncDecl(FuncDecl fd) {
        String funcSignature = "";
        if(fd.params() != null)
            funcSignature = fd.toString() + "(" + fd.paramSignature() + ")" + fd.returnType().typeSignature();

        if(currentScope.hasMethodSomewhere(funcSignature))
            ScopeError.RedeclError(fd);

        if(fd.returnType().isClassType()) {
            if(!currentScope.hasName(fd.returnType().typeName()))
                ScopeError.LocalDeclError(fd);
        }

        SymbolTable newScope = currentScope.openNewScope();
        currentScope.addName(fd.toString(), fd);
        currentScope.addMethod(funcSignature, newScope);

        currentScope = newScope;

        // Check if the parameter names are valid
        if(fd.params() != null)
            fd.params().visit(this);

        // Check the body of the function now
        fd.funcBlock().decls().visit(this);
        fd.funcBlock().stmts().visit(this);

        fd.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
        For a GlobalDecl, we check if the name binded
        to the global variable was already used in the current scope.
    */
    public void visitGlobalDecl(GlobalDecl gd) {
        String globalName = gd.toString();

        if(currentScope.isNameUsedAnywhere(globalName))
            printScopeError(globalName,gd);

        if(gd.var().init() != null) {
            if(gd.var().init().equals(globalName)) {
                System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);

                gd.printLine();
                System.out.println(PrettyPrint.RED + gd.getStartPosition() + ": Scoping Error! Global variable \'"
                        + globalName + "\' can not be initialized to itself." + PrettyPrint.RESET);
                System.exit(1);
            }
        }
            gd.var().init().visit(this);

        currentScope.addName(gd.toString(), gd);
    }

    /*
    ------------------- IfSmt OPENS a scope -------------------

        An IfStmt will store 2 separate symbol tables:
            1. The symbol table for the IF branch
            2. The symbol table for the ELSE branch (OPTIONAL)
    */
    public void visitIfStmt(IfStmt is) {

        is.condition().visit(this);

        is.ifBlock().visit(this);
        is.symbolTableIfBlock = currentScope;
        currentScope = currentScope.closeScope();

        if(is.elifStmts().size() > 0)
            is.elifStmts().visit(this);

        if(is.elseBlock() != null) {
            is.elseBlock().visit(this);
            is.symbolTableElseBlock = currentScope;
            currentScope = currentScope.closeScope();
        }
    }

    /*
        Right now, we are only going to be name checking function
        invocations. Method invocations are not possible right now
        since we need to know the type of the target expression to
        determine what class a method is contained in, but we don't
        have enough information to perform this check yet.

        We could check for simple method invocations (and by extension,
        simple field expressions) if we really wanted to, but it's better
        to wait until type checking to do these tasks. :)
    */
    public void visitInvocation(Invocation in) {
        String name = in.toString();

        /*
            ERROR CHECK #1:
                If we have a function invocation, we want to check
                whether the function has already been defined in
                either the current file or an import.
        */
        if(in.target() == null) {
            if(!currentScope.hasNameSomewhere(name))
                ScopeError.LocalDeclError(in);
        }

        // Regardless if we have a method or function, we will
        // perform a name check on any arguments that represent names
        if(in.arguments() != null)
            in.arguments().visit(this);
    }

    /*
        When a user declares a local variable, we are going to check
        if the name used by this local already exists in the current scope.

        We also are going to be pedantic and make sure the name is not
        already binded to a TopLevelDecl node in order to avoid confusion.

        If we have any form of redeclaration, we are going to error out.
    */
    public void visitLocalDecl(LocalDecl ld) {
        String localName = ld.toString();

        if(currentScope.isNameUsedAnywhere(localName)) {
            if(currentScope.hasName(localName))
                printScopeError(localName,ld);
        }

        if(ld.var().init() != null) {
            if(ld.var().init().toString().equals(localName)) {
                System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);

                ld.printLine();
                System.out.println(PrettyPrint.RED + ld.getStartPosition() + ": Scoping Error! Local variable \'"
                        + localName + "\' can not be initialized to itself." + PrettyPrint.RESET);
                System.exit(1);
            }
            ld.var().init().visit(this);
        }

        currentScope.addName(localName,ld);
    }

    /*
        Since we want C Minor to mimic the scoping rules of C++, we will
        have to create a new scope when we visit 'main' since C++ allows
        a global variable to be redeclared locally.
    */
    public void visitMainDecl(MainDecl md) {
//        if(md.returnType().isClassType()) {                           Check in type checker?
//            if(!currentScope.hasName(md.returnType().typeName()))
//                ScopeError.LocalDeclError(md);
//        }

        if(md.args() != null)
            md.args().visit(this);

        md.mainBody().visit(this);

        md.symbolTable = currentScope;
        currentScope.closeScope();
    }

    // MethodDecl: Opens a new scope
    public void visitMethodDecl(MethodDecl md) {
        String methodSignature = md.toString() + "(" + md.paramSignature() + ")" + md.returnType().typeSignature();
        if(currentScope.hasMethod(methodSignature))
            ScopeError.LocalDeclError(md);

        if(md.returnType().isClassType()) {
            if(!currentScope.hasName(md.returnType().typeName()))
                ScopeError.LocalDeclError(md);
        }

        SymbolTable newScope = currentScope.openNewScope();
        currentScope.addName(md.toString(),md);
        currentScope.addMethod(methodSignature,newScope);
        currentScope = newScope;

        if(md.params() != null)
            md.params().visit(this);

        BlockStmt methodBlock = md.methodBlock();
        md.methodBlock().decls().visit(this);
        md.methodBlock().stmts().visit(this);

        md.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
        Any time we encounter a NameExpr, we check to make sure the
        name is found somewhere in the scope hierarchy. If the name
        couldn't be found, then it was not declared, so we will output
        an error message to the user.
    */
    public void visitNameExpr(NameExpr ne) {
        String name = ne.toString();
        if(!currentScope.isNameUsedAnywhere(name)) {
            System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);

            ne.printLine();
            System.out.println(PrettyPrint.RED + ne.getStartPosition() + ": Scoping Error! \'"
                    + name + "\' has not yet been declared in the current scope.\n" + PrettyPrint.RESET);
            System.exit(1);
        }
    }

    /*
        When we are instantiating an object using the "new"
        keyword, we have to check two things.

            1. We need to first see if the class has been defined somewhere
               in the scope hierarchy. This could mean either the class
               is declared within the main program or in any imports
               that the user included.
            2. To assign default values to class fields, a user must explicitly
               write what value will be stored into each field. This means we
               have to check if the user wrote the appropriate field name, and
               they only assigned a value to each field once.
    */
    public void visitNewExpr(NewExpr ne) {
        // Error Check #1: Check if the class has been defined in the scope hierarchy
        String lookupName = ne.classType().toString();
        NameNode cd = currentScope.findName(lookupName);
        if(cd == null) {
            System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);

            ne.printLine();
            System.out.println(PrettyPrint.RED + ne.getStartPosition() + ": Scoping Error! Class \'"
                    + lookupName + "\' does not exist and can not be instantiated.\n" + PrettyPrint.RESET);
            System.exit(1);
        }

        SymbolTable classST = cd.declName().asTopLevelDecl().asClassDecl().symbolTable;

        Vector<Var> newArgs = ne.args();
        HashSet<String> seen = new HashSet<String>();
        for(int i = 0; i < newArgs.size(); i++) {
            String fieldName = newArgs.get(i).name().toString();
            // Error Check #2: Check if the field name was not defined in the class
            if(!classST.hasName(fieldName))
                ScopeError.LocalDeclError(ne);
            // Error Check #3: Check if the field name was already given a default value earlier
            else if(seen.contains(fieldName))
                ScopeError.LocalDeclError(ne);
            else
                seen.add(fieldName);

            newArgs.get(i).init().visit(this);
        }
    }

    /*
        A ParamDecl will be put in the scope of the current function/method we are in.
        We are going to be strict with the scope checking for the parameter name because
        we do not want it to be the same name as any enumerations/classes/globals we have
        already declared since they will not be able to be used within the function.
    */
    public void visitParamDecl(ParamDecl pd) {
        String paramName = pd.toString();

        if(currentScope.isNameUsedAnywhere(paramName))
            printScopeError(paramName,pd);

        currentScope.addName(paramName,pd);
    }

    /*
    ------------------- WhileStmt OPENS a scope -------------------

        A WhileStmt will store 1 symbol table.
    */
    public void visitWhileStmt(WhileStmt ws) {
        ws.condition().visit(this);

        if(ws.nextExpr() != null)
            ws.nextExpr().visit(this);

        ws.whileBlock().visit(this);

        ws.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }
}
