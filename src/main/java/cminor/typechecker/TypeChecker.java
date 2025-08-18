package cminor.typechecker;

// TODO: Remember since you are doing List[Void] internally, you need to make sure the user doesn't write that as a valid type. ?

import cminor.ast.AST;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.Invocation.InvocationBuilder;
import cminor.ast.expressions.Literal.ConstantType;
import cminor.ast.misc.Name;
import cminor.ast.operators.BinaryOp.BinaryType;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.GlobalDecl;
import cminor.ast.topleveldecls.MainDecl;
import cminor.ast.types.*;
import cminor.ast.types.ListType.ListTypeBuilder;
import cminor.ast.types.ScalarType.Scalars;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.ErrorBuilder;
import cminor.messages.errors.type.TypeError;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private TypeCheckerHelper helper;
    private ClassDecl currentClass;
    private Type currentTarget;
 //   private TypeValidityPass typeValidityPass;
    private boolean parentFound = false;
    private boolean inControlStmt = false;

    /**
     * Creates type checker in compilation mode
     */
    public TypeChecker(String fileName) {
        this.currentScope = null;
        this.currentClass = null;
        this.handler = new MessageHandler();
    }

    /**
     * Creates type checker in interpretation mode
     * @param st Symbol Table
     */
    public TypeChecker(SymbolTable st) {
        this.currentScope = st;
        this.handler = new MessageHandler();
        this.helper = new TypeCheckerHelper();
       //  this.typeValidityPass = new TypeValidityPass(this.currentScope.getRootTable());
    }

