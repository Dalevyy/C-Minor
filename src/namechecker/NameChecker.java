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
import ast.misc.Compilation;
import ast.misc.Name;
import ast.misc.ParamDecl;
import ast.misc.TypeParam;
import ast.misc.Var;
import ast.misc.VarDecl;
import ast.statements.AssignStmt;
import ast.statements.BlockStmt;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.LocalDecl;
import ast.statements.Statement;
import ast.statements.RetypeStmt;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.ImportDecl;
import ast.topleveldecls.MainDecl;
import messages.MessageType;
import messages.errors.Error;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeErrorBuilder;
import messages.errors.scope.ScopeErrorFactory;
import messages.errors.semantic.SemanticErrorFactory;
import utilities.SymbolTable;
import utilities.SymbolTable.NameIterator;
import utilities.Vector;
import utilities.Visitor;

/**
 * Name Resolution Pass.
 * <p>
 *     This is the first major semantic pass responsible for scope resolution. Here,
 *     we are checking whether or not a user properly used names within their program.
 *     At the end of this phase, we will have generated a {@link SymbolTable} for each
 *     {@link ast.misc.ScopeDecl} that we will constantly reference during other compilation phases.
 * </p>
 * @author Daniel Levy
 */
public class NameChecker extends Visitor {

    /**
     * Current scope we are name checking within.
     */
    private SymbolTable currentScope;

    /**
     * Error factory to generate scope errors.
     */
    private final ScopeErrorFactory scopeErrorGenerator;

    /**
     * Error factory to generate semantic errors not specific to scope resolution.
     */
    private final SemanticErrorFactory generalErrorGenerator;

    /**
     * List of errors we have found.
     */
    private final Vector<Error> errors;

    /**
     * Instance of {@link NameCheckerHelper} that will be used for additional name checking tasks.
     */
    private final NameCheckerHelper helper;

