package cminor.micropasses;

import cminor.ast.AST;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.GlobalDecl;
import cminor.ast.topleveldecls.ImportDecl;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.ErrorBuilder;
import cminor.messages.errors.semantic.SemanticError;
import cminor.utilities.Visitor;

/**
 * A {@link Visitor} designed to perform an introductory semantic analysis to a C Minor program.
 * <p>
 *     This will be the first {@link Visitor} executed after parsing is complete. The goal of this
 *     pass is to examine some high level semantics regarding a user's code in order to ensure there
 *     are no issues prior to beginning the major semantic passes. Here is a list of the checks
 *     performed in this class:
 *     <ul>
 *         <li>A check to see if every global and local variable was initialized.</li>
 *         <li>A check to see if the {@code break} and {@code continue} keywords were used only in loops.</li>
 *         <li>A check to see if input and output statements were written correctly.</li>
 *         <li>A check to make sure assignment statements were written correctly.</li>
 *         <li>A check to make sure an {@code instanceof} and {@code !instanceof} expression is written correctly.</li>
 *         <li>A check to make sure a user correctly declared operator overloads.</li>
 *     </ul>
 * </p>
 * @author Daniel Levy
 */
public class SemanticAnalyzer extends Visitor {

    /**
     * Instance of {@link SemanticAnalyzer} that will be used for additional semantic checking tasks.
     */
    private final SemanticAnalyzerHelper helper;

    /**
     * Default constructor for {@link SemanticAnalyzer}.
     */
    public SemanticAnalyzer() {
        this.handler = new MessageHandler();
        this.helper = new SemanticAnalyzerHelper();
    }

