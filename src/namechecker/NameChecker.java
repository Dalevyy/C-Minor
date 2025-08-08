package namechecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.BinaryExpr;
import ast.expressions.Expression;
import ast.expressions.FieldExpr;
import ast.expressions.Invocation;
import ast.expressions.NameExpr;
import ast.expressions.NewExpr;
import ast.misc.CompilationUnit;
import ast.misc.ParamDecl;
import ast.misc.TypeParam;
import ast.misc.Var;
import ast.statements.AssignStmt;
import ast.statements.BlockStmt;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.LocalDecl;
import ast.statements.RetypeStmt;
import ast.statements.Statement;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.ImportDecl;
import ast.topleveldecls.MainDecl;
import java.util.HashSet;
import messages.MessageHandler;
import messages.errors.ErrorBuilder;
import messages.MessageNumber;
import messages.errors.scope.ScopeError;
import messages.errors.semantic.SemanticError;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * C Minor Scope Resolution Pass
 * <p><br>
 *     This is the first major semantic pass in C Minor which checks
 *     if all names within a program can correctly be resolved.
 * </p>
 * @author Daniel Levy
 */
public class NameChecker extends Visitor {

    /**
     * Current scope we are name checking within.
     */
    private SymbolTable currentScope;

    /**
     * Current class we are name checking in (if applicable).
     */
    private ClassDecl currentClass;

    /**
     * Current variable declaration we are name checking (for initialization errors)
     */
    private String currentVariable = "";

    /**
     * Creates name checker in compilation mode
     */
    public NameChecker(String fileName) {
        this.currentScope = new SymbolTable();
        this.handler = new MessageHandler(fileName);
    }

    /**
     * Creates name checker in interpretation mode
     * @param st Compilation Unit Symbol Table
     */
    public NameChecker(SymbolTable st) {
        this.currentScope = st;
        this.handler = new MessageHandler();
    }

    /**
     * Updates the {@code currentClass} symbol table to contain all entries from a base class's symbol table.
     * <p>
     *     We will go through the base class's symbol table and if there are no errors,
     *     we will add all fields and methods to the current class, so it knows which
     *     declarations it has access to. We will also check if the user redeclared any
     *     fields from the base class since this isn't permitted.
     * </p>
     * @param baseClass The base class the user is inheriting from.
     */
    private void addBaseClassTable(ClassDecl baseClass) {
        for(String name : baseClass.getScope().getAllNames().keySet()) {
            AST currentDecl = baseClass.getScope().findName(name).getDecl();
            if(currentDecl.asClassNode().isFieldDecl()) {
                // ERROR CHECK #1: This checks if the field was already defined in the class hierarchy.
                if(currentScope.hasName(name)) {
                    handler.createErrorBuilder(ScopeError.class)
                           .addLocation(currentClass.getScope().findName(name).getDecl())
                           .addErrorNumber(MessageNumber.SCOPE_ERROR_324)
                           .addErrorArgs(name, currentClass)
                           .asScopeErrorBuilder()
                           .addOriginalDeclaration(currentDecl)
                           .generateError();
                }
                currentScope.addName(name,currentDecl.asClassNode().asFieldDecl());
            }
        }

        for(String name : baseClass.getScope().getMethodNames())
            currentScope.addMethod(name);
    }

