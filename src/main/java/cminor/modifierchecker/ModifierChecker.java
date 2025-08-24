package cminor.modifierchecker;

//TODO: A template class can only be inherited when it's instantiated!

import cminor.ast.AST;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.MainDecl;
import cminor.ast.types.ClassType;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.mod.ModError;
import cminor.namechecker.NameChecker;
import cminor.typechecker.TypeChecker;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

public class ModifierChecker extends Visitor {

    /**
     * Current scope we are resolving modifiers in.
     */
    private SymbolTable currentScope;

    /**
     * Instance of {@link ModifierCheckerHelper} that will be used for additional modifier checking tasks.
     */
    private final ModifierCheckerHelper helper;

    /**
     * Creates the {@link ModifierChecker} in compilation mode.
     */
    public ModifierChecker() {
        this.handler = new MessageHandler();
        this.helper = new ModifierCheckerHelper();
    }

    /**
     * Creates the {@link ModifierChecker} in interpretation mode.
     * @param globalScope The {@link SymbolTable} representing the global scope.
     */
    public ModifierChecker(SymbolTable globalScope) {
        this();
        this.currentScope = globalScope;
    }

    /**
     * Verifies if modifiers are used correctly when executing an assignment statement.
     * <p>
     * </p>
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        // If the LHS represents a field expression, we will let a separate visit error check it.
        if(as.getLHS().isFieldExpr()) {
            as.getLHS().visit(this);
            return;
        }

        AST LHS = currentScope.findName(as.getLHS());
        if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
            // ERROR CHECK #1: A global constant can not have its value changed.
            if(LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(as)
                       .addErrorNumber(MessageNumber.MOD_ERROR_505)
                       .addErrorArgs(as.getLHS())
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1505)
                       .generateError();
            }
        }
        // ERROR CHECK #2: An enum constant can not be reassigned its value.
        else if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isEnumDecl()) {
            handler.createErrorBuilder(ModError.class)
                   .addLocation(as)
                   .addErrorNumber(MessageNumber.MOD_ERROR_508)
                   .addErrorArgs(as.getLHS())
                   .generateError();
        }
    }

    /**
     * Sets the current scope to be in a {@link CaseStmt}
     * @param cs {@link CaseStmt}
     */
    public void visitCaseStmt(CaseStmt cs) {
        currentScope = cs.getScope();
        super.visitCaseStmt(cs);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the current scope to be in a {@link ChoiceStmt}
     * @param cs {@link ChoiceStmt}
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        for(CaseStmt c : cs.getCases())
            c.visit(this);

        currentScope = cs.getScope();
        cs.getDefaultBody().visit(this);
        currentScope = currentScope.closeScope();
    }