//    /**
//     * <p>
//     *     Array expressions are how users can access memory from array.
//     *     First, we make sure the target is an array (or a list) since it
//     *     does not make sense to dereference a non-array type. Then, we
//     *     will make sure the index evaluates to an integer. We will not
//     *     check if the integer is a valid index or not since this needs
//     *     to be done at runtime.
//     * </p>
//     * @param ae Array Expression
//     */
//    public void visitArrayExpr(ArrayExpr ae) {
//        ae.getArrayTarget().visit(this);
//
//        // ERROR CHECK #1: This checks if the target represents a valid Array or List type.
//        if(!ae.getArrayTarget().type.isArrayType() && !ae.getArrayTarget().type.isListType()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ae)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_453)
//                    .addErrorArgs(ae.getArrayTarget())
//                    .generateError();
//        }
//
//        Type arrType;
//        if(currentTarget == null)
//            arrType = currentScope.findName(ae.getArrayTarget()).getDecl().asType();
//        else {
//            ClassDecl cd = currentScope.findName(currentTarget).getDecl().asTopLevelDecl().asClassDecl();
//            arrType = cd.getScope().findName(ae.getArrayTarget()).getDecl().asClassNode().asFieldDecl().getDeclaredType().asArrayType();
//        }
//
//        if(arrType.isArrayType()) {
//            // ERROR CHECK #2: This checks if the number of indices exceeds the number of dimensions for the array.
//            if(ae.getArrayIndex().size() > arrType.asArrayType().numOfDims) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ae)
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_448)
//                        .addErrorArgs("array", ae.getArrayTarget(), arrType.asArrayType().numOfDims, ae.getArrayIndex().size())
//                        .generateError();
//            }
//        }
//        else {
//            // ERROR CHECK #3: Same as the previous error check, but for lists instead.
//            if(ae.getArrayIndex().size() > arrType.asListType().numOfDims) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ae)
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_448)
//                        .addErrorArgs("list", ae.getArrayTarget(), arrType.asListType().numOfDims, ae.getArrayIndex().size())
//                        .generateError();
//            }
//        }
//
//
//
//        for(Expression e : ae.getArrayIndex()) {
//            e.visit(this);
//
//            // ERROR CHECK #3: For each index, make sure the
//            //                 value evaluates to be an Int
//            if(!e.type.isInt()) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ae)
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_454)
//                        .addErrorArgs(e.type)
//                        .generateError();
//            }
//        }
//        if(arrType.isArrayType()) {
//            if(arrType.asArrayType().numOfDims != ae.getArrayIndex().size())
//                ae.type = new ArrayType(arrType.asArrayType().baseType(),ae.getArrayIndex().size());
//            else
//                ae.type = arrType.asArrayType().baseType();
//        }
//        else {
//            if(arrType.asListType().numOfDims != ae.getArrayIndex().size())
//                ae.type = new ListType(arrType.asListType().baseType(),ae.getArrayIndex().size());
//            else
//                ae.type = arrType.asListType().baseType();
//        }
//    }
//
//    /**
//     *     For array literals, we will call the helper method
//     *     arrayAssignmentCompatible to handle all type checking
//     *     for us.
//     * @param al Array Literal
//     */
//    public void visitArrayLiteral(ArrayLiteral al) {
//        // If current target doesn't represent an array type, then we'll set the
//        // array literal to be some arbitrary array type to prevent type assignment
//        // compatibility from leading to an exception
//        if(currentTarget == null || !currentTarget.isArrayType()) {
//            super.visitArrayLiteral(al);
//            if(al.getArrayInits().isEmpty())
//                al.type = new ArrayType(new VoidType(),al.getNumOfDims());
//            else
//                al.type = new ArrayType(al.getArrayInits().get(0).type,al.getNumOfDims());
//        }
//        else {
//            arrayAssignmentCompatibility(currentTarget.asArrayType().numOfDims,
//                                         currentTarget.asArrayType().baseType(),
//                                         al.getArrayDims(), al);
//            al.type = currentTarget.asArrayType();
//        }
//
//    }

        public void visitArrayLiteral(ArrayLiteral al) {     }


    /**
     * Evaluates the types of the LHS and RHS of an assignment statement.
     * <p>
     *     Since there is no type coercion, the LHS and RHS of an assignment must have
     *     the exact same type to prevent a compilation error from occurring. Additionally,
     *     C Minor supports the compound assignment operations, so we will do the same checks
     *     we do for arithmetic binary expressions in this visit.
     * </p>
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        as.getLHS().visit(this);
        Type LHS = as.getLHS().type;

        as.getRHS().visit(this);
        Type RHS = as.getRHS().type;

        // ERROR CHECK #1: Both the LHS and RHS of an assignment statement have to evaluate to the same types.
        if(!Type.assignmentCompatible(LHS,RHS)) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(as)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_403)
                   .addErrorArgs(as.getLHS(), LHS, RHS)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1401)
                   .addSuggestionArgs(LHS)
                   .generateError();
        }

        switch(as.getOperator().getAssignOp()) {
            case EQ:
                break;
            case PLUSEQ:
                // ERROR CHECK #2: The += operation is only supported for Int, Real, and String variables.
                if(!LHS.isInt() && !LHS.isReal() && !LHS.isString()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(as)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_403)
                           .addErrorArgs(as.getLHS(), LHS, RHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1402)
                           .generateError();
                }
                break;
            default:
                // ERROR CHECK #3: All other assignment operations are only supported for Int and Real variables.
                if(!LHS.isInt() && !LHS.isReal()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(as)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_404)
                           .addErrorArgs(as.getOperator(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1403)
                           .addSuggestionArgs(as.getOperator())
                           .generateError();
                }
        }
    }

    /**
     * Evaluates the binary expression's type.
     * <p>
     *     For binary operations, the operands have to be the same type since C Minor
     *     does not support type coercion. There are currently 24 binary operators.
     *     The following is a list of the operators with the types they are allowed to operate on:
     *     <ol>
     *         <li>
     *             '==', '!='
     *             <ul>
     *                 <li>Operand Type: Both operands are SAME type</li>
     *                 <li>Binary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *         <li>
     *             '>', '>=', '<', '<=', '<>', '<=>'
     *             <ul>
     *                 <li>Operand Type: Int, Real, Char</li>
     *                 <li>Binary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *         <li>
     *             '+', '-', '*', '/', '%', '**'
     *             <ul>
     *                 <li>Operand Type: Int, Real, or String (for '+' only)</li>
     *                 <li>Binary Expression Type: Operand Type</li>
     *             </ul>
     *         </li>
     *         <li>
     *             '<<', '>>'
     *             <ul>
     *                 <li>Operand Type: Int</li>
     *                 <li>Binary Expression Type: Int</li>
     *             </ul>
     *         </li>
     *         <li>
     *             '&', '|', '^'
     *             <ul>
     *                 <li>Operand Type: Discrete</li>
     *                 <li>Binary Expression Type: Bool or Int (for '^')</li>
     *             </ul>
     *         </li>
     *         <li>
     *             'and', 'or'
     *             <ul>
     *                 <li>Operand Type: Bool</li>
     *                 <li>Binary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *         <li>
     *             'instanceof', '!instanceof', 'as?'
     *             <ul>
     *                 <li>Operand Type: Class</li>
     *                 <li>Binary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *     </ol>
     *     We will also check if any binary operators are overloaded since operator
     *     overloading is supported in the language.
     * </p>
     * @param be {@link BinaryExpr}
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.getLHS().visit(this);
        Type LHS = be.getLHS().type;

        be.getRHS().visit(this);
        Type RHS = be.getRHS().type;

        BinaryType binType = be.getBinaryOp().getBinaryType();

        /*
            If the LHS represents an object, then we will treat the binary expression as an operator
            overload if the operator used is found in the list of operators that can be overloaded.
         */
        if(LHS.isClassOrMulti()) {
            switch(binType) {
                case EQEQ:
                case NEQ:
                case GT:
                case GTEQ:
                case LT:
                case LTEQ:
                case LTGT:
                case UFO:
                case PLUS:
                case MINUS:
                case MULT:
                case DIV:
                case MOD:
                case EXP:
                    FieldExpr fe =
                        new FieldExprBuilder()
                            .setTarget(be.getLHS())
                            .setAccessExpr(
                                new InvocationBuilder()
                                .setName(new Name("operator" + be.getBinaryOp()))
                                .setArgs(new Vector<>(be.getRHS()))
                                .create()
                            )
                            .create();
                    be.replaceWith(fe);
                    fe.visit(this);
                    return;
            }
        }

        // It is okay for 'instanceof', '!instanceof', and 'as?' to have different types for the LHS and RHS.
        if(!binType.equals(BinaryType.INSTOF) && !binType.equals(BinaryType.NINSTOF) && !binType.equals(BinaryType.AS)){
            // ERROR CHECK #1: The LHS and RHS types have to be the same in order to perform the binary operation.
            if(!Type.assignmentCompatible(LHS,RHS)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(be)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_405)
                       .addErrorArgs(be.getBinaryOp(), LHS, RHS)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1404)
                       .addSuggestionArgs(be.getBinaryOp())
                       .generateError();
            }
        }

        switch(be.getBinaryOp().getBinaryType()) {
            case EQEQ:
            case NEQ:
                be.type = new DiscreteType(Scalars.BOOL);
                break;
            case GT:
            case GTEQ:
            case LT:
            case LTEQ:
            case LTGT:
            case UFO:
                // ERROR CHECK #2: The operands need to be numeric, so we can perform a proper comparison.
                if(!LHS.isNumeric()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1405)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                be.type = new DiscreteType(Scalars.BOOL);
                break;
            case PLUS:
                // Special Case: String concatenation is supported with '+'.
                if(LHS.isString()) {
                    be.type = LHS;
                    break;
                }
            case MINUS:
            case MULT:
            case DIV:
            case MOD:
            case EXP:
                // ERROR CHECK #3: To perform arithmetic operations, the operands must either be Ints or Reals.
                if(!LHS.isInt() && !LHS.isReal()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1405)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                be.type = LHS;
                break;
            case SLEFT:
            case SRIGHT:
                // ERROR CHECK #4: A bitwise left and right shift can only be performed on Ints.
                if(!LHS.isInt()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1406)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                be.type = new DiscreteType(Scalars.INT);
                break;
            case BAND:
            case BOR:
            case XOR:
                // ERROR CHECK #5: The bitwise operations are supported for any Discrete type.
                if(!LHS.isDiscrete()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1407)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                if(binType.equals(BinaryType.XOR))
                    be.type = new DiscreteType(Scalars.INT);
                else
                    be.type = new DiscreteType(Scalars.BOOL);
                break;
            case AND:
            case OR:
                // ERROR CHECK #6: The logical operators are only supported for Bools.
                if(!LHS.isBool()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1408)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                be.type = new DiscreteType(Scalars.BOOL);
                break;
            case INSTOF:
            case NINSTOF:
            case AS:
                // ERROR CHECK #7: The LHS has to represent an object.
                if(!LHS.isClassOrMulti()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(be)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_407)
                           .addErrorArgs(be.getBinaryOp(), LHS)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1409)
                           .addSuggestionArgs(be.getBinaryOp())
                           .generateError();
                }
                be.type = new DiscreteType(Scalars.BOOL);
                break;
        }
    }

    /**
     * Evaluates the cast expression's type.
     * <p>
     *     In C Minor, there are three valid cast expressions:
     *     <ol>
     *         <li> Char <=> Int</li>
     *         <li> Int <=> Real</li>
     *         <li> Char => String</li>
     *     </ol>
     *     Users may also cast values of the same type (i.e. Bool(True) is allowed).
     *     When working with mixed type expressions, users will be expected to type
     *     cast values since there is no type coercion.
     * </p>
     * @param ce {@link CastExpr}
     */
    public void visitCastExpr(CastExpr ce) {
        ce.getCastExpr().visit(this);

        if(ce.getCastExpr().type.isInt()) {
            // ERROR CHECK #1: An Int can only typecast into a Char and a Real.
            if(!ce.getCastType().isChar() && !ce.getCastType().isReal() && !ce.getCastType().isInt()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ce)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_409)
                       .addErrorArgs(ce.getCastType())
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1412)
                       .addSuggestionArgs(ce.getCastExpr())
                       .generateError();
            }
        }
        else if(ce.getCastExpr().type.isChar()) {
            // ERROR CHECK #2: A Char can only typecast into an Int and a String.
            if(!ce.getCastType().isInt() && !ce.getCastType().isString() && !ce.getCastType().isChar()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ce)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_409)
                       .addErrorArgs(ce.getCastType())
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1413)
                       .addSuggestionArgs(ce.getCastExpr())
                       .generateError();
            }
        }
        else if(ce.getCastExpr().type.isReal()) {
            // ERROR CHECK #3: A Real can only typecast into an Int.
            if(!ce.getCastType().isInt() && !ce.getCastType().isReal()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ce)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_409)
                       .addErrorArgs(ce.getCastType())
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1414)
                       .addSuggestionArgs(ce.getCastExpr())
                       .generateError();
            }
        }
        else {
            // ERROR CHECK #4: By default, all other typecasts will be considered invalid if both types are different.
            if(!Type.assignmentCompatible(ce.getCastType(),ce.getCastExpr().type)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ce)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_409)
                       .addErrorArgs(ce.getCastType())
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1415)
                       .generateError();
            }
        }
        ce.type = ce.getCastType();
    }

    /**
     * Evaluates the types of the case statement labels.
     * <p>
     *     During this visit, we are concerned with making sure the choice value used
     *     can properly choose which case statement to execute. Currently, we are only
     *     supporting a choice on Int, Char, and String values. Here we will ensure that
     *     all labels were correctly written and evaluate to the type of the choice value.
     * </p>
     * @param cs {@link ChoiceStmt}
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.getChoiceValue().visit(this);
        Type choice = cs.getChoiceValue().type;

        // ERROR CHECK #1: A choice statement can only choose from an Int, Char, or String value.
        if(!choice.isInt() && !choice.isChar() && !choice.isString()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(cs.getChoiceValue())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_416)
                   .addErrorArgs(choice)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1422)
                   .generateError();
        }

        for(CaseStmt currentCase : cs.getCases()) {
            currentCase.getLabel().visit(this);
            Type label = currentCase.getLabel().getLeftConstant().type;

            // ERROR CHECK #2: The label has to have the same type as the choice value.
            if(!Type.assignmentCompatible(label,choice)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(currentCase.getLabel())
                       .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                       .addErrorArgs(label, choice)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1423)
                       .generateError();
            }

            if(currentCase.getLabel().getRightConstant() != null) {
                label = currentCase.getLabel().getRightConstant().type;
                // ERROR CHECK #3: Same as ERROR CHECK #2, but now we check if the right label is typed correctly.
                if(!Type.assignmentCompatible(label, choice)) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(currentCase.getLabel())
                           .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                           .addErrorArgs(label, choice)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1423)
                           .generateError();
                }

                // ERROR CHECK #4: A label may not have a range when working with String choice values.
                if(choice.isString()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(currentCase.getLabel())
                           .addErrorNumber(MessageNumber.TYPE_ERROR_418)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1424)
                           .generateError();
                }
                else if(choice.isInt()) {
                    int lLabel = currentCase.getLabel().getLeftConstant().asInt();
                    int rLabel = currentCase.getLabel().getRightConstant().asInt();
                    // ERROR CHECK #5: The right Int label has to be greater than the left Int label.
                    if(rLabel <= lLabel) {
                        handler.createErrorBuilder(TypeError.class)
                               .addLocation(currentCase.getLabel())
                               .addErrorNumber(MessageNumber.TYPE_ERROR_419)
                               .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1425)
                               .generateError();
                    }
                }
                else {
                    char lLabel = currentCase.getLabel().getLeftConstant().asChar();
                    char rLabel = currentCase.getLabel().getRightConstant().asChar();
                    // ERROR CHECK #6: The right Char label has to be greater than the left Char label.
                    if(rLabel <= lLabel) {
                        handler.createErrorBuilder(TypeError.class)
                               .addLocation(currentCase.getLabel())
                               .addErrorNumber(MessageNumber.TYPE_ERROR_419)
                               .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1425)
                               .generateError();
                    }
                }
            }

            currentScope = currentCase.getScope();
            currentCase.getBody().visit(this);
            currentScope = currentScope.closeScope();
        }

        // Visit the other branch.
        currentScope = cs.getScope();
        cs.getDefaultBody().visit(this);
        currentScope = currentScope.closeScope();
    }

//    /**
//     * Creates the class hierarchy for the current class.<br><br>
//     * <p>
//     *     We do not need to do any explicit type checking with the class
//     *     declaration itself. All we need to do here is to internally keep
//     *     track of all inherited classes, so we can use this information
//     *     elsewhere in the type checker.
//     * </p>
//     * @param cd Class Declaration
//     */
//    public void visitClassDecl(ClassDecl cd) {
//        // Only create the class hierarchy if the class inherits from another class
//        if(cd.getSuperClass() != null) {
//            // Add each inherited class to an internal class hierarchy list
//            ClassDecl base = currentScope.findName(cd.getSuperClass().toString()).getDecl().asTopLevelDecl().asClassDecl();
//            while(base.getSuperClass() != null) {
//                cd.addBaseClass(base.getName());
//                if(base.getSuperClass() != null)
//                    base = currentScope.findName(base.getSuperClass().toString()).getDecl().asTopLevelDecl().asClassDecl();
//            }
//            cd.addBaseClass(base.getName());
//        }
//
//        SymbolTable oldScope = currentScope;
//        ClassDecl oldClass = currentClass;
//        currentScope = cd.getScope();
//        currentClass = cd;
//
//        // Do not type check the class if it's a template class.
//        if(cd.getTypeParams().isEmpty())
//            super.visitClassDecl(cd);
//
//        currentClass = oldClass;
//        currentScope = oldScope;
//    }
//
//    public void visitCompilationUnit(CompilationUnit cu) {
//        for(ImportDecl id : cu.getImports())
//            id.getCompilationUnit().visit(this);
//        currentScope = cu.getScope();
//        this.typeValidityPass = new TypeValidityPass(cu.getScope());
//        super.visitCompilationUnit(cu);
//    }

    /**
     * Verifies the type of the do while loop's condition.
     * <p>
     *     The conditional expression for a do while loop must evaluate to be a
     *     boolean. This is the only type-related check performed with this visit.
     * </p>
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        currentScope = ds.scope;
        ds.getBody().visit(this);
        currentScope = currentScope.closeScope();

        ds.getCondition().visit(this);
        // ERROR CHECK #1: The do while loop's condition must evaluate to be a boolean.
        if(!ds.getCondition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(ds.getCondition())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_412)
                   .addErrorArgs(ds.getCondition().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1418)
                   .generateError();
        }
    }
//
//    /**
//     * Evaluates if an enumeration was written correctly<br>
//     * <p>
//     *     In C Minor, an enumeration can only store values of type Int
//     *     and Char for each constant. Additionally, we are going to be
//     *     strict and require the user to initialize all values of the
//     *     enumeration if at least one constant was initialized to a default value.
//     * </p>
//     * @param ed Enumeration
//     */
//    public void visitEnumDecl(EnumDecl ed) {
//        // Step 1: Count how many constants were initialized.
//        int initCount = 0;
//        for(Var constant : ed.getConstants()) {
//            if(constant.hasInitialValue()) {
//                initCount++;
//                if(ed.getConstantType() == null) {
//                    constant.getInitialValue().visit(this);
//
//                    // ERROR CHECK #1: A constant in an Enum can only be assigned Int or Char values
//                    if(!constant.getInitialValue().type.isInt() && !constant.getInitialValue().type.isChar()) {
//                        handler.createErrorBuilder(TypeError.class)
//                                .addLocation(ed)
//                                .addErrorNumber(MessageNumber.TYPE_ERROR_436)
//                                .addErrorArgs(ed,constant.getInitialValue().type)
//                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1434)
//                                .generateError();
//                    }
//
//                    EnumTypeBuilder typeBuilder = new EnumTypeBuilder().setName(ed.getName());
//
//                    if(constant.getInitialValue().type.isInt())
//                        typeBuilder.setConstantType(Discretes.INT);
//                    else
//                        typeBuilder.setConstantType(Discretes.CHAR);
//
//                    //ed.setType(typeBuilder.create());
//                }
//            }
//        }
//
//        if(constantInitCount == 0) {
//            // By default, an Enum will have Int constants starting at [1,inf)
//            // if the user did not initialize any of the constant values.
////            ed.setType(
////                new EnumTypeBuilder()
////                    .setName(ed.name())
////                    .setConstantType(Discretes.INT)
////                    .create()
////            );
//
//            int currValue = 1;
//            for(Var constant : ed.getConstants()) {
////                constant.setInit(
////                    new LiteralBuilder()
////                        .setConstantKind(ConstantType.INT)
////                        .setValue(String.valueOf(currValue))
////                        .create()
////                );
////                currValue++;
////                constant.init().type = ed.type();
////                constant.setType(ed.type());
//            }
//        }
//        // ERROR CHECK #2: Make sure each constant in the Enum was initialized
//        else if(constantInitCount != ed.getConstants().size()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ed)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_437)
//                    .addErrorArgs(ed)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1435)
//                    .generateError();
//        }
//        else {
//            for(Var constant : ed.getConstants()) {
//                constant.getInitialValue().visit(this);
//
//                // ERROR CHECK #3: Make sure the initial value given to a
//                //                 constant matches the enum's constant type
//                if(!Type.assignmentCompatible(ed.getConstantType().constantType(),constant.getInitialValue().type)) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(constant)
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_438)
//                            .addErrorArgs(constant,ed,constant.getInitialValue().type,ed.getConstantType().constantType())
//                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1436)
//                            .generateError();
//                }
////                constant.init().type = ed.type();
////                constant.setType(ed.type());
//            }
//        }
//    }
//
//    /**
//     * Evaluates the type of a field expression<br>
//     * <p>
//     * For a field expression, we will first evaluate the target and make sure the
//     * type corresponds to some previously declared class. Then, we will type check
//     * the expression the target is trying to access. We will use {@code currentTarget}
//     * to keep track of the target's type if we're trying to perform method invocations.
//     * </p>
//     * @param fe Field Expression
//     */
//    public void visitFieldExpr(FieldExpr fe) {
//        fe.getTarget().visit(this);
//
//        // ERROR CHECK #1: We want to make sure the target is indeed an object,
//        //                 so make sure it's assigned a class type
//        if(!fe.getTarget().type.isClassOrMultiType()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(fe)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_435)
//                    .addErrorArgs(fe.getTarget(),fe.getTarget().type)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1433)
//                    .generateError();
//        }
//
//        Type oldTarget = currentTarget;
//        currentTarget = fe.getTarget().type;
//        fe.getAccessExpr().visit(this);
//
//        fe.type = fe.getAccessExpr().type;
//        currentTarget = oldTarget;
//        parentFound = false;
//    }

    /**
     * Evaluates the types found in the for loop's header.
     * <p>
     *     This visit will handle the type checking related to the for loop's control
     *     variable. Only {@code Int}, {@code Char}, and {@link EnumType} are allowed
     *     to be iterated.
     * </p>
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.getScope();
        fs.getControlVariable().visit(this);

        Type variable = fs.getControlVariable().getType();

        // ERROR CHECK #1: The control variable can only iterate over Int, Char, or Enum values.
        if(!variable.isInt() && !variable.isChar() && !variable.isEnum()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(fs.getControlVariable())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_413)
                   .addErrorArgs(fs.getControlVariable(), variable)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1419)
                   .generateError();
        }

        fs.getStartValue().visit(this);
        fs.getEndValue().visit(this);

        // ERROR CHECK #2: The starting and ending values need to represent the same type!
        if(!Type.assignmentCompatible(fs.getStartValue().type,fs.getEndValue().type)) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(fs)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_414)
                   .addErrorArgs(fs.getStartValue().type,fs.getEndValue().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1420)
                   .generateError();
        }

        // ERROR CHECK #3: The control variable must match the types of the starting and ending values.
        if(!Type.assignmentCompatible(variable, fs.getStartValue().type)) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(fs)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_415)
                   .addErrorArgs(fs.getControlVariable(), variable, fs.getStartValue().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1421)
                   .generateError();
        }

        fs.getBody().visit(this);
    }

    /**
     * Evaluates the function's return type.
     * <p>
     *     There is no specific type checks that are needed during this visit. We are
     *     only concerned with making sure non-Void functions will be guaranteed to
     *     return a value. Additionally, we want to make sure we do not type check template
     *     functions, so we will wait until they're instantiated to do so.
     * </p>
     * @param fd {@link FuncDecl}
     */
    public void visitFuncDecl(FuncDecl fd) {
        // Do not type check a template function until it is instantiated!
        if(fd.isTemplate())
            return;

        currentScope = fd.getScope();
        super.visitFuncDecl(fd);
        currentScope = currentScope.closeScope();

        // ERROR CHECK #1: A non-Void function must be guaranteed to return a value!
        if(!fd.getReturnType().isVoid() && !fd.containsReturnStmt()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(fd)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_422)
                   .addErrorArgs(fd, fd.getReturnType())
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1427)
                   .generateError();
        }
    }

    /**
     * Evaluates the global variable's type.
     * <p>
     *     When a global variable is initialized, the type of its initial
     *     value must explicitly match its declared type. We will also try
     *     to generate an initial value for the global variable if applicable.
     * </p>
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.getInitialValue() == null)
            gd.setInitialValue(helper.generateDefaultValue(gd.getType()));

        // We should not check for assignment compatibility if uninit is used for structured types.
        if(gd.getInitialValue() != null) {
            gd.getInitialValue().visit(this);

            // ERROR CHECK #1: The global variable's declared type must match the type of its initial value.
            if(!Type.assignmentCompatible(gd.getType(), gd.getInitialValue().type)) {
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                         .addLocation(gd)
                                         .addErrorArgs(gd, gd.getType(), gd.getInitialValue().type)
                                         .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                                         .addSuggestionArgs(gd.getType());
                if(!gd.isConstant())
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_401);
                else
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_402);
                eb.generateError();
            }
        }
    }

    /**
     * Verifies the type of the if statement's conditional expression.
     * <p>
     *     For each if and elif branch, we need to make sure the conditional
     *     expression evaluates to be a boolean. This is the only type-related
     *     check performed with this visit.
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        is.getCondition().visit(this);

        // ERROR CHECK #1: The if statement's conditional expression must be a boolean.
        if(!is.getCondition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(is.getCondition())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_410)
                   .addErrorArgs(is.getCondition().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1416)
                   .generateError();
        }

        currentScope = is.getIfScope();
        is.getIfBody().visit(this);
        currentScope = currentScope.closeScope();

        for(IfStmt elif : is.getElifs())
            elif.visit(this);

        if(is.containsElse()) {
            currentScope = is.getElseScope();
            is.getElseBody().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Evaluates each input expression's type.
     * <p>
     *     This visit will ensure each input expression represents a scalar type
     *     since these are the only types that we can accept terminal input for.
     *     By default, an input statement will be given a {@link VoidType} since
     *     it will not be used for anything.
     * </p>
     * @param in {@link InStmt}
     */
    public void visitInStmt(InStmt in) {
        for(Expression expr : in.getInExprs()) {
            expr.visit(this);
            // ERROR CHECK #1: The input expression needs to evaluate to be a scalar type.
            if(!expr.type.isScalar() || expr.type.isEnum()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(in)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_440)
                       .addErrorArgs(expr, expr.type)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1437)
                       .generateError();
            }
        }
        in.type = new VoidType();
    }

    /**
     * Evaluates the type of an invocation.
     * <p>
     *     There are two types of invocations in C Minor. A function
     *     invocation will be checked to see if a valid overload for the
     *     function exists somewhere in the program. Meanwhile, a method
     *     invocation will be searched through the class hierarchy based
     *     on the current target type.
     * </p>
     * @param in Invocation
     */
    public void visitInvocation(Invocation in) {
        StringBuilder argSignature = new StringBuilder();

        for(Expression arg :in.getArgs()) {
            arg.visit(this);
            argSignature.append(arg.type.typeSignature());
        }

        in.targetType = new VoidType();

        //if(currentScope.hasMethodOverload())

//        // Function Invocation
//        if(currentTarget == null && currentClass == null) {
//            in.targetType = new VoidType();
//
//            if(in.isLengthInvocation()) {
//                // ERROR CHECK #1: Make sure 'length' call only has one argument
//                if(in.getArgs().size() != 1) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(in)
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_451)
//                            .generateError();
//                }
//                // ERROR CHECK #2: Make sure argument evaluates to an array or list
//                if(!(in.getArgs().get(0).type.isArrayType() || in.getArgs().get(0).type.isListType())) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(in)
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_452)
//                            .generateError();
//                }
//                in.targetType = new VoidType();
//                in.type = new DiscreteType(Discretes.INT);
//                in.setLengthInvocation();
//                return;
//            }
//
//            else if(in.isTemplate()) {
//                FuncDecl template = findValidFuncTemplate(in.toString(), in);
//                checkIfFuncTemplateCallIsValid(template, in);
//                in.templatedFunction = typeValidityPass.instantiatesFuncTemplate(template, in);
//
//                for(int i = 0; i < in.getArgs().size(); i++) {
//                    Type paramType = in.templatedFunction.getParams().get(i).getType();
//                    Type argType = in.getArgs().get(i).type;
//
//                    if(!Type.assignmentCompatible(paramType, argType)) {
//                        String argumentTypes = Type.createTypeString(in.getArgs());
//                        ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                                .addLocation(in)
//                                .addErrorArgs(in,argumentTypes)
//                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1431)
//                                .addSuggestionArgs(in,in+"("+argumentTypes+")");
//
//                        if(in.getArgs().isEmpty())
//                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_429).generateError();
//                        else if(in.getArgs().size() == 1)
//                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_430).generateError();
//                        else
//                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_431).generateError();
//                    }
//                }
//
//                in.type = in.templatedFunction.getReturnType();
//                in.templatedFunction.visit(this);
//                return;
//            }
//
//            // ERROR CHECK #3: Check if function overload exists
//            if(!currentScope.hasNameSomewhere(signature.toString())) {
//                String argumentTypes = Type.createTypeString(in.getArgs());
//                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                                      .addLocation(in)
//                                      .addErrorArgs(in,argumentTypes)
//                                      .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1431)
//                                      .addSuggestionArgs(in,in+"("+argumentTypes+")");
//
//                if(in.getArgs().isEmpty())
//                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_429).generateError();
//                else if(in.getArgs().size() == 1)
//                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_430).generateError();
//                else
//                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_431).generateError();
//            }
//
//            FuncDecl fd = currentScope.findName(signature.toString()).getDecl().asTopLevelDecl().asFuncDecl();
//            in.type = fd.getReturnType();
//        }
//        // Method Invocation
//        else {
//            ClassDecl cd = null;
//            if(currentTarget != null && currentTarget.isMultiType()) {
//                for(ClassType ct : currentTarget.asMultiType().getAllTypes()) {
//                    cd = currentScope.findName(ct.toString()).getDecl().asTopLevelDecl().asClassDecl();
//                    if(cd.getScope().hasMethod(in.toString()))
//                        break;
//                }
//            }
//            else if(parentFound)
//                cd = currentScope.findName(currentClass.getSuperClass().toString()).getDecl().asTopLevelDecl().asClassDecl();
//            else if(currentClass != null)
//                cd = currentClass;
//            else
//                cd = currentScope.findName(currentTarget.asClassType().toString()).getDecl().asTopLevelDecl().asClassDecl();
//
//            String className = cd.toString();
//            // ERROR CHECK #4: Check if the method was defined in the class hierarchy
//            if(!cd.getScope().hasMethod(in.toString())) {
////                if(interpretMode)
////                    currentTarget = null;
//                handler.createErrorBuilder(ScopeError.class)
//                        .addLocation(in)
//                        .addErrorNumber(MessageNumber.SCOPE_ERROR_327)
//                        .addErrorArgs(in)
//                        .generateError();
//            }
//
//            // ERROR CHECK #5: Check if a valid method overload exists
//            while(!cd.getScope().hasName(signature.toString())) {
//                if(cd.getSuperClass() == null) {
////                    if(interpretMode)
////                        currentTarget = null;
//
//                    String argumentTypes = Type.createTypeString(in.getArgs());
//                    ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                                          .addLocation(in)
//                                          .addErrorArgs(in,className,argumentTypes)
//                                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1432)
//                                          .addSuggestionArgs(in,className,in+"("+argumentTypes+")");
//
//                    if(in.getArgs().isEmpty())
//                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_432).generateError();
//                    else if(in.getArgs().size() == 1)
//                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_433).generateError();
//                    else
//                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_434).generateError();
//                }
//                cd = currentScope.findName(cd.getSuperClass().toString()).getDecl().asTopLevelDecl().asClassDecl();
//            }
//
//            MethodDecl md = cd.getScope().findName(signature.toString()).getDecl().asClassNode().asMethodDecl();
//            if(currentTarget.isClassType() && currentTarget.asClassType().isTemplatedType())
//                in.targetType = currentTarget;
//            else
//                in.targetType = new ClassType(cd.toString());
//            in.type = md.getReturnType();
//            currentTarget = md.getReturnType();
//        }
    }

    /**
     * Creates a type for a {@link Literal} value.
     * <p>
     *     The type of the parsed constant will determine the type of
     *     the literal. This visit is only concerned with {@link ScalarType},
     *     and other visits will handle the structured types.
     * </p>
     * @param li {@link Literal}
     */
    public void visitLiteral(Literal li) {
        switch(li.getConstantKind()) {
            case INT:
                li.type = new DiscreteType(Scalars.INT);
                break;
            case CHAR:
                li.type = new DiscreteType(Scalars.CHAR);
                break;
            case BOOL:
                li.type = new DiscreteType(Scalars.BOOL);
                break;
            case REAL:
                li.type = new ScalarType(Scalars.REAL);
                break;
            case STR:
                li.type = new ScalarType(Scalars.STR);
                break;
            case TEXT:
                li.type = new ScalarType(Scalars.TEXT);
        }
    }

    /**
     * Creates a type for a {@link ListLiteral}.
     * <p>
     *     When a list is created by the user, we need to first visit the initial values of
     *     the list. From there, we will call {@link TypeCheckerHelper#generateListType(ListLiteral)}
     *     to make sure all the initial values are assignment compatible before we generate
     *     the {@link ListType} that the current list will represent.
     * </p>
     * @param ll {@link ListLiteral}
     */
    public void visitListLiteral(ListLiteral ll) {
        for(Expression init : ll.getInits())
            init.visit(this);

        ll.type = helper.generateListType(ll);
    }

