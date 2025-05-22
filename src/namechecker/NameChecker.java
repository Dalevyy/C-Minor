package namechecker;

import ast.*;
import ast.classbody.*;
import ast.expressions.*;
import ast.misc.NameNode;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.ClassType;
import messages.errors.ErrorBuilder;
import messages.MessageType;
import messages.errors.scope_error.ScopeErrorFactory;
import utilities.*;

import java.util.HashSet;

/**
 * C Minor - Scope Resolution Pass
 */
public class NameChecker extends Visitor {

    private SymbolTable currentScope;
    private final ScopeErrorFactory generateScopeError;
    private final Vector<String> errors;

    /**
     * Creates name checker in compilation mode
     */
    public NameChecker() {
        this.currentScope = new SymbolTable();
        this.generateScopeError = new ScopeErrorFactory();
        this.errors = new Vector<>();
    }

    /**
     * Creates name checker in interpretation mode
     * @param st Compilation Unit Symbol Table
     */
    public NameChecker(SymbolTable st) {
        this.currentScope = st;
        this.interpretMode = true;
        this.generateScopeError = new ScopeErrorFactory();
        this.errors = new Vector<>();
    }

    /**
     * Assignment Statements<br>
     *
     * We have 2 different types of assignment statements.
     *
     *      <ol>
     *          <li>Set Statements</li>
     *          <li>Retype Statements </li>
     *      </ol>
     */
    public void visitAssignStmt(AssignStmt as) {
        // ERROR CHECK #1: Make sure the LHS of an assignment is a name, field, or array expression
        if(!(as.LHS().isNameExpr() || as.LHS().isFieldExpr() || as.LHS().isArrayExpr())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(as)
                        .addErrorType(MessageType.SCOPE_ERROR_325)
                        .addArgs(as.LHS().toString())
                        .addSuggestType(MessageType.SCOPE_SUGGEST_1302)
                        .error()
            );
        }
        super.visitAssignStmt(as);
    }

    /**
     * <p>
     *     For a binary expression, we need to check if the
     *     <code>instanceof</code> and <code>as?</code> operators correctly
     *     reference a class name on the RHS.
     * </p>
     * @param be Binary Expression
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.LHS().visit(this);

        switch(be.binaryOp().toString()) {
            case "instanceof":
            case "!instanceof":
            case "as?":
                // ERROR CHECK #1: Check if the name has been declared
                if(!currentScope.hasNameSomewhere(be.RHS().toString())) {
                    errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SCOPE_ERROR_328)
                            .addArgs(be.RHS().toString())
                            .error()
                    );
                }
                AST cd = currentScope.findName(be.RHS().toString()).decl();
                // ERROR CHECK #2: Check if the name represents a class
                if(!cd.isTopLevelDecl() || !cd.asTopLevelDecl().isClassDecl()) {
                    errors.add(
                        new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.SCOPE_ERROR_329)
                            .addArgs(be.RHS().toString())
                            .error()
                    );
                }
                break;
            default:
                be.RHS().visit(this);
        }
    }

    /**
     * <p>
     *     When we visit a block statement, we will open a scope and visit
     *     the block's statements. The scope will be closed by whichever AST
     *     node called this method after the scope is saved into the node.
     * </p>
     * @param bs Block Statement
     */
    public void visitBlockStmt(BlockStmt bs) {
        currentScope = currentScope.openNewScope();

        for(LocalDecl ld : bs.decls()) { ld.visit(this); }
        for(Statement s : bs.stmts()) { s.visit(this); }
    }

    /**
     * <p>
     *     A case statement opens a scope for its block statement.
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
     * <p>
     *     A choice statement will visit all of its case statements first.
     *     Then, a new scope will be opened for the `other` case that is
     *     stored in the choice statement itself.
     * </p>
     * @param cs Choice Statement
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);
        for(CaseStmt c : cs.caseStmts()) { c.visit(this); }

        cs.otherBlock().visit(this);
        cs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * <p>
     *     A class opens a scope to store its field and method declarations.
     *     Most of the name checking done here relates to inheritance. We need
     *     to make sure the class doesn't inherit itself, and the class we are
     *     trying to inherit from exists. From there, we are going to add every
     *     field and method declaration from the base class into the subclass,
     *     so these nodes can be accessed by the subclass.
     * </p>
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        // ERROR CHECK #1: Make sure the class name has not been used yet
        if(currentScope.isNameUsedAnywhere(cd.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.SCOPE_ERROR_316)
                        .addArgs(cd.toString())
                        .error()
            );
        }
        currentScope.addName(cd.toString(),cd);

        if(cd.superClass() != null) {
            // ERROR CHECK #2: Make sure the class isn't inheriting itself
            if(cd.toString().equals(cd.superClass().toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(cd)
                            .addErrorType(MessageType.SCOPE_ERROR_317)
                            .addArgs(cd.toString())
                            .error()
                );
            }

            // ERROR CHECK #3: Make sure the inherited class exists
            if(!currentScope.hasNameSomewhere(cd.superClass().toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(cd)
                            .addErrorType(MessageType.SCOPE_ERROR_322)
                            .addArgs(cd.superClass().toString())
                            .error()
                );
            }
        }

        currentScope = currentScope.openNewScope();
        cd.symbolTable = currentScope;

        for(FieldDecl fd : cd.classBlock().fieldDecls()) { fd.visit(this); }

        if(cd.superClass() != null) {
            ClassDecl base = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            // Go through each declaration in the base class
            for(String name : base.symbolTable.getAllNames().keySet()) {
                AST decl = base.symbolTable.findName(name).decl();
                if(decl.isFieldDecl()) {
                    // ERROR CHECK #3: Each field name in a subclass needs to be unique
                    //                 from the fields declared in the base class
                    if(currentScope.hasName(name)) {
                        errors.add(
                            new ErrorBuilder(generateScopeError, interpretMode)
                                    .addLocation(cd)
                                    .addErrorType(MessageType.SCOPE_ERROR_318)
                                    .addArgs(name)
                                    .error()
                        );
                    }
                    currentScope.addName(name,decl.asFieldDecl());
                }
                if (name.contains("/")) { currentScope.addName(name,decl.asMethodDecl()); }
                else { currentScope.addName(name + "/" + base,decl.asMethodDecl()); }
            }

            for(String name: base.symbolTable.getMethodNames()) { currentScope.addMethod(name); }
        }

        for(MethodDecl md : cd.classBlock().methodDecls()) { md.visit(this); }
        currentScope = currentScope.closeScope();
    }

    /**
     * Checks to see if the name used for the class type was used
     * @param ct Class Type
     */
    public void visitClassType(ClassType ct) {
        if(!currentScope.hasNameSomewhere(ct.toString())) {
            errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(ct)
                            .addErrorType(MessageType.SCOPE_ERROR_329)
                            .addArgs(ct.toString())
                            .error()
            );
        }
    }

    /**
     * <p>
     *     A do statement will store a symbol table for its block statement.
     *     We will open a new scope when we visit the do statement's block
     *     and once the scope is saved into the <code>DoStmt</code> node, we
     *     will name check its conditional expression.
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
     * <p>
     *     An <code>EnumDecl</code>will be the first construct a user can define in a C Minor
     *     program. Thus, we want to check if the name bound to an Enum isn't
     *     already used by another Enum in the same file OR by some other construct
     *     located in an import file.
     *     <br><br>
     *     Additionally, every constant inside an Enum will become global. Thus,
     *     if we have two or more Enums, all their constants need to have different
     *     names in order for the program to compile.
     * </p>
     * @param ed Enum Declaration
     */
    public void visitEnumDecl(EnumDecl ed) {
        // ERROR CHECK #1: Make sure the enum name has not been used yet
        if(currentScope.hasName(ed.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(ed)
                    .addErrorType(MessageType.SCOPE_ERROR_305)
                    .addArgs(ed.toString())
                    .error()
            );
        }
        currentScope.addName(ed.toString(),ed);

        // ERROR CHECK #2: Each constant name associated with the
        //                 current enum can not have already been used.
        for(Var constant : ed.constants()) {
            if(currentScope.hasName(constant.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ed)
                        .addErrorType(MessageType.SCOPE_ERROR_306)
                        .addArgs(constant.toString())
                        .error()
                );
            }
            currentScope.addName(constant.toString(),ed);
        }
    }

    /**
     * <p>
     *     When a user declares a field, we want to make sure its name
     *     doesn't already refer to something else in the class scope.
     * </p>
     * @param fd Field Declaration
     */
    public void visitFieldDecl(FieldDecl fd) {
        // ERROR CHECK #1: Make sure the field name has not been used
        if(currentScope.hasName(fd.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(fd)
                        .addErrorType(MessageType.SCOPE_ERROR_315)
                        .addArgs(fd.toString())
                        .error()
            );
        }
        fd.type().visit(this);

        if(fd.var().init() != null) {
            // ERROR CHECK #2: Do not allow a field to be initialized to itself
            if(fd.var().init().toString().equals(fd.toString())) {
                errors.add(
                        new ErrorBuilder(generateScopeError,interpretMode)
                                .addLocation(fd)
                                .addErrorType(MessageType.SCOPE_ERROR_331)
                                .addArgs(fd.toString())
                                .error()
                );
            }
            fd.var().init().visit(this);
        }
        currentScope.addName(fd.toString(),fd);
    }

    /**
     * <p>
     *     For a field expression, we will only check to see if the target's
     *     name can be resolved at this point. We have to wait until we know
     *     the target's type to do proper name checking for the rest of the
     *     access expression (in case it is too complicated).
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        if(!fe.fieldTarget().isThis()) { fe.fieldTarget().visit(this); }
    }

    /**
     * <p>
     *     A for statement will store a symbol table for its block statement.
     *     In C Minor, the loop control variable will be contained in the
     *     same scope as the block statement in order to prevent a user from
     *     redeclaring it.
     * </p>
     *
     * @param fs For Statement
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = currentScope.openNewScope();

        fs.loopVar().visit(this);
        fs.condLHS().visit(this);
        fs.condRHS().visit(this);

        for(LocalDecl ld : fs.forBlock().decls()) { ld.visit(this); }
        for(Statement s : fs.forBlock().stmts()) { s.visit(this); }

        fs.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * <p>
     *     When we visit a function, we want to make sure both the name and
     *     signature of the function is unique in order to support function
     *     overloading. From there, we will open a new scope.
     * </p>
     * @param fd Function Declaration
     */
    public void visitFuncDecl(FuncDecl fd) {
        // ERROR CHECK #1: Make sure the function name is not being used already
        if(currentScope.hasName(fd.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(fd)
                        .addErrorType(MessageType.SCOPE_ERROR_311)
                        .addArgs(fd.toString())
                        .error()
            );
        }

        String funcSignature = fd + "/";
        if(fd.params() != null) { funcSignature += fd.paramSignature(); }

        // ERROR CHECK #2: Make sure the function signature is unique to support overloading
        if(currentScope.hasNameSomewhere(funcSignature)) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(fd)
                        .addErrorType(MessageType.SCOPE_ERROR_312)
                        .addArgs(fd.toString())
                        .error()
            );
        }
        currentScope.addName(funcSignature, fd);
        currentScope.addMethod(fd.toString());

        currentScope = currentScope.openNewScope();
        for(ParamDecl pd : fd.params()) { pd.visit(this); }

        for(LocalDecl ld : fd.funcBlock().decls()) { ld.visit(this); }
        for(Statement s : fd.funcBlock().stmts()) { s.visit(this); }

        fd.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * <p>
     *     When a user declares a global variable, we want to make sure its name
     *     doesn't already refer to something else in the current scope.
     * </p>
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // ERROR CHECK #1: Check if the name was declared in the current scope
        if(currentScope.hasName(gd.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(gd)
                    .addErrorType(MessageType.SCOPE_ERROR_302)
                    .addArgs(gd.toString())
                    .error()
            );
        }
        gd.type().visit(this);

        if(gd.var().init() != null) {
            // ERROR CHECK #2: Do not allow a global variable to be initialized to itself
            if(gd.var().init().toString().equals(gd.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(gd)
                            .addErrorType(MessageType.SCOPE_ERROR_303)
                            .addArgs(gd.toString())
                            .error()
                );
            }
            gd.var().init().visit(this);
        }
        currentScope.addName(gd.toString(), gd);
    }

    /**
     * <p>
     *     An if statement will have two symbol tables: one for the if branch
     *     block and one for the else branch block (if it exists). For else if
     *     statements, only one symbol table will ever exist.
     * </p>
     * @param is If Statement
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

    /**
     * <p>
     *      At this point, an invocation visit only happens if we are visiting
     *      a function. We can not name check method invocations yet since we
     *      are going to need to know the types of the target to deduce whether
     *      the invocation can be executed in the first place. Thus, we only check
     *      if a function can be called.
     * </p>
     * @param in Invocation
     */
    public void visitInvocation(Invocation in) {
        String funcName = in.toString();
        // ERROR CHECK #1: Make sure the function was declared previously
        if(!currentScope.hasMethodSomewhere(in.toString())) {
            errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(in)
                    .addErrorType(MessageType.SCOPE_ERROR_319)
                    .addArgs(funcName)
                    .error());
        }

        for(Expression e : in.arguments()) { e.visit(this); }
    }

    /**
     * <p>
     *     When a user declares a local variable, we want to make sure its name
     *     doesn't already refer to something else in the current scope.
     * </p>
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: Check if the name was declared in the current scope
        if(currentScope.hasName(ld.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ld)
                        .addErrorType(MessageType.SCOPE_ERROR_300)
                        .addArgs(ld.toString())
                        .error()
            );
        }
        ld.type().visit(this);

        if(ld.var().init() != null) {
            // ERROR CHECK #2: Do not allow a local variable to be initialized to itself
            if(ld.var().init().toString().equals(ld.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                            .addLocation(ld)
                            .addErrorType(MessageType.SCOPE_ERROR_301)
                            .addArgs(ld.toString())
                            .error()
                );
            }
            ld.var().init().visit(this);
        }
        currentScope.addName(ld.toString(),ld);
    }

    /**
     * <p>
     *     For the main function of the program, we will treat its scope to be
     *     the same as the global scope. Thus, we are not going to create a new
     *     scope for the block associated with main. We do this since we want to
     *     prevent a user from redefining any top level declarations.
     * </p>
     * @param md Main Declaration
     */
    public void visitMainDecl(MainDecl md) {
        for(ParamDecl e : md.args()) { e.visit(this); }

        for(LocalDecl ld : md.mainBody().decls()) { ld.visit(this); }
        for(Statement s : md.mainBody().stmts()) { s.visit(this); }

        md.symbolTable = currentScope;
        currentScope.closeScope();
    }

    /**
     * <p>
     *     When we visit a method, we want to make sure both the name and
     *     signature of the method is unique in order to support method
     *     overloading. From there, we will open a new scope.
     * </p>
     * @param md Method Declaration
     */
    public void visitMethodDecl(MethodDecl md) {
        // ERROR CHECK #1: Make sure the method name has not been used yet
        if(currentScope.hasName(md.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.SCOPE_ERROR_313)
                        .addArgs(md.toString())
                        .error()
            );
        }

        String methodSignature = md + "/";
        if(md.params() != null) { methodSignature += md.paramSignature(); }

        // ERROR CHECK #2: Make sure method signature is unique to support overloading
        if(currentScope.hasName(methodSignature)) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.SCOPE_ERROR_314)
                        .addArgs(md.toString())
                        .error()
            );
        }
        currentScope.addName(methodSignature, md);
        currentScope.addMethod(md.toString());

        currentScope = currentScope.openNewScope();
        for(ParamDecl pd : md.params()) { pd.visit(this); }

        for(LocalDecl ld : md.methodBlock().decls()) { ld.visit(this); }
        for(Statement s : md.methodBlock().stmts()) { s.visit(this); }

        md.symbolTable = currentScope;
        currentScope = currentScope.closeScope();
    }

    /**
     * <p>
     *     For any name, we check if the name can be traced back to a declaration.
     *     All names without prior declarations will result in scoping errors.
     * </p>
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        // ERROR CHECK #1: Check if the name used was previously
        //                 declared somewhere in the program
        if(!currentScope.isNameUsedAnywhere(ne.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(ne)
                    .addErrorType(MessageType.SCOPE_ERROR_307)
                    .addArgs(ne.toString())
                    .error()
            );
        }
        else {
            AST varDecl = currentScope.findName(ne.toString()).decl();
            // ERROR CHECK #2: Make sure the name does not reference an
            //                 enumeration when used by itself
            if(varDecl.isTopLevelDecl()
                    && (varDecl.asTopLevelDecl().isEnumDecl() && varDecl.toString().equals(ne.toString()))) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_326)
                        .addArgs(ne.toString())
                        .error()
                );
            }
        }
    }

    /**
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
        NameNode nn = currentScope.findName(ne.classType().toString());
        // ERROR CHECK #1: Check if the class exists in the program
        if(nn == null) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_308)
                        .addArgs(ne.classType().toString())
                        .error()
            );
        }

        ClassDecl cd = nn.decl().asTopLevelDecl().asClassDecl();
        HashSet<String> seen = new HashSet<>();
        for(Var v : ne.args()) {
            // ERROR CHECK #2: Check if the field name was defined in the class
            if(!cd.symbolTable.hasName(v.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_309)
                        .addArgs(v.toString(),ne.classType().toString())
                        .error()
                );
            }
            // ERROR CHECK #3: Check if the field name was already used
            //                 in the name expression earlier
            else if(seen.contains(v.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.SCOPE_ERROR_310)
                        .addArgs(v.toString())
                        .error()
                );
            }
            seen.add(v.toString());
            v.init().visit(this);
        }
    }

    /**
     * <p>
     *     We will add the parameter to the current function or method symbol
     *     table. We are not going to allow users to shadow the names of previously
     *     defined constructs, so they can still be used inside of the function or
     *     method we are checking the parameter for.
     * </p>
     * @param pd Parameter Declaration
     */
    public void visitParamDecl(ParamDecl pd) {
        // ERROR CHECK #1: Make sure the parameter name wasn't used already
        if(currentScope.isNameUsedAnywhere(pd.toString())) {
            errors.add(
                new ErrorBuilder(generateScopeError,interpretMode)
                    .addLocation(pd)
                    .addErrorType(MessageType.SCOPE_ERROR_304)
                    .addArgs(pd.toString())
                    .error()
            );
        }
        pd.type().visit(this);
        
        currentScope.addName(pd.toString(),pd);
    }

    /**
     * <p>
     *     A while loop will store a symbol table for its block statement.
     *     We will open a new scope when we visit the block statement and
     *     save the scope into the <code>WhileStmt</code> node.
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