    /**
     * Validates if a user properly overrides methods when using inheritance.
     * <p>
     *     This method helps to perform error checking when a user overrides
     *     base class methods. For every method in the class, we want to ensure
     *     the user correctly uses the 'override' keyword if they are going to
     *     redefine any methods. This ensures the user explicitly understands
     *     which methods are being overridden to help understand their code better.
     * </p>
     * @param baseClass The base class declaration the {@code currentClass} inherits from
     */
    private void checkOverriddenMethods(ClassDecl baseClass) {
        // Visit each method defined in the current class we are in
        for(MethodDecl subMethod : currentClass.getClassBody().getMethods()) {
            boolean methodFoundInBaseClass = false;

            subMethod.visit(this);

            if(baseClass != null) {
                for(MethodDecl baseMethod : baseClass.getClassBody().getMethods()) {
                    if(baseMethod.getMethodSignature().equals(subMethod.getMethodSignature())) {
                        methodFoundInBaseClass = true;
                        // ERROR CHECK #1: This checks if the user marked the method as overridden in the subclass.
                        if(!subMethod.isOverridden()) {
                            handler.createErrorBuilder(ScopeError.class)
                                   .addLocation(subMethod)
                                   .addErrorNumber(MessageNumber.SCOPE_ERROR_311)
                                   .addErrorArgs(subMethod, baseClass)
                                   .generateError();
                        }
                    }
                }
            }

            // ERROR CHECK #2: This checks if a user tries to redefine a method not found in the base class.
            if(!methodFoundInBaseClass && subMethod.isOverridden()) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(subMethod)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_312)
                       .addErrorArgs(subMethod)
                       .generateError();
            }
        }
    }

    /**
     * Evaluates the names of an assignment statement.
     * <p>
     *     For an assignment statement, we want to make sure the LHS
     *     evaluates to some variable or name in the program since we
     *     can only assign to something that takes up a memory location.
     *     Since this is a more of a semantic issue, we will generate a
     *     semantic error message to the user if there are any issues.
     * </p>
     * @param as Assignment Statement
     */
    public void visitAssignStmt(AssignStmt as) {
        // ERROR CHECK #1: This checks if a valid name was used in the LHS of an assignment statement.
        if(!as.getLHS().isNameExpr() && !as.getLHS().isFieldExpr() && !as.getLHS().isArrayExpr()) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(as)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_707)
                   .addErrorArgs(as.getLHS())
                   .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1702)
                   .generateError();
        }

        super.visitAssignStmt(as);
    }

    /**
     * Evaluates the names of a binary expression.
     * <p>
     *     When visiting a binary expression, we want to specifically check
     *     if a user properly uses names for the built-in binary operators
     *     for objects. This includes {@code instanceof}, {@code !instanceof},
     *     and {@code as?}. For all other operators, we will let other
     *     visits handle the error checking for us.
     * </p>
     * @param be Binary Expression
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.getLHS().visit(this);
        be.getRHS().visit(this);

        String binOp = be.getBinaryOp().toString();
        switch(binOp) {
            case "instanceof":
            case "!instanceof":
            case "as?":
                // ERROR CHECK #1: This checks if the LHS represents a valid variable name.
                if(!be.getLHS().isNameExpr() && !be.getLHS().isFieldExpr() && !be.getLHS().isArrayExpr()) {
                    handler.createErrorBuilder(SemanticError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.SEMANTIC_ERROR_708)
                           .addErrorArgs(be.getLHS(), binOp)
                           .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1703)
                           .addSuggestionArgs(binOp)
                           .generateError();
                }

                AST cd = currentScope.findName(be.getRHS().toString()).getDecl();
                // ERROR CHECK #2: This checks if the RHS represents a class name.
                if(!cd.isTopLevelDecl() || !cd.asTopLevelDecl().isClassDecl()) {
                    handler.createErrorBuilder(ScopeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.SCOPE_ERROR_323)
                           .addErrorArgs(be.getRHS(),binOp)
                           .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1304)
                           .addSuggestionArgs(binOp)
                           .generateError();
                }
        }
    }

    /**
     * Evaluates the scope of a block statement.
     * <p>
     *     When we visit a block statement, a new scope is opened.
     *     All local declarations and statements will then be visited.
     *     If {@code visitBlockStatement} was called by another construct,
     *     that construct will close the {@code currentScope}. Otherwise,
     *     we will do it here.
     * </p>
     * @param bs Block Statement
     */
    public void visitBlockStmt(BlockStmt bs) {
        currentScope = currentScope.openNewScope();

        for(LocalDecl ld : bs.getLocalDecls())
            ld.visit(this);
        for(Statement s : bs.getStatements())
            s.visit(this);

        if(bs.getParent() == null)
            currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the scope of a case statement.
     * <p>
     *     A case statement contains one symbol
     *     table for its block statement.
     * </p>
     * @param cs Case Statement
     */
    public void visitCaseStmt(CaseStmt cs) {
        cs.getLabel().visit(this);
        cs.getBody().visit(this);

        cs.scope = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the scope of a choice statement.
     * <p>
     *     A choice statement contains a symbol table
     *     for the default branch. All other case
     *     statements will contain their own individual
     *     symbol table.
     * </p>
     * @param cs Choice Statement
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.getChoiceValue().visit(this);

        for(CaseStmt c : cs.getCases())
            c.visit(this);

        cs.getDefaultBody().visit(this);
        cs.scope = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the scope of a class.
     * <p>
     *     A class contains a symbol table for its block statement. During
     *     this visit, we are primarily concerned with making sure the user
     *     correctly uses inheritance.
     * </p>
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        ClassDecl baseClass = null;

        // ERROR CHECK #1: This checks if the class name is already declared in the program.
        if(currentScope.isNameUsedAnywhere(cd.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(cd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_307)
                   .addErrorArgs(cd)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(cd.toString()).getDecl())
                   .generateError();
        }

        currentScope.addName(cd.toString(),cd);

        if(cd.getSuperClass() != null) {
            // ERROR CHECK #2: This checks if the class tries to inherit itself.
            if(cd.toString().equals(cd.getSuperClass().toString())) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(cd)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_308)
                        .addErrorArgs(cd)
                        .generateError();
            }

            // ERROR CHECK #3: This checks if the inherited class was declared in the program.
            if(!currentScope.hasNameSomewhere(cd.getSuperClass().toString())) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(cd)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_309)
                        .addErrorArgs(cd,cd.getSuperClass())
                        .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1301)
                        .addSuggestionArgs(cd.getSuperClass())
                        .generateError();
            }

            baseClass = currentScope.findName(cd.getSuperClass().toString()).getDecl().asTopLevelDecl().asClassDecl();
        }

        currentScope = currentScope.openNewScope();
        cd.setScope(currentScope);
        currentClass = cd;

        for(TypeParam tp : cd.getTypeParams())
            currentScope.addName(tp.toString(),tp);

        for(FieldDecl fd : cd.getClassBody().getFields())
            fd.visit(this);

        if(baseClass != null)
            addBaseClassTable(baseClass);
        checkOverriddenMethods(baseClass);

        currentScope = currentScope.closeScope();
        currentClass = null;
    }

    /**
     * Begins the C Minor scope resolution pass.
     * <p>
     *     During compilation mode, {@code visitCompilation} will be the first
     *     method executed when we start the scope resolution pass.
     * </p>
     * @param c Compilation Unit
     */
    public void visitCompilationUnit(CompilationUnit c) {
        super.visitCompilationUnit(c);
        c.setScope(currentScope);
    }

    /**
     * Evaluates the scope of a do statement.
     * <p>
     *     A do while loop contains one symbol table
     *     for its block statement.
     * </p>
     * @param ds Do Statement
     */
    public void visitDoStmt(DoStmt ds) {
        ds.getBody().visit(this);

        ds.scope = currentScope;
        currentScope = currentScope.closeScope();

        ds.getCondition().visit(this);
    }

    /**
     * Evaluates the name of an enum.
     * <p>
     *     An {@code EnumDecl} is the first construct a user is allowed to define
     *     in a C Minor. We want to ensure the user hasn't used the enum name before,
     *     and we also want to make sure each constant name associated with the enum
     *     hasn't been used before.
     * </p>
     * @param ed Enum Declaration
     */
    public void visitEnumDecl(EnumDecl ed) {
        // ERROR CHECK #1: This checks if the enum name was already declared somewhere in the program.
        if(currentScope.hasName(ed.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(ed)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_321)
                    .addErrorArgs(ed)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(currentScope.findName(ed.toString()).getDecl())
                    .generateError();
        }

        currentScope.addName(ed.toString(),ed);

        // ERROR CHECK #2: This checks if each constant name in the enum was already declared somewhere in the program.
        for(Var constant : ed.getConstants()) {
            if(currentScope.hasName(constant.toString())) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ed)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_322)
                        .addErrorArgs(constant,ed)
                        .asScopeErrorBuilder()
                        .addOriginalDeclaration(currentScope.findName(constant.toString()).getDecl())
                        .generateError();
            }
            currentScope.addName(constant.toString(),ed);
        }
    }

    /**
     * Evaluates the name of a field declaration.
     * <p>
     *     When declaring a field inside of a class, we want to make sure the name doesn't
     *     conflict with any other names previously declared by the user. If there are no
     *     issues with the field, we will add it to the {@code currentScope} of the class.
     * </p>
     * @param fd Field Declaration
     */
    public void visitFieldDecl(FieldDecl fd) {
        // ERROR CHECK #1: This checks if the field name was already used in the current class.
        if(currentScope.hasName(fd.toString())) {
            AST varLocation = currentScope.findName(fd.toString()).getDecl();

//            if(interpretMode)
//                currentScope = currentScope.closeScope();

            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(fd)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_310)
                    .addErrorArgs(fd,currentClass)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(varLocation)
                    .generateError();
        }

        currentScope.addName(fd.toString(),fd);