//    /**
//     * Checks the type of a list statement.
//     * <p><br>
//     *     In C Minor, there are currently 3 list commands: {@code append}, {@code insert},
//     *     and {@code remove}. This method will type check all arguments passed to the command
//     *     to ensure the user correctly wrote the command. For {@code remove}, we will not type
//     *     check the value that is removed since we will throw a runtime exception if the value
//     *     can not be removed from the list.
//     * </p>
//     * @param ls List Statement
//     */
//    public void visitListStmt(ListStmt ls) {
//        // SPECIAL CASE!!!
//        // If a user writes their own function/method using the list command name, we want to rewrite
//        // the current list statement as an invocation for better type checking.
//        if(currentScope.hasMethodSomewhere(ls.toString())) {
//            StringBuilder commandSignature = new StringBuilder(ls + "/");
//
//            for(Expression e : ls.getAllArgs()) {
//                e.visit(this);
//                commandSignature.append(e.type.typeSignature());
//            }
//
//            if(currentScope.hasNameSomewhere(commandSignature.toString())) {
//                Invocation in = new InvocationBuilder()
//                                .setMetaData(ls)
//                                .setName(new Name(ls.toString()))
//                                .setArgs(ls.getAllArgs())
//                                .create();
//                ls.replaceWith(in);
//                in.visit(this);
//
//                // This is needed because the list statement reference will still be stored by the VM
//                // if the list statement was the only line of code the user wrote. We want to call the newly
//                // created invocation and not the list statement, so this is the best solution I came up with...
//                if(ls.getFullLocation() == null)
//                    ls.setInvocation(in);
//
//                return;
//            }
//        }
//
//        // ERROR CHECK #1: This ensures the correct number of arguments were passed to the list command.
//        //                 append/remove => 2 args, insert => 3 args
//        if(ls.getAllArgs().size() != ls.getExpectedNumOfArgs()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ls)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_449)
//                    .addErrorArgs(ls,ls.getExpectedNumOfArgs(),ls.getAllArgs().size())
//                    .generateError();
//        }
//
//        ls.getList().visit(this);
//        // ERROR CHECK #2: The first argument in a list command must be the list the command acts on.
//        if(!ls.getList().type.isListType()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ls)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_450)
//                    .addErrorArgs(ls.getList(),ls)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1445)
//                    .addSuggestionArgs(ls)
//                    .generateError();
//        }
//
//        ls.getSecondArg().visit(this);
//        Type finalArgType = ls.getSecondArg().type;
//        // ERROR CHECK #3: The second argument for the insert command must be an integer.
//        if(ls.isInsert()) {
//            if(!finalArgType.isInt()) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ls)
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_451)
//                        .addErrorArgs(ls.getAllArgs().get(1))
//                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1446)
//                        .generateError();
//            }
//
//            ls.getThirdArg().visit(this);
//            finalArgType = ls.getThirdArg().type;
//        }
//
//        // ERROR CHECK #4: The final argument for the append/insert command needs to be a value
//        //                 that can either be stored or merged into the list.
//        if(ls.isAppend() || ls.isInsert()) {
//            if(!ls.getListType().isSubList(finalArgType)) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ls)
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_452)
//                        .addErrorArgs(ls.getList(), ls.getListType(), ls, finalArgType)
//                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1447)
//                        .addSuggestionArgs(ls, ls.getList(), ls.getListType(), ls.getListType().validSublist())
//                        .generateError();
//            }
//        }
//    }

    /**
     * Evaluates the local variable's type.
     * <p>
     *     When a local variable is initialized, the type of its initial
     *     value must explicitly match its declared type. We will also try
     *     to generate an initial value for the local variable if applicable.
     * </p>
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        if(ld.getInitialValue() == null)
            ld.setInitialValue(helper.generateDefaultValue(ld.getType()));

        // We should not check for assignment compatibility if uninit is used for structured types.
        if(ld.getInitialValue() != null) {
            ld.getInitialValue().visit(this);

            // ERROR CHECK #1: The local variable's declared type must match the type of its initial value.
            if(!Type.assignmentCompatible(ld.getType(), ld.getInitialValue().type)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ld)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_400)
                       .addErrorArgs(ld, ld.getType(), ld.getInitialValue().type)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                       .addSuggestionArgs(ld.getType())
                       .generateError();
            }
        }
    }

    /**
     * Verifies the main function's return type.
     * <p>
     *     The main function should be declared as a {@code Void} type since it should
     *     not return any value. This is the only type check we will perform in this visit.
     * </p>
     * @param md {@link MainDecl}
     */
    public void visitMainDecl(MainDecl md) {
        // ERROR CHECK #1: The main function should not return any value since it will terminate the program.
        if(!md.getReturnType().isVoid()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(md)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                   .addErrorArgs(md.getReturnType())
                   .generateError();
        }

        currentScope = md.getScope();
        super.visitMainDecl(md);
    }

    /**
     * Evaluates the method's return type.
     * <p>
     *     There is no specific type checks that are needed during this visit. We are only
     *     concerned with making sure non-Void methods will be guaranteed to return a value.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.getScope();
        super.visitMethodDecl(md);
        currentScope = currentScope.closeScope();

        // ERROR CHECK #1: A non-Void function must be guaranteed to return a value!
        if(!md.getReturnType().isVoid() && !md.containsReturnStmt()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(md)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_424)
                   .addErrorArgs(md, md.getClassDecl(), md.getReturnType())
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1428)
                   .generateError();
        }
    }

    /**
     * Evaluates the type of the name expression.<br>
     * <p>
     *     For a name expression, the type will be based on the declaration
     *     type. Here, we will also perform name checking when it comes to
     *     evaluating complex field expressions.
     * </p>
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        AST declaration = currentScope.findName(ne);

        if(declaration.isClassNode())
            ne.type = declaration.asClassNode().asFieldDecl().getType();
        else if(declaration.isTopLevelDecl())
            ne.type = declaration.asTopLevelDecl().asGlobalDecl().getType();
        else
            ne.type = declaration.asStatement().asLocalDecl().getType();

//        if(ne.isParentKeyword()) {
//            if(parentFound) {
//                parentFound = false;
//                handler.createErrorBuilder(ScopeError.class)
//                        .addLocation(ne)
//                        .addErrorNumber(MessageNumber.SCOPE_ERROR_337)
//                        .generateError();
//            }
//            parentFound = true;
//            ne.type = currentClass.getSuperClass();
//        }
//        else if(currentTarget != null && currentTarget.isClassOrMultiType()) {
//            String targetName = currentTarget.toString();
//            ClassDecl cd = null;
//            if(currentTarget.isClassType()) {
//                cd = currentScope.findName(targetName).getDecl().asTopLevelDecl().asClassDecl();
//
//                // ERROR CHECK #1: Make sure the class name exists if we are
//                //                 evaluating a complex field expression
//                if(!cd.getScope().hasName(ne.toString())) {
//                    // We need to reset currentTarget if there's an error during interpretation
////                    if(interpretMode)
////                        currentTarget = null;
//                    handler.createErrorBuilder(ScopeError.class)
//                            .addLocation(ne.getFullLocation())
//                            .addErrorNumber(MessageNumber.SCOPE_ERROR_329)
//                            .addErrorArgs(ne,targetName)
//                            .generateError();
//                }
//            }
//            else {
//                boolean found = false;
//                for(ClassType ct : currentTarget.asMultiType().getAllTypes()) {
//                    cd = currentScope.findName(ct.toString()).getDecl().asTopLevelDecl().asClassDecl();
//                    if(cd.getScope().hasName(ne.toString())) {
//                        found = true;
//                        break;
//                    }
//                }
//                // ERROR CHECK #2: Make sure the class that declared the
//                //                 field was found for a MultiTyped name
//                if(!found) {
////                    if (interpretMode)
////                        currentTarget = null;
//                    handler.createErrorBuilder(ScopeError.class)
//                            .addLocation(ne)
//                            .addErrorNumber(MessageNumber.SCOPE_ERROR_309)
//                            .addErrorArgs(ne.toString(), targetName)
//                            .generateError();
//                }
//            }
//            ne.type = cd.getScope().findName(ne.toString()).getDecl().asClassNode().asFieldDecl().getDeclaredType();
//        }
//        else {
//            AST decl = currentScope.findName(ne.toString()).getDecl();
//            if(decl.isStatement())
//                ne.type = decl.asStatement().asLocalDecl().getDeclaredType();
////            else if(decl.isParamDecl())
////                ne.type = decl.asParamDecl().type();
////            else if(decl.isFieldDecl())
////                ne.type = decl.asFieldDecl().type();
//            else {
//                TopLevelDecl tDecl = decl.asTopLevelDecl();
//                if(tDecl.isEnumDecl())
//                    ne.type = tDecl.asEnumDecl().getConstantType();
//                else if(tDecl.isGlobalDecl())
//                    ne.type = tDecl.asGlobalDecl().getDeclaredType();
//                else
//                    ne.type = new ClassType(tDecl.asClassDecl().getName());
//            }
//        }
    }

//    /**
//     * <p>
//     *     In C Minor, a constructor is automatically generated for the user.
//     *     Thus, we do not need to check if a new expression can be called for
//     *     the class we are trying to instantiate. Instead, we only need to check
//     *     if for each argument, the type of the value corresponds to the type of
//     *     the field declaration we're saving the argument into.
//     * </p>
//     * @param ne New Expression
//     */
//    public void visitNewExpr(NewExpr ne) {
//        ClassDecl cd = currentScope.findName(ne.getClassType()).getDecl().asTopLevelDecl().asClassDecl();
//
//        for(Var v : ne.getInitialFields()) {
//            Type fType = cd.getScope().findName(v.toString()).getDecl().asClassNode().asFieldDecl().getDeclaredType();
//
//            if((fType.isArrayType() && v.getInitialValue().isArrayLiteral()) || (fType.isListType() && v.getInitialValue().isListLiteral())) {
//                Type oldTarget = currentTarget;
//                currentTarget = fType;
//                v.getInitialValue().visit(this);
//                currentTarget = oldTarget;
//            }
//            else {
//                v.getInitialValue().visit(this);
//
//                // ERROR CHECK #1: Make sure the type of the argument matches
//                //                 the type of the field declaration
//                if(!Type.assignmentCompatible(v.getInitialValue().type,fType)) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(ne)
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_420)
//                            .addErrorArgs(v.toString(),fType,v.getInitialValue().type)
//                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
//                            .addSuggestionArgs(fType)
//                            .generateError();
//                }
//            }
//        }
//        ne.type = ne.getClassType();
//        ne.type.asClassType().setInheritedTypes(cd.getInheritedClasses());
//
//        // If the new expression is bounded to an instantiated class, we now want to visit
//        // said class and perform type checking based on the provided arguments.
//        if(ne.createsFromTemplate())
//            ne.getInstantiatedClass().visit(this);
//    }

    /**
     * Evaluates each output expression's type.
     * <p>
     *     This visit will simply visit each output expression to ensure they
     *     are correctly typed. By default, an output statement will be given
     *     a {@link VoidType} since it will not be used for anything.
     * </p>
     * @param os {@link OutStmt}
     */
    public void visitOutStmt(OutStmt os) {
        super.visitOutStmt(os);
        os.type = new VoidType();
    }

    /**
     * Evaluates the return value's type (if applicable)
     * <p>
     *     A return statement is only found in a function or method, so we need to ensure
     *     that the return value correctly matches the expected return type.
     * </p>
     * @param rs {@link ReturnStmt}
     */
    public void visitReturnStmt(ReturnStmt rs) {
        Type returnType;
        AST method = rs.getFunctionLocation();

        if(method.isTopLevelDecl()) {
            if(method.asTopLevelDecl().isFuncDecl())
                returnType = method.asTopLevelDecl().asFuncDecl().getReturnType();
            else
                returnType = method.asTopLevelDecl().asMainDecl().getReturnType();
        } else
            returnType = method.asClassNode().asMethodDecl().getReturnType();

        helper.isReturnStmtValid(rs, method);

        // If the user wrote 'return' without a value, then no type checking is done here! :)
        if(rs.getReturnValue() == null)
            return;

        rs.getReturnValue().visit(this);
        // ERROR CHECK #1: A return statement can not return a value if it's written inside a Void method.
        if(returnType.isVoid()) {
            ErrorBuilder eb = handler.createErrorBuilder(TypeError.class).addLocation(rs);
            if(method.isTopLevelDecl()) {
                eb.addErrorNumber(MessageNumber.TYPE_ERROR_425)
                  .addErrorArgs(method, rs.getReturnValue().type)
                  .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1429)
                  .generateError();
            } else {
                eb.addErrorNumber(MessageNumber.TYPE_ERROR_426)
                  .addErrorArgs(method, method.asClassNode().asMethodDecl().getClassDecl(), rs.getReturnValue().type)
                  .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1430)
                  .generateError();
            }
        }

        // ERROR CHECK #2: The return value should match the return type of the method!
        if(!Type.assignmentCompatible(returnType, rs.getReturnValue().type)) {
            ErrorBuilder eb = handler.createErrorBuilder(TypeError.class).addLocation(rs);
            if(method.isTopLevelDecl()) {
                eb.addErrorNumber(MessageNumber.TYPE_ERROR_427)
                  .addErrorArgs(method, rs.getReturnValue().type, returnType)
                  .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1427)
                  .generateError();
            } else {
                eb.addErrorNumber(MessageNumber.TYPE_ERROR_428)
                  .addErrorArgs(method,
                          method.asClassNode().asMethodDecl().getClassDecl(), rs.getReturnValue().type, returnType)
                  .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1428)
                  .generateError();
            }
        }
    }

