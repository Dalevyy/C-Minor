package cminor.modifierchecker;

import cminor.ast.AST;
import cminor.ast.classbody.ClassNode;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.FieldExpr;
import cminor.ast.expressions.Invocation;
import cminor.ast.expressions.NewExpr;
import cminor.ast.misc.Modifier;
import cminor.ast.misc.ParamDecl;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.MainDecl;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.mod.ModError;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * Modifier Checking Pass.
 * <p>
 *     This is the last major semantic pass and is responsible for modifier checking. Modifiers
 *     denote special characteristics that certain programming constructs are expected to follow.
 *     Modifiers can come in the form of access (denoting how a construct should be accessed by
 *     the user) or rule (denoting how a construct should behave). This pass is responsible for ensuring
 *     a user properly follows all modifier behavior.
 * </p>
 * @author Daniel Levy
 */
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

        // WARNING CHECK #1: This checks if a warning needs to be generated when a potential side effect could occur.
        if(helper.insidePureMethod() && helper.methodChangesState(currentScope.findName(as.getLHS()))) {
            handler.createWarningBuilder()
                   .addLocation(as)
                   .addWarningNumber(MessageNumber.WARNING_1)
                   .addWarningArgs(as.getLHS(),helper.currentContext)
                   .generateWarning();
        }

        // If the LHS represents a field expression, we will let a separate visit error check it.
        if(as.getLHS().isFieldExpr()) {
            as.getLHS().visit(this);
            return;
        }

        // We will only do the modifier checks when the LHS represents an array or name!
        if(!as.getLHS().isArrayExpr() && !as.getLHS().isNameExpr())
            return;

        AST LHS;
        if(as.getLHS().isArrayExpr())
            LHS = currentScope.findName(as.getLHS().asArrayExpr().getArrayTarget());
        else
            LHS = currentScope.findName(as.getLHS());

        if(LHS.isTopLevelDecl() && LHS.asTopLevelDecl().isGlobalDecl()) {
            // ERROR CHECK #1: If we have a global constant, then its value can not be reassigned.
            if(LHS.asTopLevelDecl().asGlobalDecl().isConstant()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(as)
                       .addErrorNumber(MessageNumber.MOD_ERROR_505)
                       .addErrorArgs(as.getLHS())
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1505)
                       .generateError();
            }
        }
        // ERROR CHECK #2: Similarly, a constant declared in an Enum can not have its value reassigned.
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
            ClassDecl superClass = currentScope.findName(cd.getSuperClass().getTypeName()).asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: A class may not inherit from a superclass that was labeled as 'final'.
            if(superClass.mod.isFinal()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(cd)
                       .addErrorNumber(MessageNumber.MOD_ERROR_500)
                       .addErrorArgs(cd, superClass)
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1500)
                       .generateError();
            }

            // ERROR CHECK #2: A template class can not be inherited by itself!
            if(superClass.isTemplate()) {
                handler.createErrorBuilder(ModError.class)
                       .addLocation(cd)
                       .addErrorNumber(MessageNumber.MOD_ERROR_500)
                       .addErrorArgs(cd, superClass)
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1508)
                       .generateError();
            }

            // If the current class inherits from an abstract class, then we are not done checking this class!
            if(!cd.mod.isAbstract() && superClass.mod.isAbstract())
                helper.checkAbstrClassImplementation(cd, superClass);
        }

        helper.currentContext = cd;
        super.visitClassDecl(cd);
        helper.currentContext = null;
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

    /**
     * Verifies if field expressions can be accessed based on modifier usage.
     * <p>
     *     We want to make sure that all fields and invoked methods are properly
     *     accessed by the user based on the given modifier.
     * </p>
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) {
        // 1. We first need to get the class where the field/method belongs to.
        ClassDecl cd;
        if(fe.getTargetType().isClass())
            cd = currentScope.findName(fe.getTargetType().getTypeName()).asTopLevelDecl().asClassDecl();
        else {
            fe.getTarget().visit(this);
            fe.getAccessExpr().visit(this);
            return;
        }

        // 2. Then, we need to find the field/method in the class and retrieve its modifier.
        Modifier mod = helper.getModifier(cd.getScope(),fe);

        // ERROR CHECK #1: A member can not be accessed outside a class if it was marked as 'protected' or 'property'.
        if(!fe.getTarget().isThisStmt() && !mod.isPublic() && !helper.insideClass(cd)) {
            AST field;
            if(fe.getAccessExpr().isFieldExpr())
                field = fe.getAccessExpr().asFieldExpr().getTarget();
            else
                field = fe.getAccessExpr();

            handler.createErrorBuilder(ModError.class)
                   .addLocation(fe)
                   .addErrorNumber(MessageNumber.MOD_ERROR_507)
                   .addErrorArgs(fe.getTarget(),field)
                   .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1507)
                   .generateError();
        }

        if(fe.getTarget().isInvocation())
            fe.getTarget().visit(this);
        fe.getAccessExpr().visit(this);
    }

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
        helper.currentContext = fd;
        super.visitFuncDecl(fd);
        helper.currentContext = null;
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

    /**
     * Checks if an {@link Invocation} correctly calls a recursive method.
     * <p>
     *     This visit primarily checks to make sure a user properly invokes a recursive
     *     method based on the given modifier associated with the method.
     * </p>
     * @param in {@link Invocation}
     */
    public void visitInvocation(Invocation in) {
        // Function Invocation
        if(!in.isMethodInvocation()) {
            if(in.insideFunction()) {
                FuncDecl fd = currentScope.findMethod(in).asTopLevelDecl().asFuncDecl();
                // ERROR CHECK #1: A function can not call itself without the `recurs` keyword.
                if(helper.insideFunction(fd) && !fd.mod.isRecursive()) {
                    handler.createErrorBuilder(ModError.class)
                           .addLocation(in)
                           .addErrorNumber(MessageNumber.MOD_ERROR_502)
                           .addErrorArgs(fd)
                           .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1502)
                           .generateError();
                }
            }
        }
        // Method Invocation
        else {
            ClassDecl cd = currentScope.findName(in.getTargetType().getTypeName()).asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.getScope().findMethod(in).asClassNode().asMethodDecl();
            if(in.insideMethod()) {
                // ERROR CHECK #2: A method can not call itself without the `recurs` keyword.
                if(helper.insideMethod(md) && !md.mod.isRecursive()) {
                    handler.createErrorBuilder(ModError.class)
                           .addLocation(in)
                           .addErrorNumber(MessageNumber.MOD_ERROR_503)
                           .addErrorArgs(md)
                           .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1503)
                           .generateError();
                }
            }
        }
        super.visitInvocation(in);
    }

    /**
     * Sets the current scope to be in a {@link MainDecl}.
     * @param md {@link MainDecl}
     */
    public void visitMainDecl(MainDecl md) {
        currentScope = md.getScope();
        super.visitMainDecl(md);
    }

    /**
     * Sets the current scope to be in a {@link MethodDecl}.
     * <p>
     *     During this visit, we also check if an overridden method was allowed to be
     *     overridden based on whether the parent class marked the method as 'final'.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        ClassDecl currentClass = helper.currentContext.asTopLevelDecl().asClassDecl();
        if(currentClass.getSuperClass() != null) {
            ClassDecl superClass = currentScope.getGlobalScope()
                                               .findName(currentClass.getSuperClass().getTypeName())
                                               .asTopLevelDecl().asClassDecl();
            if(superClass.getScope().hasMethodOverload(md)) {
                MethodDecl superMethod = superClass.getScope().findMethod(md).asClassNode().asMethodDecl();
                // ERROR CHECK #1: A method can not be overridden if it was declared as final!
                if(superMethod.mod.isFinal()) {
                    handler.createErrorBuilder(ModError.class)
                           .addLocation(md)
                           .addErrorNumber(MessageNumber.MOD_ERROR_509)
                           .addErrorArgs(md)
                           .generateError();
                }
            }
        }

        currentScope = md.getScope();
        AST oldContext = helper.currentContext;
        helper.currentContext = md;
        super.visitMethodDecl(md);
        helper.currentContext = oldContext;
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
        ClassDecl cd = ne.getInstantiatedClass();
        if(cd == null)
            cd = currentScope.findName(ne.type.asClass().getClassName()).asTopLevelDecl().asClassDecl();

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
         * {@link AST} representing the current context we are in.
         * <p>
         *     This will help speed up the modifier checking, so we can know the exact
         *     context we are in to do specific checks for! :)
         * </p>
         */
        public AST currentContext;

        /**
         * Determines if a variable is changing state while inside a pure method.
         * <p>
         *     This is a helper method that checks if a given declaration from the
         *     {@link AST} matches a set of criteria defined in the method. If this
         *     criteria is met, this means the variable associated with the declaration
         *     is updated in some way and thus could be producing a side effect.
         * </p>
         * @param decl {@link AST} node that could be changing state
         * @return Boolean
         */
        private boolean methodChangesState(AST decl) {
            if(!decl.isSubNode() || !decl.asSubNode().isParamDecl())
                return false;
            ParamDecl pd = decl.asSubNode().asParamDecl();
            if(!pd.mod.isInMode())
                return true;

            return decl.isTopLevelDecl() && decl.asTopLevelDecl().isGlobalDecl();
        }

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
                       .addErrorArgs(subClass,superClass)
                       .addSuggestionNumber(MessageNumber.MOD_SUGGEST_1501)
                       .generateError();
            }
        }

        /**
         * Retrieves the modifier for a {@link ClassNode}, called by {@link #visitFieldExpr(FieldExpr)}.
         * @param classScope The class {@link SymbolTable we wish to find a field/method in.
         * @param field The current {@link FieldExpr} we want to retrieve a modifier of.
         * @return
         */
        private Modifier getModifier(SymbolTable classScope, FieldExpr field) {
            AST node;

            // If we are inside a complex field expression, then the next target will be the field/method we need.
            if(field.getAccessExpr().isFieldExpr()) {
                field = field.getAccessExpr().asFieldExpr();
                if(field.getTarget().isArrayExpr())
                    node = classScope.findName(field.getTarget().asArrayExpr().getArrayTarget());
                else if(field.getTarget().isInvocation())
                    node = classScope.findMethod(field.getTarget().asInvocation());
                else
                    node = classScope.findName(field.getTarget());
            }
            // Otherwise, the access expression contains the field/method we need to find!
            else {
                if(field.getAccessExpr().isArrayExpr())
                    node = classScope.findName(field.getAccessExpr().asArrayExpr().getArrayTarget());
                else if(field.getAccessExpr().isInvocation())
                    node = classScope.findMethod(field.getAccessExpr().asInvocation());
                else
                    node = classScope.findName(field.getAccessExpr());
            }


            if(node.asClassNode().isFieldDecl())
                return node.asClassNode().asFieldDecl().mod;
            else
                return node.asClassNode().asMethodDecl().mod;
        }

        /**
         * Checks if {@link #currentContext} represents the passed {@link ClassDecl}.
         * @param cd The {@link ClassDecl} we wish to check for equality.
         * @return {@code True} if the {@link #currentContext} represents the passed class, {@code False} otherwise.
         */
        private boolean insideClass(ClassDecl cd) {
            return currentContext != null
                && currentContext.isTopLevelDecl()
                && currentContext.asTopLevelDecl().isClassDecl()
                && currentContext.asTopLevelDecl().asClassDecl().getName().equals(cd.getName());
        }

        /**
         * Checks if {@link #currentContext} represents the passed {@link FuncDecl}.
         * @param fd The {@link FuncDecl} we wish to check for equality.
         * @return {@code True} if the {@link #currentContext} represents the passed function, {@code False} otherwise.
         */
        private boolean insideFunction(FuncDecl fd) {
            return currentContext != null
                && currentContext.isTopLevelDecl()
                && currentContext.asTopLevelDecl().isFuncDecl()
                && currentContext.asTopLevelDecl().asFuncDecl().equals(fd);
        }

        /**
         * Checks if {@link #currentContext} represents the passed {@link MethodDecl}.
         * @param md The {@link MethodDecl} we wish to check for equality.
         * @return {@code True} if the {@link #currentContext} represents the passed method, {@code False} otherwise.
         */
        private boolean insideMethod(MethodDecl md) {
            return currentContext != null
                && currentContext.isClassNode()
                && currentContext.asClassNode().isMethodDecl()
                && currentContext.asClassNode().asMethodDecl().equals(md);
        }

        /**
         * Checks if {@link #currentContext} is inside a pure function or method.
         * @return {@code True} if the checker is in a pure function or method, {@code False} otherwise.
         */
        private boolean insidePureMethod() {
            return (currentContext != null
                && currentContext.isTopLevelDecl()
                && currentContext.asTopLevelDecl().isFuncDecl()
                && currentContext.asTopLevelDecl().asFuncDecl().mod.isPure())
            || (   currentContext != null
                && currentContext.isClassNode()
                && currentContext.asClassNode().isMethodDecl()
                && currentContext.asClassNode().asMethodDecl().mod.isPure());
        }
    }
}