//        if(fd.var().init() != null) {
//            currentVariable = fd.toString();
//            fd.var().init().visit(this);
//            currentVariable = "";
//        }
    }

    /**
     * Evaluates the name of a field expression.
     * <p>
     *     For a field expression, we will only check to see if the target's
     *     name can be resolved at this point. We have to wait until we know
     *     the target's type to do proper name checking for the rest of the
     *     access expression (in case it is too complicated).
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        if(!fe.getTarget().isThisStmt())
            fe.getTarget().visit(this);
    }

    /**
     * Evaluates the scope of a for statement.
     * <p>
     *     A for loop contains a symbol table for its block statement.
     *     In C Minor, the loop control variable will be stored in the
     *     scope of the block statement in order to prevent a user from
     *     redeclaring the variable in the block statement's scope.
     * </p>
     *
     * @param fs For Statement
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = currentScope.openNewScope();

        fs.getControlVariable().visit(this);
        fs.getStartValue().visit(this);
        fs.getEndValue().visit(this);

        for(LocalDecl ld : fs.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : fs.getBody().getStatements())
            s.visit(this);

        fs.scope = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the scope of a function.
     * <p>
     *     A function contains a symbol table for its block
     *     statement. For functions, we will be storing their
     *     signatures internally in order to support function
     *     overloading, so we will make sure each function that
     *     is declared has a unique signature.
     * </p>
     * @param fd Function Declaration
     */
    public void visitFuncDecl(FuncDecl fd) {
        // ERROR CHECK #1: We need to check if the current function redeclares a previous function.
        if(currentScope.hasNameSomewhere(fd.getSignature())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(fd)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_306)
                    .addErrorArgs(fd)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(currentScope.findName(fd.getSignature()).getDecl())
                    .generateError();
        }

        currentScope.addName(fd.getSignature(),fd);
        currentScope.addMethod(fd.toString());

        currentScope = currentScope.openNewScope();
        fd.setScope(currentScope);

        for(TypeParam tp : fd.getTypeParams())
            currentScope.addName(tp.toString(),tp);
        for(ParamDecl pd : fd.getParams())
            pd.visit(this);
        for(LocalDecl ld : fd.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : fd.getBody().getStatements())
            s.visit(this);

        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the name of a global declaration.
     * <p>
     *     When declaring a global variable, we want to make sure the name doesn't
     *     conflict with any other names previously declared by the user. If there
     *     are no issues with the global variable, we will add it to the {@code currentScope}.
     * </p>
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // ERROR CHECK #1: This checks if the user is trying to redeclare a variable with the same name.
        if(currentScope.hasName(gd.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(gd)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_302)
                    .addErrorArgs(gd)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(currentScope.findName(gd.toString()).getDecl())
                    .generateError();
        }

        currentScope.addName(gd.toString(), gd);