    /**
     * Verifies if modifiers are used correctly for a class.
     * <p>
     *     This visit is mainly concerned with handling modifiers related to
     *     inheritance. C Minor classes can be marked as either {@code final}
     *     or {@code abstr}. All other modifier checks are handled by separate visits.
     * </p>
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.getScope();

        if(cd.getSuperClass() != null) {
            ClassDecl superClass = currentScope.findName(cd.getSuperClass()).asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: A class may not inherit from a superclass that was labeled as 'final'.
            if(superClass.mod.isFinal()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(cd)
                       .addErrorNumber(MessageNumber.MOD_ERROR_500)
                       .addErrorArgs(cd, superClass)
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1500)
                       .generateError();
            }

            // If the current class inherits from an abstract class, then we are not done checking this class!
            if(!cd.mod.isAbstract() && superClass.mod.isAbstract())
                helper.checkAbstrClassImplementation(cd, superClass);
        }

        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the current scope to be in a {@link DoStmt}
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        currentScope = ds.getScope();
        ds.getBody().visit(this);
        currentScope = currentScope.closeScope();
        ds.getCondition().visit(this);
    }

////    *
////     * Checks field expression modifier usage.<br><br>
////     * <p>
////     *     We only have one modifier check to perform for field expressions
////     *     involving the access scope of a field. Fields can only be accessed
////     *     outside of a class if they were declared public.
////     * </p>
////     * @param fe Field Expressions
//
//    public void visitFieldExpr(FieldExpr fe) {
//        fe.getTarget().visit(this);
//        if(fe.getAccessExpr().isNameExpr() || fe.getAccessExpr().isArrayExpr()) {
//            ClassDecl cd;
//            FieldDecl fd = null;
//            if(fe.getTarget().type.isClassType()) {
//                cd = currentScope.findName(fe.getTarget().type.toString()).getDecl().asTopLevelDecl().asClassDecl();
//                fd = cd.getScope().findName(fe.getAccessExpr().toString()).getDecl().asClassNode().asFieldDecl();
//            }
//            else {
//                for(ClassType ct : fe.getTarget().type.asMultiType().getAllTypes()) {
//                    cd = currentScope.findName(ct.toString()).getDecl().asTopLevelDecl().asClassDecl();
//                    if(cd.getScope().hasName(fe.getAccessExpr().toString())) {
//                        fd = cd.getScope().findName(fe.getAccessExpr().toString()).getDecl().asClassNode().asFieldDecl();
//                        break;
//                    }
//                }
//            }
//
//            // ERROR CHECK #1: Only fields declared as 'public' can be accessed outside a class
//            if (!fe.getTarget().toString().equals("this") && !fd.mod.isPublic()) {
//                handler.createErrorBuilder(ModError.class)
//                        .addLocation(fe)
//                        .addErrorNumber(MessageNumber.MOD_ERROR_507)
//                        .addErrorArgs(fe.getTarget().toString(), fd.toString())
//                        .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1507)
//                        .generateError();
//            }
//        }
//        fe.getAccessExpr().visit(this);
//        parentFound = false;
//    }

    /**
     * Sets the current scope to be in a {@link ForStmt}
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.getScope();
        super.visitForStmt(fs);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the current scope to be in a {@link FuncDecl}
     * @param fd {@link FuncDecl}
     */
    public void visitFuncDecl(FuncDecl fd) {
        currentScope = fd.getScope();
        super.visitFuncDecl(fd);
        currentScope = currentScope.closeScope();
    }

    /**
     * Sets the current scope to be in an {@link IfStmt}
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        is.getCondition().visit(this);

        currentScope = is.getIfScope();
        is.getIfBody().visit(this);
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.getElifs())
            e.visit(this);

        if(is.containsElse()) {
            currentScope = is.getElseScope();
            is.getElseBody().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

////    *
////     * Checks invocation modifier usage.<br><br>
////     * <p>
////     *     For both function and method invocations, we need to check if
////     *     the user explicitly allowed recursion with the `recurs` keyword
////     *     in order to allow recursive invocations. Additionally, for method
////     *     invocations, we want to make sure the method was declared public in
////     *     order to be able to call it outside of the class.
////     * </p>
////     * @param in Invocation
//
//    public void visitInvocation(Invocation in) {
//        String funcSignature = in.getSignature();
//
//        // Temporary here to prevent exception, probably move in the future :)
//        if(in.toString().equals("length")) {
//            funcSignature = null;
//        }
//        // Function Invocation
//        else if(!in.targetType.isClassOrMultiType()) {
//            FuncDecl fd = in.templatedFunction != null ? in.templatedFunction :
//                                              currentScope.findName(funcSignature).getDecl().asTopLevelDecl().asFuncDecl();
//
//            if(currentContext == fd && fd.getSignature().equals(funcSignature))  {
//                // ERROR CHECK #1: A function can not call itself without `recurs` modifier
//                if(!fd.mod.isRecursive()) {
//                    handler.createErrorBuilder(ModError.class)
//                            .addLocation(in)
//                            .addErrorNumber(MessageNumber.MOD_ERROR_502)
//                            .addErrorArgs(fd.toString())
//                            .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1502)
//                            .generateError();
//                }
//            }
//            if(fd.isTemplate())
//                in.templatedFunction.visit(this);
//        }
//        // Method Invocation
//        else {
//            ClassDecl cd = currentScope.findName(in.targetType.toString()).getDecl().asTopLevelDecl().asClassDecl();
//            MethodDecl md = cd.getScope().findName(in.getSignature()).getDecl().asClassNode().asMethodDecl();
//
//            // ERROR CHECK #2: A method can not call itself without `recurs` modifier
//            if(currentContext == md && md.toString().equals(in.toString()) && !parentFound) {
//                if(!md.mod.isRecursive()) {
//                    handler.createErrorBuilder(ModError.class)
//                            .addLocation(in)
//                            .addErrorNumber(MessageNumber.MOD_ERROR_503)
//                            .addErrorArgs(md.toString())
//                            .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1503)
//                            .generateError();
//                }
//            }
//            // ERROR CHECK #3: An object can only invoke public methods outside its class
//            if(!md.mod.isPublic() && (currentClass == null || (currentClass != cd && !currentClass.inherits(cd.toString())))) {
//                handler.createErrorBuilder(ModError.class)
//                        .addLocation(in)
//                        .addErrorNumber(MessageNumber.MOD_ERROR_504)
//                        .addErrorArgs("this",in.toString())
//                        .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1504)
//                        .generateError();
//            }
//        }
//        super.visitInvocation(in);
//    }
//
////    *
////     * Sets current scope to be inside <verb>main</verb> function.
////     * @param md Main Declaration
//
//    public void visitMainDecl(MainDecl md) {
//        currentScope = md.getScope();
//        currentContext = md;
//        super.visitMainDecl(md);
//    }
//
//    *
//     * Sets current scope to be inside current method.
//     * @param md Method Declaration

    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.getScope();
        super.visitMethodDecl(md);
        currentScope = currentScope.closeScope();
    }

    /**
     * Checks if a {@link NewExpr} is valid.
     * <p>
     *     When we are instantiating an object, we want to make sure a user is
     *     not trying to instantiate from an abstract class since that defeats
     *     the purpose of having an abstract class.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.asClass().getClassName()).asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: A user can not instantiate an object from an abstract class.
        if(cd.mod.isAbstract()) {
            handler.createErrorBuilder(ModError.class)
                   .addLocation(ne)
                   .addErrorNumber(MessageNumber.MOD_ERROR_506)
                   .addErrorArgs(ne.getParent().getParent().asStatement().asLocalDecl().getVariableName())
                   .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1506)
                   .generateError();
        }
        super.visitNewExpr(ne);
    }

    /**
     * Sets the current scope to be in a {@link WhileStmt}
     * @param ws {@link WhileStmt}
     */
    public void visitWhileStmt(WhileStmt ws) {
        currentScope = ws.getScope();
        super.visitWhileStmt(ws);
        currentScope = currentScope.closeScope();
    }

