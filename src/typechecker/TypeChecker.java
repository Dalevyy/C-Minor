package typechecker;

import ast.*;
import ast.classbody.*;
import ast.expressions.*;
import ast.expressions.Literal.*;
import ast.expressions.Literal.LiteralBuilder;
import ast.misc.Name;
import ast.misc.NameNode;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.*;
import ast.types.DiscreteType.*;
import ast.types.EnumType.EnumTypeBuilder;
import ast.types.ScalarType.*;
import messages.MessageType;
import messages.errors.*;
import messages.errors.scope_error.ScopeErrorFactory;
import messages.errors.type_error.TypeErrorFactory;
import utilities.*;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private ClassDecl currentClass;
    private AST currentContext;
    private Type currentTarget;
    private final TypeErrorFactory generateTypeError;
    private final ScopeErrorFactory generateScopeError;
    private final Vector<String> errors;
    private boolean returnStatementFound = false;

    /**
     * Creates type checker in compilation mode
     */
    public TypeChecker() {
        this.currentScope = null;
        this.currentContext = null;
        this.currentClass = null;
        this.generateTypeError = new TypeErrorFactory();
        this.generateScopeError = new ScopeErrorFactory();
        this.errors = new Vector<>();
    }

    /**
     * Creates type checker in interpretation mode
     * @param st Symbol Table
     */
    public TypeChecker(SymbolTable st) {
        this();
        this.currentScope = st;
        this.interpretMode = true;
    }

    /**
     * Creates a default value for a variable
     * <p>
     *     When we visit any variable declaration, we will check to see if
     *     the variable was initialized. If it wasn't, then we will call this
     *     method to assign a default value to be used during runtime.
     * </p>
     * @param varType Variable Type
     * @return Expression
     */
    private Expression setDefaultValue(Type varType) {
        Expression init;
        if(varType.isInt()) { init = new Literal(ConstantKind.INT, "0"); }
        else if(varType.isChar()) { init = new Literal(ConstantKind.CHAR, ""); }
        else if(varType.isBool()) { init = new Literal(ConstantKind.BOOL, "False"); }
        else if(varType.isReal()) { init = new Literal(ConstantKind.REAL, "0.0"); }
        else if(varType.isString()) { init = new Literal(ConstantKind.STR, ""); }
        else if(varType.isEnumType()){
            EnumDecl ed = currentScope.findName(varType.toString()).decl().asTopLevelDecl().asEnumDecl();
            init = new NameExpr(ed.constants().get(0).toString());
        }
        else { return null; }

        init.visit(this);
        return init;
    }

    /*
        For Array Literals Type Checking
            1. Check rows (Count # of arguments and check if it matches first index)
            2. Make sure it's an array if it's multidimensional
            3. Evaluate how many arguments Array(...) is (same as columns)
                Recursion
    */
    private boolean arrayAssignmentCompatibility(int currDepth, Type t, Vector<Expression> dims, ArrayLiteral curr) {
        if(currDepth == 1) {
            // ERROR CHECK #1: If we are checking a single dimension array, then we
            //                 want to ensure there are not 2 or more dimensions specified
            if(curr.arrayDims().size() > 1) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(curr)
                        .addErrorType(MessageType.TYPE_ERROR_443)
                        .error());
                return false;
            }
            else if(curr.arrayDims().size() == 1) {
                Expression dim = curr.arrayDims().get(0);
                dim.visit(this);

                // ERROR CHECK #2: If a user specified a size for the 1D array, then we want to make sure
                //                 that value is either an integer literal or integer constant
                if(!(dim.type.isInt() && (dim.isLiteral() || dim.isTopLevelDecl() && dim.asTopLevelDecl().asGlobalDecl().isConstant()))) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_435)
                            .error());
                    return false;
                }
                if(Integer.parseInt(dims.get(dims.size()-currDepth).asLiteral().toString()) != Integer.parseInt(dim.asLiteral().toString())) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_446)
                            .error());
                    return false;
                }
                // ERROR CHECK #4: We will also check if the arguments passed into the array
                //                 matches the specified size for the array
                if(Integer.parseInt(dim.asLiteral().toString()) != curr.arrayInits().size()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_444)
                            .error());
                    return false;
                }
            }
            else if(dims.size() != 0) {
                if(Integer.parseInt(dims.get(dims.size()-currDepth).asLiteral().toString()) != curr.arrayInits().size()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_444)
                            .error());
                    return false;
                }
            }

            Vector<Expression> inits = curr.arrayInits();
            for(int i = 0; i < inits.size(); i++) {
                Expression val = inits.get(i);
                val.visit(this);
                // ERROR CHECK #5: For each argument value for the array, we will make sure its
                //                 base type corresponds to the type declared
                if(!Type.assignmentCompatible(t,val.type)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_445)
                            .addArgs(t,val.type)
                            .error());
                    return false;
                }
            }
            return true;
        }
        if(currDepth > 1) {
            ArrayLiteral al = curr.asArrayLiteral();

            // ERROR CHECK #1: For all n-dimensional array literals (where n>1), we need to make sure the user
            //                 explicitly writes down the size given for each possible dimension.
            if(al.arrayDims().size() != currDepth) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(al)
                        .addErrorType(MessageType.TYPE_ERROR_442)
                        .error());
                return false;
            }

            for(int i = 0; i < al.arrayDims().size(); i++) {
                Expression dim = al.arrayDims().get(i);
                dim.visit(this);
                // ERROR CHECK #: Make sure its integer constant
                if(!(dim.type.isInt() && (dim.isLiteral() || dim.isTopLevelDecl() && dim.asTopLevelDecl().asGlobalDecl().isConstant()))) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_435)
                            .error());
                    return false;
                }
            }

            if(Integer.parseInt(dims.get(dims.size()-currDepth).asLiteral().toString()) != Integer.parseInt(al.arrayDims().get(0).toString())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(curr)
                        .addErrorType(MessageType.TYPE_ERROR_446)
                        .error());
                return false;
            }

            if(Integer.parseInt(dims.get(dims.size()-currDepth).asLiteral().toString()) != curr.arrayInits().size()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(curr)
                        .addErrorType(MessageType.TYPE_ERROR_444)
                        .error());
                return false;
            }

            for(int i = 0; i < al.arrayInits().size();i++) {
                if(!al.arrayInits().get(i).isArrayLiteral()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(curr)
                            .addErrorType(MessageType.TYPE_ERROR_447)
                            .error());
                    return false;
                }
                arrayAssignmentCompatibility(currDepth-1,t,dims,al.arrayInits().get(i).asArrayLiteral());
            }
            return true;
        }
        return false;
    }

