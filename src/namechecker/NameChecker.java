package namechecker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.*;
import ast.top_level_decls.*;
import messages.errors.ErrorBuilder;
import messages.MessageType;
import messages.errors.scope_error.ScopeErrorFactory;
import utilities.*;

import java.util.HashSet;

public class NameChecker extends Visitor {

    private SymbolTable currentScope;
    private ClassDecl currentClass;
    private ScopeErrorFactory generateScopeError;
    private Vector<String> errors;

    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.generateScopeError = new ScopeErrorFactory();
        this.errors = new Vector<>();
    }

    public NameChecker(SymbolTable st) {
        this.currentScope = st;
        this.interpretMode = true;
        this.generateScopeError = new ScopeErrorFactory();
        this.errors = new Vector<>();
    }

    /*
    _________________________ Assignment Statements _________________________
    We need to make sure the LHS of an assignment statement is either a name
    expression or a field expression. It does not make sense to have any
    construct here since we can only assign a value to a declared variable.
    _________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {

        // ERROR CHECK #1 : Make sure the LHS of an assignment is a name or field
        if(!(as.LHS() instanceof NameExpr) && !(as.LHS() instanceof FieldExpr) && !(as.LHS() instanceof ArrayExpr)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(as)
                    .addErrorType(MessageType.SCOPE_ERROR_325)
                    .addArgs(as.LHS().toString())
                    .addSuggestType(MessageType.SCOPE_SUGGEST_1302)
                    .error());
        }

        super.visitAssignStmt(as);
    }

    public void visitBinaryExpr(BinaryExpr be) {
        be.LHS().visit(this);

        switch(be.binaryOp().toString()) {
            case "instanceof":
            case "!instanceof":
            case "as?":
                if(!currentScope.hasNameSomewhere(be.RHS().toString())) {
                    errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SCOPE_ERROR_328)
                            .addArgs(be.RHS().toString())
                            .error());
                }
                AST classDecl = currentScope.findName(be.RHS().toString()).decl();
                if(!classDecl.isTopLevelDecl() || !classDecl.asTopLevelDecl().isClassDecl()) {
                    errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SCOPE_ERROR_329)
                            .addArgs(be.RHS().toString())
                            .error());
                }
                break;
            default:
                be.RHS().visit(this);
        }
    }

    /*
    _________________________ Block Statements _________________________
    Any time we visit a block statement, we will open a new scope and
    visit the declarations and statements associated with the block
    statement. Once we are finished, then we will close the scope in
    whichever NameNode we called this visit on BlockStmt from.
    ____________________________________________________________________
    */
    public void visitBlockStmt(BlockStmt bs) {
        currentScope = currentScope.openNewScope();

        for(LocalDecl ld : bs.decls()) { ld.visit(this); }
        for(Statement s : bs.stmts()) { s.visit(this); }
    }

    /*
    ______________________ Case Statements ______________________
    A case statement will open a new scope associated with its
    block. There will be no additional error checks done here.
    _____________________________________________________________
    */
    public void visitCaseStmt(CaseStmt cs) {

        cs.choiceLabel().visit(this);
        currentScope = currentScope.openNewScope();

        cs.caseBlock().visit(this);

        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
    ____________________ Choice Statements ____________________
    Like a case statement, a choice statement will open a new
    scope for its block, and there are no additional checks
    needed here.
    ___________________________________________________________
    */
    public void visitChoiceStmt(ChoiceStmt cs) {

        cs.choiceExpr().visit(this);
        for(CaseStmt c : cs.caseStmts()) { c.visit(this); }

        cs.choiceBlock().visit(this);

        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /*
    _________________________ Class Declarations _________________________
    A class will open a new scope, and we will check to make sure the
    class name we are using hasn't been used elsewhere in the program.

    Most of the name checking done here relates to inheritance. We first
    need to make sure the class doesn't inherit itself. Then, we are going
    to be adding every field from the base class into the inherited class
    which means we are not going to allow the redeclaration of field names.
    _____________________________________________________________________
    */
    public void visitClassDecl(ClassDecl cd) {
        String className = cd.name().toString();

        // ERROR CHECK #1: Make sure class name has not been used already
        if(currentScope.isNameUsedAnywhere(className)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(cd)
                    .addErrorType(MessageType.SCOPE_ERROR_316)
                    .addArgs(className)
                    .error());
        }

        currentScope.addName(className,cd);

        String baseClass = null;
        if(cd.superClass() != null) {
            baseClass = cd.superClass().getName().toString();

            // ERROR CHECK #2: Make sure the class isn't inheriting itself
            if(className.equals(baseClass)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.SCOPE_ERROR_317)
                        .addArgs(className)
                        .error());
            }

            // ERROR CHECK #3: Make sure the class we are inheriting from
            //                 already exists
            if(!currentScope.hasNameSomewhere(baseClass)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.SCOPE_ERROR_322)
                        .addArgs(baseClass)
                        .error());
            }
        }

        currentScope = currentScope.openNewScope();
        cd.symbolTable = currentScope;

        for(FieldDecl fd : cd.classBlock().fieldDecls()) { fd.visit(this); }

        if(baseClass != null) {
            ClassDecl baseDecl = currentScope.findName(baseClass).decl().asTopLevelDecl().asClassDecl();
            for(String name : baseDecl.symbolTable.getAllNames().keySet()) {
                if(baseDecl.symbolTable.getAllNames().get(name).decl().isFieldDecl()) {
                    // ERROR CHECK #3: Each field name in a subclass needs to be unique
                    //                 from the fields declared in the base class
                    if(currentScope.hasName(name)) {
                        errors.add(new ErrorBuilder(generateScopeError, interpretMode)
                                .addLocation(cd)
                                .addErrorType(MessageType.SCOPE_ERROR_318)
                                .addArgs(name)
                                .error());
                    }
                    currentScope.addName(name,baseDecl.symbolTable.getAllNames().get(name));
                }
                if (name.contains("/")) { currentScope.addName(name,baseDecl.symbolTable.getAllNames().get(name)); }
                else { currentScope.addName(name + "/" + baseDecl.toString(),baseDecl.symbolTable.getAllNames().get(name)); }
            }
            for(String name: baseDecl.symbolTable.getMethodNames()) { currentScope.addMethod(name); }
        }

        currentClass = cd;
        for(MethodDecl md : cd.classBlock().methodDecls()) { md.visit(this); }
        currentClass = null;

        currentScope = currentScope.closeScope();
    }

    public void visitCompilation(Compilation c) {
        super.visitCompilation(c);

        if(errors.size() > 0) {
            for(String s : errors) { System.out.println(s); }
            System.exit(1);
        }
    }

    /*
    _________________________ Do Statements _________________________
    A Do statement will store one symbol table for its body, and it
    will visit its components to do any additional scope checking.
    _________________________________________________________________
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
                    .addErrorType(MessageType.SCOPE_ERROR_305)
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
                        .addErrorType(MessageType.SCOPE_ERROR_306)
                        .addArgs(constantName)
                        .error());
            }
            currentScope.addName(constantName,ed);
        }
    }

    /*
    _________________________ Field Declarations _________________________
    For field declarations, we want to make sure each field has a unique
    name and none of the names are reused. There are no other checks
    needed here.
    ______________________________________________________________________
    */
    public void visitFieldDecl(FieldDecl fd) {
        String fieldName = fd.var().name().toString();

        // ERROR CHECK #1: Make sure the name hasn't been used already
        if(currentScope.hasName(fieldName)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.SCOPE_ERROR_315)
                    .addArgs(fd.toString())
                    .error());
        }
        currentScope.addName(fieldName, fd);
    }

    /*
    _________________________ Field Expressions _________________________
    During name checking, we will check whether the name of the object was
    declared if we want to access a field. We can not name check fields yet
    because we will need the type of the object, so this has to be checked
    during type checking instead.
    _____________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) {
        if(!fe.fieldTarget().toString().equals("this")) { fe.fieldTarget().visit(this); }
    }

    /*
    _________________________ For Statements _________________________
    A for statement only needs to store a single symbol table for its
    body. No scope checking is done in this visit, so we will just
    visit the various components of the for statement to handle any
    potential error checks for us.
    __________________________________________________________________
    */
    public void visitForStmt(ForStmt fs) {
        currentScope = currentScope.openNewScope();

        fs.loopVar().visit(this);
        fs.condLHS().visit(this);
        fs.condRHS().visit(this);
        fs.forBlock().visit(this);

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
        // ERROR CHECK #1: Make sure the function name is not being used already
        if(currentScope.hasName(fd.toString())) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.SCOPE_ERROR_311)
                    .addArgs(fd.toString())
                    .error());
        }

        String funcSignature = fd.toString() + "/";
        if(fd.params() != null) { funcSignature += fd.paramSignature(); }

        // ERROR CHECK #2: Make sure the function signature does NOT already exist
        //                 somewhere in the scope hierarchy
        if(currentScope.hasNameSomewhere(funcSignature)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.SCOPE_ERROR_312)
                    .addArgs(fd.toString())
                    .error());
        }

        currentScope.addName(funcSignature, fd);
        currentScope.addMethod(fd.toString());

        SymbolTable newScope = currentScope.openNewScope();
        currentScope = newScope;

        // If we have any parameters, then we will visit them to ensure
        // their names do not produce any conflicts.
        for(ParamDecl pd : fd.params()) { pd.visit(this); }

        for(LocalDecl ld : fd.funcBlock().decls()) { ld.visit(this); }
        for(Statement s : fd.funcBlock().stmts()) { s.visit(this); }

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
                    .addErrorType(MessageType.SCOPE_ERROR_302)
                    .addArgs(globalName)
                    .error());
        }
        // ERROR CHECK #2) Make sure a global variable is not initialized to itself
        if(gd.var().init() != null) {
            if(gd.var().init().toString().equals(globalName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(gd)
                        .addErrorType(MessageType.SCOPE_ERROR_303)
                        .addArgs(globalName)
                        .error());
            }
            gd.var().init().visit(this);
        }

        currentScope.addName(gd.toString(), gd);
    }

    /*
    ___________________________ If Statements ___________________________
    For an If statement, we are going to be storing two separate symbol
    tables. One symbol table will be stored for the body of the if branch
    while the second symbol table is for the else branch (if it exists).
    _____________________________________________________________________
    */
    public void visitIfStmt(IfStmt is) {

        is.condition().visit(this);

        is.ifBlock().visit(this);
        is.symbolTableIfBlock = currentScope;
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.elifStmts()) { e.visit(this); }

        if(is.elseBlock() != null) {
            is.elseBlock().visit(this);
            is.symbolTableElseBlock = currentScope;
            currentScope = currentScope.closeScope();
        }
    }

    /*
    ___________________________ Invocations ___________________________
    For invocations, we will only check if the function name has been
    declared in the program. We can not name check methods yet since
    we have to know the class in which they were declared in. This will
    only be known during type checking.
    ___________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        // Function Invocation Case
        if(in.target() == null) {
            String funcName = in.toString();
            // ERROR CHECK #1: Make sure the function was declared previously
            if(!currentScope.hasMethodSomewhere(funcName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.SCOPE_ERROR_319)
                        .addArgs(funcName)
                        .error());
            }
        }
        else { in.target().visit(this); }

        for(Expression e : in.arguments()) { e.visit(this); }
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
                     .addErrorType(MessageType.SCOPE_ERROR_300)
                     .addArgs(localName)
                     .error());
        }

        // ERROR CHECK #2) Make sure a local variable
        //                 is not initialized to itself
        if(ld.var().init() != null) {
            if(ld.var().init().toString().equals(localName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ld)
                        .addErrorType(MessageType.SCOPE_ERROR_301)
                        .addArgs(localName)
                        .error());
            }
            ld.var().init().visit(this);
        }

        currentScope.addName(localName,ld);
    }

    /*
    _______________________ Main Declaration _______________________
    When we visit a main function, it's scope will be in the same
    scope as the global scope. This means users can not redefine
    global scope values unless they are in smaller scopes.
    ________________________________________________________________
    */
    public void visitMainDecl(MainDecl md) {
        for(ParamDecl e : md.args()) { e.visit(this); }

        for(LocalDecl ld : md.mainBody().decls()) { ld.visit(this); }
        for(Statement s : md.mainBody().stmts()) { s.visit(this); }

        md.symbolTable = currentScope;
        currentScope.closeScope();
    }

    /*
    ________________________ Method Declarations ________________________
    Method declarations work the same way as function declarations. We
    will open a new scope when visiting a method, and we will store methods
    by their function signatures since we can overload methods in C Minor.
    _____________________________________________________________________
    */
    public void visitMethodDecl(MethodDecl md) {
        // ERROR CHECK #1: Make sure the method name has not been used yet
        if(currentScope.hasName(md.toString())) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(md)
                    .addErrorType(MessageType.SCOPE_ERROR_313)
                    .addArgs(md.toString())
                    .error());
        }

        String methodSignature = md + "/";
        if(md.params() != null) { methodSignature += md.paramSignature(); }

        // ERROR CHECK #2: Make sure method signature is unique to support overloading
        if(currentScope.hasName(methodSignature)) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(md)
                    .addErrorType(MessageType.SCOPE_ERROR_314)
                    .addArgs(md.toString())
                    .error());
        }

        currentScope.addName(methodSignature,md);
        currentScope.addMethod(md.toString());

        SymbolTable newScope = currentScope.openNewScope();
        currentScope = newScope;

        for(ParamDecl pd : md.params()) { pd.visit(this); }

        for(LocalDecl ld : md.methodBlock().decls()) { ld.visit(this); }
        for(Statement s : md.methodBlock().stmts()) { s.visit(this); }

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
                    .addErrorType(MessageType.SCOPE_ERROR_307)
                    .addArgs(name)
                    .error());
        }
        else {
            NameNode nn = currentScope.findName(name);
            if(nn.decl().isTopLevelDecl()
                    && (nn.decl().asTopLevelDecl().isEnumDecl()
                    || nn.decl().asTopLevelDecl().isClassDecl())
                    && nn.decl().toString().equals(name)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                                .addLocation(ne)
                                .addErrorType(MessageType.SCOPE_ERROR_326)
                                .addArgs(name)
                                .error());
            }
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
        if(cd == null) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(ne)
                    .addErrorType(MessageType.SCOPE_ERROR_308)
                    .addArgs(lookupName)
                    .error());
        }

        SymbolTable classST = cd.decl().asTopLevelDecl().asClassDecl().symbolTable;

        Vector<Var> newArgs = ne.args();
        HashSet<String> seen = new HashSet<String>();
        for(int i = 0; i < newArgs.size(); i++) {
            Var v = newArgs.get(i);
            String fieldName = v.name().toString();
            // ERROR CHECK #2) Check if the field name was not defined in the class
            if(!classST.hasName(fieldName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_309)
                        .addArgs(fieldName,lookupName)
                        .error());
            }
            // ERROR CHECK #3) Check if the field name was already given a default value earlier
            else if(seen.contains(fieldName)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_310)
                        .addArgs(fieldName)
                        .error());
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
                    .addErrorType(MessageType.SCOPE_ERROR_304)
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
