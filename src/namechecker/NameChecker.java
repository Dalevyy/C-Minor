package namechecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.*;
import ast.misc.Compilation;
import ast.misc.ParamDecl;
import ast.misc.Typeifier;
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
import messages.errors.ErrorBuilder;
import messages.MessageType;
import messages.errors.scope.ScopeErrorBuilder;
import messages.errors.scope.ScopeErrorFactory;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/**
 * C Minor Scope Resolution Pass
 * <p>
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
     * Current file we are name checking in (set by {@link #visitCompilation(Compilation)}).
     */
    private String currentFile = "";

    /**
     * Error factory to generate scope errors.
     */
    private final ScopeErrorFactory generateScopeError;

    /**
     * Error factory to generate semantic errors not specific to scope resolution.
     */
    private final SemanticErrorFactory generateSemanticError;

    /**
     * List of errors we have found.
     */
    private final Vector<String> errors;

    /**
     * Creates name checker in compilation mode
     */
    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.generateScopeError = new ScopeErrorFactory();
        this.generateSemanticError = new SemanticErrorFactory();
        this.errors = new Vector<>();
    }

    /**
     * Creates name checker in interpretation mode
     * @param st Compilation Unit Symbol Table
     */
    public NameChecker(SymbolTable st) {
        this();
        this.currentScope = st;
        this.interpretMode = true;
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
        for(String name : baseClass.symbolTable.getAllNames().keySet()) {
            AST currentDecl = baseClass.symbolTable.findName(name).decl();
                if(currentDecl.isFieldDecl()) {
                    // ERROR CHECK #1: This checks if the field was already defined in the class hierarchy.
                    if(currentScope.hasName(name)) {
                        errors.add(
                            new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                                .addLocation(currentClass.symbolTable.findName(name).decl())
                                .addErrorType(MessageType.SCOPE_ERROR_324)
                                .addArgs(name,currentClass)
                                .asScopeErrorBuilder()
                                .addRedeclaration(currentDecl)
                                .error()
                        );
                    }
                    currentScope.addName(name,currentDecl.asFieldDecl());
                }
                else
                    currentScope.addMethod(name.substring(0,name.indexOf('/')));
        }
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
        for(MethodDecl subMethod : currentClass.classBlock().methodDecls()) {
            subMethod.visit(this);
            boolean methodFoundInBaseClass = false;

            if(baseClass != null) {
                for(MethodDecl baseMethod : baseClass.classBlock().methodDecls()) {
                    if(baseMethod.methodSignature().equals(subMethod.methodSignature())) {
                        methodFoundInBaseClass = true;
                        // ERROR CHECK #1: This checks if the user marked the method as overridden in the subclass.
                        if(!subMethod.isOverridden()) {
                            errors.add(
                                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                                    .addLocation(subMethod)
                                    .addErrorType(MessageType.SCOPE_ERROR_311)
                                    .addArgs(subMethod, baseClass)
                                    .error()
                            );
                        }
                    }
                }
            }

            // ERROR CHECK #2: This checks if a user tries to redefine a method not found in the base class.
            if(!methodFoundInBaseClass && subMethod.isOverridden()) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(subMethod)
                        .addErrorType(MessageType.SCOPE_ERROR_312)
                        .addArgs(subMethod)
                        .error()
                );
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
        if(!as.LHS().isNameExpr() && !as.LHS().isFieldExpr() && !as.LHS().isArrayExpr()) {
            errors.add(
                new ErrorBuilder(generateSemanticError,currentFile,interpretMode)
                    .addLocation(as)
                    .addErrorType(MessageType.SEMANTIC_ERROR_707)
                    .addArgs(as.LHS())
                    .addSuggestType(MessageType.SEMANTIC_SUGGEST_1702)
                    .error()
            );
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
        be.LHS().visit(this);
        be.RHS().visit(this);

        String binOp = be.binaryOp().toString();
        switch(binOp) {
            case "instanceof":
            case "!instanceof":
            case "as?":
                // ERROR CHECK #1: This checks if the LHS represents a valid variable name.
                if(!be.LHS().isNameExpr() && !be.LHS().isFieldExpr() && !be.LHS().isArrayExpr()) {
                    errors.add(
                        new ErrorBuilder(generateSemanticError,currentFile,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SEMANTIC_ERROR_708)
                            .addArgs(be.LHS(), binOp)
                            .addSuggestType(MessageType.SEMANTIC_SUGGEST_1703)
                            .addSuggestArgs(binOp)
                            .error()
                    );
                }

                AST cd = currentScope.findName(be.RHS().toString()).decl();
                // ERROR CHECK #2: This checks if the RHS represents a class name.
                if(!cd.isTopLevelDecl() || !cd.asTopLevelDecl().isClassDecl()) {
                    errors.add(
                        new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SCOPE_ERROR_323)
                            .addArgs(be.RHS(),binOp)
                            .addSuggestType(MessageType.SCOPE_SUGGEST_1304)
                            .addSuggestArgs(binOp)
                            .error()
                    );
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

        for(LocalDecl ld : bs.decls())
            ld.visit(this);
        for(Statement s : bs.stmts())
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
        cs.choiceLabel().visit(this);
        cs.caseBlock().visit(this);

        cs.symbolTable = currentScope;
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
        cs.choiceExpr().visit(this);

        for(CaseStmt c : cs.caseStmts())
            c.visit(this);

        cs.otherBlock().visit(this);
        cs.symbolTable = currentScope;
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
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(cd)
                    .addErrorType(MessageType.SCOPE_ERROR_307)
                    .addArgs(cd)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(cd.toString()).decl())
                    .error()
            );
        }

        currentScope.addName(cd.toString(),cd);

        if(cd.superClass() != null) {
            // ERROR CHECK #2: This checks if the class tries to inherit itself.
            if(cd.toString().equals(cd.superClass().toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.SCOPE_ERROR_308)
                        .addArgs(cd)
                        .error()
                );
            }

            // ERROR CHECK #3: This checks if the inherited class was declared in the program.
            if(!currentScope.hasNameSomewhere(cd.superClass().toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.SCOPE_ERROR_309)
                        .addArgs(cd,cd.superClass())
                        .addSuggestType(MessageType.SCOPE_SUGGEST_1301)
                        .addSuggestArgs(cd.superClass())
                        .error()
                );
            }

            baseClass = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
        }

        currentScope = currentScope.openNewScope();
        cd.symbolTable = currentScope;
        currentClass = cd;

        for(FieldDecl fd : cd.classBlock().fieldDecls())
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
    public void visitCompilation(Compilation c) {
        currentFile = c.getFile();
        super.visitCompilation(c);
        c.globalTable = currentScope;
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
        ds.doBlock().visit(this);

        ds.symbolTable = currentScope;
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);
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
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(ed)
                    .addErrorType(MessageType.SCOPE_ERROR_321)
                    .addArgs(ed)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(ed.toString()).decl())
                    .error()
            );
        }

        currentScope.addName(ed.toString(),ed);

        // ERROR CHECK #2: This checks if each constant name in the enum was already declared somewhere in the program.
        for(Var constant : ed.constants()) {
            if(currentScope.hasName(constant.toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(ed)
                        .addErrorType(MessageType.SCOPE_ERROR_322)
                        .addArgs(constant,ed)
                        .asScopeErrorBuilder()
                        .addRedeclaration(currentScope.findName(constant.toString()).decl())
                        .error()
                );
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
        // ERROR CHECK #1: This checks if the field name was used anywhere in the program.
        if(currentScope.hasNameSomewhere(fd.toString())) {
            AST varLocation = currentScope.findName(fd.toString()).decl();

            if(interpretMode)
                currentScope = currentScope.closeScope();

            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.SCOPE_ERROR_310)
                    .addArgs(fd,currentClass)
                    .asScopeErrorBuilder()
                    .addRedeclaration(varLocation)
                    .error()
            );
        }

        currentScope.addName(fd.toString(),fd);

        if(fd.var().init() != null) {
            currentVariable = fd.toString();
            fd.var().init().visit(this);
            currentVariable = "";
        }
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
        if(!fe.fieldTarget().isThis())
            fe.fieldTarget().visit(this);
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

        fs.loopVar().visit(this);
        fs.condLHS().visit(this);
        fs.condRHS().visit(this);

        for(LocalDecl ld : fs.forBlock().decls())
            ld.visit(this);
        for(Statement s : fs.forBlock().stmts())
            s.visit(this);

        fs.symbolTable = currentScope;
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
        for(Typeifier tp : fd.typeParams()) {
            // ERROR CHECK #1: This checks if the type parameter name was already used in the program.
            if(currentScope.hasNameSomewhere(tp.toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(fd)
                        .addErrorType(MessageType.SCOPE_ERROR_317)
                        .addArgs(tp,fd)
                        .asScopeErrorBuilder()
                        .addRedeclaration(currentScope.findName(tp.toString()).decl())
                        .error()
                );
            }
        }

        String funcSignature = fd + "/";
        if(fd.params() != null)
            funcSignature += fd.paramSignature();

        // ERROR CHECK #2: This checks to make sure we are not redeclaring a function with the same type arguments.
        if(currentScope.hasNameSomewhere(funcSignature)) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.SCOPE_ERROR_306)
                    .addArgs(fd)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(funcSignature).decl())
                    .error()
            );
        }

        currentScope.addName(funcSignature,fd);
        currentScope.addMethod(fd.toString());

        currentScope = currentScope.openNewScope();
        fd.symbolTable = currentScope;

        for(Typeifier tp : fd.typeParams())
            currentScope.addName(tp.toString(),tp);
        for(ParamDecl pd : fd.params())
            pd.visit(this);
        for(LocalDecl ld : fd.funcBlock().decls())
            ld.visit(this);
        for(Statement s : fd.funcBlock().stmts())
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
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(gd)
                    .addErrorType(MessageType.SCOPE_ERROR_302)
                    .addArgs(gd)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(gd.toString()).decl())
                    .error()
            );
        }

        currentScope.addName(gd.toString(), gd);

        if(gd.var().init() != null) {
            currentVariable = gd.toString();
            gd.var().init().visit(this);
            currentVariable = "";
        }
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
        is.condition().visit(this);
        is.ifBlock().visit(this);

        is.symbolTableIfBlock = currentScope;
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.elifStmts())
            e.visit(this);

        if(is.elseBlock() != null) {
            is.elseBlock().visit(this);
            is.symbolTableElseBlock = currentScope;
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
        String oldFileName = currentFile;
        SymbolTable oldScope = currentScope;
        currentScope = new SymbolTable();

        im.getCompilationUnit().visit(this);

        oldScope.setImportParent(currentScope);
        currentScope = oldScope;
        currentFile = oldFileName;
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
        if(!currentScope.hasMethodSomewhere(in.toString()) && !in.isLengthInvocation()) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(in)
                    .addErrorType(MessageType.SCOPE_ERROR_325)
                    .addArgs(in)
                    .error()
            );
        }

        for(Expression e : in.arguments())
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
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(ld)
                    .addErrorType(MessageType.SCOPE_ERROR_300)
                    .addArgs(ld)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(ld.toString()).decl())
                    .error()
            );
        }

        currentScope.addName(ld.toString(),ld);

        if(ld.var().init() != null) {
            currentVariable = ld.toString();
            ld.var().init().visit(this);
            currentVariable = "";
        }
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
        for(ParamDecl e : md.args())
            e.visit(this);

        for(LocalDecl ld : md.mainBody().decls())
            ld.visit(this);
        for(Statement s : md.mainBody().stmts())
            s.visit(this);

        md.symbolTable = currentScope;
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
        String methodSignature = md + "/";
        if(md.params() != null)
            methodSignature += md.paramSignature();

        // ERROR CHECK #1: This checks to make sure we are not redeclaring a method with the same type arguments.
        if(currentScope.hasName(methodSignature)) {
            // We will only error out if the method was redeclared in the same class twice. If the method overrides
            // a base class method, then we will check if the method was overridden correctly at a later point.
            if(currentClass.superClass() == null || !currentClass.symbolTable.hasName(methodSignature)) {
                if(interpretMode)
                    currentScope = currentScope.closeScope();
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.SCOPE_ERROR_313)
                        .addArgs(md,currentClass)
                        .asScopeErrorBuilder()
                        .addRedeclaration(currentScope.findName(methodSignature).decl())
                        .error()
                );
            }
        }

        currentScope.addName(methodSignature, md);
        currentScope.addMethod(md.toString());

        currentScope = currentScope.openNewScope();
        md.symbolTable = currentScope;

        for(ParamDecl pd : md.params())
            pd.visit(this);
        for(LocalDecl ld : md.methodBlock().decls())
            ld.visit(this);
        for(Statement s : md.methodBlock().stmts())
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
        if(ne.isParent()) {
            // ERROR CHECK #1: This checks if the 'parent' keyword was used outside of a class.
            if(currentClass == null) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(ne.getRootParent())
                        .addErrorType(MessageType.SCOPE_ERROR_318)
                        .addSuggestType(MessageType.SCOPE_SUGGEST_1302)
                        .error()
                );
            }
            // ERROR CHECK #2: This checks if the 'parent' keyword is used in a class with no inherited classes.
            if(currentClass.superClass() == null) {
                errors.add(
                    new ErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(ne.getRootParent())
                        .addArgs(currentClass)
                        .addErrorType(MessageType.SCOPE_ERROR_319)
                        .addSuggestType(MessageType.SCOPE_SUGGEST_1303)
                        .error()
                );
            }
        }
        // ERROR CHECK #3: This checks if the name was not declared somewhere in the program.
        else if(!currentScope.isNameUsedAnywhere(ne.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(ne.getRootParent())
                    .addErrorType(MessageType.SCOPE_ERROR_304)
                    .addArgs(ne)
                    .addSuggestType(MessageType.SCOPE_SUGGEST_1300)
                    .addSuggestArgs(ne)
                    .error()
            );
        }
        else {
            AST varDecl = currentScope.findName(ne.toString()).decl();

            // ERROR CHECK #4: This checks if an enum type is used as a name.
            if(varDecl.isTopLevelDecl()) {
                if (varDecl.asTopLevelDecl().isEnumDecl() && varDecl.toString().equals(ne.toString())) {
                    errors.add(
                        new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                            .addLocation(ne.getRootParent())
                            .addErrorType(MessageType.SCOPE_ERROR_320)
                            .addArgs(ne)
                            .error()
                    );
                }
            }

            // ERROR CHECK #5: This checks if any variable declaration contains its own name when initialized.
            if(!currentVariable.isEmpty() && currentVariable.equals(ne.toString())) {
                AST decl = currentScope.findName(currentVariable).decl();
                ErrorBuilder eb = new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                                      .addLocation(decl)
                                      .addArgs(currentVariable);

                // Global Declaration Error
                if(decl.isTopLevelDecl())
                    errors.add(eb.addErrorType(MessageType.SCOPE_ERROR_303).error());
                // Local Declaration Error
                else if(decl.isStatement())
                    errors.add(eb.addErrorType(MessageType.SCOPE_ERROR_301).error());
                // Field Declaration Error
                else
                    errors.add(eb.addErrorType(MessageType.SCOPE_ERROR_326).error());

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
        if(!currentScope.hasNameSomewhere(ne.classType().toString())) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(ne)
                    .addErrorType(MessageType.SCOPE_ERROR_314)
                    .addArgs(ne.classType())
                    .error()
            );
        }

        ClassDecl cd = currentScope.findName(ne.classType().toString()).decl().asTopLevelDecl().asClassDecl();
        HashSet<String> seen = new HashSet<>();
        for(Var v : ne.args()) {
            // ERROR CHECK #2: This checks if the field being initialized was declared in the class.
            if(!cd.symbolTable.hasName(v.toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_315)
                        .addArgs(v,ne.classType())
                        .error()
                );
            }
            // ERROR CHECK #3: This checks if a user tries to initialize a field more than once.
            else if(seen.contains(v.toString())) {
                errors.add(
                    new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_316)
                        .addArgs(v,ne.classType())
                        .error()
                );
            }
            seen.add(v.toString());
            v.init().visit(this);
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
        // ERROR CHECK #1: This checks if the parameter name was already used somewhere in the program.
        if(currentScope.isNameUsedAnywhere(pd.toString())) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError, currentFile,interpretMode)
                    .addLocation(pd)
                    .addErrorType(MessageType.SCOPE_ERROR_305)
                    .addArgs(pd)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(pd.toString()).decl())
                    .error()
            );
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
            errors.add(
                new ErrorBuilder(generateSemanticError,currentFile,interpretMode)
                    .addLocation(rs)
                    .addErrorType(MessageType.SEMANTIC_ERROR_709)
                    .addArgs(rs.getName())
                    .addSuggestType(MessageType.SEMANTIC_SUGGEST_1704)
                    .error()
            );
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
        ws.condition().visit(this);
        ws.whileBlock().visit(this);

        ws.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }
}
