package typechecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.*;
import ast.expressions.FieldExpr.FieldExprBuilder;
import ast.expressions.Invocation.InvocationBuilder;
import ast.expressions.Literal.ConstantType;
import ast.expressions.Literal.LiteralBuilder;
import ast.misc.*;
import ast.operators.AssignOp.AssignType;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.*;
import ast.types.ArrayType.ArrayTypeBuilder;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType.EnumTypeBuilder;
import ast.types.ListType.ListTypeBuilder;
import ast.types.ScalarType.Scalars;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeError;
import messages.errors.type.TypeError;
import micropasses.TypeValidityPass;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private ClassDecl currentClass;
    private AST currentMethod;
    private Type currentTarget;
    private TypeValidityPass typeValidityPass;
    private boolean returnFound = false;
    private boolean parentFound = false;
    private boolean inControlStmt = false;

    /**
     * Creates type checker in compilation mode
     */
    public TypeChecker(String fileName) {
        this.currentScope = null;
        this.currentMethod = null;
        this.currentClass = null;
        this.handler = new MessageHandler(fileName);
    }

    /**
     * Creates type checker in interpretation mode
     * @param st Symbol Table
     */
    public TypeChecker(SymbolTable st) {
        this.currentScope = st;
        this.handler = new MessageHandler();
        this.typeValidityPass = new TypeValidityPass(this.currentScope.getRootTable());
    }


    /* ######################################## HELPERS ######################################## */

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
        if(varType.isInt()) { init = new Literal(ConstantType.INT, "0"); }
        else if(varType.isChar()) { init = new Literal(ConstantType.CHAR, ""); }
        else if(varType.isBool()) { init = new Literal(ConstantType.BOOL, "False"); }
        else if(varType.isReal()) { init = new Literal(ConstantType.REAL, "0.0"); }
        else if(varType.isString()) { init = new Literal(ConstantType.STR, ""); }
        else if(varType.isEnumType()){
            EnumDecl ed = currentScope.findName(varType.toString()).decl().asTopLevelDecl().asEnumDecl();
            init = new NameExpr(ed.constants().get(0).toString());
        }
        else { return null; }

        init.visit(this);
        return init;
    }

    /**
     * Changes the type of a variable.<br><br>
     * <p>
     *     ThisStmt method will only be called from {@code visitRetypeStmt}
     *     when a user changes the type of an object.
     * </p>
     * @param varName Variable we are changing the type of
     * @param varType New type for the variable
     */
    private void setVarType(String varName, Type varType) {
        AST varDecl = currentScope.findName(varName).decl();
        
        if(varDecl.isTopLevelDecl())
            varDecl.asTopLevelDecl().asGlobalDecl().setType(varType);
        else if(varDecl.isParamDecl())
            varDecl.asParamDecl().setType(varType);
        else if(varDecl.isFieldDecl())
            varDecl.asFieldDecl().setType(varType);
        else
            varDecl.asStatement().asLocalDecl().setType(varType);
    }

    /**
     * Checks if an array literal is assignment compatible with an array type.<br><br>
     * <p>
     *     ThisStmt is a recursive algorithm to verify whether an array literal can
     *     be assigned to an array type in C Minor. ThisStmt algorithm was based off
     *     a similar algorithm found in Dr. Pedersen's textbook for compilers.
     * </p>
     * @param depth Current level of recursion (final depth is 1)
     * @param baseType Array type
     * @param dims Expressions representing the dimensions for the array
     * @param currArr Array Literal aka the current array literal we are checking
     * @return Boolean - True if assignment compatible and False otherwise
     */
    private boolean arrayAssignmentCompatibility(int depth, Type baseType, Vector<Expression> dims, ArrayLiteral currArr) {
        if(depth == 1) {
            // ERROR CHECK #1: This makes sure the user only specified one dimension for a 1D array.
            if(currArr.getArrayDims().size() > 1) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(currArr.getRootParent())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_455)
                        .generateError();
                return false;
            }

            if(currArr.getArrayDims().size() == 1)
                checkArrayDims(depth, dims, currArr.getArrayDims().get(0), currArr);
//            else if(!dims.isEmpty()) {
//                if(dims.get(dims.size()-depth).asLiteral().) != currArr.getArrayInits().size()) {
//                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
//                            .addLocation(currArr)
//                            .addErrorType(MessageType.TYPE_ERROR_444)
//                            .error());
//                    return false;
//                }
//            }

            for(Expression init : currArr.getArrayInits()) {
                init.visit(this);

                // ERROR CHECK #2: For every initial value in the array, we check to make sure the
                //                 value's type is assignment compatible with the array's base type.
                if(!Type.assignmentCompatible(baseType,init.type)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(currArr)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_459)
                            .addErrorArgs(init.type,baseType)
                            .generateError();
                    return false;
                }
            }

            currArr.type = new ArrayTypeBuilder()
                               .setMetaData(currArr)
                               .setBaseType(baseType)
                               .setNumOfDims(depth)
                               .create();
            return true;
        }
        else if(depth > 1) {
            ArrayLiteral al = currArr.asArrayLiteral();

            // ERROR CHECK #3: For all n-dimensional array literals (where n>1), we need to make sure the user
            //                 explicitly writes down the size given for each possible dimension.
            if(al.getArrayDims().size() != depth) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(al.getRootParent())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_460)
                        .generateError();
                return false;
            }

            for(Expression dim : al.getArrayDims())
                checkArrayDims(depth,dims,dim,currArr);

            for(Expression init : al.getArrayInits()) {
                // ERROR CHECK #4: For every initial value in the multidimensional array, we need to make
                //                 sure the initial value is an array itself.
                if(!init.isArrayLiteral()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(currArr)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_461)
                            .generateError();
                    return false;
                }

                arrayAssignmentCompatibility(depth-1,baseType,dims,init.asArrayLiteral());
            }

            currArr.type = new ArrayTypeBuilder()
                        .setMetaData(currArr)
                        .setBaseType(baseType)
                        .setNumOfDims(depth)
                        .create();
            return true;
        }
        else
            return false;
    }

    private boolean checkArrayDims(int depth, Vector<Expression> dims, Expression dim,ArrayLiteral currArr) {
        dim.visit(this);

        // ERROR CHECK #1: The given array dimension has to be an Int constant since the size of the array
        //                 must be known at compile-time. An Int constant in this context is either an Int
        //                 literal or a global Int constant.
        if(!dim.type.isInt() || (!dim.isLiteral() && !isGlobalConstant(dim))) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(dim.getRootParent())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_456)
                    .addErrorArgs(dim)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1448)
                    .generateError();
            return false;
        }

        int dimValue;
        if(dim.isLiteral())
            dimValue = dim.asLiteral().asInt();
        else
            dimValue = currentScope.findName(dim).decl().asTopLevelDecl().asGlobalDecl().var().init().asLiteral().asInt();

        // yeah idk what this is
        // TYPE_ERROR_446 = Innermost array literal dimension must match the outermost array literal dimension.

        // ERROR CHECK #9:
        //if(dims.get(dims.size()-currDepth).asListLiteral().toString().equals())
