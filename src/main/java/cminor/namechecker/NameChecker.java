package cminor.namechecker;

import cminor.ast.AST;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.misc.*;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.ErrorBuilder;
import cminor.messages.errors.scope.ScopeError;
import cminor.messages.errors.semantic.SemanticError;
import cminor.utilities.SymbolTable;
import cminor.utilities.SymbolTable.NameIterator;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * Name Resolution Pass.
 * <p>
 *     This is the first major semantic pass responsible for scope resolution. Here,
 *     we are checking whether a user properly used names within their program.
 *     At the end of this phase, we will have generated a {@link SymbolTable} for each
 *     {@link cminor.ast.misc.ScopeDecl} that we will constantly reference during other compilation phases.
 * </p>
 * @author Daniel Levy
 */
public class NameChecker extends Visitor {

    /**
     * Current scope we are resolving names in.
     */
    private SymbolTable currentScope;

    /**
     * Instance of {@link NameCheckerHelper} that will be used for additional name checking tasks.
     */
    private final NameCheckerHelper helper;

    /**
     * Creates {@link NameChecker} in compilation mode
     */
    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.helper = new NameCheckerHelper();
        this.handler = new MessageHandler();
    }

    /**
     * Creates {@link NameChecker} in interpretation mode
     * @param globalScope {@link SymbolTable} representing the default scope of the {@link cminor.interpreter.VM}.
     */
    public NameChecker(SymbolTable globalScope) {
        this();
        this.currentScope = globalScope;
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
                    handler.createErrorBuilder(SemanticError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.SEMANTIC_ERROR_708)
                           .addErrorArgs(be.getLHS(), be.getBinaryOp())
                           .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1703)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }

                AST declOfRHS = currentScope.findName(be.getRHS());
                // ERROR CHECK #2: The RHS of an instanceof operation has to be the name of a class.
                if (!RHS.isNameExpr() || !declOfRHS.isTopLevelDecl() || !declOfRHS.asTopLevelDecl().isClassDecl()) {
                    handler.createErrorBuilder(ScopeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.SCOPE_ERROR_323)
                           .addErrorArgs(be.getRHS(), be.getBinaryOp())
                           .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1304)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
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
        if(currentScope.hasName(cd)) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(cd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_307)
                   .addErrorArgs(cd)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(cd))
                   .generateError();
        }
        currentScope.addName(cd);

        ClassDecl baseClass = null;
        if(cd.getSuperClass() != null) {
            // ERROR CHECK #2: The user can not have a class inherit from itself.
            if(cd.getDeclName().equals(cd.getSuperClass().getClassName().toString())) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(cd)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_308)
                       .addErrorArgs(cd)
                       .generateError();
            }

            // ERROR CHECK #3: This ensures the inherited class was already declared in the program. The scope
            //                 resolution is done in a single pass, so classes must be declared in the right order.
            if(!helper.isValidClassName(cd.getSuperClass().getClassName())) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(cd)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_309)
                       .addErrorArgs(cd, cd.getSuperClass())
                       .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1301)
                       .addSuggestionArgs(cd.getSuperClass())
                       .generateError();
            }

            baseClass = currentScope.findName(cd.getSuperClass().getClassName()).asTopLevelDecl().asClassDecl();
        }

        currentScope = currentScope.openScope();

        // We are allowing type parameters to shadow previous top level declarations.
        for(TypeParam tp : cd.getTypeParams())
            tp.visit(this);

        for(FieldDecl fd : cd.getClassBody().getFields())
            fd.visit(this);

        // Before visiting methods, update the current class scope to contain fields from the base class.
        if(baseClass != null)
            helper.updateSubClassSymbolTable(cd, baseClass);

        for(MethodDecl md : cd.getClassBody().getMethods())
            md.visit(this);

        helper.checkOverriddenMethods(cd, baseClass);

        cd.setScope(currentScope);
        currentScope = currentScope.closeScope();
    }

    /**
     * Begins the C Minor scope resolution pass in compilation mode.
     * <p>
     *     This will be the first visit when we are executing the {@link NameChecker} in compilation mode.
     * </p>
     * @param c {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit c) {
        super.visitCompilationUnit(c);
        c.setScope(currentScope);
        handler.printMessages();
    }

    /**
     * Creates a new scope for a {@link DoStmt}.
     * <p>
     *     A do while loop will contain one {@link SymbolTable} for its body.
     * </p>
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        ds.getBody().visit(this);
        ds.setScope(currentScope);
        currentScope = currentScope.closeScope();
        ds.getCondition().visit(this);
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
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(ed)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_321)
                   .addErrorArgs(ed)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(ed))
                   .generateError();
        }
        currentScope.addName(ed);

        // ERROR CHECK #2: Each constant declared in the enum will be treated as a global constant. As a result,
        //                 we need to make sure the constant's name is not already taken by a separate top level
        //                 declaration.
        for(Var constant : ed.getConstants()) {
            if(currentScope.hasName(constant)) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(ed)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_322)
                       .addErrorArgs(constant,ed)
                       .asScopeErrorBuilder()
                       .addOriginalDeclaration(currentScope.findName(constant))
                       .generateError();
            }

            constant.getInitialValue().visit(this);
            // Add the constant and have it point back to the enum.
            currentScope.addName(constant.toString(), ed);
        }
    }

    /**
     * Resolves all names associated with a {@link FieldDecl}.
     * <p>
     *     When declaring a field inside a class, we want to make sure its name is unique to
     *     all other declared fields in the class. If the field is declared inside a subclass, then
     *     we will check if it's contained in the base class when returning to {@link #visitClassDecl(ClassDecl)}.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        // ERROR CHECK #1: We need to make sure the current field does not redeclare an existing field.
        if(currentScope.hasName(fd)) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(fd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_310)
                   .addErrorArgs(fd, fd.getClassDecl())
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(fd))
                   .generateError();
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

        fs.getControlVariable().visit(this);
        fs.getStartValue().visit(this);
        fs.getEndValue().visit(this);

        for(LocalDecl ld : fs.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : fs.getBody().getStatements())
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
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(fd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_306)
                   .addErrorArgs(fd)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findMethod(fd))
                   .generateError();
        }
        currentScope.addMethod(fd);

        currentScope = currentScope.openScope();

        for(TypeParam tp : fd.getTypeParams())
            tp.visit(this);
        for(ParamDecl pd : fd.getParams())
            pd.visit(this);
        for(LocalDecl ld : fd.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : fd.getBody().getStatements())
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
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(gd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_302)
                   .addErrorArgs(gd)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(gd))
                   .generateError();
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
        is.getCondition().visit(this);
        is.getIfBody().visit(this);

        is.setScope(currentScope);
        currentScope = currentScope.closeScope();

        for(IfStmt elifStmt : is.getElifs())
            elifStmt.visit(this);

        if(is.containsElse()) {
            is.getElseBody().visit(this);
            is.setScope(currentScope);
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Creates a new scope for an {@link ImportDecl}.
     * <p>
     *     For an import declaration, we will run the {@code NameChecker} on its {@link CompilationUnit}
     *     unit and save the compilation unit's symbol table into the current scope in order to access
     *     any names declared in the {@link ImportDecl}.
     * </p>
     * @param im {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl im) {
       // currentScope = currentScope.openImportScope();
        im.getCompilationUnit().visit(this);
        //currentScope = previousCompilationScope;
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
        // ERROR CHECK #1: A user can not redeclare a local variable with a name that
        //                 was already declared in the current scope.
        if(currentScope.hasName(ld)) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(ld)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_300)
                   .addErrorArgs(ld)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(ld))
                   .generateError();
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
     *     is stored by {@link CompilationUnit}.
     * </p>
     * @param md {@link MainDecl}
     */
    public void visitMainDecl(MainDecl md) {
        for(ParamDecl param : md.getParams())
            param.visit(this);
        for(LocalDecl ld : md.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : md.getBody().getStatements())
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
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(md)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_313)
                   .addErrorArgs(md, md.getClassDecl())
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findMethod(md))
                   .generateError();
        }
        currentScope.addMethod(md);

        currentScope = currentScope.openScope();

        for(ParamDecl pd : md.getParams())
            pd.visit(this);
        for(LocalDecl ld : md.getBody().getLocalDecls())
            ld.visit(this);
        for(Statement s : md.getBody().getStatements())
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
        // ERROR CHECK #1: This checks if the name was not declared somewhere in the program.
        if(!currentScope.hasNameInProgram(ne)) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(ne.getFullLocation())
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_304)
                   .addErrorArgs(ne)
                   .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1300)
                   .addSuggestionArgs(ne)
                   .generateError();
        }
        else {
            AST varDecl = currentScope.findName(ne);

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
        if(!helper.isValidClassName(ne.getClassName())) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(ne)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_314)
                   .addErrorArgs(ne.getClassType())
                   .generateError();
        }

        ClassDecl cd = currentScope.findName(ne.getClassName()).asTopLevelDecl().asClassDecl();
        Vector<String> initializedFields = new Vector<>();

        for(Var field : ne.getInitialFields()) {
            // ERROR CHECK #2: For each field specified, we need to make sure it was actually declared in the class.
            if(!cd.getScope().hasName(field)) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(ne)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_315)
                       .addErrorArgs(field, ne.getClassType())
                       .generateError();
            }
            // ERROR CHECK #3: An object's field can only be initialized once per instantiation.
            if(initializedFields.contains(field.toString())) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(ne)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_316)
                       .addErrorArgs(field,ne.getClassType())
                       .generateError();
            }

            initializedFields.add(field.toString());
            field.getInitialValue().visit(this);
        }
    }

    /**
     * Resolves if the {@code parent} keyword was correctly used.
     * <p>
     *     For the {@code parent} keyword, we need to ensure it is only used within
     *     classes that inherit from other classes. All other usages will generate an error.
     * </p>
     * @param ps {@link ParentStmt}
     */
    public void visitParentStmt(ParentStmt ps) {
        AST root = ps.getTopLevelDecl();

        // ERROR CHECK #1: This checks if the 'parent' keyword was used outside a class.
        if(!root.isTopLevelDecl() || !root.asTopLevelDecl().isClassDecl()) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(root)
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_318)
                    .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1302)
                    .generateError();
        }

        // ERROR CHECK #2: This checks if the 'parent' keyword is used in a class with no inherited classes.
        if(root.asTopLevelDecl().asClassDecl().getSuperClass() == null) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(root)
                    .addErrorArgs(root.asTopLevelDecl().asClassDecl().getName())
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_319)
                    .addSuggestionNumber(MessageNumber.SCOPE_SUGGEST_1303)
                    .generateError();
        }
    }

    /**
     * Resolves the name of a {@link ParamDecl}.
     * <p>
     *     We are going to allow a user to shadow a name already declared in a top level declaration
     *     or inside a class. This means the parameter name checking is concerned with making sure
     *     we are not using the same parameter name for multiple parameters.
     * </p>
     * @param pd {@link ParamDecl}
     */
    public void visitParamDecl(ParamDecl pd) {
        // ERROR CHECK #1: A parameter can not have the same name as another parameter.
        if(currentScope.hasName(pd)) {
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(pd)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_305)
                   .addErrorArgs(pd)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(pd))
                   .generateError();
        }
        currentScope.addName(pd);
    }

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
            handler.createErrorBuilder(ScopeError.class)
                   .addLocation(tp)
                   .addErrorNumber(MessageNumber.SCOPE_ERROR_330)
                   .addErrorArgs(tp)
                   .asScopeErrorBuilder()
                   .addOriginalDeclaration(currentScope.findName(tp))
                   .generateError();
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
         * Checks if a passed {@link AST} node represents a valid class name.
         * @param name The {@link AST} node we want to verify if it represents a class.
         * @return {@code True} if the node can be traced back to a
         *          {@link cminor.ast.topleveldecls.ClassDecl}, {@code False} otherwise.
         */
        public boolean isValidClassName(AST name) {
            if(!currentScope.hasNameInProgram(name))
                return false;

            AST decl = currentScope.findName(name);
            return decl.isTopLevelDecl() && decl.asTopLevelDecl().isClassDecl();
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
                FieldDecl currentField = namesDeclaredInBaseClass.next().asClassNode().asFieldDecl();

                // ERROR CHECK #1: If a class is inherited, then we need to make sure the subclass will have access to
                //                 all fields declared by the base class. This means a user can not redeclare a base
                //                 class field within a subclass.
                if(currentScope.hasName(currentField)) {
                    handler.createErrorBuilder(ScopeError.class)
                           .addLocation(currentScope.findName(currentField))
                           .addErrorNumber(MessageNumber.SCOPE_ERROR_324)
                           .addErrorArgs(currentField, subClass)
                           .asScopeErrorBuilder()
                           .addOriginalDeclaration(currentField)
                           .generateError();
                }

                currentScope.addName(currentField);
            }

            // currentScope.addAllMethods(baseClass.getScope());
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
            for(MethodDecl subMethod : subClass.getClassBody().getMethods()) {
                boolean methodFoundInBaseClass = false;

                // ERROR CHECK #1: If two methods from a base and subclass respectively have the same signature,
                //                 then the user must explicitly use the `override` keyword in the subclass to denote
                //                 the subclass method will be called instead of the base class method when using
                //                 objects of the subclass type.
                if(baseClass != null && baseClass.containsMethod(subMethod)) {
                    methodFoundInBaseClass = true;
                    if(!subMethod.isOverridden()) {
                        handler.createErrorBuilder(ScopeError.class)
                                .addLocation(subMethod)
                                .addErrorNumber(MessageNumber.SCOPE_ERROR_311)
                                .addErrorArgs(subMethod, baseClass)
                                .generateError();
                    }
                }

                // ERROR CHECK #2: The 'override' keyword can only be used when a subclass method shares the same
                //                 signature as a base class method (or else the method is not really overridden).
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
         * Checks if a variable is self initialized to itself.
         * @param variable The {@link VarDecl} we are checking for self-initialization.
         */
        public void checkVariableSelfInitialization(VarDecl variable) {
            // ERROR CHECK #1: This checks if a variable declaration contains its own name when initialized.
            if(variableNameInInitialization(variable.getVariableName(), variable.getInitialValue())) {
                ErrorBuilder eb = handler.createErrorBuilder(ScopeError.class)
                                         .addLocation(variable.asAST())
                                         .addErrorArgs(variable);

                if(variable.asAST().isTopLevelDecl()) // Global Declaration Error
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_303);
                else if(variable.asAST().isStatement()) // Local Declaration Error
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_301);
                else // Field Declaration Error
                    eb.addErrorNumber(MessageNumber.SCOPE_ERROR_326);

                eb.generateError();
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
            for(AST child : currExpr.getChildren()) {
                if(child.isExpression()) {
                    // Ignore invocations and field expressions since their names might be the same as the variable!
                    if(child.asExpression().isInvocation() || child.asExpression().isFieldExpr())
                        continue;

                    if(variableNameInInitialization(varName, child.asExpression()))
                        return true;
                }
            }

            return false;
        }
    }
}
