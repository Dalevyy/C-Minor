package cminor.typechecker;

// TODO: Remember since you are doing List[Void] internally, you need to make sure the user doesn't write that as a valid type. ?

import cminor.ast.AST;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.expressions.FieldExpr.FieldExprBuilder;
import cminor.ast.expressions.Invocation.InvocationBuilder;
import cminor.ast.expressions.Literal.ConstantType;
import cminor.ast.expressions.Literal.LiteralBuilder;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.Name;
import cminor.ast.misc.Var;
import cminor.ast.operators.BinaryOp.BinaryType;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.ast.types.*;
import cminor.ast.types.ArrayType.ArrayTypeBuilder;
import cminor.ast.types.ListType.ListTypeBuilder;
import cminor.ast.types.ScalarType.Scalars;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.ErrorBuilder;
import cminor.messages.errors.scope.ScopeError;
import cminor.messages.errors.type.TypeError;
import cminor.namechecker.NameChecker;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

public class TypeChecker extends Visitor {

    /**
     * Current scope we are resolving types in.
     */
    private SymbolTable currentScope;

    /**
     * Instance of {@link TypeCheckerHelper} that will be used for additional name checking tasks.
     */
    private final TypeCheckerHelper helper;

    /**
     * Creates the {@link TypeChecker} in compilation mode
     */
    public TypeChecker() {
        this.handler = new MessageHandler();
        this.helper = new TypeCheckerHelper();
    }

    /**
     * Creates the {@link TypeChecker} in interpretation mode
     * @param globalScope {@link SymbolTable} representing the VM's global scope.
     */
    public TypeChecker(SymbolTable globalScope) {
        this();
        this.currentScope = globalScope;
    }

    /**
     * Evaluates the array expression's type.
     * <p>
     *     Array expressions are how users access elements within an array or list.
     *     This check ensures that we are able to properly evaluate an array expression
     *     in order to correctly access the element we need at runtime. Note: At this
     *     moment, we won't care about whether any indices will result in a segfault.
     *     We will do this check in the {@link cminor.interpreter.Interpreter}.
     * </p>
     * @param ae {@link ArrayExpr}
     */
    public void visitArrayExpr(ArrayExpr ae) {
        ae.getArrayTarget().visit(this);

        // ERROR CHECK #1: This checks if the target represents a valid Array or List type.
        if(!ae.getArrayTarget().type.isArray() && !ae.getArrayTarget().type.isList()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(ae)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_453)
                   .addErrorArgs(ae.getArrayTarget())
                   .generateError();
        }

        Type lst = helper.getTargetType(ae.getArrayTarget());
        if(lst == null) {
            ClassDecl cd = currentScope.findName(ae.getArrayTarget().getTargetType()).asTopLevelDecl().asClassDecl();
            if(ae.getArrayTarget().isFieldExpr())
                lst = cd.getScope().findName(ae.getArrayTarget().asFieldExpr().getAccessExpr()).asClassNode().asFieldDecl().getType();
            else
                lst = cd.getScope().findName(ae.getArrayTarget()).asClassNode().asFieldDecl().getType();
        }

