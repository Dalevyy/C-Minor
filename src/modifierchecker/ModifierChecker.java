package modifierchecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.FieldExpr;
import ast.expressions.Invocation;
import ast.expressions.NewExpr;
import ast.statements.AssignStmt;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.MainDecl;
import java.util.HashSet;
import messages.errors.ErrorBuilder;
import messages.MessageType;
import messages.errors.mod.ModErrorFactory;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

public class ModifierChecker extends Visitor {

    private SymbolTable currentScope;
    private AST currentContext;
    private ClassDecl currentClass;
    private final ModErrorFactory generateModError;
    private final Vector<String> errors;

    /**
     * Creates modifier checker in compilation mode
     */
    public ModifierChecker() {
        this.currentScope = null;
        this.generateModError = new ModErrorFactory();
        this.errors = new Vector<>();
    }

    /**
     * Creates modifier checker in interpretation mode
     * @param st Symbol Table
     */
    public ModifierChecker(SymbolTable st) {
        this();
        this.currentScope = st;
        this.interpretMode = true;
    }

    /**
     * Determines if abstract methods were implemented in concrete classes.<br><br>
     * <p>
     *     This algorithm comes from Dr. Pedersen's compilers textbook, and it will
     *     determine whether or not a method is considered abstract or concrete based
     *     on its implementation inside of a class. This will only be called by
     *     {@link #checkAbstrClassImplementation(ClassDecl, ClassDecl)} when we are
     *     checking if a class correctly implements all methods from an abstract class.
     * </p>
     * @param abstr Current set of abstract methods
     * @param concrete Current set of concrete methods
     * @param cd Current class we are checking method implementation for
     */
    private void sortClassMethods(HashSet<String> abstr, HashSet<String> concrete, ClassDecl cd) {

        // Start from the top of the inheritance hierarchy
        if(cd.superClass() != null) {
            sortClassMethods(abstr,concrete,
                    currentScope.findName(cd.superClass().typeName()).decl().asTopLevelDecl().asClassDecl());
        }

        for(String conName : concrete)
            abstr.remove(conName);

        for(MethodDecl md : cd.classBlock().methodDecls()) {
            if(cd.mod.isAbstract()) {
                abstr.add(md.toString());
                concrete.remove(md.toString());
            }
            else {
                concrete.add(md.toString());
                abstr.remove(md.toString());
            }
        }
    }

    /**
     * Checks if the user correctly implements an inherited abstract class.<br><br>
     * <p>
     *     This method will validate if a user correctly inherits from an abstract
     *     class. A valid inheritance implies the user has implemented every single
     *     method declared in the abstract class in their base class. To determine
     *     which methods were implemented, we will call {@link #sortClassMethods(HashSet, HashSet, ClassDecl)}.
     * </p>
     * @param subClass   Subclass representing concrete class inheriting abstract class
     * @param superClass Superclass representing an abstract class
     */
    private void checkAbstrClassImplementation(ClassDecl subClass, ClassDecl superClass) {
        HashSet<String> concretes = new HashSet<>();
        HashSet<String> abstracts = new HashSet<>();
        sortClassMethods(abstracts,concretes,subClass);

        // ERROR CHECK #1: Make sure all abstract methods from the
        //                 superclass were implemented in the subclass
        if(!abstracts.isEmpty()) {
            errors.add(
                new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(subClass)
                        .addErrorType(MessageType.MOD_ERROR_501)
                        .addArgs(subClass.toString(),superClass.toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1501)
                        .error()
            );
        }
    }

