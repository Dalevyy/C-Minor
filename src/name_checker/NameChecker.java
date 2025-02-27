package name_checker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.*;
import ast.top_level_decls.*;
import messages.errors.ErrorBuilder;
import messages.errors.ErrorType;
import messages.errors.ScopeErrorFactory;
import utilities.*;
import java.util.ArrayList;

import java.util.HashSet;

public class NameChecker extends Visitor {

    private SymbolTable currentScope;
    private ScopeErrorFactory generateScopeError;
    private ArrayList<String> errors;

    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.generateScopeError = new ScopeErrorFactory();
    }

    public NameChecker(SymbolTable st) {
        this.currentScope = st;
        this.interpretMode = true;
        this.generateScopeError = new ScopeErrorFactory();
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

        cs.caseBlock().visit(this);

        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    // ChoiceStmt: Opens a new scope
    public void visitChoiceStmt(ChoiceStmt cs) {

        cs.choiceExpr().visit(this);
        cs.caseStmts().visit(this);

        cs.choiceBlock().visit(this);

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

        if(currentScope.isNameUsedAnywhere(className)) {
//            msg = new ScopeError(className,cd,currentScope, ErrorType.REDECL);
//            try { msg.printMsg(); }
//            catch(Exception e) { throw e; }
        }

        String baseClass = null;
        if(cd.superClass() != null) {
            baseClass = cd.superClass().getName().toString();
            if(className.equals(baseClass)) {
//                msg = new ScopeError(className,cd,currentScope,ErrorType.INHERIT_SELF);
//                try { msg.printMsg(); }
//                catch(Exception e) { throw e; }
            }
            // className += "_" + cd.superClass().getName().toString();
        }

        currentScope.addName(className,cd);

        currentScope = currentScope.openNewScope();

        cd.classBlock().fieldDecls().visit(this);

        if(baseClass != null) {
            ClassDecl baseDecl = currentScope.findName(baseClass).declName().asTopLevelDecl().asClassDecl();
            for(String name : baseDecl.symbolTable.getVarNames().keySet()) {
                if(baseDecl.symbolTable.getVarNames().get(name).declName().isFieldDecl()) {
                    if(currentScope.hasName(name)) {
//                        msg = new ScopeError(name,cd,currentScope,ErrorType.FIELD_NAME_USED_ALREADY);
//                        try { msg.printMsg(); }
//                        catch(Exception e) { throw e; }
                    }
                    currentScope.addName(name,baseDecl.symbolTable.getVarNames().get(name));
                }
            }
        }

        cd.classBlock().methodDecls().visit(this);

        cd.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    public void visitCompilation(Compilation c) {
        super.visitCompilation(c);

//        if(msgs.size() > 0) {
//            for(Message m : msgs)
//                m.printMsg();
//            System.exit(1);
//        }
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
    __________________________ Enum Declarations __________________________
    An EnumDecl will be the first construct a user can define in a C Minor
    program. Thus, we want to check if the name binded to an Enum isn't
    already used by another Enum in the same file OR by some other construct
    located in an import file.

    Additionally, every constant inside an Enum will become global. Thus,
    if we have two or more Enums, all their constants need to have different
    names in order for the program to compile.
    _______________________________________________________________________
    */
    public void visitEnumDecl(EnumDecl ed) {
        String enumName = ed.name().toString();

        // ERROR CHECK #1) Make sure the Enum name has not been used yet
        if(currentScope.hasName(enumName)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(ed)
                    .addErrorType(ErrorType.SCOPE_ERROR_305)
                    .addArgs(enumName)
                    .error());
        }
        currentScope.addName(enumName,ed);

        // ERROR CHECK #2) Make sure each constant in the enum has a name
        //                 that hasn't been used elsewhere yet
        for(int i = 0; i < ed.enumVars().size(); i++) {
            String constantName = ed.enumVars().get(i).asVar().toString();
            if(currentScope.hasName(constantName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ed)
                        .addErrorType(ErrorType.SCOPE_ERROR_306)
                        .addArgs(constantName)
                        .error());
            }
            currentScope.addName(constantName,ed);
        }
    }

    /*
    Inside a class, it is fine if we have a field declaration's
    name be binded to some other construct outside the class
    since we will treat the class as its own namespace. :)
    */
    public void visitFieldDecl(FieldDecl fd) {
        String fieldName = fd.var().name().toString();

        if(currentScope.hasName(fieldName)) {
//            msg = new ScopeError(fieldName,fd,currentScope,ErrorType.REDECL);
//            try { msg.printMsg(); }
//            catch(Exception e) { throw e; }
        }

        currentScope.addName(fieldName, fd);
    }

    /*
    ___________________________ Field Expressions ___________________________
    During name checking, we will check whether the name of the object was
    declared if we want to access a field. We can not name check fields yet
    because we will need the type of the object, so this has to be checked
    during type checking instead.
    _________________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) { fe.fieldTarget().visit(this); }

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

    /*
    _______________________ Function Declarations _______________________
    When we visit a function, we will open a new scope directly.

    This is done since in C Minor, parameters will be in the same scope
    as the function body.

    For functions, we are going to store their references as function
    signatures since C Minor supports function overloading.
    _____________________________________________________________________
    */
    public void visitFuncDecl(FuncDecl fd) {
        String funcSignature = fd.toString() + "/";
        if(fd.params() != null) { funcSignature += fd.paramSignature(); }
        System.out.println(funcSignature);

        // ERROR CHECK #1: Make sure the function signature does NOT already exist
        //                 somewhere in the scope hierarchy
        if(currentScope.hasNameSomewhere(funcSignature)) {
//            msg = new ScopeError(funcSignature,fd,currentScope,ErrorType.REDECL);
//            try { msg.printMsg(); }
//            catch(Exception e) { throw e; }
        }

        SymbolTable newScope = currentScope.openNewScope();
        currentScope.addName(funcSignature, fd);
        currentScope = newScope;

        // If we have any parameters, then we will visit them to ensure
        // their names do not produce any conflicts.
        if(fd.params() != null) { fd.params().visit(this); }

        fd.funcBlock().decls().visit(this);
        fd.funcBlock().stmts().visit(this);

        fd.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
    _______________________ Global Declarations _______________________
    For global variables, we want to make sure the name used does not
    conflict with the name of any previously declared enumerations.

    Additionally, we will need to make sure the name doesn't conflict
    with any other names from import files in the future.
    ____________________________________________________________________
    */
    public void visitGlobalDecl(GlobalDecl gd) {
        String globalName = gd.toString();

        // ERROR CHECK #1) Check if the name is already declared in the current scope
        if(currentScope.hasName(globalName)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(gd)
                    .addErrorType(ErrorType.SCOPE_ERROR_302)
                    .addArgs(globalName)
                    .error());
        }
        // ERROR CHECK #2) Make sure a global variable is not initialized to itself
        if(gd.var().init() != null) {
            if(gd.var().init().toString().equals(globalName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(gd)
                        .addErrorType(ErrorType.SCOPE_ERROR_303)
                        .addArgs(globalName)
                        .error());
            }
            gd.var().init().visit(this);
        }

        currentScope.addName(gd.toString(), gd);
    }

    /*
    ------------------- IfSmt OPENS a scope -------------------

        An IfStmt will store 2 separate symbol tables:
            1. The symbol table for the IF branch
            2. The symbol table for the ELSE branch (OPTIONAL)
    */

    /*
    ___________________________ If Statements _____________________________
    While loops will contain one symbol table for their block statement. We
    will open a scope when we visit the block statement associated with a
    while statement and then save the scope into the while statement node.
    ________________________________________________________________________
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
    __________________________ Invocations __________________________
    Right now, we are only going to be name checking function
    invocations. Method invocations are not possible right now since
    we need to know the type of the target expression to determine
    what class a method is contained in, but we don't have enough information to perform this check yet.

    We could check for simple method invocations (and by extension,
    simple field expressions) if we really wanted to, but it's better
    to wait until type checking to do these tasks. :)
    _________________________________________________________________
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
            if(!currentScope.hasNameSomewhere(name)) {
//                msg = new ScopeError(name,in,currentScope,ErrorType.CLASS_DECLARATION_IS_MISSING);
//                try { msg.printMsg(); }
//                catch(Exception e) { throw e; }
            }
        }

        // Regardless if we have a method or function, we will
        // perform a name check on any arguments that represent names
        if(in.arguments() != null)
            in.arguments().visit(this);
    }

    /*
    _________________________ Local Declarations _________________________
    When a user declares a local variable, we are going to check if the
    name used by this local already exists in the current scope.

    We also are going to be pedantic and make sure the name is not already
    binded to a TopLevelDecl node in order to avoid confusion.

    If we have any form of redeclaration, we are going to error out.
    ______________________________________________________________________
    */
    public void visitLocalDecl(LocalDecl ld) {
        String localName = ld.toString();

        // ERROR CHECK #1) Check if the name has already been
        //                 declared in the current scope
        if(currentScope.hasName(localName)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                     .addLocation(ld)
                     .addErrorType(ErrorType.SCOPE_ERROR_300)
                     .addArgs(localName)
                     .error());
        }

        // ERROR CHECK #2) Make sure a local variable
        //                 is not initialized to itself
        if(ld.var().init() != null) {
            if(ld.var().init().toString().equals(localName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ld)
                        .addErrorType(ErrorType.SCOPE_ERROR_301)
                        .addArgs(localName)
                        .error());
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
        if(md.args() != null)
            md.args().visit(this);

        md.mainBody().visit(this);

        md.symbolTable = currentScope;
        currentScope.closeScope();
    }

    /*
    ________________________ Method Declarations ________________________
    Create a scope.
    _________________________________________________________________________
    */
    public void visitMethodDecl(MethodDecl md) {
        String methodSignature = md.toString() + "(" + md.paramSignature() + ")" + md.returnType().typeSignature();
        if(currentScope.hasName(methodSignature)) {
//            msg = new ScopeError(methodSignature,md,currentScope,ErrorType.REDECL);
//            try { msg.printMsg(); }
//            catch(Exception e) { throw e; }
        }

        SymbolTable newScope = currentScope.openNewScope();
        currentScope.addName(methodSignature,md);
        currentScope = newScope;

        if(md.params() != null) { md.params().visit(this); }

        md.methodBlock().decls().visit(this);
        md.methodBlock().stmts().visit(this);

        md.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
    ___________________________ Name Expressions ___________________________
    Any time we encounter a NameExpr, we check to make sure the name is
    found somewhere in the scope hierarchy. If the name couldn't be found,
    then it was not declared, so we will output an error message to the user.
    ________________________________________________________________________
    */
    public void visitNameExpr(NameExpr ne) {
        String name = ne.toString();
        if(!currentScope.isNameUsedAnywhere(name)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(ne)
                    .addErrorType(ErrorType.SCOPE_ERROR_307)
                    .addArgs(name)
                    .error());
        }
    }

    /*
    ____________________________ New Expressions ____________________________
    When we are instantiating an object using the "new" keyword, there are
    2 name checks we must perform.

         1. We need to first see if the class has been defined somewhere
            in the scope hierarchy. This could mean either the class
            is declared within the main program or in any imports
            that the user included.

         2. To assign default values to class fields, a user must explicitly
            write what value will be stored into each field. This means we
            have to check if the user wrote the appropriate field name, and
            they only assigned a value to each field once.
    _________________________________________________________________________
    */
    public void visitNewExpr(NewExpr ne) {
        // ERROR CHECK #1) Check if the class has been defined in the scope hierarchy
        String lookupName = ne.classType().toString();
        NameNode cd = currentScope.findName(lookupName);
        if(cd == null)
        {} // msg = new ScopeError(ne,currentScope, ErrorType.CLASS_DECLARATION_IS_MISSING);

        SymbolTable classST = cd.declName().asTopLevelDecl().asClassDecl().symbolTable;

        Vector<Var> newArgs = ne.args();
        HashSet<String> seen = new HashSet<String>();
        for(int i = 0; i < newArgs.size(); i++) {
            Var v = newArgs.get(i);
            String fieldName = v.name().toString();
            // ERROR CHECK #2) Check if the field name was not defined in the class
            if(!classST.hasName(fieldName)) {
//                msg = new ScopeError(fieldName,v,currentScope, ErrorType.MISSING_FIELD);
//                try { msg.printMsg(); }
//                catch(Exception e) { throw e; }
            }
            // ERROR CHECK #3) Check if the field name was already given a default value earlier
            else if(seen.contains(fieldName)) {
//                msg = new ScopeError(fieldName,v,currentScope, ErrorType.FIELD_VAL_GIVEN);
//                try { msg.printMsg(); }
//                catch(Exception e) { throw e; }
            }

            seen.add(fieldName);
            newArgs.get(i).init().visit(this);
        }
    }

    /*
    ________________________ Parameter Declarations ________________________
    A parameter will be added to the symbol table associated with a function
    or method. Parameter names can not shadow the names of enumerations,
    classes, or global names since we want to allow users to access these
    constructs inside a function or method.
    ________________________________________________________________________
    */
    public void visitParamDecl(ParamDecl pd) {
        String paramName = pd.toString();

        // ERROR CHECK #1) Make sure the parameter name hasn't been used
        // in any constructs that were declared before the function/method
        if(currentScope.isNameUsedAnywhere(paramName)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(pd)
                    .addErrorType(ErrorType.SCOPE_ERROR_304)
                    .addArgs(paramName)
                    .error());
        }

        currentScope.addName(paramName,pd);
    }

    /*
    __________________________ While Statements __________________________
    While loops will contain one symbol table for their block statement.
    We will open a scope when we visit the block statement associated with
    a WhileStmt and then save the scope into the while statement node.
    ______________________________________________________________________
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
