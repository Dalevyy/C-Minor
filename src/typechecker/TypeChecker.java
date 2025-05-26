package typechecker;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.ArrayExpr;
import ast.expressions.ArrayLiteral;
import ast.expressions.BinaryExpr;
import ast.expressions.CastExpr;
import ast.expressions.Expression;
import ast.expressions.FieldExpr;
import ast.expressions.InStmt;
import ast.expressions.Invocation;
import ast.expressions.ListLiteral;
import ast.expressions.Literal;
import ast.expressions.Literal.ConstantKind;
import ast.expressions.Literal.LiteralBuilder;
import ast.expressions.NameExpr;
import ast.expressions.NewExpr;
import ast.expressions.This;
import ast.expressions.UnaryExpr;
import ast.misc.Name;
import ast.misc.Var;
import ast.operators.AssignOp.AssignType;
import ast.statements.AssignStmt;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.ListStmt;
import ast.statements.LocalDecl;
import ast.statements.ReturnStmt;
import ast.statements.RetypeStmt;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.MainDecl;
import ast.topleveldecls.TopLevelDecl;
import ast.types.Type;
import ast.types.ArrayType;
import ast.types.ClassType;
import ast.types.DiscreteType;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType.EnumTypeBuilder;
import ast.types.ListType;
import ast.types.MultiType;
import ast.types.ScalarType;
import ast.types.ScalarType.Scalars;
import ast.types.VoidType;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeErrorFactory;
import messages.errors.type.TypeErrorFactory;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private ClassDecl currentClass;
    private AST currentMethod;
    private Type currentTarget;
    private final TypeErrorFactory generateTypeError;
    private final ScopeErrorFactory generateScopeError;
    private final Vector<String> errors;
    private boolean returnFound = false;

    /**
     * Creates type checker in compilation mode
     */
    public TypeChecker() {
        this.currentScope = null;
        this.currentMethod = null;
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

    /**
     * Checks if an array literal is assignment compatible with an array type.<br><br>
     * <p>
     *     This is a recursive algorithm to verify whether an array literal can
     *     be assigned to an array type in C Minor. This algorithm was based off
     *     a similar algorithm found in Dr. Pedersen's textbook for compilers.
     * </p>
     * @param currDepth Current level of recursion (final depth is 1)
     * @param t Array type
     * @param dims Expressions representing the dimensions for the array
     * @param curr Array Literal aka the current array literal we are checking
     * @return Boolean - True if assignment compatible and False otherwise
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
        else if(currDepth > 1) {
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
        else
            return false;
    }

    /**
     * Checks if a list literal is assignment compatible with a list type.<br><br>
     * <p>
     *     This is a recursive algorithm to check if a list literal can be assigned
     *     to a list type in C Minor. This algorithm is based on the algorithm used
     *     for array assignment compatibility albeit it's simpler and has less error checks.
     * </p>
     * @param currDepth Current level of recursion (final depth is 10
     * @param baseType Base type of the list
     * @param curr List literal aka the current list literal we are checking
     * @return Boolean - True if assignment compatible and False otherwise
     */
    private boolean listAssignmentCompatibility(int currDepth, Type baseType, ListLiteral curr) {
        if(currDepth == 1) {
            for(Expression e : curr.inits()) {
                e.visit(this);
                // ERROR CHECK #1: Make sure the current expression matches the type of the list
                if(!Type.assignmentCompatible(baseType,e.type)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(curr)
                                .addErrorType(MessageType.TYPE_ERROR_445)
                                .addArgs(baseType,e.type)
                                .error()
                    );
                    return false;
                }
            }
            return true;
        }
        else if(currDepth > 1) {
            for(Expression e : curr.inits()) {
                if(!e.isListLiteral()) {
                    // ERROR CHECK #2: Make sure everything is a list if we're not at depth = 1
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(curr)
                                .addErrorType(MessageType.TYPE_ERROR_455)
                                .error()
                    );
                    return false;
                }
                listAssignmentCompatibility(currDepth-1,baseType,e.asListLiteral());
            }
            return true;
        }
        else
            return false;
    }

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
        ae.arrayTarget().visit(this);

        // ERROR CHECK #1: Make sure the target represents an array or list
        if(!ae.arrayTarget().type.isArrayType() && !ae.arrayTarget().type.isListType()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ae)
                        .addErrorType(MessageType.TYPE_ERROR_434)
                        .addArgs(ae.arrayTarget().toString())
                        .error()
            );
        }

        if(ae.arrayTarget().type.isArrayType()) {

        }

        Type currType;
        if(currentTarget != null) {
            ClassDecl cd = currentScope.findName(currentTarget.toString()).decl().asTopLevelDecl().asClassDecl();
            currType = cd.symbolTable.findName(ae.arrayTarget().toString()).decl().asFieldDecl().type().asArrayType();
        }
        else {
            currType = currentScope.findName(ae.arrayTarget().toString()).decl().getType();
        }

        if(currType.isArrayType()) {
            // ERROR CHECK #2: Make sure the number of indices matches
            //                 the number of dimensions for the array
            if(currType.asArrayType().numOfDims != ae.arrayIndex().size()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ae)
                            .addErrorType(MessageType.TYPE_ERROR_448)
                            .addArgs(ae.arrayTarget().toString(),currType.asArrayType().numOfDims,ae.arrayIndex().size())
                            .error()
                );
            }
        }
        else {
            // ERROR CHECK #3: Make sure the number of indices matches
            //                 the number of dimensions for the list
            if(currType.asListType().numOfDims != ae.arrayIndex().size()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ae)
                            .addErrorType(MessageType.TYPE_ERROR_448)
                            .addArgs(ae.arrayTarget().toString(),currType.asListType().numOfDims,ae.arrayIndex().size())
                            .error()
                );
            }
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
        if(currType.isArrayType())
            ae.type = currType.asArrayType().baseType();
        else
            ae.type = currType.asListType().baseType();
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
            al.type = new ArrayType();
        }
        else {
            arrayAssignmentCompatibility(currentTarget.asArrayType().numOfDims,
                                         currentTarget.asArrayType().baseType(),
                                         al.arrayDims(), al);
            al.type = currentTarget.asArrayType();
        }

    }

    /**
     *     If we want to assign a new value to a variable, we need to make sure the
     *     value's type matches the type of the variable.
     *
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
            errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(as)
                            .addErrorType(MessageType.TYPE_ERROR_450)
                            .error()
            );
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
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(as)
                    .addErrorType(MessageType.TYPE_ERROR_402)
                    .addArgs(as.LHS().toString(),lType,rType)
                    .error());
        }

        if(as.assignOp().getAssignOp() == AssignType.PLUSEQ) {
            // ERROR CHECK #2: For a '+=' operation, the only allowed types
            //                 are Int, Real, String, and Object
            if (lType.isBool() || lType.isChar())
                errors.add(new ErrorBuilder(generateTypeError, interpretMode)
                        .addLocation(as)
                        .addErrorType(MessageType.TYPE_ERROR_403)
                        .addArgs(as.assignOp().toString(), lType)
                        .error());
        }
        else {
            // ERROR CHECK #3: For all other assignment operators, the types
            //                 Int, Real, and Object have to be used
            if(lType.isBool() || lType.isChar() || lType.isString())
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(as)
                        .addErrorType(MessageType.TYPE_ERROR_403)
                        .addArgs(as.assignOp().toString(),lType)
                        .error());
        }
    }

    /**
     * <p>
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
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
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
                // ERROR CHECK #2: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                // ERROR CHECK #3: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1401)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
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
                // ERROR CHECK #4: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be.getParent())
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                // ERROR CHECK #5: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1402)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                be.type = lType;
                break;
            }
            case "<<":
            case ">>": {
                // ERROR CHECK #6: Both LHS and RHS have to be an INT for shift operations
                if(!lType.isInt() || !rType.isInt()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1403)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "&":
            case "|":
            case "^": {
                // ERROR CHECK #7: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1400)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                // ERROR CHECK #8: Make sure both types are discrete
                if(!lType.isDiscreteType() || !rType.isDiscreteType()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1404)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                if(binOp.equals("^")) { be.type = new DiscreteType(Discretes.INT); }
                else { be.type = new DiscreteType(Discretes.BOOL); }
                break;
            }
            case "and":
            case "or": {
                // ERROR CHECK #9: Make sure both types are Bool
                if(!lType.isBool() || !rType.isBool()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1405)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                // ERROR CHECK #10: Make sure the LHS is not a class name
                if(!lType.isClassType()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(be)
                                .addErrorType(MessageType.TYPE_ERROR_404)
                                .addArgs(lType,rType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1406)
                                .addArgsForSuggestion(binOp)
                                .error()
                    );
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
        }
    }

    /**
     * <p>
     *     In C Minor, there are four valid cast expressions.
     *     <ol>
     *         <li> Char <=> Int</li>
     *         <li> Int <=> Real</li>
     *         <li> Char => String</li>
     *         <li> Parent Object <= Child Object (Runtime)</li>
     *     </ol>
     *     For mixed type expressions, this means the programmer must perform
     *     explicit type casts or else the compiler will generate a typing error.
     * </p>
     * @param ce Cast Expression
     */
    public void visitCastExpr(CastExpr ce) {
        ce.castExpr().visit(this);

        if(ce.castExpr().type.isInt()) {
            // ERROR CHECK #1: An Int can only be typecasted into a Char and a Real
            if(!ce.castType().isChar() && !ce.castType().isReal() && !ce.castType().isInt()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ce)
                            .addErrorType(MessageType.TYPE_ERROR_408)
                            .error()
                );
            }
        }
        else if(ce.castExpr().type.isChar()) {
            // ERROR CHECK #2: A Char can only be type casted into an Int and a String
            if(!ce.castType().isInt() && !ce.castType().isString() && !ce.castType().isChar()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ce)
                            .addErrorType(MessageType.TYPE_ERROR_409)
                            .error()
                );
            }
        }
        else if(ce.castExpr().type.isReal()) {
            // ERROR CHECK #3: A Real can only be type casted into an Int
            if(!ce.castType().isInt() && !ce.castType().isReal()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(ce)
                            .addErrorType(MessageType.TYPE_ERROR_410)
                            .error()
                );
            }
        }
        else {
            // By default, all other cast expressions will be considered invalid
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(MessageType.TYPE_ERROR_411)
                        .addArgs(ce.castExpr().type,ce.castType())
                        .error()
            );
        }

        ce.type = ce.castType();
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
        cs.choiceExpr().visit(this);
        Type choiceType = cs.choiceExpr().type;

        // ERROR CHECK #1: Choice statements only support Int, Char, and String
        if(!(choiceType.isInt() || choiceType.isChar() || choiceType.isString())) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(cs.choiceExpr())
                        .addErrorType(MessageType.TYPE_ERROR_426)
                        .addArgs(choiceType)
                        .addSuggestType(MessageType.TYPE_SUGGEST_1409)
                        .error()
            );
        }

        for(CaseStmt curr : cs.caseStmts()) {
            curr.choiceLabel().visit(this);
            Type labelType = curr.choiceLabel().leftLabel().type;

            // ERROR CHECK #2: Make sure the case label's type corresponds
            //                 to the type of the choice statement expression
            if(!Type.assignmentCompatible(labelType,choiceType)) {
                errors.add(
                    new ErrorBuilder(generateTypeError, interpretMode)
                            .addLocation(curr.choiceLabel())
                            .addErrorType(MessageType.TYPE_ERROR_427)
                            .addArgs(labelType, choiceType)
                            .addSuggestType(MessageType.TYPE_SUGGEST_1410)
                            .error()
                );
            }
            if(curr.choiceLabel().rightLabel() != null) {
                // ERROR CHECK #3: If we allow to choose from String values, then
                //                 there is only one label allowed per case statement
                if (choiceType.isString()) {
                    errors.add(
                        new ErrorBuilder(generateTypeError, interpretMode)
                                .addLocation(curr.choiceLabel())
                                .addErrorType(MessageType.TYPE_ERROR_432)
                                .error()
                    );
                }

                labelType = curr.choiceLabel().rightLabel().type;
                // ERROR CHECK #4: Same as ERROR CHECK #2, but now for the right label
                if(!Type.assignmentCompatible(labelType,choiceType)) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(curr.choiceLabel())
                                .addErrorType(MessageType.TYPE_ERROR_426)
                                .addArgs(labelType,choiceType)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1410)
                                .error()
                    );
                }
                // ERROR CHECK #5: Make sure label's right constant is greater than left constant
                if(choiceType.isInt()) {
                    int lLabel = Integer.parseInt(curr.choiceLabel().leftLabel().toString());
                    int rLabel = Integer.parseInt(curr.choiceLabel().rightLabel().toString());
                    if(rLabel <= lLabel) {
                        errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(curr.choiceLabel())
                                    .addErrorType(MessageType.TYPE_ERROR_433)
                                    .error()
                        );
                    }
                }
                else if(choiceType.isChar()) {
                    char lLabel = curr.choiceLabel().leftLabel().getText().charAt(1);
                    char rLabel = curr.choiceLabel().rightLabel().getText().charAt(1);
                    if(rLabel <= lLabel) {
                        errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(curr.choiceLabel())
                                .addErrorType(MessageType.TYPE_ERROR_433)
                                .error()
                        );
                    }
                }
            }
            currentScope = curr.symbolTable;
            curr.caseBlock().visit(this);
            currentScope = currentScope.closeScope();
        }

        currentScope = cs.symbolTable;
        cs.otherBlock().visit(this);
        currentScope = currentScope.closeScope();
    }

    /**
     *     For a class declaration, we will set the class type that represents
     *     the inheritance hierarchy of the class before we proceed to visit the
     *     class body.
     * @param cd Class Declaration
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
        currentMethod = cd;
        currentClass = cd;
        super.visitClassDecl(cd);
        currentMethod = null;
        currentClass = null;
        currentScope = currentScope.closeScope();
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
        currentScope = ds.symbolTable;
        ds.doBlock().visit(this);
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);
        // ERROR CHECK #1: The do while loop's condition must be a boolean
        if(!ds.condition().type.isBool()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ds.condition())
                        .addErrorType(MessageType.TYPE_ERROR_407)
                        .addArgs(ds.condition().type)
                        .error()
            );
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

    /**
     * Evaluates the type of a field expression<br>
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

        // ERROR CHECK #1: We want to make sure the target is indeed an object,
        //                 so make sure it's assigned a class type
        if(!fe.fieldTarget().type.isClassType() && !fe.fieldTarget().type.isMultiType()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fe)
                    .addErrorType(MessageType.TYPE_ERROR_416)
                    .addArgs(fe.fieldTarget().toString(),currentTarget)
                    .error()
            );
        }

        Type oldTarget = currentTarget;
        currentTarget = fe.fieldTarget().type;
        fe.accessExpr().visit(this);

        fe.type = fe.accessExpr().type;
        currentTarget = oldTarget;
    }

    /**
     *     Unlike the other two loop statements, we have a few error checks that
     *     need to be done with for statements. We mainly need to make sure the
     *     for loop has a loop control variable that represents an Int, and its
     *     condition contains Int literals. Once done, then there's nothing else
     *     FOR us to type check here. ;)
     * @param fs For Statement
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

    /**
     * Evaluates function declaration
     * <p>
     *     For functions, we are going to check if the return types are valid,
     *     and if the correct value is returned from the function.
     * </p>
     * @param fd Function Declaration
     */
    public void visitFuncDecl(FuncDecl fd) {
        currentScope = fd.symbolTable;
        currentMethod = fd;

        // ERROR CHECK #1: Make sure function return type is valid
        if(fd.returnType().isClassType()) {
            if(!currentScope.hasNameSomewhere(fd.returnType().toString())) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(fd)
                            .addErrorType(MessageType.TYPE_ERROR_418)
                            .addArgs(fd.returnType().toString(),fd.toString())
                            .error()
                );
            }
        }

        super.visitFuncDecl(fd);

        // ERROR CHECK #2: If the function has a non-void return type, make
        //                 sure a return statement is found in the function
        if(!fd.returnType().isVoidType() && !returnFound) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(MessageType.TYPE_ERROR_419)
                    .addArgs(fd.toString(),fd.returnType().toString())
                    .error()
            );
        }
        currentScope = currentScope.closeScope();
        returnFound = false;
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
        is.condition().visit(this);

        // ERROR CHECK #1: The if statement's conditional expression must be a boolean
        if(!is.condition().type.isBool()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(is.condition())
                        .addErrorType(MessageType.TYPE_ERROR_406)
                        .addArgs(is.condition().type)
                        .error()
            );
        }

        currentScope = is.symbolTableIfBlock;
        is.ifBlock().visit(this);
        currentScope = currentScope.closeScope();

        for(IfStmt e : is.elifStmts()) { e.visit(this); }

        if(is.elseBlock() != null) {
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

    /**
     * Evaluates input statements.
     * <p>
     *     For input statements, we want to make sure a user can only input
     *     values into a variable that represents a primitive type (not counting
     *     an enum).
     * </p>
     * @param is Input Statement
     */
    public void visitInStmt(InStmt is) {
        for(Expression e : is.inExprs()) {
            e.visit(this);
            // ERROR CHECK #1: Make sure expression is discrete (not enum) or scalar
            if(!(e.type.isDiscreteType() || e.type.isScalarType()) || e.type.isEnumType()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(is)
                            .addErrorType(MessageType.TYPE_ERROR_449)
                            .error()
                );
            }
        }
    }

    /**
     * Evaluates the type of a invocation.<br>
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
        StringBuilder funcSignature = new StringBuilder(in.toString() + "/");

        for(Expression e : in.arguments()) {
            e.visit(this);
            funcSignature.append(e.type.typeSignature());
        }

        // Function Invocation
        if(currentTarget == null && currentClass == null) {
            in.targetType = new VoidType();

            if(in.toString().equals("length")) {
                // ERROR CHECK #1: Make sure 'length' call only has one argument
                if(in.arguments().size() != 1) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(in)
                                .addErrorType(MessageType.TYPE_ERROR_451)
                                .error()
                    );
                }
                // ERROR CHECK #2: Make sure argument evaluates to an array or list
                if(!(in.arguments().get(0).type.isArrayType() || in.arguments().get(0).type.isListType())) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(in)
                                    .addErrorType(MessageType.TYPE_ERROR_452)
                                    .error()
                    );
                }
                in.type = new DiscreteType(Discretes.INT);
                return;
            }

            // ERROR CHECK #3: Check if function overload exists
            if(!currentScope.hasNameSomewhere(funcSignature.toString())) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.TYPE_ERROR_428)
                            .addArgs(in.toString())
                            .error()
                );
            }

            FuncDecl fd = currentScope.findName(funcSignature.toString()).decl().asTopLevelDecl().asFuncDecl();
            in.type = fd.returnType();
        }
        // Method Invocation
        else {
            ClassDecl cd = null;
            if(currentTarget.isMultiType()) {
                for(ClassType ct : currentTarget.asMultiType().getAllTypes()) {
                    cd = currentScope.findName(ct.toString()).decl().asTopLevelDecl().asClassDecl();
                    if(cd.symbolTable.hasMethod(in.toString()))
                        break;
                }
            }
            else
                cd = currentScope.findName(currentTarget.toString()).decl().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #4: Make sure the method was declared in the class
            if(!cd.symbolTable.hasMethod(in.toString())) {
                errors.add(
                    new ErrorBuilder(generateScopeError, interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.SCOPE_ERROR_321)
                            .addArgs(in.toString())
                            .error()
                );
            }

            // ERROR CHECK #5: Check if the method overload exists
            while(!cd.symbolTable.hasName(funcSignature.toString())) {
                if(cd.superClass() == null) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(in)
                                .addErrorType(MessageType.TYPE_ERROR_429)
                                .addArgs(in.toString())
                                .error()
                    );
                }
                cd = currentScope.findName(cd.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
            }

            MethodDecl md = cd.symbolTable.findName(funcSignature.toString()).decl().asMethodDecl();
            in.targetType = new ClassType(cd.toString());
            in.type = md.returnType();
            currentTarget = md.returnType();
        }
        in.setInvokeSignature(funcSignature.toString());
    }

    /**
     * Evaluates the type of a literal
     * <p>
     *     We will create a type for the literal based on its assigned
     *     constant kind value. This visit only handles primitive type
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
        if(currentTarget == null || !currentTarget.isListType()) {
            super.visitListLiteral(ll);
            ll.type = new ListType();
        }
        else {
            listAssignmentCompatibility(currentTarget.asListType().numOfDims,
                                        currentTarget.asListType().baseType(), ll);
            ll.type = currentTarget.asListType();
        }
    }

    public void visitListStmt(ListStmt ls) {
        switch(ls.getCommand()) {
            case APPEND:
                // ERROR CHECK #1: Make sure append only takes in 2 arguments.
                if(ls.getAllArgs().size() != 2) {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(ls)
                                .addErrorType(MessageType.TYPE_ERROR_457)
                                .addArgs("append",2,ls.getAllArgs().size())
                                .error()
                    );
                }
                ls.getListName().visit(this);
                // ERROR CHECK #2: Make sure a valid list type is given for the 1st argument
                if(!ls.getListName().type.isListType()) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_458)
                                    .addArgs("append")
                                    .error()
                    );
                }
                //  Type.assignmentCompatible(ls.getListName().type,ls.getAllArgs().get(1).type)
                ls.getAllArgs().get(1).visit(this);
                if(!ls.getListName().type.asListType().baseTypeCompatible(ls.getAllArgs().get(1).type)) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_459)
                                    .addArgs("append")
                                    .error()
                    );
                }
                break;
            case INSERT:
                // ERROR CHECK #1: Make sure append only takes in 3 arguments.
                if(ls.getAllArgs().size() != 3) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_457)
                                    .addArgs("insert",3,ls.getAllArgs().size())
                                    .error()
                    );
                }
                ls.getListName().visit(this);
                // ERROR CHECK #2: Make sure a valid list type is given for the 1st argument
                if(!ls.getListName().type.isListType()) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_458)
                                    .addArgs("insert")
                                    .error()
                    );
                }

                ls.getAllArgs().get(1).visit(this);
                if(!ls.getAllArgs().get(1).type.isInt()) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_460)
                                    .error()
                    );
                }

                ls.getAllArgs().get(2).visit(this);
                if(!ls.getListName().type.asListType().baseTypeCompatible(ls.getAllArgs().get(2).type)) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_461)
                                    .error()
                    );
                }
                break;
            case REMOVE:
                // ERROR CHECK #1: Make sure append only takes in 2 arguments.
                if(ls.getAllArgs().size() != 2) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_457)
                                    .addArgs("remove",2,ls.getAllArgs().size())
                                    .error()
                    );
                }
                ls.getListName().visit(this);
                // ERROR CHECK #2: Make sure a valid list type is given for the 1st argument
                if(!ls.getListName().type.isListType()) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_458)
                                    .addArgs("remove")
                                    .error()
                    );
                }
                //  Type.assignmentCompatible(ls.getListName().type,ls.getAllArgs().get(1).type)
                ls.getAllArgs().get(1).visit(this);
                if(!ls.getListName().type.asListType().baseTypeCompatible(ls.getAllArgs().get(1).type)) {
                    errors.add(
                            new ErrorBuilder(generateTypeError,interpretMode)
                                    .addLocation(ls)
                                    .addErrorType(MessageType.TYPE_ERROR_459)
                                    .addArgs("remove")
                                    .error()
                    );
                }
                break;

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
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.TYPE_ERROR_417)
                        .error()
            );
        }
        super.visitMainDecl(md);
        currentScope = currentScope.closeScope();
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
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(md)
                        .addErrorType(MessageType.TYPE_ERROR_420)
                        .addArgs(md.returnType().toString(),md.toString())
                        .error()
                );
            }
        }

        super.visitMethodDecl(md);

        // ERROR CHECK #2: If the method has a non-void return type, make
        //                 sure a return statement is found in the method
        if(!md.returnType().isVoidType() && !returnFound) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(md)
                    .addErrorType(MessageType.TYPE_ERROR_421)
                    .addArgs(md.toString(),md.returnType().toString())
                    .error()
            );
        }
        currentScope = currentScope.closeScope();
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
        if(currentTarget != null && currentTarget.isClassType()) {
            ClassDecl cd = currentScope.findName(currentTarget.toString()).decl().asTopLevelDecl().asClassDecl();
            // ERROR CHECK #1: Make sure the class name exists if we are
            //                 evaluating a complex field expression
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
        else {
            AST decl = currentScope.findName(ne.toString()).decl();
            if(decl.isStatement()) { ne.type = decl.asStatement().asLocalDecl().type(); }
            else if(decl.isParamDecl()) { ne.type = decl.asParamDecl().type(); }
            else if(decl.isFieldDecl()) { ne.type = decl.asFieldDecl().type(); }
            else {
                TopLevelDecl tDecl = decl.asTopLevelDecl();
                if(tDecl.isEnumDecl()) { ne.type = tDecl.asEnumDecl().type(); }
                else if(tDecl.isGlobalDecl()) { ne.type = tDecl.asGlobalDecl().type(); }
                else { ne.type = new ClassType(tDecl.asClassDecl().name()); }
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
        ClassDecl cd = currentScope.findName(ne.classType().toString()).decl().asTopLevelDecl().asClassDecl();

        for(Var v : ne.args()) {
            Type fType = cd.symbolTable.findName(v.toString()).decl().asFieldDecl().type();

            if(fType.isArrayType() && v.init().isArrayLiteral()) {
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
            else {
                if(currentMethod.asTopLevelDecl().isFuncDecl())
                    rType = currentMethod.asTopLevelDecl().asFuncDecl().returnType();
                else
                    rType = currentMethod.asTopLevelDecl().asMainDecl().returnType();
            }

            // ERROR CHECK #1: A Void function can not return anything
            if(rType.isVoidType()) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(rs)
                            .addErrorType(MessageType.TYPE_ERROR_413)
                            .addArgs(rs.expr().type,currentMethod.toString())
                            .error()
                );
            }
            // ERROR CHECK #2: Check if the return value's type matches the return type
            if(!Type.assignmentCompatible(rType,rs.expr().type)) {
                errors.add(
                    new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(rs)
                            .addErrorType(MessageType.TYPE_ERROR_414)
                            .addArgs(rs.expr().type,currentMethod.toString(),rType)
                            .error()
                );
            }
        }
        returnFound = true;
    }

    public void visitRetypeStmt(RetypeStmt rs) {
        rs.getName().visit(this);
        Type lType = rs.getName().type;

        rs.getNewObject().visit(this);
        ClassType rType = rs.getNewObject().type.asClassType();

        // ERROR CHECK #1: Make sure the LHS does represent an object
        if(!lType.isClassType() && !lType.isMultiType()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(rs)
                        .addErrorType(MessageType.TYPE_ERROR_453)
                        .addArgs(lType)
                        .error()
            );
        }

        // ERROR CHECK #2: Make sure the types are class assignment compatible
        if(!ClassType.classAssignmentCompatibility(lType,rType)) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(rs)
                        .addErrorType(MessageType.TYPE_ERROR_454)
                        .addArgs(rs.getName().toString(),rType)
                        .error()
            );
        }

        if(!lType.isMultiType()) {
            Vector<ClassType> types = new Vector<>();
            types.add(lType.asClassType());
            types.add(rType.asClassType());
            MultiType mt = new MultiType(lType.asClassType(),types);

            AST decl = currentScope.findName(rs.getName().toString()).decl();
            if(decl.isTopLevelDecl() && decl.asTopLevelDecl().isGlobalDecl())
                decl.asTopLevelDecl().asGlobalDecl().setType(mt);
            else if(decl.isParamDecl())
                decl.asParamDecl().setType(mt);
            else if(decl.isFieldDecl())
                decl.asFieldDecl().setType(mt);
            else
                decl.asStatement().asLocalDecl().setType(mt);
        }
        else
            lType.asMultiType().addType(rType.asClassType());
    }

    /**
     * Evaluates the type of a reference to This
     * <p>
     *     If we have a <code>this</code> written in the code, then the
     *     type will be evaluated to be whatever the current class is.
     * </p>
     * @param t This
     */
    public void visitThis(This t) {
        t.type = new ClassType(currentClass.toString());
    }

    /**
     * Evaluates the type of a unary expression
     * <p>
     *     In C Minor, there are only two unary operators. The following is
     *     how we will perform the type checking.
     *     <ol>
     *         <li>
     *             '~'
     *             <ul>
     *                 <li>Operand Type: Int, Real</li>
     *                 <li>Unary Expression Type: Operand Type</li>
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
     *     Both unary operators may also be overloaded, so we also will check if
     *     the overload was defined by the user.
     * </p>
     * @param ue Unary Expression
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.expr().visit(this);
        switch(ue.unaryOp().toString()) {
            case "~":
                // ERROR CHECK #1: An integer or real can be the only types that are negated
                if(ue.expr().type.isInt()) { ue.type = new DiscreteType(Discretes.INT); }
                else if(ue.expr().type.isReal()) { ue.type = new ScalarType(Scalars.REAL); }
                else {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(ue)
                                .addErrorType(MessageType.TYPE_ERROR_405)
                                .addArgs(ue.expr().type)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1407)
                                .addArgsForSuggestion(ue.unaryOp().toString())
                                .error()
                    );
                }
                break;
            case "not":
                // ERROR CHECK #2: A 'not' operation can only occur on a boolean expression
                if(ue.expr().type.isBool()) { ue.type = new DiscreteType(Discretes.BOOL); }
                else {
                    errors.add(
                        new ErrorBuilder(generateTypeError,interpretMode)
                                .addLocation(ue)
                                .addErrorType(MessageType.TYPE_ERROR_405)
                                .addArgs(ue.expr().type)
                                .addSuggestType(MessageType.TYPE_SUGGEST_1408)
                                .addArgsForSuggestion(ue.unaryOp().toString())
                                .error()
                    );
                }
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
        ws.condition().visit(this);

        // ERROR CHECK #1: The while loop's condition must be a boolean
        if(!ws.condition().type.isBool()) {
            errors.add(
                new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ws.condition())
                        .addErrorType(MessageType.TYPE_ERROR_407)
                        .addArgs(ws.condition().type)
                        .error()
            );
        }

        currentScope = ws.symbolTable;
        ws.whileBlock().visit(this);
        currentScope = currentScope.closeScope();
    }
}