//    /**
//     * Evaluates the type of a retype statement.
//     * @param rt Retype Statement
//     */
//    public void visitRetypeStmt(RetypeStmt rt) {
//        rt.getName().visit(this);
//        Type objType = rt.getName().type;
//
//        // ERROR CHECK #1: Make sure the LHS does represent an object
//        if(!objType.isClassOrMultiType()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(rt)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_441)
//                    .addErrorArgs(rt.getName(),objType)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1438)
//                    .generateError();
//        }
//
//        rt.getNewObject().visit(this);
//        ClassType newObjType = rt.getNewObject().type.asClassType();
//        Type objBaseType = objType.isMultiType() ? objType.asMultiType().getInitialType() : objType;
//
//        // ERROR CHECK #2: Make sure the types are class assignment compatible
//        if(!ClassType.classAssignmentCompatibility(objBaseType,newObjType)) {
//            ClassDecl cd = currentScope.findName(objBaseType.toString()).getDecl().asTopLevelDecl().asClassDecl();
//
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(rt)
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_442)
//                    .addErrorArgs(rt.getName(),newObjType)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1439)
//                    .addSuggestionArgs(rt.getName())
//                    .generateError();
//        }
//
//        if(inControlStmt) {
//            if(objType.isMultiType())
//                objType.asMultiType().addType(newObjType);
//            else {
//                MultiType mt = MultiType.create(objType.asClassType(),newObjType);
//                setVarType(rt.getName().toString(),mt);
//            }
//        }
//        else
//            setVarType(rt.getName().toString(),newObjType);
//    }

    /**
     * TODO: hmmm?
     * Evaluates the type of a reference to ThisStmt
     * <p>
     *     If we have a <code>this</code> written in the code, then the
     *     type will be evaluated to be whatever the current class is.
     * </p>
     * @param ts {@link ThisStmt}
     */
    public void visitThis(ThisStmt ts) { ts.type = new ClassType(currentClass.toString()); }

    /**
     * Evaluates the unary expression's type.
     * <p>
     *     In C Minor, there are only two unary operators. The following is
     *     how we will perform the type checking.
     *     <ol>
     *         <li>
     *             '~'
     *             <ul>
     *                 <li>Operand Type: Discrete</li>
     *                 <li>Unary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *         <li>
     *             'not'
     *             <ul>
     *                 <li>Operand Type: Bool</li>
     *                 <li>Unary Expression Type: Bool</li>
     *             </ul>
     *         </li>
     *     </ol>
     *     Both unary operators may also be overloaded. If the unary expression
     *     contains a class type, then we will let {@link #visitFieldExpr(FieldExpr)} handle
     *     the type checking for us.
     * </p>
     * @param ue {@link UnaryExpr}
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.getExpr().visit(this);

        /*
            If the unary expression evaluates to be an object, then we might have a
            unary operator overload. Thus, we are going to create a field expression
            to replace the current unary expression and let other visitors handle the
            type checking for us.
        */
        if(ue.getExpr().type.isClassOrMulti()) {
            FieldExpr unaryOverload =
                new FieldExprBuilder()
                    .setTarget(ue.getExpr())
                    .setAccessExpr(
                        new InvocationBuilder()
                            .setName(new Name("operator" + ue.getUnaryOp()))
                            .create()
                    )
                    .create();
            ue.replaceWith(unaryOverload);
            unaryOverload.visit(this);
            return;
        }

        switch(ue.getUnaryOp().getUnaryType()) {
            case BNOT:
                // ERROR CHECK #1: A bitwise negation can only occur with a discrete type.
                if(!ue.getExpr().type.isDiscrete()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(ue)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_408)
                           .addErrorArgs(ue.getUnaryOp(), ue.getExpr().type)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1410)
                           .addSuggestionArgs(ue.getUnaryOp())
                           .generateError();
                }

                ue.type = new DiscreteType(Scalars.BOOL);
                break;
            case NOT:
                // ERROR CHECK #2: A 'not' operation can only occur on a boolean expression.
                if(!ue.getExpr().type.isBool()) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(ue)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_408)
                           .addErrorArgs(ue.getUnaryOp(),ue.getExpr().type)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1411)
                           .addSuggestionArgs(ue.getUnaryOp())
                           .generateError();
                }

                ue.type = new DiscreteType(Scalars.BOOL);
        }
    }

    /**
     * Verifies the type of the while loop's condition.
     * <p>
     *     The conditional expression for a while loop must evaluate to be a
     *     boolean. This is the only type-related check performed with this visit.
     * </p>
     * @param ws {@link WhileStmt}
     */
    public void visitWhileStmt(WhileStmt ws) {
        ws.getCondition().visit(this);

        // ERROR CHECK #1: The while loop's condition must evaluate to be a boolean.
        if(!ws.getCondition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(ws.getCondition())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_411)
                   .addErrorArgs(ws.getCondition().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1417)
                   .generateError();
        }

        currentScope = ws.scope;
        ws.getBody().visit(this);
        currentScope = currentScope.closeScope();
    }

    /**
     * An internal helper class for {@link TypeChecker}.
     */
    private class TypeCheckerHelper {

        /**
         * Creates a default value for a variable.
         * <p>
         *     When we visit any variable declaration, we will check if the variable was
         *     marked as {@code uninit}. This is determined by seeing if the variable contains
         *     a default value. If there is no default value, then we need to call this method,
         *     so we can generate a default value for the user. For all scalars (outside Enum),
         *     a default value will be generated. All other types will not have a default value.
         * </p>
         * @param type The {@link Type} we want to generate a default value for
         * @return {@link Literal} representing the default value or {@code null} if not applicable.
         */
        private Literal generateDefaultValue(Type type) {
            if(type.isInt())
                return new Literal(ConstantType.INT, "0");
            else if(type.isChar())
                return new Literal(ConstantType.CHAR, "");
            else if(type.isBool())
                return new Literal(ConstantType.BOOL, "False");
            else if(type.isReal())
                return new Literal(ConstantType.REAL, "0.0");
            else if(type.isString())
                return new Literal(ConstantType.STR, "");
            else if(type.isText())
                return new Literal(ConstantType.TEXT, "");
            else
                return null;
        }
//
//        /**
//         * Checks if an array literal is assignment compatible with an array type.<br><br>
//         * <p>
//         *     ThisStmt is a recursive algorithm to verify whether an array literal can
//         *     be assigned to an array type in C Minor. ThisStmt algorithm was based off
//         *     a similar algorithm found in Dr. Pedersen's textbook for compilers.
//         * </p>
//         * @param depth Current level of recursion (final depth is 1)
//         * @param baseType Array type
//         * @param dims Expressions representing the dimensions for the array
//         * @param currArr Array Literal aka the current array literal we are checking
//         * @return Boolean - True if assignment compatible and False otherwise
//         */
//        private boolean arrayAssignmentCompatibility(int depth, Type baseType, Vector<Expression> dims, ArrayLiteral currArr) {
//            if(depth == 1) {
//                // ERROR CHECK #1: This makes sure the user only specified one dimension for a 1D array.
//                if(currArr.getArrayDims().size() > 1) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(currArr.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_455)
//                            .generateError();
//                    return false;
//                }
//
//                if(currArr.getArrayDims().size() == 1)
//                    checkArrayDims(depth, dims, currArr.getArrayDims().get(0), currArr);
////            else if(!dims.isEmpty()) {
////                if(dims.get(dims.size()-depth).asLiteral().) != currArr.getArrayInits().size()) {
////                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
////                            .addLocation(currArr)
////                            .addErrorType(MessageType.TYPE_ERROR_444)
////                            .error());
////                    return false;
////                }
////            }
//
//                for(Expression init : currArr.getArrayInits()) {
//                    init.visit(this);
//
//                    // ERROR CHECK #2: For every initial value in the array, we check to make sure the
//                    //                 value's type is assignment compatible with the array's base type.
//                    if(!Type.assignmentCompatible(baseType,init.type)) {
//                        handler.createErrorBuilder(TypeError.class)
//                                .addLocation(currArr)
//                                .addErrorNumber(MessageNumber.TYPE_ERROR_459)
//                                .addErrorArgs(init.type,baseType)
//                                .generateError();
//                        return false;
//                    }
//                }
//
//                currArr.type = new ArrayTypeBuilder()
//                        .setMetaData(currArr)
//                        .setBaseType(baseType)
//                        .setNumOfDims(depth)
//                        .create();
//                return true;
//            }
//            else if(depth > 1) {
//                ArrayLiteral al = currArr.asArrayLiteral();
//
//                // ERROR CHECK #3: For all n-dimensional array literals (where n>1), we need to make sure the user
//                //                 explicitly writes down the size given for each possible dimension.
//                if(al.getArrayDims().size() != depth) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(al.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_460)
//                            .generateError();
//                    return false;
//                }
//
//                for(Expression dim : al.getArrayDims())
//                    checkArrayDims(depth,dims,dim,currArr);
//
//                for(Expression init : al.getArrayInits()) {
//                    // ERROR CHECK #4: For every initial value in the multidimensional array, we need to make
//                    //                 sure the initial value is an array itself.
//                    if(!init.isArrayLiteral()) {
//                        handler.createErrorBuilder(TypeError.class)
//                                .addLocation(currArr)
//                                .addErrorNumber(MessageNumber.TYPE_ERROR_461)
//                                .generateError();
//                        return false;
//                    }
//
//                    arrayAssignmentCompatibility(depth-1,baseType,dims,init.asArrayLiteral());
//                }
//
//                currArr.type = new ArrayTypeBuilder()
//                        .setMetaData(currArr)
//                        .setBaseType(baseType)
//                        .setNumOfDims(depth)
//                        .create();
//                return true;
//            }
//            else
//                return false;
//        }
//
//        private boolean checkArrayDims(int depth, Vector<Expression> dims, Expression dim,ArrayLiteral currArr) {
//            dim.visit(this);
//
//            // ERROR CHECK #1: The given array dimension has to be an Int constant since the size of the array
//            //                 must be known at compile-time. An Int constant in this context is either an Int
//            //                 literal or a global Int constant.
//            if(!dim.type.isInt() || (!dim.isLiteral() && !isGlobalConstant(dim))) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(dim.getFullLocation())
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_456)
//                        .addErrorArgs(dim)
//                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1448)
//                        .generateError();
//                return false;
//            }
//
//            int dimValue;
//            if(dim.isLiteral())
//                dimValue = dim.asLiteral().asInt();
//            else
//                dimValue = currentScope.findName(dim).getDecl().asTopLevelDecl().asGlobalDecl().getInitialValue().asLiteral().asInt();
//
//            // yeah idk what this is
//            // TYPE_ERROR_446 = Innermost array literal dimension must match the outermost array literal dimension.
//
//            // ERROR CHECK #9:
//            //if(dims.get(dims.size()-currDepth).asListLiteral().toString().equals())
////        if(Integer.parseInt(dims.get(dims.size()-depth).asLiteral().toString()) != Integer.parseInt(al.getArrayDims().get(0).toString())) {
////            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
////                    .addLocation(currArr.getRootParent())
////                    .addErrorType(MessageType.TYPE_ERROR_457)
////                    .error());
////            return false;
////        }
////
////        if(Integer.parseInt(dims.get(dims.size()-depth).asLiteral().toString()) != currArr.getArrayInits().size()) {
////            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
////                    .addLocation(currArr)
////                    .addErrorType(MessageType.TYPE_ERROR_444)
////                    .error());
////            return false;
////        }
//
//            // ERROR CHECK #2: This checks if the user correctly initialized the array based on its size.
//            if(currArr.getArrayInits().size() > dimValue) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(currArr.getFullLocation())
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_458)
//                        .addErrorArgs(dimValue, currArr.getArrayInits().size())
//                        .generateError();
//                return false;
//            }
//
//            return true;
//        }
//
//        /**
//         * Checks if a given {@link Expression} represents a global constant.
//         * <p><br>
//         *     This is a helper method for to
//         *     determine if a given array dimension actually represents a global constant declared
//         *     by the user. Since constants will always have the same value, this means the compiler
//         *     can definitively know the size of an array.
//         * </p>
//         * @param expr Expression
//         * @return Boolean
//         */
//        private boolean isGlobalConstant(Expression expr) {
//            if(!expr.isNameExpr())
//                return false;
//
//            AST decl = currentScope.findName(expr.asNameExpr()).getDecl();
//            if(!decl.isTopLevelDecl())
//                return false;
//            else if(!decl.asTopLevelDecl().isGlobalDecl())
//                return false;
//            else
//                return decl.asTopLevelDecl().asGlobalDecl().isConstant();
//        }
//

        /**
         * Checks if a return statement is guaranteed to be executed.
         * <p>
         *     Without control flow graphs, we need to manually determine
         *     if a return statement will be executed.
         * </p>
         * @param rs The {@link ReturnStmt} we wish to validate.
         * @param method The {@link AST} representing the function or method the return statement is found in.
         */
        private void isReturnStmtValid(ReturnStmt rs, AST method) {
            if(!rs.isInsideControlFlow()) {
                if(method.isTopLevelDecl()) {
                    if(method.asTopLevelDecl().isFuncDecl())
                        method.asTopLevelDecl().asFuncDecl().setIfReturnStmtFound();
                    else
                        method.asTopLevelDecl().asMainDecl().setIfReturnStmtFound();
                }
                else
                    method.asClassNode().asMethodDecl().setIfReturnStmtFound();
            }
        }

        /**
         * Generates a {@link ListType} based on a passed {@link ListLiteral}
         * <p>
         *     This is a helper method for {@link #visitListLiteral(ListLiteral)}. The goal is
         *     to generate a {@link ListType} that is solely determined by the {@link ListLiteral}
         *     we are currently type checking. We are not going to reference any variables in order
         *     to avoid unnecessary dependencies! This will dynamically create a {@link ListType} for
         *     all possible {@link ListLiteral} no matter the dimensions.
         * </p>
         * @param lst The current {@link ListLiteral} we wish to generate a {@link ListType} for.
         * @return {@link ListType}
         */
        private ListType generateListType(ListLiteral lst) {
            ListTypeBuilder builder = new ListTypeBuilder();

            // By default, an empty list will be Void and have a single dimension
            if(lst.getInits().isEmpty()) {
                return builder.setBaseType(new VoidType())
                              .setNumOfDims(1)
                              .create();
            }

            /*
                ERROR CHECK #1: We need to make sure each initial value is assignment compatible with each other.
                                We will use the type of the first initial value in the list literal to do this check.
            */
            Type baseType = lst.getInits().getFirst().type;

            for(Expression init : lst.getInits()) {
                if(!Type.assignmentCompatible(baseType,init.type)) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(lst)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_447)
                           .addErrorArgs(baseType, init.type)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1444)
                           .addSuggestionArgs(baseType)
                           .generateError();
                }
            }

            if(baseType.isList()) {
                builder.setBaseType(baseType.asList().getBaseType())
                       .setNumOfDims(baseType.asList().getDims() + 1);
            } else {
                builder.setBaseType(baseType)
                       .setNumOfDims(1);
            }

            return builder.create();
        }