//        if(Integer.parseInt(dims.get(dims.size()-depth).asLiteral().toString()) != Integer.parseInt(al.getArrayDims().get(0).toString())) {
//            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
//                    .addLocation(currArr.getRootParent())
//                    .addErrorType(MessageType.TYPE_ERROR_457)
//                    .error());
//            return false;
//        }
//
//        if(Integer.parseInt(dims.get(dims.size()-depth).asLiteral().toString()) != currArr.getArrayInits().size()) {
//            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
//                    .addLocation(currArr)
//                    .addErrorType(MessageType.TYPE_ERROR_444)
//                    .error());
//            return false;
//        }

        // ERROR CHECK #2: This checks if the user correctly initialized the array based on its size.
        if(currArr.getArrayInits().size() > dimValue) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(currArr.getRootParent())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_458)
                    .addErrorArgs(dimValue, currArr.getArrayInits().size())
                    .generateError();
            return false;
        }

        return true;
    }

    /**
     * Checks if a given {@link Expression} represents a global constant.
     * <p><br>
     *     This is a helper method for to
     *     determine if a given array dimension actually represents a global constant declared
     *     by the user. Since constants will always have the same value, this means the compiler
     *     can definitively know the size of an array.
     * </p>
     * @param expr Expression
     * @return Boolean
     */
    private boolean isGlobalConstant(Expression expr) {
        if(!expr.isNameExpr())
            return false;

        AST decl = currentScope.findName(expr.asNameExpr()).decl();
        if(!decl.isTopLevelDecl())
            return false;
        else if(!decl.asTopLevelDecl().isGlobalDecl())
            return false;
        else
            return decl.asTopLevelDecl().asGlobalDecl().isConstant();
    }

    /**
     * Checks if a list literal is assignment compatible with a list type.
     * <p><br>
     *     This is a recursive algorithm to check if a list literal can be assigned
     *     to a list type in C Minor. This algorithm is based on the algorithm used
     *     for array assignment compatibility albeit it's simpler and has less error checks.
     * </p>
     * @param currDepth Current level of recursion (final depth is 1)
     * @param baseType Base type of the list
     * @param curr List literal aka the current list literal we are checking
     * @return Boolean - True if assignment compatible and False otherwise
     */
    private boolean listAssignmentCompatibility(int currDepth, Type baseType, ListLiteral curr) {
        if(currDepth == 1) {
            for(Expression init : curr.getInits()) {
                init.visit(this);

                // ERROR CHECK #1: This checks to see if the current expression's type matches the list's base type.
                if(!Type.assignmentCompatible(baseType,init.type)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(curr.getRootParent())
                            .addErrorNumber(MessageNumber.TYPE_ERROR_447)
                            .addErrorArgs(baseType,init.type)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1444)
                            .addSuggestionArgs(baseType)
                            .generateError();
                    return false;
                }
            }

            curr.type = new ListTypeBuilder()
                            .setMetaData(curr)
                            .setBaseType(baseType)
                            .setNumOfDims(currDepth)
                            .create();
            return true;
        }
        else if(currDepth > 1) {
            for(Expression e : curr.getInits()) {
                if(!e.isListLiteral()) {
                    // ERROR CHECK #2: This checks to make sure we have nested lists if the current list is multidimensional.
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(curr.getRootParent())
                            .addErrorNumber(MessageNumber.TYPE_ERROR_448)
                            .addErrorArgs(curr)
                            .generateError();
                    return false;
                }
                listAssignmentCompatibility(currDepth-1,baseType,e.asListLiteral());
            }

            curr.type = new ListTypeBuilder()
                            .setMetaData(curr)
                            .setBaseType(baseType)
                            .setNumOfDims(currDepth)
                            .create();

            return true;
        }
        else
            return false;
    }

    private FuncDecl findSpecificFunction(FuncDecl candidate, FuncDecl currentTemplate, Invocation in) {
        // If we do not have a possible template function, then the candidate will default to be the candidate
        if(currentTemplate == null)
            return candidate;

        // We will now do a type analysis of the candidate's parameters with the invocation's arguments.
        for(int i = 0; i < candidate.params().size(); i++) {
            ParamDecl candidateParam = candidate.params().get(i);
            ParamDecl templateParam = currentTemplate.params().get(i);
            Expression currArg = in.getArgs().get(i);


            // If the candidate's current parameter is assignment compatible with the current argument, this means we
            // might have a more specific template to use. If we can confirm
            if(Type.assignmentCompatible(candidateParam.type(), currArg.getType())) {
                if(templateParam.isParamTypeTemplated(currentTemplate.typeParams()))
                    return candidate;
            }
        }

        return currentTemplate;
    }

    private FuncDecl findValidFuncTemplate(String funcName, Invocation in) {
        final SymbolTable rootTable = currentScope.getRootTable();
        FuncDecl template = null;

        for(FuncDecl candidate: currentScope.getAllFuncNames()) {
            // First, we need to make sure the current candidate represents a template function
            // and the candidate matches the name of the function that is called.
            if(!candidate.isTemplate() || !candidate.toString().equals(funcName))
                continue;

            // Next, we need to make sure the candidate parameter count matches the argument count
            // If it doesn't, then we know this candidate can be eliminated.
            if(candidate.params().size() != in.getArgs().size())
                continue;

            template = findSpecificFunction(candidate,template,in);
        }

        return template;
    }

    /**
     * Verifies the validity of a template function call.
     * <p><br>
     *     If a user writes a template function call, then this method will perform the
     *     necessary error checks to ensure the template function call was written correctly.
     *     This will allow us to instantiate the function and then have the {@link TypeChecker}
     *     check if all types can be resolved correctly. This method is identical to the one
     *     found in {@link micropasses.TypeValidityPass} for validating template types, but
     *     this method produces different error messages.
     * </p>
     * @param fd Current template function
     * @param in The template {@link Invocation} we want to check if it's been written correctly
     */
    public void checkIfFuncTemplateCallIsValid(FuncDecl fd, Invocation in) {
        // ERROR CHECK #1: When a template type is written, we want to make sure the correct number of
        //                 type arguments were passed. This will be based on the number of type parameters
        //                 the template function was declared with. There are 2 possible errors here.
        if(fd.typeParams().size() != in.getTypeArgs().size()) {
            // Case 1: This error is generated when a user writes type arguments for a non-template function.
            if(fd.typeParams().isEmpty()) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(in.getRootParent())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_462)
                        .addErrorArgs(fd)
                        .generateError();
            }
            // Case 2: This error is generated when the wrong number of type arguments were used for a template function.
            else {
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                      .addLocation(in.getRootParent())
                                      .addErrorNumber(MessageNumber.TYPE_ERROR_463)
                                      .addErrorArgs(in.getSignature())
                                      .addSuggestionArgs(fd,fd.typeParams().size());

                if(fd.typeParams().size() == 1)
                    eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1449).generateError();
                else
                    eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1450).generateError();
            }
        }

        // We now look through each type parameter of the template function.
        for (int i = 0; i < fd.typeParams().size(); i++) {
            Typeifier typeParam = fd.typeParams().get(i);

            // ERROR CHECK #2: If a user prefixed the type parameter with a type annotation, then we will check if
            //                 the passed type argument can be used in the current type argument. If no type annotation
            //                 was given, this check is not needed, and we will let the type checker handle the rest.
            if(!typeParam.isValidTypeArg(in.getTypeArgs().get(i))) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(in.getRootParent())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_446)
                        .addErrorArgs(in.getTypeArgs().get(i), fd)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1451)
                        .addSuggestionArgs(fd, typeParam.possibleTypeToString(), i + 1)
                        .generateError();
            }
        }
    }

    /* ######################################## VISITS ######################################## */

    /**
     * <p>
     *     Array expressions are how users can access memory from array.
     *     First, we make sure the target is an array (or a list) since it
     *     does not make sense to dereference a non-array type. Then, we
     *     will make sure the index evaluates to an integer. We will not
     *     check if the integer is a valid index or not since this needs
     *     to be done at runtime.
     * </p>
     * @param ae Array Expression
     */
    public void visitArrayExpr(ArrayExpr ae) {
        ae.getArrayTarget().visit(this);

        // ERROR CHECK #1: This checks if the target represents a valid Array or List type.
        if(!ae.getArrayTarget().type.isArrayType() && !ae.getArrayTarget().type.isListType()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ae)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_453)
                    .addErrorArgs(ae.getArrayTarget())
                    .generateError();
        }

        Type arrType;
        if(currentTarget == null)
            arrType = currentScope.findName(ae.getArrayTarget()).decl().getType();
        else {
            ClassDecl cd = currentScope.findName(currentTarget).decl().asTopLevelDecl().asClassDecl();
            arrType = cd.symbolTable.findName(ae.getArrayTarget()).decl().asFieldDecl().type().asArrayType();
        }

        if(arrType.isArrayType()) {
            // ERROR CHECK #2: This checks if the number of indices exceeds the number of dimensions for the array.
            if(ae.getArrayIndex().size() > arrType.asArrayType().numOfDims) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ae)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_448)
                        .addErrorArgs("array", ae.getArrayTarget(), arrType.asArrayType().numOfDims, ae.getArrayIndex().size())
                        .generateError();
            }
        }
        else {
            // ERROR CHECK #3: Same as the previous error check, but for lists instead.
            if(ae.getArrayIndex().size() > arrType.asListType().numOfDims) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ae)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_448)
                        .addErrorArgs("list", ae.getArrayTarget(), arrType.asListType().numOfDims, ae.getArrayIndex().size())
                        .generateError();
            }
        }



        for(Expression e : ae.getArrayIndex()) {
            e.visit(this);

            // ERROR CHECK #3: For each index, make sure the
            //                 value evaluates to be an Int
            if(!e.type.isInt()) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ae)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_454)
                        .addErrorArgs(e.type)
                        .generateError();
            }
        }
        if(arrType.isArrayType()) {
            if(arrType.asArrayType().numOfDims != ae.getArrayIndex().size())
                ae.type = new ArrayType(arrType.asArrayType().baseType(),ae.getArrayIndex().size());
            else
                ae.type = arrType.asArrayType().baseType();
        }
        else {
            if(arrType.asListType().numOfDims != ae.getArrayIndex().size())
                ae.type = new ListType(arrType.asListType().baseType(),ae.getArrayIndex().size());
            else
                ae.type = arrType.asListType().baseType();
        }
    }

    /**
     *     For array literals, we will call the helper method
     *     arrayAssignmentCompatible to handle all type checking
     *     for us.
     * @param al Array Literal
     */
    public void visitArrayLiteral(ArrayLiteral al) {
        // If current target doesn't represent an array type, then we'll set the
        // array literal to be some arbitrary array type to prevent type assignment
        // compatibility from leading to an exception
        if(currentTarget == null || !currentTarget.isArrayType()) {
            super.visitArrayLiteral(al);
            if(al.getArrayInits().isEmpty())
                al.type = new ArrayType(new VoidType(),al.getNumOfDims());
            else
                al.type = new ArrayType(al.getArrayInits().get(0).type,al.getNumOfDims());
        }
        else {
            arrayAssignmentCompatibility(currentTarget.asArrayType().numOfDims,
                                         currentTarget.asArrayType().baseType(),
                                         al.getArrayDims(), al);
            al.type = currentTarget.asArrayType();
        }

    }

    /**
     *     If we want to assign a new value to a variable, we need to make sure the
     *     value's type matches the type of the variable.
     *     <br><br>
     *     C Minor also supports compound assignment operations such as +=, -=, *=,
     *     etc. which means we have to do an additional check to make sure the two
     *     values can perform a legal binary operation.
     * @param as Assignment Statement
     */
    public void visitAssignStmt(AssignStmt as) {
        as.LHS().visit(this);
        Type lType = as.LHS().type;

        // ERROR CHECK #1: An array literal can only be assigned to
        //                 a variable storing an array
        if(!lType.isArrayType() && as.RHS().isArrayLiteral()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(as)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_450)
                    .generateError();
        }

        if(as.RHS().isArrayLiteral() || as.RHS().isListLiteral()) {
            Type oldTarget = currentTarget;
            currentTarget = lType;

            as.RHS().visit(this);
            currentTarget = oldTarget;
        }
        else
            as.RHS().visit(this);

        Type rType = as.RHS().type;

        // ERROR CHECK #1: Make sure both the variable and value type are the same
        if(!Type.assignmentCompatible(lType,rType)) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(as)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_403)
                    .addErrorArgs(as.LHS().toString(),lType,rType)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1401)
                    .addSuggestionArgs(lType)
                    .generateError();
        }

        if(as.assignOp().getAssignOp() != AssignType.EQ) {
            if(as.assignOp().getAssignOp() == AssignType.PLUSEQ) {
                // ERROR CHECK #2: For a '+=' operation, the only allowed types
                //                 are Int, Real, and String
                if (lType.isBool() || lType.isChar() || lType.isClassOrMultiType())
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(as)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_403)
                            .addErrorArgs(as.LHS().toString(),lType,rType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1402)
                            .generateError();
            }
            else {
                // ERROR CHECK #3: For all other assignment operators, the
                //                 only supported types are Int and Real
                if(lType.isBool() || lType.isChar() || lType.isString() || lType.isClassOrMultiType())
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(as)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_404)
                            .addErrorArgs(as.assignOp(),lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1403)
                            .addSuggestionArgs(as.assignOp())
                            .generateError();
            }
        }
    }

    /**
     * Evaluates the type of a binary expression.
     * <p><br>
     *     Since C Minor does not support type coercion, we are going to be
     *     strict about which types are allowed for every binary operator.
     *     <br><br>
     *     There are currently 24 binary operators in C Minor. The following
     *     is a list of type checks we will do for each operator:
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
     *                 <li>Operand Type: Int, Real, Char, or String (for '+')</li>
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
     *     Additionally, most of the binary operators can be overloaded by classes,
     *     so we will check if the overloaded method was defined here as well.
     * </p>
     * @param be Binary Expression
     */
    public void visitBinaryExpr(BinaryExpr be) {
        be.getLHS().visit(this);
        Type lType = be.getLHS().type;

        be.getRHS().visit(this);
        Type rType = be.getRHS().type;

        String binOp = be.getBinaryOp().toString();

        if(lType.isClassOrMultiType()) {
            switch(binOp) {
                case "&":
                case "^":
                case "|":
                case "and":
                case "or", "instanceof", "!instanceof", "as?":
                    break;
                default:
                    FieldExpr fe = new FieldExprBuilder()
                                       .setTarget(be.getLHS())
                                       .setAccessExpr(
                                            new InvocationBuilder()
                                                .setName(new Name("operator"+binOp))
                                                .setArgs(new Vector<>(be.getRHS()))
                                                .create()
                                        )
                                        .create();
                    fe.replace(be);
                    fe.visit(this);
                    return;
            }
        }

        // ERROR CHECK #1: Both LHS/RHS have to be the same type.
        if(!binOp.equals("instanceof") && !binOp.equals("!instanceof") && !binOp.equals("as?")) {
            if(!Type.assignmentCompatible(lType,rType)) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(be)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_405)
                        .addErrorArgs(binOp,lType,rType)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1404)
                        .addSuggestionArgs(binOp)
                        .generateError();
            }
        }

        switch(binOp) {
            case "==":
            case "!=": {
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "<>":
            case "<=>": {
                // ERROR CHECK #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1405)
                            .addSuggestionArgs(binOp)
                            .generateError();
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
                // ERROR CHECK #3: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1405)
                            .addSuggestionArgs(binOp)
                            .generateError();
                }
                be.type = lType;
                break;
            }
            case "<<":
            case ">>": {
                // ERROR CHECK #4: Both LHS and RHS have to be an INT for shift operations
                if(!lType.isInt()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1406)
                            .addSuggestionArgs(binOp)
                            .generateError();
                }
                be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "&":
            case "|":
            case "^": {
                // ERROR CHECK #5: Make sure both types are discrete
                if(!lType.isDiscreteType() || !rType.isDiscreteType()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1407)
                            .addSuggestionArgs(binOp)
                            .generateError();
                }
                if(binOp.equals("^"))
                    be.type = new DiscreteType(Discretes.INT);
                else
                    be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "and":
            case "or": {
                // ERROR CHECK #6: Make sure both types are Bool
                if(!lType.isBool() || !rType.isBool()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_406)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1408)
                            .addSuggestionArgs(binOp)
                            .generateError();
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                // ERROR CHECK #7: Make sure the LHS represents a class type
                if(!lType.isClassOrMultiType()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(be)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_407)
                            .addErrorArgs(binOp,lType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1409)
                            .addSuggestionArgs(binOp)
                            .generateError();
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
        }
    }

    /**
     * <p>
     *     In C Minor, there are three valid cast expressions.
     *     <ol>
     *         <li> Char <=> Int</li>
     *         <li> Int <=> Real</li>
     *         <li> Char => String</li>
     *     </ol>
     *     For mixed type expressions, this means the programmer must perform
     *     explicit type casts or else the compiler will generate a typing error.
     * </p>
     * @param ce Cast Expression
     */
    public void visitCastExpr(CastExpr ce) {
        ce.getCastExpr().visit(this);

        if(ce.getCastExpr().type.isInt()) {
            // ERROR CHECK #1: An Int can only be typecasted into a Char and a Real
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
            // ERROR CHECK #2: A Char can only be type casted into an Int and a String
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
            // ERROR CHECK #3: A Real can only be type casted into an Int
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
            // By default, all other cast expressions will be considered invalid
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ce)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_409)
                    .addErrorArgs(ce.getCastType())
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1415)
                    .generateError();
        }

        ce.type = ce.getCastType();
    }

    /**
     * <p>
     *     When we are visiting a choice statement, there are two main type checks
     *     we have to perform.<br><br>
     *     First, we make sure the choice expression is either an Int, Char, or
     *     a String. Then, we make sure each case's label corresponds to the
     *     correct type of the choice expression. If this is all valid, then we
     *     can continue with the compilation process.
     * </p>
     * @param cs Choice Statement
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        boolean prev = inControlStmt;
        cs.choiceExpr().visit(this);
        Type choiceType = cs.choiceExpr().type;

        // ERROR CHECK #1: Choice statements only support Int, Char, and String
        if(!(choiceType.isInt() || choiceType.isChar() || choiceType.isString())) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(cs.choiceExpr())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_416)
                    .addErrorArgs(choiceType)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1422)
                    .generateError();
        }

        for(CaseStmt curr : cs.caseStmts()) {
            curr.choiceLabel().visit(this);
            Type labelType = curr.choiceLabel().leftLabel().type;

            // ERROR CHECK #2: Make sure the case label's type corresponds
            //                 to the type of the choice statement expression
            if(!Type.assignmentCompatible(labelType,choiceType)) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(curr.choiceLabel())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                        .addErrorArgs(labelType, choiceType)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1423)
                        .generateError();
            }

            if(curr.choiceLabel().rightLabel() != null) {
                labelType = curr.choiceLabel().rightLabel().type;
                // ERROR CHECK #3: Same as ERROR CHECK #2, but now for the right label
                if(!Type.assignmentCompatible(labelType,choiceType)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(curr.choiceLabel())
                            .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                            .addErrorArgs(labelType,choiceType)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1423)
                            .generateError();
                }

                // ERROR CHECK #4: If we allow to choose from String values, then
                //                 there is only one label allowed per case statement
                if(choiceType.isString()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(curr.choiceLabel())
                            .addErrorNumber(MessageNumber.TYPE_ERROR_418)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1424)
                            .generateError();
                }

                // ERROR CHECK #5: Make sure label's right constant is greater than left constant
                if(choiceType.isInt()) {
                    int lLabel = curr.choiceLabel().leftLabel().asInt();
                    int rLabel = curr.choiceLabel().leftLabel().asInt();
                    if(rLabel <= lLabel) {
                        handler.createErrorBuilder(TypeError.class)
                                .addLocation(curr.choiceLabel())
                                .addErrorNumber(MessageNumber.TYPE_ERROR_419)
                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1425)
                                .generateError();
                    }
                }
                else if(choiceType.isChar()) {
                    char lLabel = curr.choiceLabel().leftLabel().asChar();
                    char rLabel = curr.choiceLabel().leftLabel().asChar();
                    if(rLabel <= lLabel) {
                        handler.createErrorBuilder(TypeError.class)
                                .addLocation(curr.choiceLabel())
                                .addErrorNumber(MessageNumber.TYPE_ERROR_419)
                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1425)
                                .generateError();
                    }
                }
            }
            currentScope = curr.symbolTable;
            inControlStmt = true;
            curr.caseBlock().visit(this);
            inControlStmt = prev;
            currentScope = currentScope.closeScope();
        }

        currentScope = cs.symbolTable;
        inControlStmt = true;
        cs.otherBlock().visit(this);
        inControlStmt = prev;
        currentScope = currentScope.closeScope();
    }

    /**
     * Creates the class hierarchy for the current class.<br><br>
     * <p>
     *     We do not need to do any explicit type checking with the class
     *     declaration itself. All we need to do here is to internally keep
     *     track of all inherited classes, so we can use this information
     *     elsewhere in the type checker.
     * </p>
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        // Only create the class hierarchy if the class inherits from another class
        if(cd.superClass() != null) {
            // Add each inherited class to an internal class hierarchy list
            ClassDecl base = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            while(base.superClass() != null) {
                cd.addBaseClass(base.name());
                if(base.superClass() != null)
                    base = currentScope.findName(base.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            }
            cd.addBaseClass(base.name());
        }

        SymbolTable oldScope = currentScope;
        ClassDecl oldClass = currentClass;
        currentScope = cd.symbolTable;
        currentClass = cd;

        // Do not type check the class if it's a template class.
        if(cd.typeParams().isEmpty())
            super.visitClassDecl(cd);

        currentClass = oldClass;
        currentScope = oldScope;
    }

    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;
        this.typeValidityPass = new TypeValidityPass(c.globalTable);
        super.visitCompilation(c);
    }

    /**
     * Evaluates the do statement's conditional expression
     * <p>
     *     We will only check to make sure the do while loop's condition
     *     evaluates to be a boolean value. All other type checks will be
     *     handled by other visits.
     * </p>
     * @param ds Do Statement
     */
    public void visitDoStmt(DoStmt ds) {
        boolean prev = inControlStmt;
        currentScope = ds.symbolTable;
        inControlStmt = true;
        ds.doBlock().visit(this);
        inControlStmt = prev;
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);
        // ERROR CHECK #1: The do while loop's condition must be a boolean
        if(!ds.condition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ds.condition())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_412)
                    .addErrorArgs(ds.condition().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1418)
                    .generateError();
        }
    }

    /**
     * Evaluates if an enumeration was written correctly<br>
     * <p>
     *     In C Minor, an enumeration can only store values of type Int
     *     and Char for each constant. Additionally, we are going to be
     *     strict and require the user to initialize all values of the
     *     enumeration if at least one constant was initialized to a default value.
     * </p>
     * @param ed Enumeration
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
                        handler.createErrorBuilder(TypeError.class)
                                .addLocation(ed)
                                .addErrorNumber(MessageNumber.TYPE_ERROR_436)
                                .addErrorArgs(ed,constant.init().type)
                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1434)
                                .generateError();
                    }

                    EnumTypeBuilder typeBuilder = new EnumTypeBuilder().setName(ed.name());

                    if(constant.init().type.isInt())
                        typeBuilder.setConstantType(Discretes.INT);
                    else
                        typeBuilder.setConstantType(Discretes.CHAR);

                    ed.setType(typeBuilder.create());
                }
            }
        }

        if(constantInitCount == 0) {
            // By default, an Enum will have Int constants starting at [1,inf)
            // if the user did not initialize any of the constant values.
            ed.setType(
                new EnumTypeBuilder()
                    .setName(ed.name())
                    .setConstantType(Discretes.INT)
                    .create()
            );

            int currValue = 1;
            for(Var constant : ed.constants()) {
                constant.setInit(
                    new LiteralBuilder()
                        .setConstantKind(ConstantType.INT)
                        .setValue(String.valueOf(currValue))
                        .create()
                );
                currValue++;
                constant.init().type = ed.type();
                constant.setType(ed.type());
            }
        }
        // ERROR CHECK #2: Make sure each constant in the Enum was initialized
        else if(constantInitCount != ed.constants().size()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ed)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_437)
                    .addErrorArgs(ed)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1435)
                    .generateError();
        }
        else {
            for(Var constant : ed.constants()) {
                constant.init().visit(this);

                // ERROR CHECK #3: Make sure the initial value given to a
                //                 constant matches the enum's constant type
                if(!Type.assignmentCompatible(ed.type().constantType(),constant.init().type)) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(constant)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_438)
                            .addErrorArgs(constant,ed,constant.init().type,ed.type().constantType())
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1436)
                            .generateError();
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
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(fd)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_420)
                        .addErrorArgs(fd.toString(),fd.type(),fd.var().init().type)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                        .addSuggestionArgs(fd.var().type())
                        .generateError();
            }
        }
        fd.var().setType(fd.type());
    }

    /**
     * Evaluates the type of a field expression<br>
     * <p>
     * For a field expression, we will first evaluate the target and make sure the
     * type corresponds to some previously declared class. Then, we will type check
     * the expression the target is trying to access. We will use {@code currentTarget}
     * to keep track of the target's type if we're trying to perform method invocations.
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.getTarget().visit(this);

        // ERROR CHECK #1: We want to make sure the target is indeed an object,
        //                 so make sure it's assigned a class type
        if(!fe.getTarget().type.isClassOrMultiType()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(fe)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_435)
                    .addErrorArgs(fe.getTarget(),fe.getTarget().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1433)
                    .generateError();
        }

        Type oldTarget = currentTarget;
        currentTarget = fe.getTarget().type;
        fe.getAccessExpr().visit(this);

        fe.type = fe.getAccessExpr().type;
        currentTarget = oldTarget;
        parentFound = false;
    }

    /**
     * Evaluates a for statement.
     * <p>
     *     Unlike the other two loop statements, we have a few error checks that
     *     need to be done with for statements. We mainly need to make sure the
     *     for loop has a loop control variable that represents an Int, and its
     *     condition contains Int literals. Once done, then there's nothing else
     *     FOR us to type check here. ;)
     * </p>
     * @param fs For Statement
     */
    public void visitForStmt(ForStmt fs) {
        boolean prev = inControlStmt;
        currentScope = fs.symbolTable;

        fs.loopVar().visit(this);
        Type varType = fs.loopVar().type();

        // ERROR CHECK #1: Make sure loop control variable is an Int, Char, or Enum
        if(!varType.isInt() && !varType.isChar() && !varType.isEnumType()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(fs.loopVar())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_413)
                    .addErrorArgs(fs.loopVar(),varType)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1419)
                    .generateError();
        }

        fs.condLHS().visit(this);
        fs.condRHS().visit(this);

        // ERROR CHECK #2: Make sure the LHS and RHS conditions have the same type
        if(!Type.assignmentCompatible(fs.condLHS().type,fs.condRHS().type)) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(fs)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_414)
                    .addErrorArgs(fs.condLHS().type,fs.condRHS().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1420)
                    .generateError();
        }

        // ERROR CHECK #3: Make sure the loop condition literals match the type of the control variable
        if(!Type.assignmentCompatible(varType,fs.condLHS().type)) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(fs)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_415)
                    .addErrorArgs(fs.loopVar(),fs.loopVar().type(),fs.condLHS().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1421)
                    .generateError();
        }

        if(fs.forBlock() != null) { 
            inControlStmt = true;
            fs.forBlock().visit(this); 
            inControlStmt = prev;
        }
        currentScope = currentScope.closeScope();
    }

    /**
     * Evaluates function declaration
     * <p>
     *     For functions, we are going to check if the return types are valid,
     *     and if the correct value is returned from the function.
     * </p>
     * @param fd Function Declaration
     */
    public void visitFuncDecl(FuncDecl fd) {
        if(fd.typeParams().isEmpty()) {
            SymbolTable oldScope = currentScope;
            currentScope = fd.symbolTable;
            currentMethod = fd;

            // ERROR CHECK #1: Make sure the function return type is valid
            if(fd.returnType().isClassType()) {
                if(!currentScope.hasNameSomewhere(fd.returnType().toString())) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(fd)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_421)
                            .addErrorArgs(fd.returnType(),fd)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1426)
                            .addSuggestionArgs(fd.returnType())
                            .generateError();
                }
            }

            super.visitFuncDecl(fd);

            // ERROR CHECK #2: If the function has a non-void return type, make
            //                 sure a return statement is found in the function
            if(!fd.returnType().isVoidType() && !returnFound) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(fd)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_422)
                        .addErrorArgs(fd,fd.returnType())
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1427)
                        .generateError();
            }

            currentScope = oldScope;
            currentMethod = null;
            returnFound = false;
        }
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
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                      .addLocation(gd)
                                      .addErrorArgs(gd.toString(),gd.type(),gd.var().init().type)
                                      .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                                      .addSuggestionArgs(gd.type());
                if(gd.isConstant())
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_402).generateError();
                else
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_401).generateError();
            }

        }

        gd.var().setType(gd.type());
        gd.setType(gd.var().init().type);
    }

    /**
     * Evaluates the if statement's conditional expression
     * <p>
     *     We will check if the if statement's condition evaluates to be
     *     a boolean value. ALl other type checks will be handled by other
     *     visits.
     * </p>
     * @param is If Statement
     */
    public void visitIfStmt(IfStmt is) {
        boolean prev = inControlStmt;
        is.condition().visit(this);

        // ERROR CHECK #1: The if statement's conditional expression must be a boolean
        if(!is.condition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(is.condition())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_410)
                    .addErrorArgs(is.condition().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1416)
                    .generateError();
        }

        currentScope = is.symbolTableIfBlock;
        inControlStmt = true;
        is.ifBlock().visit(this);
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.elifStmts()) { e.visit(this); }

        if(is.elseBlock() != null) {
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = currentScope.closeScope();
        }
        inControlStmt = prev;
    }

    public void visitImportDecl(ImportDecl im) {
        SymbolTable oldScope = currentScope;

        im.getCompilationUnit().visit(this);

        currentScope = oldScope;
    }

    /**
     * Evaluates the type of each input expression.
     * <p><br>
     *     For input statements, we want to make sure a user can only
     *     input values into a variable that represents a primitive type
     *     (not counting an enum). Once type checking is complete, we will
     *     set the input statement's type to be {@code Void} since it does
     *     not represent anything.
     * </p>
     * @param is Input Statement
     */
    public void visitInStmt(InStmt is) {
        for(Expression e : is.getInExprs()) {
            e.visit(this);
            // ERROR CHECK #1: Make sure expression is discrete (not enum) or scalar
            if(!(e.type.isDiscreteType() || e.type.isScalarType()) || e.type.isEnumType()) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(is)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_440)
                        .addErrorArgs(e,e.type)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1437)
                        .generateError();
            }
        }
        is.type = new VoidType();
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
        // First, we will create the invocation's type signature.
        StringBuilder signature = new StringBuilder(in + "(");

        for(Expression e : in.getArgs()) {
            e.visit(this);
            signature.append(e.type.typeSignature());
        }

        signature.append(")");
        in.setSignature(signature.toString());

        // Function Invocation
        if(currentTarget == null && currentClass == null) {
            in.targetType = new VoidType();

            if(in.isLengthInvocation()) {
                // ERROR CHECK #1: Make sure 'length' call only has one argument
                if(in.getArgs().size() != 1) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(in)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_451)
                            .generateError();
                }
                // ERROR CHECK #2: Make sure argument evaluates to an array or list
                if(!(in.getArgs().get(0).type.isArrayType() || in.getArgs().get(0).type.isListType())) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(in)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_452)
                            .generateError();
                }
                in.targetType = new VoidType();
                in.type = new DiscreteType(Discretes.INT);
                in.setLengthInvocation();
                return;
            }

            else if(in.isTemplate()) {
                FuncDecl template = findValidFuncTemplate(in.toString(), in);
                checkIfFuncTemplateCallIsValid(template, in);
                in.templatedFunction = typeValidityPass.instantiatesFuncTemplate(template, in);

                for(int i = 0; i < in.getArgs().size(); i++) {
                    Type paramType = in.templatedFunction.params().get(i).getType();
                    Type argType = in.getArgs().get(i).getType();

                    if(!Type.assignmentCompatible(paramType, argType)) {
                        String argumentTypes = Type.createTypeString(in.getArgs());
                        ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                .addLocation(in)
                                .addErrorArgs(in,argumentTypes)
                                .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1431)
                                .addSuggestionArgs(in,in+"("+argumentTypes+")");

                        if(in.getArgs().isEmpty())
                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_429).generateError();
                        else if(in.getArgs().size() == 1)
                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_430).generateError();
                        else
                            eb.addErrorNumber(MessageNumber.TYPE_ERROR_431).generateError();
                    }
                }

                in.type = in.templatedFunction.returnType();
                in.templatedFunction.visit(this);
                return;
            }

            // ERROR CHECK #3: Check if function overload exists
            if(!currentScope.hasNameSomewhere(signature.toString())) {
                String argumentTypes = Type.createTypeString(in.getArgs());
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                      .addLocation(in)
                                      .addErrorArgs(in,argumentTypes)
                                      .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1431)
                                      .addSuggestionArgs(in,in+"("+argumentTypes+")");

                if(in.getArgs().isEmpty())
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_429).generateError();
                else if(in.getArgs().size() == 1)
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_430).generateError();
                else
                    eb.addErrorNumber(MessageNumber.TYPE_ERROR_431).generateError();
            }

            FuncDecl fd = currentScope.findName(signature.toString()).decl().asTopLevelDecl().asFuncDecl();
            in.type = fd.returnType();
        }
        // Method Invocation
        else {
            ClassDecl cd = null;
            if(currentTarget != null && currentTarget.isMultiType()) {
                for(ClassType ct : currentTarget.asMultiType().getAllTypes()) {
                    cd = currentScope.findName(ct.toString()).decl().asTopLevelDecl().asClassDecl();
                    if(cd.symbolTable.hasMethod(in.toString()))
                        break;
                }
            }
            else if(parentFound)
                cd = currentScope.findName(currentClass.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            else if(currentClass != null)
                cd = currentClass;
            else
                cd = currentScope.findName(currentTarget.asClassType().toString()).decl().asTopLevelDecl().asClassDecl();

            String className = cd.toString();
            // ERROR CHECK #4: Check if the method was defined in the class hierarchy
            if(!cd.symbolTable.hasMethod(in.toString())) {
//                if(interpretMode)
//                    currentTarget = null;
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(in)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_327)
                        .addErrorArgs(in)
                        .generateError();
            }

            // ERROR CHECK #5: Check if a valid method overload exists
            while(!cd.symbolTable.hasName(signature.toString())) {
                if(cd.superClass() == null) {
//                    if(interpretMode)
//                        currentTarget = null;

                    String argumentTypes = Type.createTypeString(in.getArgs());
                    ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
                                          .addLocation(in)
                                          .addErrorArgs(in,className,argumentTypes)
                                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1432)
                                          .addSuggestionArgs(in,className,in+"("+argumentTypes+")");

                    if(in.getArgs().isEmpty())
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_432).generateError();
                    else if(in.getArgs().size() == 1)
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_433).generateError();
                    else
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_434).generateError();
                }
                cd = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            }

            MethodDecl md = cd.symbolTable.findName(signature.toString()).decl().asMethodDecl();
            if(currentTarget.isClassType() && currentTarget.asClassType().isTemplatedType())
                in.targetType = currentTarget;
            else
                in.targetType = new ClassType(cd.toString());
            in.type = md.returnType();
            currentTarget = md.returnType();
        }
    }

    /**
     * Evaluates the type of a literal
     * <p>
     *     We will create a type for the literal based on its assigned
     *     constant kind value. ThisStmt visit only handles primitive type
     *     literals and not structured type literals.
     * </p>
     * @param li Literals
     */
    public void visitLiteral(Literal li) {
        switch(li.getConstantKind()) {
            case INT:
                li.type = new DiscreteType(Discretes.INT);
                break;
            case CHAR:
                li.type = new DiscreteType(Discretes.CHAR);
                break;
            case BOOL:
                li.type = new DiscreteType(Discretes.BOOL);
                break;
            case REAL:
                li.type = new ScalarType(Scalars.REAL);
                break;
            case STR:
                li.type = new ScalarType(Scalars.STR);
                break;
            case ENUM:
                li.type = new DiscreteType(Discretes.ENUM);
                break;
        }
    }

    /**
     * Evaluates the type of a list literal.
     * <p>
     *     We will call {@link #listAssignmentCompatibility} to check
     *     if the current list literal can indeed be stored into the
     *     variable we are trying to store the list into.
     * </p>
     * @param ll List Literal
     */
    public void visitListLiteral(ListLiteral ll) {
        // Special case where we have a list literal by itself
        if(currentTarget == null || !currentTarget.isListType()) {
            // Since a list is dynamic, we need to manually figure out the size of the literal
            // This figures out how many dimensions are in the list
            int numOfDims = 0;
            Expression curr = ll;
            while(curr.isListLiteral()) {
                numOfDims += 1;
                if(!curr.asListLiteral().getInits().isEmpty())
                    curr = curr.asListLiteral().getInits().get(0);
                else
                    break;
            }

            if(curr.isListLiteral())
                listAssignmentCompatibility(numOfDims, null, ll);
            else {
                curr.visit(this);
                listAssignmentCompatibility(numOfDims, curr.type, ll);
            }
            ll.type = new ListType(curr.type,numOfDims);
        }
        else {
            listAssignmentCompatibility(currentTarget.asListType().numOfDims,
                                        currentTarget.asListType().baseType(), ll);
            ll.type = currentTarget.asListType();
        }
    }

    /**
     * Checks the type of a list statement.
     * <p><br>
     *     In C Minor, there are currently 3 list commands: {@code append}, {@code insert},
     *     and {@code remove}. This method will type check all arguments passed to the command
     *     to ensure the user correctly wrote the command. For {@code remove}, we will not type
     *     check the value that is removed since we will throw a runtime exception if the value
     *     can not be removed from the list.
     * </p>
     * @param ls List Statement
     */
    public void visitListStmt(ListStmt ls) {
        // SPECIAL CASE!!!
        // If a user writes their own function/method using the list command name, we want to rewrite
        // the current list statement as an invocation for better type checking.
        if(currentScope.hasMethodSomewhere(ls.toString())) {
            StringBuilder commandSignature = new StringBuilder(ls + "/");

            for(Expression e : ls.getAllArgs()) {
                e.visit(this);
                commandSignature.append(e.type.typeSignature());
            }

            if(currentScope.hasNameSomewhere(commandSignature.toString())) {
                Invocation in = new InvocationBuilder()
                                .setMetaData(ls)
                                .setName(new Name(ls.toString()))
                                .setArgs(ls.getAllArgs())
                                .create();

                in.replace(ls);
                in.visit(this);

                // This is needed because the list statement reference will still be stored by the VM
                // if the list statement was the only line of code the user wrote. We want to call the newly
                // created invocation and not the list statement, so this is the best solution I came up with...
                if(ls.getRootParent() == null)
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
        if(!ls.getList().type.isListType()) {
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

        if((ld.type().isArrayType() && ld.var().init().isArrayLiteral())
                || (ld.type().isListType() && ld.var().init().isListLiteral())) {
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
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ld)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_400)
                        .addErrorArgs(ld,ld.type(),ld.var().init().type)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                        .addSuggestionArgs(ld.type())
                        .generateError();
            }
        }

        ld.var().setType(ld.var().init().type);
        ld.setType(ld.var().init().type);
    }

    /**
     * Evaluates the main function for the program.
     * <p>
     *     We will make sure the main function does not return anything
     *     since its termination will mark the end of a C Minor program's
     *     execution.
     * </p>
     * @param md Main Declaration
     */
    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        currentMethod = md;

        // ERROR CHECK #1: Make sure main does not return any value
        if(!md.returnType().isVoidType()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(md)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_417)
                    .addErrorArgs(md.returnType())
                    .generateError();
        }
        super.visitMainDecl(md);
        currentScope = currentScope.closeScope();
        currentMethod = null;
    }

    /**
     * Evaluates method declaration
     * <p>
     *     For methods, we are going to check if the return types are valid,
     *     and if the correct value is returned from the method.
     * </p>
     * @param md Method Declaration
     */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.symbolTable;
        currentMethod = md;

        // ERROR CHECK #1: Make sure the method's return type is valid
        if(md.returnType().isClassType()) {
            if(!currentScope.hasNameSomewhere(md.returnType().toString())) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(md)
                        .addErrorNumber(MessageNumber.TYPE_ERROR_423)
                        .addErrorArgs(md.returnType(),md,currentClass)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1426)
                        .addSuggestionArgs(md.returnType())
                        .generateError();
            }
        }

        super.visitMethodDecl(md);

        // ERROR CHECK #2: If the method has a non-void return type, make
        //                 sure a return statement is found in the method
        if(!md.returnType().isVoidType() && !returnFound) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(md)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_424)
                    .addErrorArgs(md,currentClass,md.returnType())
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1428)
                    .generateError();
        }
        currentScope = currentScope.closeScope();
        currentMethod = null;
        returnFound = false;
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
        if(ne.isParentKeyword()) {
            if(parentFound) {
                parentFound = false;
                handler.createErrorBuilder(ScopeError.class)
                        .addLocation(ne)
                        .addErrorNumber(MessageNumber.SCOPE_ERROR_337)
                        .generateError();
            }
            parentFound = true;
            ne.type = currentClass.superClass();
        }
        else if(currentTarget != null && currentTarget.isClassOrMultiType()) {
            String targetName = currentTarget.toString();
            ClassDecl cd = null;
            if(currentTarget.isClassType()) {
                cd = currentScope.findName(targetName).decl().asTopLevelDecl().asClassDecl();

                // ERROR CHECK #1: Make sure the class name exists if we are
                //                 evaluating a complex field expression
                if(!cd.symbolTable.hasName(ne.toString())) {
                    // We need to reset currentTarget if there's an error during interpretation
//                    if(interpretMode)
//                        currentTarget = null;
                    handler.createErrorBuilder(ScopeError.class)
                            .addLocation(ne.getRootParent())
                            .addErrorNumber(MessageNumber.SCOPE_ERROR_329)
                            .addErrorArgs(ne,targetName)
                            .generateError();
                }
            }
            else {
                boolean found = false;
                for(ClassType ct : currentTarget.asMultiType().getAllTypes()) {
                    cd = currentScope.findName(ct.toString()).decl().asTopLevelDecl().asClassDecl();
                    if(cd.symbolTable.hasName(ne.toString())) {
                        found = true;
                        break;
                    }
                }
                // ERROR CHECK #2: Make sure the class that declared the
                //                 field was found for a MultiTyped name
                if(!found) {
//                    if (interpretMode)
//                        currentTarget = null;
                    handler.createErrorBuilder(ScopeError.class)
                            .addLocation(ne)
                            .addErrorNumber(MessageNumber.SCOPE_ERROR_309)
                            .addErrorArgs(ne.toString(), targetName)
                            .generateError();
                }
            }
            ne.type = cd.symbolTable.findName(ne.toString()).decl().asFieldDecl().type();
        }
        else {
            AST decl = currentScope.findName(ne.toString()).decl();
            if(decl.isStatement())
                ne.type = decl.asStatement().asLocalDecl().type();
            else if(decl.isParamDecl())
                ne.type = decl.asParamDecl().type();
            else if(decl.isFieldDecl())
                ne.type = decl.asFieldDecl().type();
            else {
                TopLevelDecl tDecl = decl.asTopLevelDecl();
                if(tDecl.isEnumDecl())
                    ne.type = tDecl.asEnumDecl().type();
                else if(tDecl.isGlobalDecl())
                    ne.type = tDecl.asGlobalDecl().type();
                else
                    ne.type = new ClassType(tDecl.asClassDecl().name());
            }
        }
    }

    /**
     * <p>
     *     In C Minor, a constructor is automatically generated for the user.
     *     Thus, we do not need to check if a new expression can be called for
     *     the class we are trying to instantiate. Instead, we only need to check
     *     if for each argument, the type of the value corresponds to the type of
     *     the field declaration we're saving the argument into.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.getClassType()).decl().asTopLevelDecl().asClassDecl();

        for(Var v : ne.getInitialFields()) {
            Type fType = cd.symbolTable.findName(v.toString()).decl().asFieldDecl().type();

            if((fType.isArrayType() && v.init().isArrayLiteral()) || (fType.isListType() && v.init().isListLiteral())) {
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
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(ne)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_420)
                            .addErrorArgs(v.toString(),fType,v.init().type)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1400)
                            .addSuggestionArgs(fType)
                            .generateError();
                }
            }
        }
        ne.type = ne.getClassType();
        ne.type.asClassType().setInheritedTypes(cd.getInheritedClasses());

        // If the new expression is bounded to an instantiated class, we now want to visit
        // said class and perform type checking based on the provided arguments.
        if(ne.createsFromTemplate())
            ne.getInstantiatedClass().visit(this);
    }

    /**
     * Evaluates the type of each output expression.
     * <p><br>
     *     For each output expression, we want to evaluate its type, so
     *     we can know how to display its value at runtime. We will also
     *     set the output statement's type to be a default {@code Void}
     *     type since it doesn't represent anything.
     * </p>
     * @param os Output Statement
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.getOutExprs())
            e.visit(this);
        os.type = new VoidType();
    }

    /**
     * Evaluates a return statement.
     * <p>
     *     A return statement will be found inside of a function or method. If
     *     the return statement has a value, then we will evaluate and type
     *     check the value during this visit.
     * </p>
     * @param rs Return Statement
     */
    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.expr() != null) {
            rs.expr().visit(this);

            Type rType;
            if(currentMethod.isMethodDecl())
                rType = currentMethod.asMethodDecl().returnType();
            else if(currentMethod.asTopLevelDecl().isFuncDecl())
                rType = currentMethod.asTopLevelDecl().asFuncDecl().returnType();
            else
                rType = currentMethod.asTopLevelDecl().asMainDecl().returnType();

            // ERROR CHECK #1: A Void function can not return anything
            if(rType.isVoidType()) {
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class).addLocation(rs);
                if(currentMethod.isMethodDecl()) {
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_426)
                          .addErrorArgs(currentMethod,currentClass,rs.expr().type)
                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1430)
                          .generateError();
                }
                else {
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_425)
                          .addErrorArgs(currentMethod,rs.expr().type)
                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1429)
                          .generateError();
                }
            }
            // ERROR CHECK #2: Check if the return value's type matches the return type
            if(!Type.assignmentCompatible(rType,rs.expr().type)) {
                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class).addLocation(rs);
                if(currentMethod.isMethodDecl()) {
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_428)
                          .addErrorArgs(currentMethod,currentClass,rs.expr().type,rType)
                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1428)
                          .generateError();
                }
                else {
                        eb.addErrorNumber(MessageNumber.TYPE_ERROR_427)
                          .addErrorArgs(currentMethod,rs.expr().type,rType)
                          .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1427)
                          .generateError();
                }
            }
        }
        returnFound = true;
    }

    /**
     * Evaluates the type of a retype statement.
     * @param rt Retype Statement
     */
    public void visitRetypeStmt(RetypeStmt rt) {
        rt.getName().visit(this);
        Type objType = rt.getName().type;

        // ERROR CHECK #1: Make sure the LHS does represent an object
        if(!objType.isClassOrMultiType()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(rt)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_441)
                    .addErrorArgs(rt.getName(),objType)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1438)
                    .generateError();
        }

        rt.getNewObject().visit(this);
        ClassType newObjType = rt.getNewObject().type.asClassType();
        Type objBaseType = objType.isMultiType() ? objType.asMultiType().getInitialType() : objType;

        // ERROR CHECK #2: Make sure the types are class assignment compatible
        if(!ClassType.classAssignmentCompatibility(objBaseType,newObjType)) {
            ClassDecl cd = currentScope.findName(objBaseType.toString()).decl().asTopLevelDecl().asClassDecl();

            handler.createErrorBuilder(TypeError.class)
                    .addLocation(rt)
                    .addErrorNumber(MessageNumber.TYPE_ERROR_442)
                    .addErrorArgs(rt.getName(),newObjType)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1439)
                    .addSuggestionArgs(rt.getName())
                    .generateError();
        }
        
        if(inControlStmt) {
            if(objType.isMultiType())
                objType.asMultiType().addType(newObjType);
            else {
                MultiType mt = MultiType.create(objType.asClassType(),newObjType);
                setVarType(rt.getName().toString(),mt);
            }
        }
        else
            setVarType(rt.getName().toString(),newObjType);
    }

    /**
     * Evaluates the type of a reference to ThisStmt
     * <p>
     *     If we have a <code>this</code> written in the code, then the
     *     type will be evaluated to be whatever the current class is.
     * </p>
     * @param t ThisStmt
     */
    public void visitThis(ThisStmt t) { t.type = new ClassType(currentClass.toString()); }

    /**
     * Evaluates the type of a unary expression
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
     *     contains a class type, then we will let {@code visitFieldExpr} handle
     *     the type checking for us.
     * </p>
     * @param ue Unary Expression
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.getExpr().visit(this);

        /*
            If the unary expression evaluates to be an object, then we might have a
            unary operator overload. Thus, we are going to create a field expression
            to replace the current unary expression and let other visitors handle the
            type checking for us.
        */
        if(ue.getExpr().type.isClassOrMultiType()) {
            FieldExpr unaryOverload = new FieldExprBuilder()
                                          .setTarget(ue.getExpr())
                                          .setAccessExpr(
                                              new InvocationBuilder()
                                              .setName(new Name("operator"+ue.getUnaryOp()))
                                              .setArgs(new Vector<>())
                                              .create()
                                          )
                                          .create();
            unaryOverload.replace(ue);
            unaryOverload.visit(this);
            return;
        }

        switch(ue.getUnaryOp().toString()) {
            case "~":
                // ERROR CHECK #1: A bitwise negation can only occur with a discrete type
                if(!ue.getExpr().type.isDiscreteType()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(ue)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_408)
                            .addErrorArgs(ue.getUnaryOp(),ue.getExpr().type)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1410)
                            .addSuggestionArgs(ue.getUnaryOp())
                            .generateError();
                }
                ue.type = new DiscreteType(Discretes.BOOL);
                break;
            case "not":
                // ERROR CHECK #2: A 'not' operation can only occur on a boolean expression
                if(!ue.getExpr().type.isBool()) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(ue)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_408)
                            .addErrorArgs(ue.getUnaryOp(),ue.getExpr().type)
                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1411)
                            .addSuggestionArgs(ue.getUnaryOp())
                            .generateError();
                }
                ue.type = new DiscreteType(Discretes.BOOL);
                break;
        }
    }

    /**
     * Evaluates the type of the while statement's conditional expression
     * <p>
     *     When we visit a while statement, the only explicit check we do here
     *     is to make sure the conditional expression of the while loop can be
     *     evaluated into a boolean. All other type checks will be handled in
     *     other visits.
     * </p>
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        boolean prev = inControlStmt;
        ws.condition().visit(this);

        // ERROR CHECK #1: The while loop's condition must be a boolean
        if(!ws.condition().type.isBool()) {
            handler.createErrorBuilder(TypeError.class)
                    .addLocation(ws.condition())
                    .addErrorNumber(MessageNumber.TYPE_ERROR_411)
                    .addErrorArgs(ws.condition().type)
                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1417)
                    .generateError();
        }

        currentScope = ws.symbolTable;
        inControlStmt = true;
        ws.whileBlock().visit(this);
        inControlStmt = prev;
        currentScope = currentScope.closeScope();
    }
}