    /**
     * An internal helper class for {@link ModifierChecker}.
     */
    private class ModifierCheckerHelper {

        /**
         * Determines if abstract methods were implemented in concrete classes.x
         * <p>
         *     This algorithm comes from Dr. Pedersen's compilers textbook, and it will
         *     determine whether a method is considered abstract or concrete based on its
         *     implementation inside a class. This will only be called by
         *     {@link #checkAbstrClassImplementation(ClassDecl, ClassDecl)} when we are
         *     checking if a class correctly implements all methods from an abstract class.
         * </p>
         * @param abstr {@link Vector} containing the current set of abstract methods.
         * @param concrete {@link Vector} containing the current set of concrete methods.
         * @param cd {@link ClassDecl} representing the current class we are checking the implementation of.
         */
        private void sortClassMethods(Vector<String> abstr, Vector<String> concrete, ClassDecl cd) {

            // Start from the top of the inheritance hierarchy
            if(cd.getSuperClass() != null)
                sortClassMethods(abstr,concrete,
                        currentScope.findName(cd.getSuperClass().getClassName()).asTopLevelDecl().asClassDecl());

            for(String conName : concrete)
                abstr.remove(conName);

            for(MethodDecl md : cd.getClassBody().getMethods()) {
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
         *
         * Checks if the user correctly implements an inherited abstract class.
         * <p>
         *     This method will validate if a user correctly inherits from an abstract
         *     class. A valid inheritance implies the user has implemented every single
         *     method declared in the abstract class in their subclass. To determine
         *     which methods were implemented, we will call
         *     {@link ModifierCheckerHelper#sortClassMethods(Vector, Vector, ClassDecl)}.
         * </p>
         * @param subClass {@link ClassDecl} representing concrete class inheriting abstract class
         * @param superClass {@link ClassDecl} representing an abstract class
         */
        private void checkAbstrClassImplementation(ClassDecl subClass, ClassDecl superClass) {
            Vector<String> concretes = new Vector<>(), abstracts = new Vector<>();
            sortClassMethods(abstracts,concretes,subClass);

            // ERROR CHECK #1: This ensures every abstract method in the superclass was implemented by the subclass.
            if(!abstracts.isEmpty()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(subClass)
                       .addErrorNumber(MessageNumber.MOD_ERROR_501)
                       .addErrorArgs(subClass.toString(),superClass.toString())
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1501)
                       .generateError();
            }
        }
    }
}