    /**
     * Checks if an {@link AssignStmt} is written correctly.
     * <p>
     *     This visit will check if a user has a valid LHS that can be assigned to.
     *     See {@link SemanticAnalyzerHelper#canExpressionBeAssignedTo(Expression)}.
     * </p>
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        as.getLHS().visit(this);

        // ERROR CHECK #1: For an assignment, we need to make sure the LHS can actually store a value.
        if(!helper.canExpressionBeAssignedTo(as.getLHS())) {
            ErrorBuilder eb = handler.createErrorBuilder(SemanticError.class)
                                     .addLocation(as)
                                     .addErrorArgs(as.getLHS());

            // We will generate a different error message when an error occurs within a retype statement.
            if(as.isRetypeStmt()) {
                eb.addErrorNumber(MessageNumber.SEMANTIC_ERROR_709)
                  .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1704);
            }
            else {
                eb.addErrorNumber(MessageNumber.SEMANTIC_ERROR_707)
                  .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1702);

            }
            eb.generateError();
        }

        as.getRHS().visit(this);
    }

    /**
     * Checks if an {@code instanceof} and {@code !instanceof} operation was written correctly.
     * @param be {@link BinaryExpr}
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.getLHS().visit(this);
        be.getRHS().visit(this);

        switch (be.getBinaryOp().getBinaryType()) {
            case INSTOF:
            case NINSTOF:
                // ERROR CHECK #1: This makes sure some name is present on the LHS of an instanceof operation.
                if (!helper.isValidLHS(be.getLHS())) {
                    handler.createErrorBuilder(SemanticError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.SEMANTIC_ERROR_708)
                            .addErrorArgs(be.getLHS(), be.getBinaryOp())
                            .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1703)
                            .addSuggestionArgs(be.getBinaryOp())
                            .generateError();
                }
        }
    }

    /**
     * Checks if the {@code break} keyword was written inside a loop statement.
     * @param bs {@link BreakStmt}
     */
    public void visitBreakStmt(BreakStmt bs) {
        // ERROR CHECK #1: The 'break' keyword should only be written inside a loop.
        if(!helper.insideLoop(bs)) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(bs)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_712)
                   .generateError();
        }
    }

    /**
     * Begins the {@link SemanticAnalyzer} pass in compilation mode.
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        super.visitCompilationUnit(cu);
        handler.printMessages();
    }

    /**
     * Checks if the {@code continue} keyword was written inside a loop statement.
     * @param cs {@link ContinueStmt}
     */
    public void visitContinueStmt(ContinueStmt cs) {
        // ERROR CHECK #1: The 'continue' keyword should only be written inside a loop.
        if(!helper.insideLoop(cs)) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(cs)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_713)
                   .generateError();
        }
    }

    /**
     * Checks if a field was initialized to a value.
     * <p>
     *     A field in C Minor is not allowed to be preassigned a value. The user themselves
     *     must either initialize the field or a default value will be assigned when an
     *     object is created.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        // ERROR CHECK #1: A field can not be initialized to any value.
        if(fd.hasInitialValue()) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(fd)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_715)
                   .addErrorArgs(fd, fd.getClassDecl())
                   .generateError();
        }
    }

    /**
     * Visits the for loop's components outside its control variable.
     * <p>
     *     No special steps are done here. We need to do a manual visit since
     *     we do not want to visit a {@link LocalDecl} for the control variable
     *     since its initialization value is set by the loop's starting value.
     * </p>
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        fs.getStartValue().visit(this);
        fs.getEndValue().visit(this);
        fs.getBody().visit(this);
    }

    /**
     * Checks if a global variable was initialized.
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // ERROR CHECK #1: The global variable has to be initialized.
        if(!gd.hasInitialValue()) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(gd)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_700)
                   .addErrorArgs("Global", gd)
                   .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1700)
                   .generateError();
        }

        // ERROR CHECK #2: If we have a global constant, then the keyword 'uninit' should not be used.
        if(gd.isConstant() && gd.getInitialValue() == null) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(gd)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_716)
                   .addErrorArgs(gd)
                   .generateError();
        }

    }

    /**
     * Visits the import's {@link CompilationUnit} to perform a high level semantic analysis.
     * @param im {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl im) { im.getCompilationUnit().visit(this); }

    /**
     * Checks if an expression can be used with an input statement.
     * @param is {@link InStmt}
     */
    public void visitInStmt(InStmt is) {
        for(Expression e : is.getInExprs()) {
            if(!helper.isValidInputExpression(e)) {
                handler.createErrorBuilder(SemanticError.class)
                        .addLocation(is)
                        .addErrorNumber(MessageNumber.SEMANTIC_ERROR_711)
                        .addErrorArgs(e)
                        .generateError();
            }
            e.visit(this);
        }
    }

    /**
     * Checks if a local variable was initialized.
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: Make sure the local variable was initialized to some value.
        if(!ld.hasInitialValue()) {
            handler.createErrorBuilder(SemanticError.class)
                   .addLocation(ld)
                   .addErrorNumber(MessageNumber.SEMANTIC_ERROR_700)
                   .addErrorArgs("Local", ld)
                   .addSuggestionNumber(MessageNumber.SEMANTIC_SUGGEST_1700)
                   .generateError();
        }
    }

    /** Checks if the operator overload was written correctly.
     * <p>
     *     When we visit a {@link MethodDecl} that corresponds to an operator
     *     overload, we want to make sure the user correctly wrote the overload.
     *     In this case, we will check if the correct number of arguments was
     *     given to the overload and if not, we will generate an error.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        if(md.isOperatorOverload()) {
            if(md.getOperatorOverload().isUnaryOp()) {
                // ERROR CHECK #1: Make sure a unary operator overload has no arguments
                if(!md.getParams().isEmpty()) {
                    handler.createErrorBuilder(SemanticError.class)
                           .addLocation(md)
                           .addErrorNumber(MessageNumber.SEMANTIC_ERROR_701)
                           .generateError();
                }
            }
            else {
                // ERROR CHECK #2: Make sure a binary operator overload has only one argument
                if(md.getParams().size() != 1) {
                    handler.createErrorBuilder(SemanticError.class)
                           .addLocation(md)
                           .addErrorNumber(MessageNumber.SEMANTIC_ERROR_702)
                           .generateError();
                }
            }
        }
    }

    /**
     * Checks if an expression can be used with an output statement.
     * @param os {@link OutStmt}
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.getOutExprs()) {
            if(!helper.isValidOutputExpression(e)) {
                handler.createErrorBuilder(SemanticError.class)
                        .addLocation(os)
                        .addErrorNumber(MessageNumber.SEMANTIC_ERROR_714)
                        .addErrorArgs(e)
                        .generateError();
            }
            e.visit(this);
        }
    }

    /**
     * Checks if a retype statement is written correctly.
     * <p>
     *     Since a retype statement is an {@link AssignStmt}, we will call {@link #visitAssignStmt(AssignStmt)}
     *     to perform semantic checking since the checks needed during this phase are identical.
     * </p>
     * @param rt {@link RetypeStmt}
     */
    public void visitRetypeStmt(RetypeStmt rt) { visitAssignStmt(rt); }

    /**
     * An inner class that stores all helper methods used by the {@link SemanticAnalyzer}.
     */
    private static class SemanticAnalyzerHelper {

        /**
         * Checks if an appropriate expression was written on the LHS of an {@link AssignStmt}.
         * <p>
         *     In this case, the LHS has to either be a {@link NameExpr} or an {@link cminor.ast.expressions.ArrayExpr}
         *     in order to allow a value to be assigned. If the LHS represents a {@link FieldExpr}, then we need
         *     to recursively call this method until we have the final expression contained in the field expression
         *     to determine if it's valid. Note: We will not allow any invocations (including those that return objects)
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
         * Checks if a passed expression is a valid LHS for a {@link BinaryExpr}.
         * <p>
         *     In this case, a valid expression is for the {@code instanceof} and {@code !instanceof}
         *     operations. The LHS must represent a name that can be evaluated.
         * </p>
         * @param LHS The {@link Expression} found on the LHS.
         * @return {@code True} if the LHS is a valid expression, {@code False} otherwise.
         */
        public boolean isValidLHS(Expression LHS) {
            return LHS.isNameExpr()
                || LHS.isFieldExpr()
                || LHS.isArrayExpr()
                || LHS.isInvocation();
        }

        /**
         * Verifies if a passed {@link AST} node is contained inside a loop statement.
         * <p>
         *     In this case, a loop will either be {@link cminor.ast.statements.DoStmt},
         *     {@link ForStmt}, or a {@link cminor.ast.statements.WhileStmt}. This is
         *     used to verify if loop keywords such as {@code break} and {@code continue}
         *     were used in the appropriate places.
         * </p>
         * @param node The {@link AST} node we wish to see if it's found in a loop.
         * @return {@code True} if the node is inside a loop, {@code False} otherwise.
         */
        public boolean insideLoop(AST node) {
            AST curr = node;

            while(curr != null) {
                if(curr.isStatement()) {
                    Statement possibleLoop = curr.asStatement();
                    if(possibleLoop.isDoStmt() || possibleLoop.isForStmt() || possibleLoop.isWhileStmt())
                        return true;
                }
                curr = curr.getParent();
            }

            return false;
        }

        /**
         * Checks if a passed {@link Expression} can be used with a cin statement.
         * <p>
         *     Since the compiler itself will handle the input verification, we are only
         *     allowing {@link cminor.ast.expressions.NameExpr}, {@link cminor.ast.expressions.ArrayExpr},
         *     and {@link cminor.ast.expressions.FieldExpr} that don't end with invocations inside
         *     cin statements. In essence, we want to make sure that all input expressions
         *     can be traced back to a variable.
         * </p>
         * @param node The {@link Expression} node we are checking if it can be used in the input statement.
         * @return {@code True} if the node can be used with the input statement, {@code False} otherwise.
         */
        public boolean isValidInputExpression(Expression node) {
            // By default, data can be stored into a name or an array.
            if(node.isNameExpr() || node.isArrayExpr())
                return true;

            // For field expressions, we want to verify the last access expression is not an invocation.
            if(node.isFieldExpr())
                return isValidInputExpression(node.asFieldExpr().getAccessExpr());

            return false;
        }

        /**
         * Checks if a passed {@link Expression} can be used with a cout statement.
         * <p>
         *     Due to the grammar's way of handling output statements, we can get some
         *     wacky programs that are syntactically valid, yet semantically unsound. This
         *     is an issue for {@link OutStmt}, so this method checks if each output expression
         *     is allowed.
         * </p>
         * @param node The current {@link Expression} we are verifying if it's correct.
         * @return {@code True} if the expression can be used in the output statement, {@code False} otherwise.
         */
        public boolean isValidOutputExpression(Expression node) {
            return node.isArrayExpr()
                || node.isBinaryExpr()
                || node.isCastExpr()
                || node.isEndl()
                || node.isFieldExpr()
                || node.isInvocation()
                || node.isLiteral()
                || node.isNameExpr()
                || node.isUnaryExpr();
        }

    }
}