//        private FuncDecl findSpecificFunction(FuncDecl candidate, FuncDecl currentTemplate, Invocation in) {
//            // If we do not have a possible template function, then the candidate will default to be the candidate
//            if(currentTemplate == null)
//                return candidate;
//
//            // We will now do a type analysis of the candidate's parameters with the invocation's arguments.
//            for(int i = 0; i < candidate.getParams().size(); i++) {
//                ParamDecl candidateParam = candidate.getParams().get(i);
//                ParamDecl templateParam = currentTemplate.getParams().get(i);
//                Expression currArg = in.getArgs().get(i);
//
//
//                // If the candidate's current parameter is assignment compatible with the current argument, this means we
//                // might have a more specific template to use. If we can confirm
//                if(Type.assignmentCompatible(candidateParam.getType(), currArg.type)) {
//                    if(templateParam.isParameterTemplated(currentTemplate.getTypeParams()))
//                        return candidate;
//                }
//            }
//
//            return currentTemplate;
//        }
//
//        private FuncDecl findValidFuncTemplate(String funcName, Invocation in) {
//            final SymbolTable rootTable = currentScope.getRootTable();
//            FuncDecl template = null;
//
//            for(FuncDecl candidate: currentScope.getAllFuncNames()) {
//                // First, we need to make sure the current candidate represents a template function
//                // and the candidate matches the name of the function that is called.
//                if(!candidate.isTemplate() || !candidate.toString().equals(funcName))
//                    continue;
//
//                // Next, we need to make sure the candidate parameter count matches the argument count
//                // If it doesn't, then we know this candidate can be eliminated.
//                if(candidate.getParams().size() != in.getArgs().size())
//                    continue;
//
//                template = findSpecificFunction(candidate,template,in);
//            }
//
//            return template;
//        }
//
//        /**
//         * Verifies the validity of a template function call.
//         * <p><br>
//         *     If a user writes a template function call, then this method will perform the
//         *     necessary error checks to ensure the template function call was written correctly.
//         *     This will allow us to instantiate the function and then have the {@link TypeChecker}
//         *     check if all types can be resolved correctly. This method is identical to the one
//         *     found in {@link micropasses.TypeValidityPass} for validating template types, but
//         *     this method produces different error messages.
//         * </p>
//         * @param fd Current template function
//         * @param in The template {@link Invocation} we want to check if it's been written correctly
//         */
//        public void checkIfFuncTemplateCallIsValid(FuncDecl fd, Invocation in) {
//            // ERROR CHECK #1: When a template type is written, we want to make sure the correct number of
//            //                 type arguments were passed. This will be based on the number of type parameters
//            //                 the template function was declared with. There are 2 possible errors here.
//            if(fd.getTypeParams().size() != in.getTypeArgs().size()) {
//                // Case 1: This error is generated when a user writes type arguments for a non-template function.
//                if(fd.getTypeParams().isEmpty()) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(in.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_462)
//                            .addErrorArgs(fd)
//                            .generateError();
//                }
//                // Case 2: This error is generated when the wrong number of type arguments were used for a template function.
//                else {
//                    ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                            .addLocation(in.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_463)
//                            .addErrorArgs(in.getSignature())
//                            .addSuggestionArgs(fd,fd.getTypeParams().size());
//
//                    if(fd.getTypeParams().size() == 1)
//                        eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1449).generateError();
//                    else
//                        eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1450).generateError();
//                }
//            }
//
//            // We now look through each type parameter of the template function.
//            for (int i = 0; i < fd.getTypeParams().size(); i++) {
//                TypeParam typeParam = fd.getTypeParams().get(i);
//
//                // ERROR CHECK #2: If a user prefixed the type parameter with a type annotation, then we will check if
//                //                 the passed type argument can be used in the current type argument. If no type annotation
//                //                 was given, this check is not needed, and we will let the type checker handle the rest.
//                if(!typeParam.isValidTypeArg(in.getTypeArgs().get(i))) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(in.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_446)
//                            .addErrorArgs(in.getTypeArgs().get(i), fd)
//                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1451)
//                            .addSuggestionArgs(fd, typeParam.getPossibleType(), i + 1)
//                            .generateError();
//                }
//            }
//        }

    }
}
