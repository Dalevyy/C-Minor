package modifierchecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.*;
import ast.statements.*;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.MainDecl;
import ast.types.ClassType;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.ErrorBuilder;
import messages.errors.mod.ModError;
import utilities.SymbolTable;
import utilities.Visitor;

import java.util.HashSet;

public class ModifierChecker extends Visitor {

    private SymbolTable currentScope;
    private AST currentContext;
    private ClassDecl currentClass;
    private boolean parentFound = false;

    /**
     * Creates modifier checker in compilation mode
     */
    public ModifierChecker(String fileName) {
        this.currentScope = null;
        this.handler = new MessageHandler(fileName);
    }

    /**
     * Creates modifier checker in interpretation mode
     * @param st Symbol Table
     */
    public ModifierChecker(SymbolTable st) {
        this.currentScope = st;
        this.handler = new MessageHandler();
    }

    /**
     * Determines if abstract methods were implemented in concrete classes.<br><br>
     * <p>
     *     ThisStmt algorithm comes from Dr. Pedersen's compilers textbook, and it will
     *     determine whether or not a method is considered abstract or concrete based
     *     on its implementation inside of a class. ThisStmt will only be called by
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

        for(MethodDecl md : cd.classBlock().getMethods()) {
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
     *     ThisStmt method will validate if a user correctly inherits from an abstract
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
            handler.createErrorBuilder(ModError.class)
                    .addLocation(subClass)
                    .addErrorNumber(MessageNumber.MOD_ERROR_501)
                    .addErrorArgs(subClass.toString(),superClass.toString())
                    .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1501)
                    .generateError();
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
        if(!as.LHS().isFieldExpr()) {
            AST LHS = currentScope.findName(as.LHS().toString()).decl();

            if (LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
                // ERROR CHECK #1: A constant can not be updated after its declaration
                if (LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                    handler.createErrorBuilder(ModError.class)
                            .addLocation(as)
                            .addErrorNumber(MessageNumber.MOD_ERROR_505)
                            .addErrorArgs(as.LHS().toString())
                            .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1505)
                            .generateError();
                }
            }
            // ERROR CHECK #2: An enum constant can not be reassigned its value
            else if (LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isEnumDecl()) {
                handler.createErrorBuilder(ModError.class)
                        .addLocation(as)
                        .addErrorNumber(MessageNumber.MOD_ERROR_508)
                        .addErrorArgs(as.LHS().toString())
                        .generateError();
            }
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
        SymbolTable oldScope = currentScope;
        currentScope = cd.symbolTable;

        if(cd.superClass() != null) {
            ClassDecl superDecl = currentScope.findName(cd.superClass().toString())
                                                            .decl().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: Make sure the superclass was not declared as 'final'
            if(superDecl.mod.isFinal()) {
                handler.createErrorBuilder(ModError.class)
                        .addLocation(cd)
                        .addErrorNumber(MessageNumber.MOD_ERROR_500)
                        .addErrorArgs(cd.toString(),superDecl.toString())
                        .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1500)
                        .generateError();
            }

            // If the current class is inheriting from an abstract class, then check
            // if every superclass method was implemented in the current class
            if(!cd.mod.isAbstract() && superDecl.mod.isAbstract())
                checkAbstrClassImplementation(cd,superDecl);
        }
        this.currentClass = cd;
        if(cd.typeParams().isEmpty())
            super.visitClassDecl(cd);
        this.currentClass = null;
        currentScope = oldScope;
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
        fe.getTarget().visit(this);
        if(fe.getAccessExpr().isNameExpr() || fe.getAccessExpr().isArrayExpr()) {
            ClassDecl cd;
            FieldDecl fd = null;
            if(fe.getTarget().type.isClassType()) {
                cd = currentScope.findName(fe.getTarget().type.toString()).decl().asTopLevelDecl().asClassDecl();
                fd = cd.symbolTable.findName(fe.getAccessExpr().toString()).decl().asFieldDecl();
            }
            else {
                for(ClassType ct : fe.getTarget().type.asMultiType().getAllTypes()) {
                    cd = currentScope.findName(ct.toString()).decl().asTopLevelDecl().asClassDecl();
                    if(cd.symbolTable.hasName(fe.getAccessExpr().toString())) {
                        fd = cd.symbolTable.findName(fe.getAccessExpr().toString()).decl().asFieldDecl();
                        break;
                    }
                }
            }

            // ERROR CHECK #1: Only fields declared as 'public' can be accessed outside a class
            if (!fe.getTarget().toString().equals("this") && !fd.mod.isPublic()) {
                handler.createErrorBuilder(ModError.class)
                        .addLocation(fe)
                        .addErrorNumber(MessageNumber.MOD_ERROR_507)
                        .addErrorArgs(fe.getTarget().toString(), fd.toString())
                        .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1507)
                        .generateError();
            }
        }
        fe.getAccessExpr().visit(this);
        parentFound = false;
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
        if(fd.typeParams().isEmpty())
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
        String funcSignature = in.getSignature();

        // Temporary here to prevent exception, probably move in the future :)
        if(in.toString().equals("length")) {
            funcSignature = null;
        }
        // Function Invocation
        else if(!in.targetType.isClassOrMultiType()) {
            FuncDecl fd = in.templatedFunction != null ? in.templatedFunction :
                                              currentScope.findName(funcSignature).decl().asTopLevelDecl().asFuncDecl();

            if(currentContext == fd && fd.getSignature().equals(funcSignature))  {
                // ERROR CHECK #1: A function can not call itself without `recurs` modifier
                if(!fd.mod.isRecurs()) {
                    handler.createErrorBuilder(ModError.class)
                            .addLocation(in)
                            .addErrorNumber(MessageNumber.MOD_ERROR_502)
                            .addErrorArgs(fd.toString())
                            .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1502)
                            .generateError();
                }
            }
            if(fd.isTemplate())
                in.templatedFunction.visit(this);
        }
        // Method Invocation
        else {
            ClassDecl cd = currentScope.findName(in.targetType.toString()).decl().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.getSignature()).decl().asMethodDecl();

            // ERROR CHECK #2: A method can not call itself without `recurs` modifier
            if(currentContext == md && md.toString().equals(in.toString()) && !parentFound) {
                if(!md.mods.isRecurs()) {
                    handler.createErrorBuilder(ModError.class)
                            .addLocation(in)
                            .addErrorNumber(MessageNumber.MOD_ERROR_503)
                            .addErrorArgs(md.toString())
                            .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1503)
                            .generateError();
                }
            }
            // ERROR CHECK #3: An object can only invoke public methods outside its class
            if(!md.mods.isPublic() && (currentClass == null || (currentClass != cd && !currentClass.inherits(cd.toString())))) {
                handler.createErrorBuilder(ModError.class)
                        .addLocation(in)
                        .addErrorNumber(MessageNumber.MOD_ERROR_504)
                        .addErrorArgs("this",in.toString())
                        .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1504)
                        .generateError();
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

    public void visitNameExpr(NameExpr ne) {
        if(ne.toString().equals("parent"))
            parentFound = true;
    }

    /**
     * Checks new expression modifier usage.<br><br>
     * <p>
     *     When we are instantiating an object, we want to make sure a user is
     *     not trying to instantiate from an abstract class since that defeats
     *     the purpose of having an abstract class. ThisStmt is the only modifier
     *     check we need to perform.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.asClassType().getClassNameAsString()).decl().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: An abstract class can not be instantiated
        if(cd.mod.isAbstract()) {
            handler.createErrorBuilder(ModError.class)
                    .addLocation(ne)
                    .addErrorNumber(MessageNumber.MOD_ERROR_506)
                    .addErrorArgs(ne.getParent().getParent().asStatement().asLocalDecl().var().toString())
                    .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1506)
                    .generateError();
        }
        super.visitNewExpr(ne);

        if(ne.createsFromTemplate())
            ne.getInstantiatedClass().visit(this);
    }

    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.getOutExprs())
            e.visit(this);
    }

    /**
     * Sets current scope inside while block.
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        SymbolTable oldScope = currentScope;
        currentScope = ws.symbolTable;
        super.visitWhileStmt(ws);
        currentScope = oldScope;
    }
}