        if(lst.isArray()) {
            // ERROR CHECK #2: This checks if the number of indices exceeds the number of dimensions for the array.
            if(ae.getArrayIndex().size() > lst.asArray().getDims()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ae)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_448)
                       .addErrorArgs("array", ae.getArrayTarget(), lst.asArray().getDims(), ae.getArrayIndex().size())
                       .generateError();
            }
        }
        else {
            // ERROR CHECK #3: Same as the previous error check, but for lists instead.
            if(ae.getArrayIndex().size() > lst.asList().getDims()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ae)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_448)
                       .addErrorArgs("list", ae.getArrayTarget(), lst.asList().getDims(), ae.getArrayIndex().size())
                       .generateError();
            }
        }

        for(Expression index : ae.getArrayIndex()) {
            index.visit(this);

            // ERROR CHECK #3: Each index value must represent an Int.
            if(!index.type.isInt()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ae)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_454)
                       .addErrorArgs(index.type)
                       .generateError();
            }
        }

        // We will allow users to access subarrays and sublists, so we want to make sure the type
        // we use for the array expression matches the expected type of the element that is accessed.
        if(lst.isList()) {
            if(lst.asList().getDims() != ae.getArrayIndex().size()) {
                ae.type = new ListTypeBuilder()
                              .setBaseType(lst.asList().getBaseType())
                              .setNumOfDims(ae.getArrayIndex().size())
                              .create();
            } else
                ae.type = lst.asList().getBaseType();
        }
        else {
            if (lst.asArray().getDims() != ae.getArrayIndex().size()) {
                ae.type = new ArrayTypeBuilder()
                        .setBaseType(lst.asArray().getBaseType())
                        .setNumOfDims(ae.getArrayIndex().size())
                        .create();
            } else
                ae.type = lst.asArray().getBaseType();
        }
    }

    /**
     * Creates a type for an {@link ArrayLiteral}.
     * <p>
     *     When an array is created by the user, we will perform a visit on its dimensions alongside its
     *     initial values. From there, we will call {@link TypeCheckerHelper#generateType(ArrayLiteral)}
     *     to make sure all the initial values are assignment compatible before we generate
     *     the {@link ArrayType} that the current array will represent.
     * </p>
     * @param al {@link ArrayLiteral}
     */
    public void visitArrayLiteral(ArrayLiteral al) {
        for(Expression dim : al.getArrayDims()) {
            dim.visit(this);
            // ERROR CHECK #1: The expression has to represent an Int literal or global constant.
            if (!helper.isValidDimension(dim)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(al)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_456)
                       .addErrorArgs(dim)
                       .generateError();
            }
        }

        for(Expression init : al.getArrayInits())
            init.visit(this);

        al.type = helper.generateType(al);
    }

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
                                .setName(new NameExpr("operator" + be.getBinaryOp()))
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

    /**
     * Visits a class.
     * <p>
     *     There are no specific type checks done during this visit. We will
     *     let other visits handle type checking for us. All we do here is make
     *     sure a template class is not type checked since we will wait until a
     *     user instantiates the class.
     * </p>
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        // Do not type check a template class until it is instantiated!
        if(cd.isTemplate())
            return;

        currentScope = cd.getScope();
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    /**
     * Begins the type checking phase in compilation mode.
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        for(ImportDecl id : cu.getImports())
            cu.getCompilationUnit().visit(this);

        currentScope = cu.getScope();
        super.visitCompilationUnit(cu);
    }

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

    /**
     * Checks if the user properly wrote an enumeration.
     * <p>
     *     This visit verifies how a user initialized an enumeration and ensures there are
     *     no problems with the types used. Currently, only Int and Char literals are allowed
     *     to be used for enumerations.
     * </p>
     * @param ed {@link EnumDecl}
     */
    public void visitEnumDecl(EnumDecl ed) {
        int initCount = 0;
        for(Var constant : ed.getConstants()) {
            // If the constant was not initialized, move on to the next constant.
            if(!constant.hasInitialValue())
                continue;

            initCount++;
            constant.getInitialValue().visit(this);

            // ERROR CHECK #1: A constant in an Enum can only be assigned Int or Char values.
            if(!constant.getInitialValue().type.isInt() && !constant.getInitialValue().type.isChar()) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ed)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_436)
                       .addErrorArgs(ed, constant.getInitialValue().type)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1434)
                       .generateError();
            }
        }

        // If no constants were initialized, then we will use default Int values for each constant.
        if(initCount == 0) {
            int currValue = 1;
            for(Var constant : ed.getConstants()) {
                constant.setInitialValue(
                    new LiteralBuilder()
                        .setConstantKind(ConstantType.INT)
                        .setValue(String.valueOf(currValue))
                        .create()
                );

                currValue++;
                constant.getInitialValue().visit(this);
            }
        }
        // ERROR CHECK #2: The user has to initialize each constant in the Enum if one constant was initialized.
        else if(initCount != ed.getConstants().size()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(ed)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_437)
                   .addErrorArgs(ed)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1435)
                   .generateError();
        }
        else {
            Type mainType = ed.getConstants().getFirst().getInitialValue().type;
            for(Var constant : ed.getConstants()) {
                // ERROR CHECK #3: We will make sure each constant type is the same!
                if(!Type.assignmentCompatible(mainType, constant.getInitialValue().type)) {
                    handler.createErrorBuilder(TypeError.class)
                           .addLocation(constant)
                           .addErrorNumber(MessageNumber.TYPE_ERROR_438)
                           .addErrorArgs(constant,ed,constant.getInitialValue().type, mainType)
                           .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1436)
                           .generateError();
                }
            }
        }
    }

    /**
     * Generates a default value for a {@link FieldDecl}.
     * <p>
     *     We will create a default value for each {@link FieldDecl} declared in a class. This
     *     will be used by the {@link cminor.micropasses.ConstructorGenerator} to correctly initialize
     *     any fields that are not explicitly instantiated by a user when writing a {@link NewExpr}.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) { fd.setInitialValue(helper.generateDefaultValue(fd.getType())); }

    /**
     * Evaluates the field expression's type.
     * <p>
     *     During this visit, we are concerned with making sure that each target located in
     *     a field expression represents an object. We can then use this information to perform
     *     other type checks related to the field expression.
     * </p>
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.getTarget().visit(this);
        // ERROR CHECK #1: The target has to always evaluate to be an object!
        if(!fe.getTarget().type.isClassOrMulti()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(fe)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_435)
                   .addErrorArgs(fe.getTarget(),fe.getTarget().type)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1433)
                   .generateError();
        }

        fe.getAccessExpr().visit(this);
        fe.type = fe.getAccessExpr().type;
    }

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
        // Create the type argument signature for the current invocation.
        for(Expression arg :in.getArgs()) {
            arg.visit(this);
            in.addTypeSignature(arg.type.typeSignature());
        }

        // Find the correct scope to check for the method name.
        SymbolTable lookup;

        // I think it's cleaner to separate the invocations like this...
        // Function Case
        if(!in.isMethodInvocation()) {
            lookup = currentScope.getGlobalScope();

            // ERROR CHECK #1: This checks if the function was defined somewhere in the global scope.
            if(!lookup.hasMethodName(in.getName())) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(in)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_325)
                       .addErrorArgs(in.getName())
                       .generateError();
            }

            // ERROR CHECK #2: This checks if a valid function overload exists for the given argument signature.
            if(!lookup.hasMethodOverload(in.getName(), in.getSignature())) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(in)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_429)
                       .addErrorArgs(in.getName())
                       .generateError();
            }

            FuncDecl fd = lookup.findMethod(in.getName().toString(), in.getSignature()).asTopLevelDecl().asFuncDecl();
            in.type = fd.getReturnType();
        }
        // Method Case
        else {
            // If the target represents multiple types, then we need to search through each target
            // to see if there is at least one class that contains a valid method for this call!
            if(in.getTargetType().isMulti()) {
                for(ClassType ct : in.getTargetType().asMulti().getAllTypes()) {
                    lookup = currentScope.findName(ct).asTopLevelDecl().asClassDecl().getScope();
                    if(lookup.hasMethodName(in.getName()) && lookup.hasMethodOverload(in.getName(), in.getSignature())) {
                        MethodDecl md = lookup.findMethod(in.getName().toString(), in.getSignature())
                                              .asClassNode().asMethodDecl();
                        in.type = md.getReturnType();
                        return;
                    }
                }
                // ERROR CHECK #1: If no methods were found, then output an error!
                handler.createErrorBuilder(TypeError.class).addErrorNumber(MessageNumber.TYPE_ERROR_471).generateError();
            }
            lookup = currentScope.findName(in.getTargetType().getTypeName()).asTopLevelDecl().asClassDecl().getScope();

            // ERROR CHECK #1: This checks if the method was defined somewhere in the class scope.
            if(!lookup.hasMethodName(in.getName())) {
                handler.createErrorBuilder(ScopeError.class)
                       .addLocation(in)
                       .addErrorNumber(MessageNumber.SCOPE_ERROR_327)
                       .addErrorArgs(in.getName(),in.getTargetType())
                       .generateError();
            }

            // ERROR CHECK #2: This checks if a valid method overload exists for the given argument signature.
            if(!lookup.hasMethodOverload(in.getName(), in.getSignature())) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(in)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_430)
                        .addErrorArgs(in.getName())
                        .generateError();
            }

            MethodDecl md = lookup.findMethod(in.getName().toString(), in.getSignature()).asClassNode().asMethodDecl();
            in.type = md.getReturnType();
        }
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
     *     the list. From there, we will call {@link TypeCheckerHelper#generateType(ListLiteral)}
     *     to make sure all the initial values are assignment compatible before we generate
     *     the {@link ListType} that the current list will represent.
     * </p>
     * @param ll {@link ListLiteral}
     */
    public void visitListLiteral(ListLiteral ll) {
        for(Expression init : ll.getInits())
            init.visit(this);

        ll.type = helper.generateType(ll);
    }

    /**
     * Evaluates the list statement's type.
     * <p>
     *     In C Minor, there are currently 3 list commands: {@code append}, {@code insert},
     *     and {@code remove}. This method will type check all arguments passed to the command
     *     to ensure the user correctly wrote the command. For {@code remove}, we will not type
     *     check the value that is removed since we will throw a runtime exception if the value
     *     can not be removed from the list.
     * </p>
     * @param ls {@link ListStmt}
     */
    public void visitListStmt(ListStmt ls) {
        // SPECIAL CASE!!!
        // If a user writes their own function/method using the list command name, we want to rewrite
        // the current list statement as an invocation for better type checking.
        if(currentScope.hasMethodName(ls)) {
            StringBuilder commandSignature = new StringBuilder(ls + "/");

            for(Expression e : ls.getAllArgs()) {
                e.visit(this);
                commandSignature.append(e.type.typeSignature());
            }

            if(currentScope.hasMethodOverload(ls,commandSignature.toString())) {
                Invocation in = new InvocationBuilder()
                                .setMetaData(ls)
                                .setName(new NameExpr(ls.toString()))
                                .setArgs(ls.getAllArgs())
                                .create();
                ls.replaceWith(in);
                in.visit(this);

                // This is needed because the list statement reference will still be stored by the VM
                // if the list statement was the only line of code the user wrote. We want to call the newly
                // created invocation and not the list statement, so this is the best solution I came up with...
                if(ls.getFullLocation() == null)
                    ls.setInvocation(in);

                return;
            }
        }

        // ERROR CHECK #1: This ensures the correct number of arguments were passed to the list command.
        //                 append/remove => 2 args, insert => 3 args
        if(ls.getAllArgs().size() != ls.getExpectedNumOfArgs()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ls)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_449)
                    .addErrorArgs(ls,ls.getExpectedNumOfArgs(),ls.getAllArgs().size())
                    .generateError();
        }

        ls.getList().visit(this);
        // ERROR CHECK #2: The first argument in a list command must be the list the command acts on.
        if(!ls.getList().type.isList()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ls)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_450)
                    .addErrorArgs(ls.getList(),ls)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1445)
                    .addSuggestionArgs(ls)
                    .generateError();
        }

        ls.getSecondArg().visit(this);
        Type finalArgType = ls.getSecondArg().type;
        // ERROR CHECK #3: The second argument for the insert command must be an integer.
        if(ls.isInsert()) {
            if(!finalArgType.isInt()) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ls)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_451)
                        .addErrorArgs(ls.getAllArgs().get(1))
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1446)
                        .generateError();
            }

            ls.getThirdArg().visit(this);
            finalArgType = ls.getThirdArg().type;
        }

        // ERROR CHECK #4: The final argument for the append/insert command needs to be a value
        //                 that can either be stored or merged into the list.
        if(ls.isAppend() || ls.isInsert()) {
            if(!ls.getListType().isSubList(finalArgType)) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ls)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_452)
                        .addErrorArgs(ls.getList(), ls.getListType(), ls, finalArgType)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1447)
                        .addSuggestionArgs(ls, ls.getList(), ls.getListType(), ls.getListType().validSublist())
                        .generateError();
            }
        }
    }

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
     * Evaluates the type represented by a name.
     * @param ne {@link NameExpr}
     */
    public void visitNameExpr(NameExpr ne) {
        // Special Case: If the name is found inside a complex field expression, then we need to check if the name
        //               is defined in the class. This
        if(ne.inComplexFieldExpr()) {
            helper.checkTargetValidity(ne);
            return;
        }

        // The name's type is based on the declared type.
        AST decl = currentScope.findName(ne);
        if(decl.isClassNode())
            ne.type = decl.asClassNode().asFieldDecl().getType();
        else if(decl.isTopLevelDecl() && decl.asTopLevelDecl().isGlobalDecl())
            ne.type = decl.asTopLevelDecl().asGlobalDecl().getType();
        else if(decl.isTopLevelDecl() && decl.asTopLevelDecl().isClassDecl())
            ne.type = new ClassType(decl.asTopLevelDecl().asClassDecl().getDeclName());
        else if(decl.isTopLevelDecl() && decl.asTopLevelDecl().isEnumDecl())
            ne.type = decl.asTopLevelDecl().asEnumDecl().getConstantType();
        else if(decl.isSubNode())
            ne.type = decl.asSubNode().asParamDecl().getType();
        else
            ne.type = decl.asStatement().asLocalDecl().getType();
    }

    /**
     * Evaluates the new expression's type.
     * <p>
     *     In C Minor, a constructor is automatically generated for the user.
     *     Thus, we do not need to check if a new expression can be called for
     *     the class we are trying to instantiate. Instead, we only need to check
     *     if for each argument, the type of the value corresponds to the type of
     *     the field declaration we're saving the argument into.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd;
        if(ne.createsFromTemplate()) {
            ne.getInstantiatedClass().visit(this);
            cd = ne.getInstantiatedClass();
        }
        else
            cd = currentScope.findName(ne.getClassType()).asTopLevelDecl().asClassDecl();

        for(Var arg : ne.getInitialFields()) {
            Type fieldType = cd.getScope().findName(arg).asClassNode().asFieldDecl().getType();
            arg.getInitialValue().visit(this);

            // ERROR CHECK #1: The type of a field's initial value should match the type of the field variable.
            if(!Type.assignmentCompatible(arg.getInitialValue().type,fieldType)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ne)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_420)
                       .addErrorArgs(arg, fieldType, arg.getInitialValue().type)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                       .addSuggestionArgs(fieldType)
                       .generateError();
            }
        }

        ne.type = ne.getClassType();
    }

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
     * Evaluates the parent keyword's type.
     * <p>
     *     The type of the parent keyword will always be the super
     *     class type that the class inherits from.
     * </p>
     * @param ps {@link ParentStmt}
     */
    public void visitParentStmt(ParentStmt ps) {
        // ERROR CHECK #1?: This checks if the 'parent' keyword was used at the start of a field expression.
        if(!ps.insideFieldExpr() || !ps.wasParentKeywordWrittenCorrectly()) {
            handler.createErrorBuilder(ScopeError.class)
                    .addLocation(ps.getFullLocation())
                    .addErrorNumber(MessageNumber.SCOPE_ERROR_330)
                    .generateError();
        }
        ps.type = ps.getClassDecl().getSuperClass();
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

    /**
     * Evaluates the types of the LHS and RHS of a retype statement.
     * @param rt {@link RetypeStmt}
     */
    public void visitRetypeStmt(RetypeStmt rt) {
        rt.getName().visit(this);
        Type objType = rt.getName().type;

        // ERROR CHECK #1: The LHS has to represent an object.
        if(!objType.isClassOrMulti()) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(rt)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_441)
                   .addErrorArgs(rt.getName(), objType)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1438)
                   .generateError();
        }

        rt.getNewObject().visit(this);
        ClassType newType = rt.getNewObject().type.asClass();
        ClassType baseType = objType.isMulti() ? objType.asMulti().getInitialType() : objType.asClass();

        // ERROR CHECK #2: The LHS and RHS have to be class assignment compatible for the retype to occur!
        if(!helper.classAssignmentCompatibility(baseType, newType)) {
            handler.createErrorBuilder(TypeError.class)
                   .addLocation(rt)
                   .addErrorNumber(MessageNumber.TYPE_ERROR_442)
                   .addErrorArgs(rt.getName(), newType)
                   .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1439)
                   .addSuggestionArgs(rt.getName())
                   .generateError();
        }

        // If the retype statement is found in some form of control flow, then we have to make the object a multitype
        // since we will not know the object's definitive type until the program is executed...
        if(rt.isInsideControlFlow()) {
            if(objType.isMulti())
                objType.asMulti().addType(newType);
            else
                helper.resetVariableType(rt.getName().toString(), MultiType.create(baseType, newType));
        }
        else
            helper.resetVariableType(rt.getName().toString(), newType);
    }

    /**
     * Evaluates the current type of the {@code This} keyword.
     * <p>
     *     The type of {@code This} will always correspond to the class it is located in.
     * </p>
     * @param ts {@link ThisStmt}
     */
    public void visitThis(ThisStmt ts) { ts.type = new ClassType(ts.getClassDecl().toString()); }

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
                            .setName(new NameExpr("operator" + ue.getUnaryOp()))
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

        private Type getTargetType(Expression node) {
            AST decl = currentScope.findName(node);

            if(decl == null)
                return null;

            if(decl.isClassNode())
                return decl.asClassNode().asFieldDecl().getType();
            else if(decl.isTopLevelDecl())
                return decl.asTopLevelDecl().asGlobalDecl().getType();
            else if(decl.isSubNode())
                return decl.asSubNode().asParamDecl().getType();
            else
                return decl.asStatement().asLocalDecl().getType();
        }

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

        /**
         * Checks if a given expression represents a valid array dimension.
         * <p>
         *     The size of an array must be known as compile-time, so only global
         *     constants and Int literals are allowed to be written inside an array
         *     expression.
         * </p>
         * @param dim {@link Expression} representing the dimension we wish to validate.
         * @return {@code True} if the expression represents a valid dimension, {@code False} otherwise.
         */
        private boolean isValidDimension(Expression dim) {
            if(dim.isNameExpr()) {
                AST decl = currentScope.findName(dim);
                if(!decl.isTopLevelDecl() && !decl.asTopLevelDecl().isGlobalDecl())
                    return false;

                return decl.asTopLevelDecl().asGlobalDecl().isConstant();
            }

            return dim.isLiteral() && dim.type.isInt();
        }

        public ArrayType generateType(ArrayLiteral al) {
            ArrayTypeBuilder builder = new ArrayTypeBuilder();

            if(!al.getArrayDims().isEmpty()) {
                if(!isArrayInitializedCorrectly(0,al.getArrayDims(),al)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(al)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_469)
                            .generateError();
                }
            }
            else {
                for(Expression init : al.getArrayInits()) {
                    if(init.isArrayLiteral() && !al.insideArrayLiteral()) {
                        handler.createErrorBuilder(TypeError.class)
                                .addLocation(al)
                                .addErrorNumber(MessageNumber.TYPE_ERROR_468)
                                .generateError();
                    }
                }
            }

            Type baseType = al.getArrayInits().getFirst().type;
            for(Expression init : al.getArrayInits()) {
                if(!Type.assignmentCompatible(baseType,init.type)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(al)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_447)
                            .addErrorArgs(baseType, init.type)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1444)
                            .addSuggestionArgs(baseType)
                            .generateError();
                }
            }

            if(baseType.isArray()) {
                builder.setBaseType(baseType.asArray().getBaseType())
                        .setNumOfDims(baseType.asArray().getDims() + 1);
            } else {
                builder.setBaseType(baseType)
                        .setNumOfDims(1);
            }

            return builder.create();
        }

        private boolean isArrayInitializedCorrectly(int depth, Vector<Expression> dims, ArrayLiteral curr){
            // Base Case 1: If the depth reaches the same size as the dimensions vector, then we have
            //              looked through all dimensions, and no issues were found.
            if(depth == dims.size())
                return true;

            // Retrieve the dimension as an Integer.
            Expression expr = dims.get(depth);
            if(expr.isNameExpr())
                expr = currentScope.findName(expr).asTopLevelDecl().asGlobalDecl().getInitialValue();
            int dim = expr.asLiteral().asInt();

            // ERROR CHECK #1: The dimensions of an array must be a positive Int.
            if(dim < 0) {
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(curr)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_470)
                        .generateError();
            }

            // Base Case 2:
            if(curr.getArrayInits().size() > dim)
                return false;

            for(Expression init : curr.getArrayInits()) {
                if(!init.isArrayLiteral()) {
                    if(depth+1 != dims.size())
                        return false;
                }
                else if(!isArrayInitializedCorrectly(depth+1,dims,init.asArrayLiteral()))
                    return false;
            }

            return true;
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
        private ListType generateType(ListLiteral lst) {
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

        private void resetVariableType(String name, Type type) {
            AST node = currentScope.findName(name);

            if(node.isTopLevelDecl())
                node.asTopLevelDecl().asGlobalDecl().setType(type);
            else if(node.isClassNode())
                node.asClassNode().asFieldDecl().setType(type);
            else if(node.isSubNode())
                node.asSubNode().asParamDecl().setType(type);
            else
                node.asStatement().asLocalDecl().setType(type);
        }

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
         * Checks if a RHS class type can be assigned into a LHS class type.
         * <p>
         *     This is a helper method for {@link #visitRetypeStmt(RetypeStmt)}. This could
         *     be written directly in {@link ClassType}, but the test bank did not like it...
         *     so it's written here instead :(
         * </p>
         * @param LHS The initial {@link ClassType}
         * @param RHS The new {@link ClassType} we wish to replace the LHS with.
         * @return {@code True} if the RHS is assignment compatible with the LHS, {@code False} otherwise.
         */
        private boolean classAssignmentCompatibility(ClassType LHS, ClassType RHS) {
            if(ClassType.isSuperClass(currentScope.getGlobalScope(), LHS, RHS))
                return true;
            else
                return ClassType.isSuperClass(currentScope.getGlobalScope(), RHS, LHS);
        }

        /**
         * Checks if a name inside a complex {@link FieldExpr} can properly be typed.
         * <p>
         *     This helper will handle the checking of all names that are contained with a
         *     complex {@link FieldExpr}. This method will determine the appropriate target
         *     type for the name, and it will ensure the name was found in the target's class
         *     to determine if the complex field expression was written correctly.
         * </p>
         * @param ne The {@link NameExpr} we wish to evaluate the type of.
         */
        private void checkTargetValidity(NameExpr ne) {
            Type target; // Type of the target that accesses the current name.

            // Case 1: The name is found inside an array expression. This is a special case!
            if(ne.inArrayExpr()) {
                // We need to go up 2 levels in the AST to get the field expression the array is in.
                FieldExpr fe = ne.getParent().getParent().asExpression().asFieldExpr();

                // Case 1.1: If the name is the final name in a complex field, then we will get the last target.
                if(fe.getAccessExpr().equals(ne.getParent()))
                    target = fe.getTargetType();
                // Case 1.2: If not, we need to get the target of the previous field.
                else
                    target = fe.getParent().asExpression().asFieldExpr().getTargetType();
            }
            // Case 2: If the name is the final name in a complex field, then get the last target.
            else if(ne.getParent().asExpression().asFieldExpr().getAccessExpr().equals(ne))
                target = ne.getParent().asExpression().getTargetType();
            // Case 3: If the name is not the final name in a complex field, then get the previous target.
            else
                target = ne.getParent().getParent().asExpression().asFieldExpr().getTargetType();

            ClassDecl cd;
            // ERROR CHECK #1: We have to check if the name that the target is accessing was declared
            //                 in the class since we haven't done this error check yet!
            if(target.isClass()) {
                cd = currentScope.findName(target.asClass().getTypeName()).asTopLevelDecl().asClassDecl();
                if(!cd.getScope().hasNameInProgram(ne)) {
                    handler.createErrorBuilder(ScopeError.class)
                           .addLocation(ne.getFullLocation())
                           .addErrorNumber(MessageNumber.SCOPE_ERROR_329)
                           .addErrorArgs(ne,cd)
                           .generateError();
                }
            } else
                cd = validateMultiType(ne.getName(),target.asMulti());

            // The name expression will have the type of the class field that is being accessed.
            ne.type = cd.getScope().findName(ne).asClassNode().asFieldDecl().getType();
        }

        private ClassDecl validateMultiType(Name name, MultiType target) {
            for(ClassType ct : target.getAllTypes()) {
                ClassDecl cd = currentScope.getGlobalScope().findName(ct.getTypeName()).asTopLevelDecl().asClassDecl();

                if(cd.getScope().hasName(name))
                    return cd;
            }

            handler.createErrorBuilder(TypeError.class)
                   .addLocation(name.getFullLocation())
                   .addErrorNumber(MessageNumber.TYPE_ERROR_471)
                   .generateError();

            return null;
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