//        if(gd.var().init() != null) {
//            currentVariable = gd.toString();
//            gd.var().init().visit(this);
//            currentVariable = "";
//        }
    }

    /**
     * Evaluates the scope of an if statement.
     * <p>
     *     An if statement contains a symbol table for its if
     *     branch alongside a symbol table for its else branch
     *     (if it exists).
     * </p>
     * @param is If Statement
     */
    public void visitIfStmt(IfStmt is) {
        is.getCondition().visit(this);
        is.getIfBody().visit(this);

        is.ifScope = currentScope;
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.getElifs())
            e.visit(this);

        if(is.getElseBody() != null) {
            is.getElseBody().visit(this);
            is.elseScope = currentScope;
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Evaluates the scope of an imported file.
     * <p>
     *     For an import declaration, we will run the {@code NameChecker}
     *     on its compilation unit and save the compilation unit's symbol
     *     table into the main symbol table we are working with. This will
     *     allow us to verify if any names used in the imported file conflict
     *     with any names found in the current file we are checking.
     * </p>
     * @param im Import Declaration
     */
    public void visitImportDecl(ImportDecl im) {
        SymbolTable oldScope = currentScope;
        currentScope = new SymbolTable();

        im.getCompilationUnit().visit(this);

        oldScope.setImportParent(currentScope);
        currentScope = oldScope;
    }

    /**
     * Evaluates the name of an invocation.
     * <p>
     *     At this point, we do not have enough information to properly name
     *     check invocations when we are trying to invoke a method. As a result,
     *     we are only going to be checking if a function invocation was written
     *     correctly, and we will handle method invocations during type checking instead.
     * </p>
     * @param in Invocation
     */
    public void visitInvocation(Invocation in) {
        // ERROR CHECK #1: This checks to make sure the invocation name was declared in the program.
        if(currentClass == null && !currentScope.hasMethodSomewhere(in.toString()) && !in.isLengthInvocation()) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(in)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_325)
                    .addErrorArgs(in)
                    .generateError();
        }

        for(Expression e : in.getArgs())
            e.visit(this);
    }

    /**
     * Evaluates the name of a local declaration.
     * <p>
     *     When declaring a local variable, we want to make sure the name doesn't
     *     conflict with any other names previously declared by the user. If there
     *     are no issues with the local variable, we will add it to the {@code currentScope}.
     * </p>
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: This checks if the user is trying to redeclare a variable with the same name.
        if(currentScope.hasName(ld.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(ld)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_300)
                    .addErrorArgs(ld)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(currentScope.findName(ld.toString()).getDecl())
                    .generateError();
        }

        currentScope.addName(ld.toString(),ld);
//
//        if(ld.var().init() != null) {
//            currentVariable = ld.toString();
//            ld.var().init().visit(this);
//            currentVariable = "";
//        }
    }

    /**
     * Evaluates the scope of the main function.
     * <p>
     *     The main function of the C Minor program will share the same
     *     symbol table that corresponds to the global scope. This means
     *     we do not create a new scope for main since we do not want a
     *     user to try to redefine any of the top level declarations they
     *     declared within main.
     * </p>
     * @param md Main Declaration
     */
    public void visitMainDecl(MainDecl md) {
        for(ParamDecl e : md.getParams())
            e.visit(this);

        for(LocalDecl ld : md.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : md.getBody().getStatements())
            s.visit(this);

        md.setScope(currentScope);
        currentScope.closeScope();
    }

    /**
     * Evaluates the scope of a method.
     * <p>
     *     A method contains a symbol table for its block statement.
     *     For methods, we will be storing their signatures internally
     *     in order to support method overloading, so we will make sure
     *     each method declared in the current class has a unique signature.
     * </p>
     * @param md Method Declaration
     */
    public void visitMethodDecl(MethodDecl md) {
        // ERROR CHECK #1: This checks to make sure we are not redeclaring a method with the same type arguments.
        if(currentScope.hasName(md.getMethodSignature())) {
            // We will only error out if the method was redeclared in the same class twice. If the method overrides
            // a base class method, then we will check if the method was overridden correctly at a later point.
            if(currentClass.getSuperClass() == null || !currentClass.getScope().hasName(md.getMethodSignature())) {
//                if(interpretMode)
//                    currentScope = currentScope.closeScope();

                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(md)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_313)
                        .addErrorArgs(md,currentClass)
                        .asScopeErrorBuilder()
                        .addOriginalDeclaration(currentScope.findName(md.getMethodSignature()).getDecl())
                        .generateError();
            }
        }

        currentScope.addName(md.getMethodSignature(), md);
        currentScope.addMethod(md.toString());

        currentScope = currentScope.openNewScope();
        md.setScope(currentScope);

        for(ParamDecl pd : md.getParams())
            pd.visit(this);
        for(LocalDecl ld : md.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : md.getBody().getStatements())
            s.visit(this);

        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates the name representing a name expression.
     * <p>
     *     For all names, we will check if they can be traced back to a declaration
     *     somewhere in the program. Additionally, we will perform error checks for
     *     the proper use of the {@code parent} keyword alongside checking if a user
     *     tries to initialize a variable with itself.
     * </p>
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        if(ne.isParentKeyword()) {
            // ERROR CHECK #1: This checks if the 'parent' keyword was used outside of a class.
            if(currentClass == null) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ne.getFullLocation())
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_318)
                        .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1302)
                        .generateError();
            }
            // ERROR CHECK #2: This checks if the 'parent' keyword is used in a class with no inherited classes.
            if(currentClass.getSuperClass() == null) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ne.getFullLocation())
                        .addErrorArgs(currentClass)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_319)
                        .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1303)
                        .generateError();
            }
        }
        // ERROR CHECK #3: This checks if the name was not declared somewhere in the program.
        else if(!currentScope.isNameUsedAnywhere(ne.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(ne.getFullLocation())
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_304)
                    .addErrorArgs(ne)
                    .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1300)
                    .addSuggestionArgs(ne)
                    .generateError();
        }
        else {
            AST varDecl = currentScope.findName(ne.toString()).getDecl();

            // ERROR CHECK #4: This checks if an enum type is used as a name.
            if(varDecl.isTopLevelDecl()) {
                if (varDecl.asTopLevelDecl().isEnumDecl() && varDecl.toString().equals(ne.toString())) {
                    handler.createErrorBuilder(ScopeError.class)
                            .addLocation(ne.getFullLocation())
                            .addErrorNumber(MessageNumber.SCOPE_ERROR_320)
                            .addErrorArgs(ne)
                            .generateError();
                }
            }

            // ERROR CHECK #5: This checks if any variable declaration contains its own name when initialized.
            if(!currentVariable.isEmpty() && currentVariable.equals(ne.toString())) {
                AST decl = currentScope.findName(currentVariable).getDecl();
                ErrorBuilder eb = handler.createErrorBuilder(ScopeError.class)
                                      .addLocation(decl)
                                      .addErrorArgs(currentVariable);

                // Global Declaration Error
                if(decl.isTopLevelDecl())
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_303).generateError();
                // Local Declaration Error
                else if(decl.isStatement())
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_301).generateError();
                // Field Declaration Error
                else
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_326).generateError();
            }
        }
    }

    /**
     * Evaluates the names of a new expression.
     * <p>
     *     When instantiating an object, there are two name checks that need
     *     to be done.
     *     <ol>
     *         <li>
     *             We need to first see if the class has been defined somewhere
     *             in the scope hierarchy. This could mean either the class
     *             is declared within the main program or in any imports
     *             that the user included.
     *         </li>
     *         <li>
     *             To assign default values to class fields, a user must explicitly
     *             write what value will be stored into each field. This means we
     *             have to check if the user wrote the appropriate field name, and
     *             they only assigned a value to each field once.
     *         </li>
     *     </ol>
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        // ERROR CHECK #1: This checks if the class was declared in the program.
        if(!currentScope.hasNameSomewhere(ne.getClassType().getClassName().toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(ne)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_314)
                    .addErrorArgs(ne.getClassType())
                    .generateError();
        }
        ClassDecl cd = currentScope.findName(ne.getClassType().getClassName().toString()).getDecl().asTopLevelDecl().asClassDecl();
        HashSet<String> seen = new HashSet<>();
        for(Var v : ne.getInitialFields()) {
            // ERROR CHECK #2: This checks if the field being initialized was declared in the class.
            if(!cd.getScope().hasName(v.toString())) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ne)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_315)
                        .addErrorArgs(v,ne.getClassType())
                        .generateError();
            }
            // ERROR CHECK #3: This checks if a user tries to initialize a field more than once.
            else if(seen.contains(v.toString())) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ne)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_316)
                        .addErrorArgs(v,ne.getClassType())
                        .generateError();
            }
            seen.add(v.toString());
            v.getInitialValue().visit(this);
        }
    }

    /**
     * Evaluates the name of a parameter.
     * <p>
     *     This visit checks if a parameter name has already been used somewhere
     *     in the user's program. We are not going to allow users to shadow the
     *     names of previously defined constructs in order to let them be used
     *     inside either a function or method. If no errors were found, then
     *     we will add the parameter name to the {@code currentScope} of the function
     *     or method.
     * </p>
     * @param pd Parameter Declaration
     */
    public void visitParamDecl(ParamDecl pd) {
        // ERROR CHECK #1: This checks if the parameter name was already used as another parameter.
        if(currentScope.hasName(pd.toString())) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(pd)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_305)
                    .addErrorArgs(pd)
                    .asScopeErrorBuilder()
                    .addOriginalDeclaration(currentScope.findName(pd).getDecl())
                    .generateError();
        }
        currentScope.addName(pd.toString(),pd);
    }

    /**
     * Evaluates the names of a retype statement.
     * <p>
     *     For retype statements specifically, we want to make sure
     *     the LHS evaluates to be a name. This name represents the
     *     object we want to retype.
     * </p>
     * @param rs Retype Statement
     */
    public void visitRetypeStmt(RetypeStmt rs) {
        // ERROR CHECK #1: This checks to make sure the LHS of a retype statement evaluates to be a name.
        if(!rs.getName().isNameExpr() && !rs.getName().isFieldExpr() && !rs.getName().isArrayExpr()) {
            handler.createErrorBuilder(SemanticError.class)
                    .addLocation(rs)
                    .addErrorNumber(MessageNumber.SEMANTIC_ERROR_709)
                    .addErrorArgs(rs.getName())
                    .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1704)
                    .generateError();
        }

        rs.getName().visit(this);
        rs.getNewObject().visit(this);
    }

    /**
     * Evaluates the scope of a while statement.
     * <p>
     *     A while loop will contain one symbol table
     *     representing its block statement.
     * </p>
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        ws.getCondition().visit(this);
        ws.getBody().visit(this);

        ws.scope = currentScope;
        currentScope = currentScope.closeScope();
    }
}