    /**
     * Creates {@link NameChecker} in compilation mode
     */
    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.scopeErrorGenerator = new ScopeErrorFactory();
        this.generalErrorGenerator = new SemanticErrorFactory();
        this.errors = new Vector<>();
        this.helper = new NameCheckerHelper();
    }

    /**
     * Creates {@link NameChecker} in interpretation mode
     * @param st {@link SymbolTable} representing the default scope of the {@link interpreter.VM}.
     */
    public NameChecker(SymbolTable st) {
        this();
        this.currentScope = st;

        this.scopeErrorGenerator.setInterpretationExecutionMode();
        this.generalErrorGenerator.setInterpretationExecutionMode();

        this.interpretMode = true;
    }

    /**
     * Checks if an {@link AssignStmt} is written correctly.
     * <p>
     *     This visit will ensure that any names used in the LHS and RHS of an assignment
     *     can properly be resolved. Additionally, we will check if a user has a valid LHS
     *     that can be assigned to. See {@link NameCheckerHelper#canExpressionBeAssignedTo(Expression)}.
     * </p>
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        as.LHS().visit(this);

        // ERROR CHECK #1: For an assignment, we need to make sure the LHS can actually store a value.
        if(helper.canExpressionBeAssignedTo(as.LHS())) {
            ErrorBuilder eb = new ErrorBuilder(generalErrorGenerator)
                                  .addLocation(as)
                                  .addArgs(as.LHS());

            // We will generate a different error message when an error occurs within a retype statement.
            if(as.isRetypeStmt())
                eb.addErrorType(MessageType.SEMANTIC_ERROR_709).addSuggestType(MessageType.SEMANTIC_SUGGEST_1704);
            else
                eb.addErrorType(MessageType.SEMANTIC_ERROR_707).addSuggestType(MessageType.SEMANTIC_SUGGEST_1702);

            errors.add(generalErrorGenerator.createCompileError(eb));
        }

        as.RHS().visit(this);
    }

    /**
     * Resolves all names found in a {@link BinaryExpr}.
     * <p>
     *     During this visit, we only have to do a specific check to ensure a user correctly wrote
     *     an {@code instanceof}, {@code !instanceof}, or {@code as?} operation. If these operations
     *     do not contain the names we expect, then we will not be able to do type checking which is
     *     why we will do the checks now.
     * </p>
     * @param be {@link BinaryExpr}
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.getLHS().visit(this);
        be.getRHS().visit(this);

        switch(be.getBinaryOp().getBinaryType()) {
            case INSTOF:
            case NINSTOF:
                Expression LHS = be.getLHS();
                Expression RHS = be.getRHS();
                // ERROR CHECK #1: This makes sure some name is present on the LHS of an instanceof operation.
                if (!(LHS.isNameExpr() || LHS.isArrayExpr() || LHS.isFieldExpr() || LHS.isInvocation())) {
                    ErrorBuilder eb = new ErrorBuilder(generalErrorGenerator)
                                          .addLocation(be)
                                          .addErrorType(MessageType.SEMANTIC_ERROR_708)
                                          .addArgs(be.getLHS(), be.getBinaryOp())
                                          .addSuggestType(MessageType.SEMANTIC_SUGGEST_1703)
                                          .addSuggestArgs(be.getBinaryOp());
                    errors.add(generalErrorGenerator.createCompileError(eb));
                }

                AST declOfRHS = currentScope.findName(be.getRHS());
                // ERROR CHECK #2: The RHS of an instanceof operation has to be the name of a class.
                if (!RHS.isNameExpr() || !declOfRHS.isTopLevelDecl() || !declOfRHS.asTopLevelDecl().isClassDecl()) {
                    ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                          .addLocation(be)
                                          .addErrorType(MessageType.SCOPE_ERROR_323)
                                          .addArgs(be.getRHS(), be.getBinaryOp())
                                          .addSuggestType(MessageType.SCOPE_SUGGEST_1304)
                                          .addSuggestArgs(be.getBinaryOp());
                    errors.add(scopeErrorGenerator.createCompileError(eb));
            }
        }
    }

    /**
     * Creates a new scope when entering a {@link BlockStmt}.
     * <p>
     *     When we visit a block statement, a new scope will always be opened. Since we will be storing
     *     the scope into the construct that contains a {@link BlockStmt}, we will not close any scope
     *     here unless we are in interpretation mode (only when a user writes a {@link BlockStmt} by itself).
     * </p>
     * @param bs {@link BlockStmt}
     */
    public void visitBlockStmt(BlockStmt bs) {
        currentScope = currentScope.openScope();

        super.visitBlockStmt(bs);

        /*
            In VM mode, a user could write a statement such as { ... { ... { ... } ... } ...} where ...
            represents a series of statements. Since a block statement written by itself has no parent, this
            conditional makes sure to close the scope in order to reset the NameChecker during interpretation.
        */
        if(bs.getParent() == null)
            currentScope = currentScope.closeScope();
    }

    /**
     * Creates a new scope for a {@link CaseStmt}.
     * <p>
     *     A case statement will contain one {@link SymbolTable} for its body.
     * </p>
     * @param cs {@link CaseStmt}
     */
    public void visitCaseStmt(CaseStmt cs) {
        super.visitCaseStmt(cs);
        cs.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Creates a new scope for a {@link ChoiceStmt}.
     * <p>
     *     A choice statement will contain one {@link SymbolTable} for the default branch
     *     denoted with the keyword {@code other}. There will also be additional scopes for
     *     each individual {@link CaseStmt} found in the current choice statement.
     * </p>
     * @param chs {@link ChoiceStmt}
     */
    public void visitChoiceStmt(ChoiceStmt chs) {
        super.visitChoiceStmt(chs);
        chs.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Creates a new scope for a {@link ClassDecl}.
     * <p>
     *     A class will contain a single {@link SymbolTable} to represent its body. We will also
     *     have individual scopes for each {@link MethodDecl} accessible within this class. The
     *     extra error checking we do during this visit is primarily concerned with inheritance
     *     and making sure the user correctly inherited from a base class (if it's applicable).
     * </p>
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        // ERROR CHECK #1: We need to make sure the current class does not redeclare an existing top level declaration.
        if(currentScope.hasNameInProgram(cd)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(cd)
                                  .addErrorType(MessageType.SCOPE_ERROR_307)
                                  .addArgs(cd)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(cd));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(cd);

        ClassDecl baseClass = null;
        if(cd.superClass() != null) {
            // ERROR CHECK #2: The user can not have a class inherit from itself.
            if(cd.name().equals(cd.superClass().getClassName())) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(cd)
                                      .addErrorType(MessageType.SCOPE_ERROR_308)
                                      .addArgs(cd);
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }

            // ERROR CHECK #3: This ensures the inherited class was already declared in the program. The scope
            //                 resolution is done in a single pass, so classes must be declared in the right order.
            if(!currentScope.hasNameInProgram(cd.superClass())) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(cd)
                                      .addErrorType(MessageType.SCOPE_ERROR_309)
                                      .addArgs(cd, cd.superClass())
                                      .addSuggestType(MessageType.SCOPE_SUGGEST_1301)
                                      .addSuggestArgs(cd.superClass());
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }

            baseClass = currentScope.findName(cd.superClass()).asTopLevelDecl().asClassDecl();
        }

        currentScope = currentScope.openScope();

        // We are allowing type parameters to shadow previous top level declarations.
        for(TypeParam tp : cd.typeParams())
            tp.visit(this);

        for(FieldDecl fd : cd.classBlock().getFields())
            fd.visit(this);

        // Before visiting methods, update the current class scope to contain fields from the base class.
        if(baseClass != null)
            helper.updateSubClassSymbolTable(cd, baseClass);

        for(MethodDecl md : cd.classBlock().getMethods())
            md.visit(this);

        helper.checkOverriddenMethods(cd, baseClass);

        cd.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Begins the C Minor scope resolution pass in compilation mode.
     * <p>
     *     This will be the first visit when we are executing the {@link NameChecker} in compilation
     *     mode, and we will set up the file names that will be used to print out errors.
     * </p>
     * @param c {@link Compilation}
     */
    public void visitCompilation(Compilation c) {
        scopeErrorGenerator.setFileName(c.getFile());
        generalErrorGenerator.setFileName(c.getFile());
        super.visitCompilation(c);
        c.setScope(currentScope);
    }

    /**
     * Creates a new scope for a {@link DoStmt}.
     * <p>
     *     A do while loop will contain one {@link SymbolTable} for its body.
     * </p>
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        ds.doBlock().visit(this);
        ds.setScope(currentScope);
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);
    }

    /**
     * Resolves all names associated with an {@link EnumDecl}.
     * <p>
     *     When visiting an enum, we want to make sure the enum's name alongside its constants
     *     do not conflict with a previous top level declaration.
     * </p>
     * @param ed {@link EnumDecl}
     */
    public void visitEnumDecl(EnumDecl ed) {
        // ERROR CHECK #1: We need to make sure the current enum does not redeclare an existing top level declaration.
        if(currentScope.hasName(ed)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(ed)
                                  .addErrorType(MessageType.SCOPE_ERROR_321)
                                  .addArgs(ed)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(ed));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(ed);

        // ERROR CHECK #2: Each constant declared in the enum will be treated as a global constant. As a result,
        //                 we need to make sure the constant's name is not already taken by a separate top level
        //                 declaration.
        for(Var constant : ed.constants()) {
            if(currentScope.hasName(constant)) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(ed)
                                      .addErrorType(MessageType.SCOPE_ERROR_322)
                                      .addArgs(constant,ed)
                                      .asScopeErrorBuilder()
                                      .addRedeclaration(currentScope.findName(constant));
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }

            constant.getInitialValue().visit(this);
            currentScope.addName(constant.toString(), ed);
        }
    }

    /**
     * Resolves all names associated with a {@link FieldDecl}.
     * <p>
     *     When declaring a field inside of a class, we want to make sure its name is unique to
     *     all other declared fields in the class. If the field is declared inside a subclass, then
     *     we will check if it's contained in the base class when returning to {@link #visitClassDecl(ClassDecl)}.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        // ERROR CHECK #1: We need to make sure the current field does not redeclare an existing field.
        if(currentScope.hasName(fd)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(fd)
                                  .addErrorType(MessageType.SCOPE_ERROR_310)
                                  .addArgs(fd, fd.getClassDecl())
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(fd));

            // This is done, so we can reset the name checker without having to recreate it.
            if(interpretMode)
                currentScope = currentScope.closeScope();
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(fd);

        if(fd.hasInitialValue()) {
            fd.getInitialValue().visit(this);
            helper.checkVariableSelfInitialization(fd);
        }
    }

    /**
     * Resolves the name of the target expression.
     * <p>
     *     At this point in the compilation process, we are not able to properly
     *     name check any field expressions. This is because we need to know the
     *     types of each expression found in complex field expressions, so we can
     *     correctly access the class scopes we need to. Thus, all we will do right
     *     now is check if the target expression can be resolved.
     * </p>
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) { fe.getTarget().visit(this); }

    /**
     * Creates a new scope for a {@link ForStmt}.
     * <p>
     *     A for loop will contain one {@link SymbolTable} for its body. One thing to note
     *     is the loop control variable will be stored in the body's scope because we are
     *     not going to allow a user to redeclare the control variable in the body's outermost scope.
     * </p>
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = currentScope.openScope();

        fs.loopVar().visit(this);
        fs.condLHS().visit(this);
        fs.condRHS().visit(this);

        for(LocalDecl ld : fs.forBlock().decls())
            ld.visit(this);
        for(Statement s : fs.forBlock().stmts())
            s.visit(this);

        fs.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Creates a new scope for a {@link FuncDecl}.
     * <p>
     *     A function will have a symbol table representing its body. Since we will support
     *     function overloading, we will be storing function signatures into the {@link SymbolTable}.
     * </p>
     * @param fd {@link FuncDecl}
     */
    public void visitFuncDecl(FuncDecl fd) {
        // ERROR CHECK #1: We need to make sure a user does not redeclare any function in a program.
        //                 A redeclaration in this context refers to a user using the same function
        //                 name with the same exact parameter types as a previous function.
        if(currentScope.hasMethodOverload(fd)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(fd)
                                  .addErrorType(MessageType.SCOPE_ERROR_306)
                                  .addArgs(fd)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findMethod(fd));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addMethod(fd);

        currentScope = currentScope.openScope();

        for(TypeParam tp : fd.typeParams())
            tp.visit(this);
        for(ParamDecl pd : fd.params())
            pd.visit(this);
        for(LocalDecl ld : fd.funcBlock().decls())
            ld.visit(this);
        for(Statement s : fd.funcBlock().stmts())
            s.visit(this);

        fd.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Resolves all names associated with a {@link GlobalDecl}.
     * <p>
     *     When declaring a global variable, we want to make sure the
     *     name doesn't conflict with a previous top level declaration.
     * </p>
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // ERROR CHECK #1: A user can not redeclare a global variable with a name that was already declared
        //                 in the current scope.
        if(currentScope.hasName(gd)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(gd)
                                  .addErrorType(MessageType.SCOPE_ERROR_302)
                                  .addArgs(gd)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(gd));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(gd);

        if(gd.hasInitialValue()) {
            gd.getInitialValue().visit(this);
            helper.checkVariableSelfInitialization(gd);
        }
    }

    /**
     * Creates a new scope for an {@link IfStmt}.
     * <p>
     *     An if statement will contain one {@link SymbolTable} for the body of the if
     *     branch and one {@link SymbolTable} for the body of the else branch (if applicable).
     * </p>
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);
        is.ifBlock().visit(this);

        is.setScope(currentScope);
        currentScope = currentScope.closeScope();

        for(IfStmt elifStmt : is.elifStmts())
            elifStmt.visit(this);

        if(is.containsElse()) {
            is.elseBlock().visit(this);
            is.setScope(currentScope);
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Creates a new scope for an {@link ImportDecl}.
     * <p>
     *     For an import declaration, we will run the {@code NameChecker} on its {@link Compilation}
     *     unit and save the compilation unit's symbol table into the current scope in order to access
     *     any names declared in the {@link ImportDecl}.
     * </p>
     * @param im {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl im) {
        SymbolTable previousCompilationScope = currentScope;
        currentScope = new SymbolTable();

        im.getCompilationUnit().visit(this);

        previousCompilationScope.setImportTable(currentScope);
        currentScope = previousCompilationScope;
    }

    /**
     * Resolves the names of the arguments passed with an {@link Invocation}.
     * <p>
     *     During this visit, we are only going to resolve any names used as arguments for the
     *     current {@link Invocation}. Since we are storing both functions and methods in the
     *     {@link SymbolTable} with their signatures, we have to wait until type checking to
     *     check if the invocation name itself is valid.
     * </p>
     * @param in {@link Invocation}
     */
    public void visitInvocation(Invocation in) {
        for(Expression arg : in.getArgs())
            arg.visit(this);
    }

    /**
     * Resolves all names associated with a {@link LocalDecl}.
     * <p>
     *     When declaring a local variable, we want to make sure the
     *     name is unique to the current scope we are in.
     * </p>
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: A user can not redeclare a local variable with a name that was already declared
        //                 in the current scope.
        if(currentScope.hasName(ld)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(ld)
                                  .addErrorType(MessageType.SCOPE_ERROR_300)
                                  .addArgs(ld)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(ld));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(ld);

        if(ld.hasInitialValue()) {
            ld.getInitialValue().visit(this);
            helper.checkVariableSelfInitialization(ld);
        }
    }

    /**
     * Creates a new scope for {@link MainDecl}.
     * <p>
     *     The {@code main} function for a C Minor program will have a symbol table for
     *     its body, and it will be the same symbol table used for the global scope which
     *     is stored by {@link Compilation}.
     * </p>
     * @param md {@link MainDecl}
     */
    public void visitMainDecl(MainDecl md) {
        for(ParamDecl param : md.args())
            param.visit(this);
        for(LocalDecl ld : md.mainBody().decls())
            ld.visit(this);
        for(Statement s : md.mainBody().stmts())
            s.visit(this);

        md.setScope(currentScope);
    }

    /**
     * Creates a new scope for a {@link MethodDecl}.
     * <p>
     *     A method will have a symbol table representing its body. Since we will support
     *     method overloading, we will be storing method signatures into the {@link SymbolTable}.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        // ERROR CHECK #1: This checks to make sure we are not redeclaring a method with the same type arguments.
        if(currentScope.hasMethodOverload(md)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(md)
                                  .addErrorType(MessageType.SCOPE_ERROR_313)
                                  .addArgs(md, md.getClassDecl())
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(md));

            // If we're executing inside the VM, we need to close the class scope to reset the name checker.
            if(interpretMode)
                currentScope = currentScope.closeScope();
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addMethod(md);

        currentScope = currentScope.openScope();

        for(ParamDecl pd : md.params())
            pd.visit(this);
        for(LocalDecl ld : md.methodBlock().decls())
            ld.visit(this);
        for(Statement s : md.methodBlock().stmts())
            s.visit(this);

        md.setScope(currentScope);
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
            AST root = ne.getRootParent();
            // ERROR CHECK #1: This checks if the 'parent' keyword was used outside of a class.
            if(!root.isTopLevelDecl() || !root.asTopLevelDecl().isClassDecl()) {
                ErrorBuilder eb =  new ScopeErrorBuilder(scopeErrorGenerator)
                                       .addLocation(ne.getRootParent())
                                       .addErrorType(MessageType.SCOPE_ERROR_318)
                                       .addSuggestType(MessageType.SCOPE_SUGGEST_1302);
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }
            // ERROR CHECK #2: This checks if the 'parent' keyword is used in a class with no inherited classes.
            if(root.asTopLevelDecl().asClassDecl().superClass() == null) {
                ErrorBuilder eb =  new ErrorBuilder(scopeErrorGenerator)
                                       .addLocation(ne.getRootParent())
                                       .addArgs(root)
                                       .addErrorType(MessageType.SCOPE_ERROR_319)
                                       .addSuggestType(MessageType.SCOPE_SUGGEST_1303);
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }
        }
        // ERROR CHECK #3: This checks if the name was not declared somewhere in the program.
        else if(!currentScope.hasNameInProgram(ne)) {
            ErrorBuilder eb = new ErrorBuilder(scopeErrorGenerator)
                                  .addLocation(ne.getRootParent())
                                  .addErrorType(MessageType.SCOPE_ERROR_304)
                                  .addArgs(ne)
                                  .addSuggestType(MessageType.SCOPE_SUGGEST_1300)
                                  .addSuggestArgs(ne);
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }
        else {
            AST varDecl = currentScope.findName(ne);

            // ERROR CHECK #4: This checks if an enum type is used as a name.
            if(varDecl.isTopLevelDecl()) {
                if (varDecl.asTopLevelDecl().isEnumDecl() && varDecl.toString().equals(ne.toString())) {
                    ErrorBuilder eb =  new ScopeErrorBuilder(scopeErrorGenerator)
                                           .addLocation(ne.getRootParent())
                                           .addErrorType(MessageType.SCOPE_ERROR_320)
                                           .addArgs(ne);
                    errors.add(scopeErrorGenerator.createCompileError(eb));
                }
            }
        }
    }

    /**
     * Resolves all names associated with a {@link NewExpr}.
     * <p>
     *     When instantiating an object, we need to make sure the class we are instantiating from
     *     was declared somewhere in the program. We then check to make sure all fields associated
     *     with the object are correct and are initialized only once. C Minor provides a constructor
     *     for all classes automatically which means the user has to specify which initial values they
     *     want to assign to different fields.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        // ERROR CHECK #1: To instantiate a class, it must be declared somewhere in the user's program.
        if(!currentScope.hasNameInProgram(ne.getClassType().getClassName())) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(ne)
                                  .addErrorType(MessageType.SCOPE_ERROR_314)
                                  .addArgs(ne.getClassType());
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        ClassDecl cd = currentScope.findName(ne.getClassType().getClassName()).asTopLevelDecl().asClassDecl();
        Vector<String> initializedFields = new Vector<>();

        for(Var field : ne.getInitialFields()) {
            // ERROR CHECK #2: For each field specified, we need to make sure it was actually declared in the class.
            if(!cd.getScope().hasName(field)) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(ne)
                                      .addErrorType(MessageType.SCOPE_ERROR_315)
                                      .addArgs(field, ne.getClassType());
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }
            // ERROR CHECK #3: An object's field can only be initialized once.
            if(initializedFields.has(field.toString())) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(ne)
                                      .addErrorType(MessageType.SCOPE_ERROR_316)
                                      .addArgs(field,ne.getClassType());
                errors.add(scopeErrorGenerator.createCompileError(eb));
            }

            initializedFields.add(field.toString());
            field.getInitialValue().visit(this);
        }
    }

    /**
     * Resolves the name of a {@link ParamDecl}.
     * <p>
     *     We are going to allow a user to shadow a name already declared in a top level declaration
     *     or inside of a class. This means the parameter name checking is concerned with making sure
     *     we are not using the same parameter name for multiple parameters.
     * </p>
     * @param pd {@link ParamDecl}
     */
    public void visitParamDecl(ParamDecl pd) {
        // ERROR CHECK #1: A parameter can not have the same name as another parameter.
        if(currentScope.hasName(pd)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(pd)
                                  .addErrorType(MessageType.SCOPE_ERROR_305)
                                  .addArgs(pd)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(pd));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }

        currentScope.addName(pd);
    }

    /**
     * Checks if a retype statement is written correctly.
     * <p>
     *     Since a retype statement is an {@link AssignStmt}, we will call {@link #visitAssignStmt(AssignStmt)}
     *     to perform name checking since the checks needed during this phase are identical.
     * </p>
     * @param rt {@link RetypeStmt}
     */
    public void visitRetypeStmt(RetypeStmt rt) { visitAssignStmt(rt); }

    /**
     * Resolves the name of a {@link TypeParam}.
     * <p>
     *     For a type parameter, we will allow a user to shadow previously declared names. Thus,
     *     this visit makes sure that each type parameter associated with either a {@link ClassDecl}
     *     or a {@link FuncDecl} has a unique name.
     * </p>
     * @param tp {@link TypeParam}
     */
    public void visitTypeParam(TypeParam tp) {
        // ERROR CHECK #1: A type parameter can not have the same name as another type parameter.
        if(currentScope.hasName(tp)) {
            ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                  .addLocation(tp)
                                  .addErrorType(MessageType.SCOPE_ERROR_330)
                                  .addArgs(tp)
                                  .asScopeErrorBuilder()
                                  .addRedeclaration(currentScope.findName(tp));
            errors.add(scopeErrorGenerator.createCompileError(eb));
        }
        currentScope.addName(tp);
    }

    /**
     * Creates a new scope for a {@link WhileStmt}.
     * <p>
     *     A while loop will contain one {@link SymbolTable} for its body.
     * </p>
     * @param ws {@link WhileStmt}
     */
    public void visitWhileStmt(WhileStmt ws) {
        super.visitWhileStmt(ws);
        ws.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * An inner class that stores all helper methods used by the {@link NameChecker}.
     */
    private class NameCheckerHelper {

        /**
         * Checks if an appropriate expression was written on the LHS of an {@link AssignStmt}.
         * <p>
         *     In this case, the LHS has to either be a {@link NameExpr} or an {@link ast.expressions.ArrayExpr}
         *     in order to allow a value to be assigned. If the LHS represents a {@link FieldExpr}, then we need
         *     to recursively call this method until we have the final expression contained in the field expression
         *     to determine if its valid. Note: We will not allow any invocations (including those that return objects)
         *     to be present on the LHS of an assignment.
         * </p>
         * @param LHS The current {@link Expression} we are checking which is found on the LHS of an {@link AssignStmt}.
         * @return {@code True} if a value can be assigned to the {@code LHS} expression, {@code False} otherwise.
         */
        public boolean canExpressionBeAssignedTo(Expression LHS) {
            if(LHS.isNameExpr() || LHS.isArrayExpr())
                return true;
            else if(LHS.isFieldExpr())
                return canExpressionBeAssignedTo(LHS.asFieldExpr().getAccessExpr());
            else
                return false;
        }

        /**
         * Updates the symbol table for a subclass to include inherited fields and methods.
         * <p>
         *     If a class inherits from a base class, then we need to ensure the subclass
         *     has access to all class body declarations found in the base class. This helper will insert all
         *     fields and methods from the base class into the subclass.
         * </p>
         * @param subClass {@link ClassDecl} representing the class we want to update the symbol table.
         * @param baseClass {@link ClassDecl} representing an inherited base class.
         */
        public void updateSubClassSymbolTable(ClassDecl subClass, ClassDecl baseClass) {
            NameIterator namesDeclaredInBaseClass = new NameIterator(baseClass.getScope());

            while(namesDeclaredInBaseClass.hasNext()) {
                FieldDecl currentField = namesDeclaredInBaseClass.next().asFieldDecl();

                // ERROR CHECK #1: If a class is inherited, then we need to make sure the subclass will have access to
                //                 all fields declared by the base class. This means a user can not redeclare a base
                //                 class field within a subclass.
                if(currentScope.hasName(currentField)) {
                    ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                          .addLocation(currentScope.findName(currentField))
                                          .addErrorType(MessageType.SCOPE_ERROR_324)
                                          .addArgs(currentField, subClass)
                                          .asScopeErrorBuilder()
                                          .addRedeclaration(currentField);
                    errors.add(scopeErrorGenerator.createCompileError(eb));
                }

                currentScope.addName(currentField);
            }

            currentScope.addAllMethods(baseClass.getScope());
        }

        /**
         * Validates if a user properly overrides methods when using inheritance.
         * <p>
         *     In C Minor, a user may choose to redefine a class method they are inheriting
         *     by using the {@code override} keyword in the method declaration. This means we
         *     have to check if each method in the subclass correctly redefines a base class
         *     method if it is applicable. This will be rather strict as we want users to clearly
         *     understand which methods will be called when working with the appropriate types as
         *     virtual functions are a common headache for beginners.
         * </p>
         * @param subClass The {@link ClassDecl} we are checking for correct method redefinitions.
         * @param baseClass The {@link ClassDecl} we will use to make sure method redefinitions are correct.
         */
        public void checkOverriddenMethods(ClassDecl subClass, ClassDecl baseClass) {
            for(MethodDecl subMethod : subClass.classBlock().getMethods()) {
                boolean methodFoundInBaseClass = false;
                // ERROR CHECK #1: If two methods from a base and subclass respectively have the same signature,
                //                 then the user must explicitly use the `override` keyword in the subclass to denote
                //                 the subclass method will be called instead of the base class method when using
                //                 objects of the subclass type.
                if(baseClass.containsMethod(subMethod)) {
                    methodFoundInBaseClass = true;
                    if(!subMethod.isOverridden()) {
                        ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                              .addLocation(subMethod)
                                              .addErrorType(MessageType.SCOPE_ERROR_311)
                                              .addArgs(subMethod, baseClass);
                        errors.add(scopeErrorGenerator.createCompileError(eb));
                    }
                }

                // ERROR CHECK #2: The 'override' keyword can only be used when a subclass method shares the same
                //                 signature as a base class method (or else the method is not really overridden).
                if(!methodFoundInBaseClass && subMethod.isOverridden()) {
                    ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                          .addLocation(subMethod)
                                          .addErrorType(MessageType.SCOPE_ERROR_312)
                                          .addArgs(subMethod);
                    errors.add(scopeErrorGenerator.createCompileError(eb));
                }
            }
        }

        /**
         * Checks if a variable is self initialized to itself.
         * @param variable The {@link VarDecl} we are checking for self-initialization.
         */
        public void checkVariableSelfInitialization(VarDecl variable) {
            // ERROR CHECK #1: This checks if a variable declaration contains its own name when initialized.
            if(variableNameInInitialization(variable.getVariableName(), variable.getInitialValue())) {
                ErrorBuilder eb = new ScopeErrorBuilder(scopeErrorGenerator)
                                      .addLocation(variable.asAST())
                                      .addArgs(variable);

                if(variable.asAST().isTopLevelDecl()) // Global Declaration Error
                    eb.addErrorType(MessageType.SCOPE_ERROR_303);
                else if(variable.asAST().isStatement()) // Local Declaration Error
                    eb.addErrorType(MessageType.SCOPE_ERROR_301);
                else // Field Declaration Error
                    eb.addErrorType(MessageType.SCOPE_ERROR_326);

                errors.add(scopeErrorGenerator.createCompileError(eb));
            }
        }

        /**
         * Searches to see if a variable's name is found in its initialization expression recursively.
         * @param varName {@link Name} representing the variable name we are searching for.
         * @param currExpr The current {@link Expression} we are checking to see if it contains the variable name.
         * @return {@code True} if the variable name is written anywhere in its initialization, {@code False} otherwise.
         */
        private boolean variableNameInInitialization(Name varName, Expression currExpr) {
            // Base Case: Return true if we found any name expression that refers to the variable name.
            if(currExpr.isNameExpr() && varName.equals(currExpr.asNameExpr().getName()))
                return true;

            // If the base case is not executed, then recursively check each child that the current expression points
            // to (Note: Every child is an expression if contained inside another expression, so no exceptions here!)
            for(AST child : currExpr.children) {
                if(variableNameInInitialization(varName, child.asExpression()))
                    return true;
            }

            return false;
        }
    }
}
