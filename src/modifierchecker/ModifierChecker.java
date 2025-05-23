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
import ast.types.ClassType;
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

    // Based on Dr. Pedersen's algorithm (?)
    public void sortClassMethods(HashSet<String> abs, HashSet<String> con, ClassDecl cd) {

        if(cd.superClass() != null) {
            ClassDecl superClass = currentScope.findName(cd.superClass().typeName()).decl().asTopLevelDecl().asClassDecl();
            sortClassMethods(abs,con,superClass);
        }

        for(String conName : con)
            abs.remove(conName);

        Vector<MethodDecl> allMethods = cd.classBlock().methodDecls();
        for(int i = 0; i < allMethods.size(); i++) {
            String currMethod = allMethods.get(i).toString();
            if(cd.mod.isAbstract()) {
                abs.add(currMethod);
                con.remove(currMethod);
            }
            else {
                con.add(currMethod);
                abs.remove(currMethod);
            }
        }
    }

    public void abstractClassImplementation(ClassDecl subClass, ClassDecl superClass) {
        HashSet<String> concretes = new HashSet<>();
        HashSet<String> abstracts = new HashSet<>();
        sortClassMethods(abstracts,concretes,subClass);

        if(abstracts.size() > 0) {
            errors.add(new ErrorBuilder(generateModError,interpretMode)
                    .addLocation(subClass)
                    .addErrorType(MessageType.MOD_ERROR_501)
                    .addArgs(subClass.toString(),superClass.toString())
                    .addSuggestType(MessageType.MOD_SUGGEST_1501)
                    .error());
        }
    }

    /*
    ________________________ Assignment Statements ________________________
    Since we allow global constants in C Minor, we have yet to check
    whether or not a user tries to reassign a constant after it has been
    declared. We will do that check as a part of modifier checking.
    _______________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {
        AST LHS = currentScope.findName(as.LHS().toString()).decl();
        if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
            // ERROR CHECK #1: A constant variable can not change its
            //                 value after declaration.
            if(LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                errors.add(new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(as)
                        .addErrorType(MessageType.MOD_ERROR_505)
                        .addArgs(as.LHS().toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1505)
                        .error());
            }
        }
        // ERROR CHECK #2: A constant found in an enumeration can not
        //                 be reassigned a value.
        else if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isEnumDecl()) {
            errors.add(new ErrorBuilder(generateModError,interpretMode)
                    .addLocation(as)
                    .addErrorType(MessageType.MOD_ERROR_508)
                    .addArgs(as.LHS().toString())
                    .error());
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

    /*
    __________________________ Class Declarations __________________________
    When a user declares a class in C Minor, there are 2 modifier checks we
    need to keep track of. Both modifier checks relate to inheritance.

        1. First, we check if an inherited superclass was not declared final.
           This means subclasses can not inherit this superclass, so we have
           to error out.
        2. Second, we need to check if the class is inheriting from an
           abstract class. If so, then we have to make sure the user is
           defining what each method in the class will do.
    ________________________________________________________________________
    */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;

        ClassType superClass = cd.superClass();
        if(superClass != null) {
            ClassDecl superDecl = currentScope.findName(superClass.toString()).decl().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: Class can only be inherited if 'final' keyword is missing
            if(superDecl.mod.isFinal()) {
                errors.add(new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(cd)
                        .addErrorType(MessageType.MOD_ERROR_500)
                        .addArgs(cd.toString(),superDecl.toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1500)
                        .error());
            }

            super.visitClassDecl(cd);

            // ERROR CHECK #2: A concrete class inheriting from an abstract class must implement all of its methods
            if(!cd.mod.isAbstract() && superDecl.mod.isAbstract()) { abstractClassImplementation(cd,superDecl); }
        }
        else { super.visitClassDecl(cd); }

        currentScope = currentScope.closeScope();
    }

    public void visitDoStmt(DoStmt ds) {
        SymbolTable oldScope = currentScope;
        currentScope = ds.symbolTable;
        super.visitDoStmt(ds);
        currentScope = oldScope;
    }

    /*
    __________________________ Field Declarations __________________________
    In C Minor, there are no specific modifier error checks that need to be
    done here. However, since we are checking modifiers, we will check
    whether or not a field has been declared with the 'property' keyword. This
    means we have to generate a getter and setter for this field, so we have
    to add two new MethodDecls to the current class per field declaration.
    ________________________________________________________________________
    */
    public void visitFieldDecl(FieldDecl fd) {
        super.visitFieldDecl(fd);
    }

    /*
    __________________________ Field Expressions __________________________
    The only check we do here is to make sure the field we are accessing is
    public. If it's not, we will output an error message.
    _______________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) {
        if(fe.accessExpr().isNameExpr() || fe.accessExpr().isArrayExpr()) {
            ClassDecl cd = currentScope.findName(fe.fieldTarget().type.typeName()).decl().asTopLevelDecl().asClassDecl();
            FieldDecl fd = cd.symbolTable.findName(fe.accessExpr().toString()).decl().asFieldDecl();

            // ERROR CHECK #1: A field is only accessible outside a class scope if it's public
            if (!fe.fieldTarget().toString().equals("this") && !fd.mod.isPublic()) {
                errors.add(new ErrorBuilder(generateModError, interpretMode)
                        .addLocation(fe)
                        .addErrorType(MessageType.MOD_ERROR_507)
                        .addArgs(fe.fieldTarget().toString(), fd.toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1507)
                        .error());
            }
        }
        fe.fieldTarget().visit(this);
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

    public void visitFuncDecl(FuncDecl fd) {
        currentContext = fd;
        currentScope = fd.symbolTable;
        super.visitFuncDecl(fd);
        currentContext = null;
        currentScope = currentScope.closeScope();
    }

    public void visitIfStmt(IfStmt is) {
        SymbolTable oldScope = currentScope;
        currentScope = is.symbolTableIfBlock;
        is.ifBlock().visit(this);
        currentScope = oldScope;

        for(IfStmt e : is.elifStmts()) { e.visit(this); }

        if(is.elseBlock() != null) {
            oldScope = currentScope;
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = oldScope;
        }
    }

    /*
    __________________________ Invocations __________________________
    In C Minor, a user who wishes to use recursive calls must use the
    'recurs' keyword when declaring methods or functions. This means
    we have to check here whether or not a user is allowed to call
    a function/method recursively.

    Additionally, if we have a method invocation, we need to make
    sure the method is accessible outside the class it was declared
    in.
    _________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        String funcSignature = in.invokeSignature();

        if(in.toString().equals("length")) {
            ;
        }
        // Function Invocation Case
        else if(!in.targetType.isClassType()) {
            FuncDecl fd = currentScope.findName(funcSignature).decl().asTopLevelDecl().asFuncDecl();

            if(currentContext == fd && fd.funcSignature().equals(funcSignature))  {
            // ERROR CHECK #1: A function can not recursively call itself without the 'recurs' keyword
            if(!fd.mod.isRecurs()) {
                errors.add(new ErrorBuilder(generateModError,interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.MOD_ERROR_502)
                        .addArgs(fd.toString())
                        .addSuggestType(MessageType.MOD_SUGGEST_1502)
                        .error());
                }
            }
        }
        // Method Invocation Case
        else {
            ClassDecl cd = currentScope.findName(in.targetType.typeName()).decl().asTopLevelDecl().asClassDecl();
            while(!cd.symbolTable.hasName(funcSignature)) {
                cd = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            }

            MethodDecl md = cd.symbolTable.findName(funcSignature).decl().asMethodDecl();

            // ERROR CHECK #2: A method can not recursively call itself without the 'recurs' keyword
            if(currentContext == md && md.toString().equals(in.toString())) {
                if(!md.mods.isRecurs()) {
                    errors.add(new ErrorBuilder(generateModError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.MOD_ERROR_503)
                            .addArgs(md.toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1503)
                            .error());
                }
            }
            // ERROR CHECK #3: An object can only access a method that is declared public
            if(!md.mods.isPublic()) {
                    errors.add(new ErrorBuilder(generateModError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.MOD_ERROR_504)
                            .addArgs(in.toString())
                            .addSuggestType(MessageType.MOD_SUGGEST_1504)
                            .error());
            }
        }
        super.visitInvocation(in);
    }

    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;
        super.visitMainDecl(md);
    }

    public void visitMethodDecl(MethodDecl md) {
        currentContext = md;
        currentScope = md.symbolTable;
        super.visitMethodDecl(md);
        currentContext = null;
        currentScope = currentScope.closeScope();
    }

    /**
     * Checks to make sure new expression can instantiate a valid class
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.toString()).decl().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: Class must be concrete
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