//    private boolean listAssignmentCompatibility(int currDepth, Type lt, ListLiteral curr) {
//
//    }

    /*
    ______________________ Array Expressions ______________________
    Array expressions are how users can access memory from array.
    First, we make sure the target is an array (or a list) since it
    does not make sense to dereference a non-array type. Then, we
    will make sure the index evaluates to an integer. We will not
    check if the integer is a valid index or not since this needs
    to be done at runtime.
    _______________________________________________________________
    */
    public void visitArrayExpr(ArrayExpr ae) {
        ae.arrayTarget().visit(this);

        // ERROR CHECK #1: Make sure the target represents an array
        if(!ae.arrayTarget().type.isArrayType()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ae)
                        .addErrorType(MessageType.TYPE_ERROR_434)
                        .addArgs(ae.arrayTarget().toString())
                        .error()
            );
        }

        ArrayType at;
        if(currentTarget != null) {
            ClassDecl cd = currentScope.findName(currentTarget.toString()).decl().asTopLevelDecl().asClassDecl();
            at = cd.symbolTable.findName(ae.arrayTarget().toString()).decl().asFieldDecl().type().asArrayType();
        }
        else {
            at = currentScope.findName(ae.arrayTarget().toString()).decl().getType().asArrayType();
        }


        // ERROR CHECK #2: Make sure the number of indices matches
        //                 the number of dimensions for the array
        if(at.numOfDims != ae.arrayIndex().size()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ae)
                        .addErrorType(MessageType.TYPE_ERROR_448)
                        .addArgs(ae.arrayTarget().toString(),at.numOfDims,ae.arrayIndex().size())
                        .error()
            );
        }

        for(Expression e : ae.arrayIndex()) {
            e.visit(this);

            // ERROR CHECK #3: For each index, make sure the
            //                 value evaluates to be an Int
            if(!e.type.isInt()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ae)
                            .addErrorType(MessageType.TYPE_ERROR_430)
                            .addArgs(e.type)
                            .error()
                );
            }
        }
        ae.type = at.baseType();
    }

    /*
    ___________________ Array Literals ___________________
    For array literals, we will call the helper method
    arrayAssignmentCompatible to handle all type checking
    for us.
    ______________________________________________________
    */
    public void visitArrayLiteral(ArrayLiteral al) {
        // If current target doesn't represent an array type, then we'll set the
        // array literal to be some arbitrary array type to prevent type assignment
        // compatibility from leading to an exception
        if(currentTarget == null || !currentTarget.isArrayType())
            al.type = new ArrayType();
        else {
            arrayAssignmentCompatibility(currentTarget.asArrayType().numOfDims,
                    currentTarget.asArrayType().baseType(),
                    al.arrayDims(), al);
            al.type = currentTarget.asArrayType();
        }

    }

    /*
    _________________________ Assignment Statements _________________________
    If we want to assign a new value to a variable, we need to make sure the
    value's type matches the type of the variable.

    C Minor also supports compound assignment operations such as +=, -=, *=,
    etc. which means we have to do an additional check to make sure the two
    values can perform a legal binary operation.
    _________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {

        as.LHS().visit(this);
        Type lType = as.LHS().type;

        as.RHS().visit(this);
        Type rType = as.RHS().type;

        String aOp = as.assignOp().toString();

        // ERROR CHECK #1: Make sure both the variable and value type are the same
        if(!Type.assignmentCompatible(lType,rType)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(as)
                    .addErrorType(MessageType.TYPE_ERROR_402)
                    .addArgs(as.LHS().toString(),lType,rType)
                    .error());
        }

        switch(aOp) {
            case "+=": {
                // ERROR CHECK #2: For a '+=' operation, the only allowed types
                //                 are Int, Real, String, and Object
                if(lType.isBool() || lType.isChar()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(as)
                            .addErrorType(MessageType.TYPE_ERROR_403)
                            .addArgs(aOp,lType)
                            .error());
                }
                break;
            }
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=": {
                // ERROR CHECK #3: For all other assignment operators, the types
                //                 Int, Real, and Object have to be used
                if(lType.isBool() || lType.isChar() || lType.isString()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(as)
                            .addErrorType(MessageType.TYPE_ERROR_403)
                            .addArgs(aOp,lType)
                            .error());
                }
                break;
            }
        }
    }

    /*
    ___________________________ Binary Expressions ___________________________
    Since C Minor does not support type coercion, we are going to be strict
    about which types are allowed for each possible binary operator.

    There are currently 24 binary operators in C Minor. The following is a
    list of each operator:

        1. '=='  '!='
            - Operand Type: Both operands have to be the SAME type
            - Binary Expression Type: Bool

        2. '>'  '>='  '<'  '<='  '<>'  '<=>'
            - Operand Type: Numeric -> Int, Real, Char
            - Binary Expression Type: Bool

        3. '+'  '-'  '*'  '/'  '%'  '**'
            - Operand Type: Numeric -> Int, Real, Char or String (+)
            - Binary Expression Type: Type of both operands

        4. '<<'  '>>'
            - Operand Type: Int
            - Binary Expression Type: Int

        5. '&'   '|'  '^'
            - Operand Type: Discrete
            - Binary Expression Type: Bool or Int (^)

        6. 'and'  'or'
            - Operand Type: Bool
            - Binary Expression Type: Bool

        7. 'instanceof'  '!instanceof'  'as?'
            - Operand Type: Class
            - Binary Expression Type: Bool

    Additionally, most of the binary operators can be overloaded by classes,
    so we will check if the overloaded method was defined here as well.
    __________________________________________________________________________
    */
    public void visitBinaryExpr(BinaryExpr be) {

        be.LHS().visit(this);
        Type lType = be.LHS().type;

        be.RHS().visit(this);
        Type rType = be.RHS().type;

        String binOp = be.binaryOp().toString();

        switch(binOp) {
            case "==":
            case "!=": {
                // ERROR CHECK #1: Both LHS/RHS have to be the same type.
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "<>":
            case "<=>": {
                // ERROR CHECK #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                // ERROR CHECK #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1401)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "+": {
                if(lType.isString() && rType.isString()) {
                    be.type = lType;
                    break;
                }
            }
            case "-":
            case "*":
            case "/":
            case "%":
            case "**": {
                // ERROR CHECK #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be.getParent())
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                // ERROR CHECK #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1402)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                be.type = lType;
                break;
            }
            case "<<":
            case ">>": {
                // ERROR CHECK #1: Both LHS and RHS have to be an INT for shift operations
                if(!lType.isInt() || !rType.isInt()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1403)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "&":
            case "|":
            case "^": {
                // ERROR CHECK #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                // ERROR CHECK #2: Make sure both types are discrete
                if(!lType.isDiscreteType() || !rType.isDiscreteType()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1404)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                if(binOp.equals("^")) { be.type = new DiscreteType(Discretes.INT); }
                else { be.type = new DiscreteType(Discretes.BOOL); }
                break;
            }
            case "and":
            case "or": {
                // ERROR CHECK #1: Make sure both types are Bool
                if(!lType.isBool() || !rType.isBool()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1405)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                if(!lType.isClassType()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(MessageType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1406)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
        }
    }

    /*
    _________________________ Cast Expressions  _________________________
    In C Minor, we have 4 valid cast expressions a programmer can use:

        1. Char <--> Int
        2. Int  <--> Real
        3. Char  --> String
        4. Parent Class Object <-- Child Class Object (Runtime check)

    For mixed type expressions, this means the programmer must perform
    explicit type casts or else the compiler will generate a typing error.
    ______________________________________________________________________
    */
    public void visitCastExpr(CastExpr ce) {
        ce.castExpr().visit(this);
        Type exprType = ce.castExpr().type;
        Type typeToCastInto = ce.castType();

        if(exprType.isInt()) {
            // ERROR CHECK #1: An Int can only be typecasted into a Char and a Real
            if(!typeToCastInto.isChar() && !typeToCastInto.isReal() && !typeToCastInto.isInt()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(MessageType.TYPE_ERROR_408)
                        .error());
            }
        }
        else if(exprType.isChar()) {
            // ERROR CHECK #2: A Char can only be type casted into an Int and a String
            if(!typeToCastInto.isInt() && !typeToCastInto.isString() && !typeToCastInto.isChar()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(MessageType.TYPE_ERROR_409)
                        .error());
            }
        }
        else if(exprType.isReal()) {
            // ERROR CHECK #3: A Real can only be type casted into an Int
            if(!typeToCastInto.isInt() && !typeToCastInto.isReal()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(MessageType.TYPE_ERROR_410)
                        .error());
            }
        }
        else {
            // By default, all other cast expressions will be considered invalid
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ce)
                    .addErrorType(MessageType.TYPE_ERROR_411)
                    .addArgs(exprType,typeToCastInto)
                    .error());
        }

        ce.type = typeToCastInto;
    }

    /*
    _________________________ Choice Statements  _________________________
    When we are visiting a choice statement, there are 2 main type checks
    we have to perform.

    First, we make sure the choice expression is either an Int, Char, or
    a String. Then, we make sure each case's label corresponds to the
    correct type of the choice expression. If this is all valid, then we
    can continue with the compilation process.
    ______________________________________________________________________
    */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);

        currentScope = cs.symbolTable;
        Type choiceType = cs.choiceExpr().type;

        // ERROR CHECK #1: Only allow Ints, Chars, and Strings
        //                 to be switched on
        if(!(choiceType.isInt() || choiceType.isChar() || choiceType.isString())) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(cs.choiceExpr())
                    .addErrorType(MessageType.TYPE_ERROR_426)
                    .addArgs(choiceType)
                    .addSuggestType(MessageType.TYPE_SUGGEST_1409)
                    .error());
        }

        for(int i = 0; i < cs.caseStmts().size(); i++) {
            CaseStmt currCase = cs.caseStmts().get(i);
            currCase.choiceLabel().visit(this);
            Type labelType = currCase.choiceLabel().leftLabel().type;

            // ERROR CHECK #2: Make sure the case label's type corresponds
            //                 to the type of the choice statement expression
            if(!Type.assignmentCompatible(labelType,choiceType)) {
                errors.add(new ErrorBuilder(generateTypeError, interpretMode)
                        .addLocation(currCase.choiceLabel())
                        .addErrorType(MessageType.TYPE_ERROR_427)
                        .addArgs(labelType, choiceType)
                        .addSuggestType(MessageType.TYPE_SUGGEST_1410)
                        .error());
            }

            if(currCase.choiceLabel().rightLabel() != null) {
                // ERROR CHECK #3: If we allow to choose from String values, then
                //                 there is only one label allowed per case statement
                if(choiceType.isString()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(currCase.choiceLabel())
                            .addErrorType(MessageType.TYPE_ERROR_432)
                            .error());
                }

                labelType = currCase.choiceLabel().rightLabel().type;
                // ERROR CHECK #4: Same as ERROR CHECK #2, but now for the right label
                if(!Type.assignmentCompatible(labelType,choiceType)) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(currCase.choiceLabel())
                            .addErrorType(MessageType.TYPE_ERROR_426)
                            .addArgs(labelType,choiceType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1410)
                            .error());
                }

                // ERROR CHECK #5: Make sure the label's right constant is greater than the left constant
                if(choiceType.isInt()) {
                    int lLabel = Integer.valueOf(currCase.choiceLabel().leftLabel().toString());
                    int rLabel = Integer.valueOf(currCase.choiceLabel().rightLabel().toString());
                    if(rLabel <= lLabel) {
                        errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(currCase.choiceLabel())
                                .addErrorType(MessageType.TYPE_ERROR_433)
                                .error());
                    }
                }
                else if(choiceType.isChar()) {
                    char lLabel = currCase.choiceLabel().leftLabel().getText().charAt(1);
                    char rLabel = currCase.choiceLabel().rightLabel().getText().charAt(1);
                    if(rLabel <= lLabel) {
                        errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(currCase.choiceLabel())
                                .addErrorType(MessageType.TYPE_ERROR_433)
                                .error());
                    }
                }
            }
            SymbolTable oldScope = currentScope;
            currentScope = currCase.symbolTable;

            currCase.caseBlock().visit(this);

            currentScope = oldScope;
        }
        if(cs.otherBlock() != null) { cs.otherBlock().visit(this); }
        currentScope = currentScope.closeScope();
    }

    /*
    _________________________ Class Declarations _________________________
    For a class declaration, we will set the class type that represents
    the inheritance hierarchy of the class before we proceed to visit the
    class body.
    ______________________________________________________________________
    */
    public void visitClassDecl(ClassDecl cd) {
        if(cd.superClass() != null) {
            String inheritedClasses = cd.toString();
            String baseClass = cd.superClass().toString();

            // Adding class names to form the class hierarchy for use in type checking
            do {
                inheritedClasses += "/" + baseClass;
                ClassDecl nextClass = currentScope.findName(baseClass).decl().asTopLevelDecl().asClassDecl();
                if(nextClass.superClass() == null) { break; }
                baseClass = nextClass.superClass().getName().toString();
            }   while(currentScope.hasName(baseClass));

            cd.setClassHierarchy(new ClassType(new Name(inheritedClasses)));
        } else { cd.setClassHierarchy(new ClassType(new Name(cd.toString()))); }

        currentScope = cd.symbolTable;
        currentContext = cd;
        currentClass = cd;
        super.visitClassDecl(cd);
        currentContext = null;
        currentClass = null;
        currentScope = currentScope.closeScope();
    }

    /*
    ___________________________ Do Statements ___________________________
    With all looping constructs, we only need to check if the type of the
    loop condition evaluates to be a Bool. That's all we DO here. ;)
    _____________________________________________________________________
    */
    public void visitDoStmt(DoStmt ds) {
        currentScope = ds.symbolTable;
        ds.doBlock().visit(this);
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);

        // ERROR CHECK #1: Make sure Do's condition evaluates to Bool
        if(!ds.condition().type.isBool()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ds.condition())
                    .addErrorType(MessageType.TYPE_ERROR_407)
                    .addArgs(ds.condition().type)
                    .error());
        }
    }

    /**
     *   In C Minor, an enumeration can only store values of type Int and Char for
     *   each constant. Additionally, we are going to be strict and require the
     *   user to initialize all values of the enumeration if at least one constant
     *   was initialized to a default value.
     * @param ed EnumDecl
     */
    public void visitEnumDecl(EnumDecl ed) {
        // First, we will figure out how many constants were initialized and the first
        // initial value of the constant will be used to determine the type of the Enum
        int constantInitCount = 0;
        for(Var constant : ed.constants()) {
            if(constant.init() != null) {
                constantInitCount++;
                if(ed.type() == null) {
                    constant.init().visit(this);

                    // ERROR CHECK #1: A constant in an Enum can only be assigned Int or Char values
                    if(!constant.init().type.isInt() && !constant.init().type.isChar()) {
                        errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ed)
                                    .addErrorType(MessageType.TYPE_ERROR_423)
                                    .addArgs(ed.toString(),constant)
                                    .addSuggestType(MessageType.TYPE_SUGGEST_1411)
                                    .error()
                        );
                    }

                    EnumTypeBuilder typeBuilder = new EnumTypeBuilder().setName(ed.toString());

                    if(constant.init().type.isInt()) { typeBuilder.setConstantType(Discretes.INT); }
                    else { typeBuilder.setConstantType(Discretes.CHAR); }

                    ed.setType(typeBuilder.createEnumType());
                }
            }
        }

        if(constantInitCount == 0) {
            // By default, an Enum will have Int constants starting at [1,inf)
            // if the user did not initialize any of the constant values.
            ed.setType(
                new EnumTypeBuilder()
                        .setName(ed.toString())
                        .setConstantType(Discretes.INT).
                        createEnumType()
            );

            int currValue = 1;
            for(Var constant : ed.constants()) {
                constant.setInit(
                    new LiteralBuilder()
                            .setConstantKind(ConstantKind.INT)
                            .setValue(String.valueOf(currValue))
                            .createLiteral()
                );
                currValue++;
                constant.init().type = ed.type();
                constant.setType(ed.type());
            }
        }
        // ERROR CHECK #2: Make sure each constant in the Enum was initialized
        else if(constantInitCount != ed.constants().size()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ed)
                    .addErrorType(MessageType.TYPE_ERROR_425)
                    .error()
            );
        }
        else {
            for(Var constant : ed.constants()) {
                constant.init().visit(this);

                // ERROR CHECK #3: Make sure the initial value given to a
                //                 constant matches the enum's constant type
                if(!Type.assignmentCompatible(ed.type().constantType(),constant.init().type)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ed)
                            .addErrorType(MessageType.TYPE_ERROR_424)
                            .addArgs(constant,constant.init().type,ed.type().constantType())
                            .error()
                    );
                }
                constant.init().type = ed.type();
                constant.setType(ed.type());
            }
        }
    }

    /**
     * Evaluates the type of a field
     * <p>
     *     If a user initialized a field to a initial value, we will check
     *     to make sure the value and the variable are assignment compatible.
     *     C Minor does NOT support type coercion, so this means the value must
     *     be the same type as the declaration.
     *     <br><br>
     *     For any uninitialized values, we will call {@link #setDefaultValue}
     *     to generate a default value for this field.
     * </p>
     * @param fd Field Declaration
     */
    public void visitFieldDecl(FieldDecl fd) {
        // An uninitialized field will be given a default value
        if(fd.var().init() == null) {
            Expression defaultValue = setDefaultValue(fd.type());
            if(defaultValue != null)
                fd.var().setInit(defaultValue);
            return;
        }

        if(fd.type().isArrayType() && fd.var().init().isArrayLiteral()) {
            Type oldTarget = currentTarget;
            currentTarget = fd.type();

            fd.var().init().visit(this);
            currentTarget = oldTarget;
        }
        else {
            fd.var().init().visit(this);

            // ERROR CHECK #1: Check if the field's declared type
            //                 matches the type of the initial value
            if(!Type.assignmentCompatible(fd.type(),fd.var().init().type)) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(fd)
                            .addErrorType(MessageType.TYPE_ERROR_415)
                            .addArgs(fd.toString(),fd.type(),fd.var().init().type)
                            .error()
                );
            }
        }
        fd.var().setType(fd.type());
    }

    /*
    ___________________________ Field Expressions ___________________________
    For a field expression, we only have to check if the target type
    represents an Object.

    If this is the case, then we can set the field expression to be the type
    of whatever the corresponding field declaration is.
    _________________________________________________________________________
    */

    /**
     * Evaluates the type of a field expression.<br>
     * <p>
     * For a field expression, we will first evaluate the target and make sure the
     * type corresponds to some previously declared class. Then, we will type check
     * the expression the target is trying to access. We will use <code>currentTarget</code>
     * to keep track of the target's type if we're trying to perform method invocations.
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        currentTarget = fe.fieldTarget().type;

        // ERROR CHECK #1: We want to make sure the target is indeed an object,
        //                 so make sure it's assigned a class type
        if(!currentTarget.isClassType()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fe)
                    .addErrorType(MessageType.TYPE_ERROR_416)
                    .addArgs(fe.fieldTarget().toString(),currentTarget)
                    .error()
            );
        }

        Type oldTarget = currentTarget;

        fe.accessExpr().visit(this);

        fe.type = currentTarget;
        currentTarget = oldTarget;
    }

    /*
    ___________________________ For Statements ___________________________
    Unlike the other two loop statements, we have a few error checks that
    need to be done with for statements. We mainly need to make sure the
    for loop has a loop control variable that represents an Int, and its
    condition contains Int literals. Once done, then there's nothing else
    FOR us to type check here. ;)
    ______________________________________________________________________
    */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.symbolTable;

        fs.loopVar().visit(this);
        Type varType = fs.loopVar().type();

        // ERROR CHECK #1: Make sure loop control variable is an Int, Char, or Enum
        if(!varType.isInt() && !varType.isChar() && !varType.isEnumType()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.loopVar())
                    .addErrorType(MessageType.TYPE_ERROR_407)
                    .addArgs()
                    .error());
        }

        // ERROR CHECK #2: Make sure LHS of condition is a literal
        if(!fs.condLHS().isLiteral() && !fs.condLHS().isNameExpr()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.condLHS())
                    .addErrorType(MessageType.TYPE_ERROR_437)
                    .addArgs()
                    .error());
        }
        fs.condLHS().visit(this);

        // ERROR CHECK #3: Make sure RHS of condition is a literal
        if(!fs.condRHS().isLiteral() && !fs.condRHS().isNameExpr()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.condRHS())
                    .addErrorType(MessageType.TYPE_ERROR_439)
                    .addArgs()
                    .error());
        }
        fs.condRHS().visit(this);

        // ERROR CHECK #4: Make sure the LHS and RHS conditions have the same type
        if(!Type.assignmentCompatible(fs.condLHS().type,fs.condRHS().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.condLHS())
                    .addErrorType(MessageType.TYPE_ERROR_438)
                    .addArgs(fs.condLHS().type)
                    .error());
        }

        // ERROR CHECK #5: Make sure the loop condition literals match the type of the control variable
        if(!Type.assignmentCompatible(varType,fs.condLHS().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.condLHS())
                    .addErrorType(MessageType.TYPE_ERROR_440)
                    .addArgs(fs.condLHS().type)
                    .error());
        }


        // ERROR CHECK #6: Make sure the LHS is smaller than the RHS of the loop condition
        if(varType.isInt()) {
            if(Integer.parseInt(fs.condLHS().toString()) >= Integer.parseInt(fs.condRHS().toString())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(fs)
                        .addErrorType(MessageType.TYPE_ERROR_441)
                        .error());
            }
        }
        else if(varType.isChar()) {
            if(fs.condLHS().asLiteral().asChar() >= fs.condRHS().asLiteral().asChar()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(fs)
                        .addErrorType(MessageType.TYPE_ERROR_441)
                        .error());
            }
        }
        else {
            EnumDecl ed = currentScope.findName(fs.loopVar().type().toString()).decl().asTopLevelDecl().asEnumDecl();
            Var RHS = null;
            for(Var v : ed.constants()) {
              if(v.toString().equals(fs.condLHS().toString())) {
                  if(RHS != null) {
                      errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                              .addLocation(fs)
                              .addErrorType(MessageType.TYPE_ERROR_441)
                              .error());
                  }
                  break;
              }
              else if(v.toString().equals(fs.condRHS().toString())) { RHS = v; }
            }
        }

        if(fs.forBlock() != null) { fs.forBlock().visit(this); }
        currentScope = currentScope.closeScope();
    }

    /*
    _______________________ Function Declarations _______________________
    For functions, we are mainly concerned with checking whether we have
    a valid return type since a function can't return a value with no
    corresponding type.

    Additionally, we want to make sure the function does indeed return
    a value if the return type is not void. This will be kept track with
    the `returnStatementFound` flag.
    _____________________________________________________________________
    */
    public void visitFuncDecl(FuncDecl fd) {
        currentScope = fd.symbolTable;
        currentContext = fd;

        // ERROR CHECK #1: Make sure function return type represents
        //                 a real type.
        if(fd.returnType().isClassType()) {
            if(!currentScope.hasNameSomewhere(fd.returnType().typeName())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.TYPE_ERROR_418)
                    .addArgs(fd.returnType().typeName(),fd.toString())
                    .error());
            }
        }
        super.visitFuncDecl(fd);

        // ERROR CHECK #2: If the function has a non-void return type, make
        //                 sure a return statement is found in the function
        if(!fd.returnType().isVoidType() && !returnStatementFound) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.TYPE_ERROR_419)
                    .addArgs(fd.toString(),fd.returnType().typeName())
                    .error());
        }
        currentScope = currentScope.closeScope();
        returnStatementFound = false;
    }

    /**
     * Evaluates the type of a global variable
     * <p>
     *     If a user initialized a global to a initial value, we will check
     *     to make sure the value and the variable are assignment compatible.
     *     C Minor does NOT support type coercion, so this means the value must
     *     be the same type as the declaration.
     *     <br><br>
     *     For any uninitialized values, we will call {@link #setDefaultValue}
     *     to generate a default value for this global variable.
     * </p>
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        // An uninitialized global variable will be given a default value
        if(gd.var().init() == null) {
            Expression defaultValue = setDefaultValue(gd.type());
            if(defaultValue != null)
                gd.var().setInit(defaultValue);
            return;
        }

        if(gd.type().isArrayType() && gd.var().init().isArrayLiteral()) {
            Type oldTarget = currentTarget;
            currentTarget = gd.type();

            gd.var().init().visit(this);
            currentTarget = oldTarget;
        }
        else {
            gd.var().init().visit(this);

            // ERROR CHECK #1: Check if the global variable's declared type
            //                 matches the type of the initial value
            if(!Type.assignmentCompatible(gd.type(),gd.var().init().type)) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(gd)
                            .addErrorType(MessageType.TYPE_ERROR_401)
                            .addArgs(gd.toString(),gd.type(),gd.var().init().type)
                            .error()
                );
            }

        }
        gd.var().setType(gd.type());
    }

    /*
    ________________________  If Statements ________________________
    Similarly to the loop constructs, we only have to check if an if
    statement's condition evaluates into a Bool. IF this is a true,
    then we are good to go visit other nodes. :)
    ________________________________________________________________
    */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);

        if(!is.condition().type.isBool()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(is.condition())
                    .addErrorType(MessageType.TYPE_ERROR_406)
                    .addArgs(is.condition().type)
                    .error());
        }

        currentScope = is.symbolTableIfBlock;
        if(is.ifBlock() != null) { is.ifBlock().visit(this); }
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.elifStmts()) { e.visit(this); }

        if(is.elseBlock() != null) {
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

    /*
    _________________________ Input Statements _________________________
    With input statements, we want to make sure only primitive typed
    input (discrete and scalar) are allowed. None of the structured
    types will be allowed to be inputted by the user.
    ____________________________________________________________________
    */
    public void visitInStmt(InStmt is) {
        for(Expression e : is.inExprs()) {
            e.visit(this);
            // ERROR CHECK #1: Make sure the current input expression is either
            //                 a discrete or scalar type
            if((!e.type.isDiscreteType() && !e.type.isScalarType()) || e.type.isEnumType()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(is)
                        .addErrorType(MessageType.TYPE_ERROR_449)
                        .error());
            }
        }
    }

    /*
    ____________________________ Invocations ____________________________
    In C Minor, both forms of invocations will have the same exact type
    checking done on them. We will be checking whether or not a valid
    overload of the function/method exist.

    In the future, this could be a much more descriptive error message,
    but for right now, we want to make sure the number of arguments and
    their types match at least one valid function/method declaration.

    We will also name check methods here since we now know the class type
    of the object, so we can get the correct symbol table to check for
    the method declaration.
    _____________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        StringBuilder funcSignature = new StringBuilder(in.toString() + "/");

        for(Expression e : in.arguments()) { e.visit(this); }

        for(int i = 0; i < in.arguments().size(); i++)
            funcSignature.append(in.arguments().get(i).type.typeSignature());

        // Function Check
        if(currentTarget == null && currentClass == null) {
            // ERROR CHECK #1: Make sure the function overload exists for the passed
            //                 argument types
            if(!currentScope.hasNameSomewhere(funcSignature.toString())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.TYPE_ERROR_428)
                        .addArgs(in.toString())
                        .error());
            }
            else {
                FuncDecl fd = currentScope.findName(funcSignature.toString()).decl().asTopLevelDecl().asFuncDecl();
                in.type = fd.returnType();
                in.targetType = new VoidType();
            }
        }
        // Method Check
        else {
            in.targetType = currentTarget;
            ClassDecl cd = currentScope.findName(in.targetType.typeName()).decl().asTopLevelDecl().asClassDecl();

            String methodName = in.toString();

            // ERROR CHECK #2: Make sure the method was declared in the class
            if(!cd.symbolTable.hasMethod(methodName)) {
                errors.add(new ErrorBuilder(generateScopeError, interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.SCOPE_ERROR_321)
                        .addArgs(in.toString())
                        .error());
            }


            // ERROR CHECK #3: Make sure the method overload exists for the passed
            //                 argument types
            while(!cd.symbolTable.hasName(funcSignature.toString())) {
                if(cd.superClass() == null) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.TYPE_ERROR_429)
                            .addArgs(in.toString())
                            .error());
                    }
                cd = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            }

            MethodDecl md = cd.symbolTable.findName(funcSignature.toString()).decl().asMethodDecl();
            in.type = md.returnType();
        }
        in.setInvokeSignature(funcSignature.toString());
    }

    /*
    ________________________ Literals ________________________
    We set the type based on the type of literal that was
    parsed. This only includes Scalar and Discrete types.
    All other literals (Arrays, Lists, and Objects) will be
    handled in separate AST visits.
    __________________________________________________________
    */
    public void visitLiteral(Literal li) {
        if(li.getConstantKind() == ConstantKind.BOOL) { li.type = new DiscreteType(Discretes.BOOL); }
        else if(li.getConstantKind() == ConstantKind.INT) { li.type = new DiscreteType(Discretes.INT); }
        else if(li.getConstantKind() == ConstantKind.CHAR) { li.type = new DiscreteType(Discretes.CHAR); }
        else if(li.getConstantKind() == ConstantKind.STR) { li.type = new ScalarType(Scalars.STR); }
        else if(li.getConstantKind() == ConstantKind.REAL) { li.type = new ScalarType(Scalars.REAL); }
        else if(li.getConstantKind() == ConstantKind.ENUM) { li.type = new DiscreteType(Discretes.ENUM); }
    }

    public void visitListLiteral(ListLiteral ll) {}

    /**
     * Evaluates the type of a local variable
     * <p>
     *     If a user initialized a local to a initial value, we will check
     *     to make sure the value and the variable are assignment compatible.
     *     C Minor does NOT support type coercion, so this means the value must
     *     be the same type as the declaration.
     *     <br><br>
     *     For any uninitialized values, we will call {@link #setDefaultValue}
     *     to generate a default value for this local variable.
     * </p>
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        // An uninitialized local variable will be given a default value
        if(ld.var().init() == null) {
            Expression defaultValue = setDefaultValue(ld.type());
            if(defaultValue != null)
                ld.var().setInit(defaultValue);
            return;
        }

        if(ld.type().isArrayType() && ld.var().init().isArrayLiteral()) {
            Type oldTarget = currentTarget;
            currentTarget = ld.type();

            ld.var().init().visit(this);
            currentTarget = oldTarget;
        }
        else {
            ld.var().init().visit(this);

            // ERROR CHECK #1: Check if the local variable's declared type
            //                 matches the type of the initial value
            if(!Type.assignmentCompatible(ld.type(),ld.var().init().type)) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ld)
                            .addErrorType(MessageType.TYPE_ERROR_400)
                            .addArgs(ld.toString(),ld.type(),ld.var().init().type)
                            .error()
                );
            }

        }
        ld.var().setType(ld.type());
    }

    /*
    _________________________ Main Declaration _________________________
    For Main, all we have to check is if the declared return type is
    'Void'. If this is true, then we can type check the rest of main.
    ____________________________________________________________________
    */
    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;

        // ERROR CHECK #1: Make sure main does not return any value
        if(!md.returnType().isVoidType()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(md)
                    .addErrorType(MessageType.TYPE_ERROR_417)
                    .error());
        }
        super.visitMainDecl(md);
        currentScope = currentScope.closeScope();
    }

    /*
    _________________________ Method Declarations _________________________
    Similarly to functions, methods have to be checked to make sure their
    return types are valid, and there is at least one return statement
    present in the method when it's return type is not void. This method
    is nearly identical to our visitFuncDecl method.
    _______________________________________________________________________
    */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;

        // ERROR CHECK #1: Make sure method return type represents
        //                 a real type.
        if(md.returnType().isClassType()) {
            if(!currentScope.hasNameSomewhere(md.returnType().typeName())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.TYPE_ERROR_420)
                        .addArgs(md.returnType().typeName(),md.toString())
                        .error());
            }
        }

        super.visitMethodDecl(md);

        // ERROR CHECK #2: If the method has a non-void return type, make
        //                 sure a return statement is found in the method
        if(!md.returnType().isVoidType() && !returnStatementFound) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(md)
                    .addErrorType(MessageType.TYPE_ERROR_421)
                    .addArgs(md.toString(),md.returnType().typeName())
                    .error());
        }
        currentScope = currentScope.closeScope();
        returnStatementFound = false;
    }

    /*
    _________________________ Name Expressions  _________________________
    All we need to do is find the declaration associated with the name
    and set it equal to the type given in its declaration.
    _____________________________________________________________________
    */
    public void visitNameExpr(NameExpr ne) {
        NameNode name = currentScope.findName(ne.toString());
        if(name != null) {
            if (name.decl().isStatement()) {
                ne.type = name.decl().asStatement().asLocalDecl().type();
            } else if (name.decl().isParamDecl()) {
                ne.type = name.decl().asParamDecl().type();
            } else if (name.decl().isFieldDecl()) {
                ne.type = name.decl().asFieldDecl().type();
            } else if (name.decl().isTopLevelDecl()) {
                TopLevelDecl tDecl = name.decl().asTopLevelDecl();

                if (tDecl.isGlobalDecl()) {
                    ne.type = tDecl.asGlobalDecl().type();
                } else if (tDecl.isEnumDecl()) {
                    ne.type = tDecl.asEnumDecl().type();
                } else if (tDecl.isClassDecl()) {
                    ne.type = new ClassType(tDecl.asClassDecl().name());
                }
            }
        }
        else {
            if(currentTarget != null && currentTarget.isClassType()) {
                ClassDecl cd = currentScope.findName(currentTarget.asClassType().toString()).decl().asTopLevelDecl().asClassDecl();
                if(!cd.symbolTable.hasName(ne.toString())) {
                    errors.add(
                            new ErrorBuilder(generateScopeError,interpretMode)
                                    .addLocation(ne)
                                    .addErrorType(MessageType.SCOPE_ERROR_309)
                                    .addArgs(ne.toString(),currentTarget.toString())
                                    .error()
                    );
                }
                ne.type = cd.symbolTable.findName(ne.toString()).decl().asFieldDecl().type();
            }
        }
    }

    /**
     * <p>
     * In C Minor, a constructor is automatically generated for the user. Thus,
     * we do need to check if a new expression can be called for the class we are
     * trying to instantiate. Instead, we only need to check if for each argument,
     * the type of the value corresponds to the type of the field declaration we're
     * saving the argument into.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.classType().toString()).decl().asTopLevelDecl().asClassDecl();

        Vector<Var> args = ne.args();
        for(Var v : ne.args()) {
            Type fType = cd.symbolTable.findName(v.toString()).decl().asFieldDecl().type();

            if(fType.isArrayType() || fType.isListType()) {
                Type oldTarget = currentTarget;
                currentTarget = fType;
                v.init().visit(this);
                currentTarget = oldTarget;
            }
            else {
                v.init().visit(this);

                // ERROR CHECK #1: Make sure the type of the argument matches
                //                 the type of the field declaration
                if(!Type.assignmentCompatible(v.init().type,fType)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(ne)
                                .addErrorType(MessageType.TYPE_ERROR_412)
                                .addArgs(v.toString(),fType,v.init().type)
                                .error()
                    );
                }
            }
        }
        ne.type = cd.classHierarchy();
    }

    /*
    _______________________ Parameter Declarations _______________________
    Each parameter will have a type, so we must check to ensure that type
    actually exists. If it doesn't, then we have to error out and stop the
    compilation process.
    ______________________________________________________________________
    */
    public void visitParamDecl(ParamDecl pd) {
        if(pd.type().isClassType()) {
            // ERROR CHECK #1: If the type is not a primitive, then make
            //                 sure it was defined somewhere in the program
            if(!currentScope.hasNameSomewhere(pd.type().typeName())) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(pd)
                        .addErrorType(MessageType.TYPE_ERROR_422)
                        .addArgs(pd.type().typeName(),pd.toString())
                        .error());
            }
        }
    }

    /*
    ___________________________ Return Statements ___________________________
    A return statement will always be found inside either a function, a
    method, or the main function of the program.

    Here, we are mainly checking to ensure the value we are returning matches
    the return type of the current context we are in. If there are any typing
    errors, then we have to create an error message.
    _________________________________________________________________________
    */
    public void visitReturnStmt(ReturnStmt rs) {

        if(rs.expr() != null) { rs.expr().visit(this); }

        Type declaredReturnType;
        if(currentContext.isMethodDecl()) { declaredReturnType = currentContext.asMethodDecl().returnType(); }
        else {
            if(currentContext.asTopLevelDecl().isFuncDecl()) {
                declaredReturnType = currentContext.asTopLevelDecl().asFuncDecl().returnType();
            }
            else { declaredReturnType = currentContext.asTopLevelDecl().asMainDecl().returnType(); }
        }

        // ERROR CHECK #1: If the function is declared "Void", then a return statement
        //                 can not return any expression.
        if(rs.expr() != null && declaredReturnType.isVoidType()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(rs)
                    .addErrorType(MessageType.TYPE_ERROR_413)
                    .addArgs(rs.expr().type,currentContext.toString())
                    .error());
        }

        // ERROR CHECK #2: If the function is declared with an explicit return type,
        //                 then we need to make sure the return statement's expression
        //                 is of the corresponding type
        if(rs.expr() != null && !Type.assignmentCompatible(declaredReturnType,rs.expr().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(rs)
                    .addErrorType(MessageType.TYPE_ERROR_414)
                    .addArgs(rs.expr().type,currentContext.toString(),declaredReturnType)
                    .error());
        }
        returnStatementFound = true;
    }

    public void visitThis(This t) {
        t.type = new ClassType(currentClass.toString());
    }

    /*
    __________________________ Unary Expressions  __________________________
    We only have 2 unary operators in C Minor, so there isn't much to check.
    Here is each operator:

        1. '~'
            - Operand Type: Int or Real
            - Unary Expression Type: Type of both operands

        2. 'not'
            - Operand Type: Bool
            - Unary Expression Type: Bool

    Both unary operators may also be overloaded, so we also will check if
    the overload was defined by the user.
    ________________________________________________________________________
    */
    public void visitUnaryExpr(UnaryExpr ue) {

        ue.expr().visit(this);
        Type eType = ue.expr().type;

        String uOp = ue.unaryOp().toString();

        switch(uOp) {
            case "~":
                // ERROR CHECK #1: Make sure we are negating an Int or Real
                if(eType.isInt()) { ue.type = new DiscreteType(Discretes.INT); }
                else if(eType.isReal()) { ue.type = new ScalarType(Scalars.REAL); }
                else {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ue)
                            .addErrorType(MessageType.TYPE_ERROR_405)
                            .addArgs(eType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1407)
                            .addArgsForSuggestion(uOp)
                            .error());
                }
                break;
            case "not":
                // ERROR CHECK #2: Make sure 'not' is performed on a Bool
                if(eType.isBool()) { ue.type = new DiscreteType(Discretes.BOOL); }
                else {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ue)
                            .addErrorType(MessageType.TYPE_ERROR_405)
                            .addArgs(eType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1408)
                            .addArgsForSuggestion(uOp)
                            .error());
                }
                break;
        }
    }

    /*
    ___________________________ While Statements ___________________________
    Similarly to the other loop constructs, we only need to check whether or
    not the while's loop condition evaluates to a boolean. All other type
    checks related to the while loop will be handled by other visits.
    ________________________________________________________________________
    */
    public void visitWhileStmt(WhileStmt ws) {

        ws.condition().visit(this);

        // ERROR CHECK #1: While's condition must be a Boolean
        if(!ws.condition().type.isBool()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ws.condition())
                    .addErrorType(MessageType.TYPE_ERROR_407)
                    .addArgs(ws.condition().type)
                    .error());
        }

        currentScope = ws.symbolTable;
        ws.whileBlock().visit(this);
        currentScope = currentScope.closeScope();
    }
}