    /**
     * Checks modifier usage for assignment statements.<br><br>
     * <p>
     *     Since constant variables are allowed in C Minor, we have to check
     *     to make sure a user isn't redefining any constants within their
     *     programs. In this case, we are checking for enum and global constants.
     * </p>
     * @param as Assignment Statement
     */
    public void visitAssignStmt(AssignStmt as) {
        AST LHS = currentScope.findName(as.LHS().toString()).decl();

        if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
            // ERROR CHECK #1: A constant can not be updated after its declaration
            if(LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                errors.add(
                    new ErrorBuilder(generateModError,interpretMode)
                            .addLocation(as)
                            .addErrorType(MessageType.MOD_ERROR_505)
                            .addArgs(as.LHS().toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1505)
                            .error()
                );
            }
        }
        // ERROR CHECK #2: An enum constant can not be reassigned its value
        else if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isEnumDecl()) {
            errors.add(
                new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(as)
                        .addErrorType(MessageType.MOD_ERROR_508)
                        .addArgs(as.LHS().toString())
                        .error()
            );
        }
    }

    /**
     * Sets current scope inside case block
     * @param cs Case Statement
     */
    public void visitCaseStmt(CaseStmt cs) {
        SymbolTable oldScope = currentScope;
        currentScope = cs.symbolTable;
        super.visitCaseStmt(cs);
        currentScope = oldScope;
    }

    /**
     * Sets current scope inside choice's other block
     * @param cs Choice Statement
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        for(CaseStmt c : cs.caseStmts())
            c.visit(this);

        SymbolTable oldScope = currentScope;
        currentScope = cs.symbolTable;
        cs.otherBlock().visit(this);
        currentScope = oldScope;
    }

    /** Checks class modifier usage.<br><br>
     * <p>
     *     All modifier checks in classes relate to inheritance, and there
     *     are currently two checks we need to do.
     *     <ol>
     *         <li>
     *             If a class inherits from a class marked as 'final', then
     *             we need to produce an error since the user does not want
     *             the superclass to be inherited by other classes.
     *         </li>
     *         <li>
     *             If a class inherits from an abstract class, then we need to
     *             ensure a user correctly implements every method declared in
     *             the abstract class inside of the concrete class that inherits
     *             from it.
     *         </li>
     *     </ol>
     * </p>
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;

        if(cd.superClass() != null) {
            ClassDecl superDecl = currentScope.findName(cd.superClass().toString())
                                                            .decl().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: Make sure the superclass was not declared as 'final'
            if(superDecl.mod.isFinal()) {
                errors.add(
                    new ErrorBuilder(generateModError,interpretMode)
                            .addLocation(cd)
                            .addErrorType(MessageType.MOD_ERROR_500)
                            .addArgs(cd.toString(),superDecl.toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1500)
                            .error()
                );
            }

            // If the current class is inheriting from an abstract class, then check
            // if every superclass method was implemented in the current class
            if(!cd.mod.isAbstract() && superDecl.mod.isAbstract())
                checkAbstrClassImplementation(cd,superDecl);
        }
        this.currentClass = cd;
        super.visitClassDecl(cd);
        this.currentClass = null;
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets current scope inside do while loop's block.
     * @param ds Do Statement
     */
    public void visitDoStmt(DoStmt ds) {
        SymbolTable oldScope = currentScope;
        currentScope = ds.symbolTable;
        ds.doBlock().visit(this);
        currentScope = oldScope;
        ds.condition().visit(this);
    }

    /**
     * Checks field expression modifier usage.<br><br>
     * <p>
     *     We only have one modifier check to perform for field expressions
     *     involving the access scope of a field. Fields can only be accessed
     *     outside of a class if they were declared public.
     * </p>
     * @param fe Field Expressions
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        if(fe.accessExpr().isNameExpr() || fe.accessExpr().isArrayExpr()) {
            ClassDecl cd = currentScope.findName(fe.fieldTarget().type.typeName()).decl().asTopLevelDecl().asClassDecl();
            FieldDecl fd = cd.symbolTable.findName(fe.accessExpr().toString()).decl().asFieldDecl();

            // ERROR CHECK #1: Only fields declared as 'public' can be accessed outside a class
            if (!fe.fieldTarget().toString().equals("this") && !fd.mod.isPublic()) {
                errors.add(
                    new ErrorBuilder(generateModError, interpretMode)
                            .addLocation(fe)
                            .addErrorType(MessageType.MOD_ERROR_507)
                            .addArgs(fe.fieldTarget().toString(), fd.toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1507)
                            .error()
                );
            }
        }
        fe.accessExpr().visit(this);
    }

    /**
     * Sets current scope inside for block
     * @param fs For Statement
     */
    public void visitForStmt(ForStmt fs) {
        SymbolTable oldScope = currentScope;
        currentScope = fs.symbolTable;
        super.visitForStmt(fs);
        currentScope = oldScope;
    }

    /**
     * Sets current scope to be inside current function.
     * @param fd Function Declaration
     */
    public void visitFuncDecl(FuncDecl fd) {
        currentContext = fd;
        currentScope = fd.symbolTable;
        super.visitFuncDecl(fd);
        currentContext = null;
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets current scope inside of if statement's blocks
     * @param is If Statement
     */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);

        SymbolTable oldScope = currentScope;
        currentScope = is.symbolTableIfBlock;
        is.ifBlock().visit(this);
        currentScope = oldScope;

        for(IfStmt e : is.elifStmts())
            e.visit(this);

        if(is.elseBlock() != null) {
            oldScope = currentScope;
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = oldScope;
        }
    }

    /**
     * Checks invocation modifier usage.<br><br>
     * <p>
     *     For both function and method invocations, we need to check if
     *     the user explicitly allowed recursion with the `recurs` keyword
     *     in order to allow recursive invocations. Additionally, for method
     *     invocations, we want to make sure the method was declared public in
     *     order to be able to call it outside of the class.
     * </p>
     * @param in Invocation
     */
    public void visitInvocation(Invocation in) {
        String funcSignature = in.invokeSignature();

        // Temporary here to prevent exception, probably move in the future :)
        if(in.toString().equals("length")) {
            funcSignature = null;
        }
        // Function Invocation
        else if(!in.targetType.isClassType() && !in.targetType.isMultiType()) {
            FuncDecl fd = currentScope.findName(funcSignature).decl().asTopLevelDecl().asFuncDecl();

            if(currentContext == fd && fd.funcSignature().equals(funcSignature))  {
                // ERROR CHECK #1: A function can not call itself without `recurs` modifier
                if(!fd.mod.isRecurs()) {
                    errors.add(
                        new ErrorBuilder(generateModError,interpretMode)
                                .addLocation(in)
                                .addErrorType(MessageType.MOD_ERROR_502)
                                .addArgs(fd.toString())
                                .addSuggestType(MessageType.MOD_SUGGEST_1502)
                                .error()
                    );
                }
            }
        }
        // Method Invocation
        else {
            ClassDecl cd = currentScope.findName(in.targetType.typeName()).decl().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.invokeSignature()).decl().asMethodDecl();

            // ERROR CHECK #2: A method can not call itself without `recurs` modifier
            if(currentContext == md && md.toString().equals(in.toString())) {
                if(!md.mods.isRecurs()) {
                    errors.add(
                        new ErrorBuilder(generateModError,interpretMode)
                                .addLocation(in)
                                .addErrorType(MessageType.MOD_ERROR_503)
                                .addArgs(md.toString())
                                .addSuggestType(MessageType.MOD_SUGGEST_1503)
                                .error()
                    );
                }
            }
            // ERROR CHECK #3: An object can only invoke public methods outside its class
            if(!md.mods.isPublic() && (currentClass == null || (currentClass != cd && !currentClass.inherits(cd.toString())))) {
                errors.add(
                    new ErrorBuilder(generateModError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.MOD_ERROR_504)
                            .addArgs("this",in.toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1504)
                            .error()
                );
            }
        }
        super.visitInvocation(in);
    }

    /**
     * Sets current scope to be inside <verb>main</verb> function.
     * @param md Main Declaration
     */
    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;
        super.visitMainDecl(md);
    }

    /**
     * Sets current scope to be inside current method.
     * @param md Method Declaration
     */
    public void visitMethodDecl(MethodDecl md) {
        currentContext = md;
        currentScope = md.symbolTable;
        super.visitMethodDecl(md);
        currentContext = null;
        currentScope = currentScope.closeScope();
    }

    /**
     * Checks new expression modifier usage.<br><br>
     * <p>
     *     When we are instantiating an object, we want to make sure a user is
     *     not trying to instantiate from an abstract class since that defeats
     *     the purpose of having an abstract class. This is the only modifier
     *     check we need to perform.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.toString()).decl().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: An abstract class can not be instantiated
        if(cd.mod.isAbstract()) {
            errors.add(
                new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(MessageType.MOD_ERROR_506)
                        .addArgs(ne.getParent().getParent().asStatement().asLocalDecl().var().toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1506)
                        .error()
            );
        }
        super.visitNewExpr(ne);
    }

    /**
     * Sets current scope inside while block
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        SymbolTable oldScope = currentScope;
        currentScope = ws.symbolTable;
        super.visitWhileStmt(ws);
        currentScope = oldScope;
    }
}
