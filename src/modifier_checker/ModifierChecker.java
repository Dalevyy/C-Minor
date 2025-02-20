package modifier_checker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.AssignStmt;
import ast.top_level_decls.*;
import ast.types.*;
import messages.*;

import java.util.HashSet;

import messages.errors.ErrorType;
import messages.errors.ModifierError;
import utilities.*;

public class ModifierChecker extends Visitor {

    private Message msg;
    private SymbolTable currentScope;
    private AST currentContext;

    public ModifierChecker() {
        currentScope = null;
    }

    public void sortClassMethods(HashSet<String> abs, HashSet<String> con, ClassDecl cd) {

        if(AST.notNull(cd.superClass())) {
            ClassDecl superClass = currentScope.findName(cd.superClass().typeName()).declName().asTopLevelDecl().asClassDecl();
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
        HashSet<String> concretes = new HashSet<String>();
        HashSet<String> abstracts = new HashSet<String>();
        sortClassMethods(abstracts,concretes,subClass);

        if(abstracts.size() > 0) { msg = new ModifierError(subClass,superClass,
                                   ErrorType.CONCRETE_CLASS_DOES_NOT_IMPLEMENT_ABSTRACT_SUPERCLASS); }
    }

    /*
    _________________________ Assignment Statements ________________________
    Since we allow global constants in C Minor, we have yet to check whether
    or not a user tries to reassign a constant after it has been declared.
    We will do that check as a part of modifier checking.
    ________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {
        AST LHS = currentScope.findName(as.LHS().toString()).declName();
        if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
            // ERROR CHECK #1) A constant variable can not change its value after declaration.
            if(LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                this.msg = new ModifierError(as,ErrorType.CONSTANT_VARAIBLE_CAN_NOT_BE_REASSIGNED);
            }
        }
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
            ClassDecl superDecl = currentScope.findName(superClass.toString()).declName().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1) Class can only be inherited if 'final' keyword is missing
            if(superDecl.mod.isFinal()) { msg = new ModifierError(cd,superDecl,ErrorType.CAN_NOT_INHERIT_FROM_A_FINAL_CLASS); }

            super.visitClassDecl(cd);

            // ERROR CHECK #2) A concrete class inheriting from an abstract class must implement all of its methods
            if(!cd.mod.isAbstract() && superDecl.mod.isAbstract()) { abstractClassImplementation(cd,superDecl); }
        }
        else { super.visitClassDecl(cd); }

        currentScope = currentScope.closeScope();
    }

    //TODO: PROPERTIES
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


        /*
            If a field was marked as a property, then this means
            the user would like us to generate a getter and setter
            for this field. We will do this during ModifierChecker.
        */
        if(fd.mod.isProperty()) {

        }
    }

    /*
    __________________________ Field Expressions __________________________
    The only check we do here is to make sure the field we are accessing is
    public. If it's not, we will output an error message.
    ________________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) {
        ClassDecl cd = currentScope.findName(fe.fieldTarget().type.typeName()).declName().asTopLevelDecl().asClassDecl();
        FieldDecl fd = cd.symbolTable.findName(fe.name().toString()).declName().asFieldDecl();

        // ERROR CHECK #1) A field is only accessible outside a class scope if it's public
        if(!fd.mod.isPublic()) { this.msg = new ModifierError(fe,cd,ErrorType.CAN_NOT_ACCESS_NON_PUBLIC_FIELD); }

        fe.fieldTarget().visit(this);
    }

    public void visitFuncDecl(FuncDecl fd) {
        currentContext = fd;
        currentScope = fd.symbolTable;
        super.visitFuncDecl(fd);
        currentContext = null;
        currentScope = currentScope.closeScope();
    }

    /*
    __________________________ Invocations __________________________
    In C Minor, a user who wishes to use recursive calls must use the
    'recurs' keyword when declaring methods or functions. This means
    we have to check here whether or not a user is allowed to call
    a function/method recursively.

    Additionally, if we have a method invocation, we need to make sure
    the method is accessible outside the class it was declared in.
    _________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        // Function Invocation Case
        if(in.target() == null) {
            FuncDecl fd = currentScope.findName(in.toString()).declName().asTopLevelDecl().asFuncDecl();

            if(currentContext == fd && fd.toString().equals(in.toString()))  {
                // ERROR CHECK #1) A function can not recursively call itself without the 'recurs' keyword
                if(!fd.mod.isRecurs()) { this.msg = new ModifierError(in,ErrorType.RECURSIVE_FUNCTION_CALL_NOT_ALLOWED); }
            }
        }
        // Method Invocation Case
        else {
            ClassDecl cd = currentScope.findName(in.target().type.typeName()).declName().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.name().toString()).declName().asMethodDecl();

            // ERROR CHECK #2) A method can not recursively call itself without the 'recurs' keyword
            if(currentContext == md && md.toString().equals(in.toString())) {
                if(!md.mods.isRecurs()) { this.msg = new ModifierError(in,ErrorType.RECURSIVE_METHOD_CALL_NOT_ALLOWED); }
            }
            // ERROR CHECK #3) An object can only a method that is declared public
            if(!md.mods.isPublic()) { this.msg = new ModifierError(in,ErrorType.CAN_NOT_ACCESS_NON_PUBLIC_METHOD); }
        }
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

    /*
    __________________________ New Expression __________________________
    We are not allowed to instantiate objects from abstract classes, so
    this is the only check that is needed when visiting a new expression.
    ____________________________________________________________________
    */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.typeName()).declName().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1) Class must be concrete
        if(cd.mod.isAbstract()) { this.msg = new ModifierError(ne,cd,ErrorType.CAN_NOT_INSTANTIATE_AN_ABSTRACT_CLASS); }

        super.visitNewExpr(ne);
    }
}
