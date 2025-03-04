package type_checker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.expressions.Literal.*;
import ast.statements.*;
import ast.top_level_decls.*;
import ast.types.*;
import ast.types.DiscreteType.*;
import ast.types.ScalarType.*;
import messages.errors.*;
import token.Token;
import utilities.*;

import java.util.ArrayList;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private AST currentContext;
    private ScopeErrorFactory generateScopeError;
    private TypeErrorFactory generateTypeError;
    private ArrayList<String> errors;

    public TypeChecker() {
        this.currentScope = null;
        this.currentContext = null;
        this.generateScopeError = new ScopeErrorFactory();
        this.generateTypeError = new TypeErrorFactory();
    }

    public TypeChecker(SymbolTable st) {
        this();
        this.currentScope = st;
    }

    // Missing ArrayLiteral and ListLiteral :')

    /*
        For an ArrayExpr, we need to type check the target expression
        alongside the index we are using to access an array's memory.
    */
    public void visitArrayExpr(ArrayExpr ae) {
        ae.arrayTarget().visit(this);

        /*
            ERROR CHECK #1:
                We first need to make sure the target expression
                represents an array.
        */
        if(!ae.arrayTarget().type.isArrayType())
            ;

        ArrayType aType = ae.arrayTarget().type.asArrayType();

        if(aType.asArrayType().getArrayDims() > 1)
            ae.type = new ArrayType(aType.getBaseType(), aType.getArrayDims()-1);
        else
            ae.type = aType.getBaseType();

        /*
            ERROR CHECK #2:
                We now need to check the type of the index being used.
                In C Minor, you can only use Ints as indices for arrays, so
                the index expression must evaluate to an Int to prevent a
                type checking error.
        */
        ae.arrayIndex().visit(this);
        if(!ae.arrayIndex().type.isInt())
            ;
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
                    .addErrorType(ErrorType.TYPE_ERROR_402)
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
                            .addErrorType(ErrorType.TYPE_ERROR_403)
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
                            .addErrorType(ErrorType.TYPE_ERROR_403)
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
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1400)
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
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                // ERROR CHECK #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1401)
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
                            .addLocation(be)
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }
                // ERROR CHECK #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1402)
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
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1403)
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
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1400)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                // ERROR CHECK #2: Make sure both types are discrete
                if(!lType.isDiscreteType() || !rType.isDiscreteType()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1404)
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
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1405)
                            .addArgsForSuggestion(binOp)
                            .error());
                }

                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                if(!lType.isClassType() && !rType.isClassType()) {
                    errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                            .addLocation(be)
                            .addErrorType(ErrorType.TYPE_ERROR_404)
                            .addArgs(lType,rType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1406)
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
            if(!typeToCastInto.isChar() && !typeToCastInto.isReal()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(ErrorType.TYPE_ERROR_408)
                        .error());
            }
        }
        else if(exprType.isChar()) {
            // ERROR CHECK #2: A Char can only be type casted into an Int and a String
            if(!typeToCastInto.isInt() && !typeToCastInto.isString()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(ErrorType.TYPE_ERROR_409)
                        .error());
            }
        }
        else if(exprType.isReal()) {
            // ERROR CHECK #3: A Real can only be type casted into an Int
            if(!typeToCastInto.isInt()) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ce)
                        .addErrorType(ErrorType.TYPE_ERROR_410)
                        .error());
            }
        }
        else {
            // By default, all other cast expressions will be considered invalid
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ce)
                    .addErrorType(ErrorType.TYPE_ERROR_411)
                    .addArgs(exprType,typeToCastInto)
                    .error());
        }

        ce.type = typeToCastInto;
    }

    public void visitCaseStmt(CaseStmt cs) {
        currentScope = cs.symbolTable;
        super.visitCaseStmt(cs);
        currentScope = currentScope.closeScope();
    }

    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);

        currentScope = cs.symbolTable;
        Type eType = cs.choiceExpr().type;

        if(!(eType.isInt() || eType.isChar() || eType.isString()))
            ; //TypeErrorOLD.GenericTypeError(cs);

        for(int i = 0; i < cs.caseStmts().size(); i++) {
            CaseStmt currCase = cs.caseStmts().get(i);
            currCase.choiceLabel().visit(this);
            Type labelType = currCase.choiceLabel().leftLabel().type;

            if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                ;

            if(currCase.choiceLabel().rightLabel() != null) {
                labelType = currCase.choiceLabel().rightLabel().type;
                if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                 ;
            }
            currCase.caseBlock().visit(this);
        }
        if(cs.choiceBlock() != null)
            cs.choiceBlock().visit(this);
        currentScope = currentScope.closeScope();


    }

    /*
    _________________________ Class Declarations _________________________
    There is no type checking needed with class declarations. We just set
    the current scope to be inside the class and visit the class fields
    and methods to type check.
    ______________________________________________________________________
    */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
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
                    .addErrorType(ErrorType.TYPE_ERROR_407)
                    .addArgs(ds.condition().type)
                    .error());
        }

        if(ds.nextExpr() != null) { ds.nextExpr().visit(this); }
    }

    /*
        By default, each field of an enumeration will be
        an Integer unless the use specifies otherwise.
    */
    public void visitEnumDecl(EnumDecl ed) {
        Type eType = ed.type();

        if(eType == null)
            eType = new DiscreteType(Discretes.INT);

        Vector<Var> eFields = ed.enumVars();
        for(int i = 0; i < eFields.size(); i++) {
            Expression e = eFields.get(i).asVar().init();
            if(e != null) {
                e.visit(this);

                if(!Type.assignmentCompatible(eType,e.type))
                    ; //TypeErrorOLD.GenericTypeError(ed);
            }
        }
    }

    /*
    __________________________ Field Declarations __________________________
    When we visit a field declaration, we want to check if the user assigned
    an initial value to the field prior to calling the class constructor. If
    this is true, then we have to make sure the initial type of the value
    matches the type of the declaration.

    Additionally, if the value is set to the keyword "uninit", we will give
    a default value based on the type specified.
    ________________________________________________________________________
    */
    public void visitFieldDecl(FieldDecl fd) {
        Var fieldVar = fd.var();

        if(fieldVar.init() == null) {
            Literal defaultValue = null;
            if (fd.type().isInt())
                defaultValue = new Literal(new Token(token.TokenType.INT_LIT, "0", fd.location), ConstantKind.INT);
            else if(fd.type().isChar())
                defaultValue = new Literal(new Token(token.TokenType.CHAR_LIT, "", fd.location), ConstantKind.CHAR);
            else if(fd.type().isBool())
                defaultValue = new Literal(new Token(token.TokenType.BOOL_LIT, "False", fd.location), ConstantKind.BOOL);
            else if(fd.type().isReal())
                defaultValue = new Literal(new Token(token.TokenType.REAL_LIT, "0.0", fd.location), ConstantKind.REAL);
            else if(fd.type().isString())
                defaultValue = new Literal(new Token(token.TokenType.STR_LIT, "", fd.location), ConstantKind.STR);
            else
                defaultValue = null;
            fieldVar.setInit(defaultValue);
        }

        if(fieldVar.init() != null) { fieldVar.init().visit(this); }

        // ERROR CHECK #1: Check if the field's declared type
        //                 matches the type of the initial value
        if(!Type.assignmentCompatible(fd.type(),fieldVar.init().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fd)
                    .addErrorType(ErrorType.TYPE_ERROR_415)
                    .addArgs(fd.toString(),fd.type(),fieldVar.init().type)
                    .error());
        }

        fieldVar.setType(fd.type());
    }

    /*
    ___________________________ Field Expressions ___________________________
    For a field expression, we only have to check if the target type
    represents an Object.

    If this is the case, then we can set the field expression to be the type
    of whatever the corresponding field declaration is.
    _________________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        Type targetType = fe.fieldTarget().type;

        // ERROR CHECK #1: We want to make sure the target is indeed an object,
        //                 so make sure it's assigned a class type
        if(!targetType.isClassType()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fe)
                    .addErrorType(ErrorType.TYPE_ERROR_416)
                    .addArgs(fe.fieldTarget().toString(),targetType)
                    .error());
        }

        ClassDecl cd = currentScope.findName(targetType.typeName()).declName().asTopLevelDecl().asClassDecl();
        FieldDecl fd = cd.symbolTable.findName(fe.name().toString()).declName().asFieldDecl();

        fe.type = fd.type();
    }

    /*
    ___________________________ For Statements ___________________________
    Just like with do statements, we only check if the for loop's
    condition evaluates to be a Bool. There's nothing else FOR us to type
    check here. ;)
    ______________________________________________________________________
    */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.symbolTable;

        fs.condition().visit(this);

        // ERROR CHECK #1: Make sure For's condition evaluates to Bool
        if(!fs.condition().type.isBool()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(fs.condition())
                    .addErrorType(ErrorType.TYPE_ERROR_407)
                    .addArgs(fs.condition().type)
                    .error());
        }

        if(fs.nextExpr() != null) { fs.nextExpr().visit(this); }
        if(fs.forBlock() != null) { fs.forBlock().visit(this); }

        currentScope = currentScope.closeScope();
    }

    /*
    _______________________ Function Declarations _______________________
    There is no type checking needing to be inside of functions. We just
    set the current scope to be inside the function and visit its body.
    _____________________________________________________________________
    */
    public void visitFuncDecl(FuncDecl fd) {
        currentScope = fd.symbolTable;
        currentContext = fd;
        super.visitFuncDecl(fd);
        currentScope = currentScope.closeScope();
    }

    /*
    ________________________ Global Declarations ________________________
    Global declarations are handled in the exact same way that local
    declarations are.

    We are checking if the global variable's declared type matches the
    type of the initial value it is assigned to. Additionally, we will
    provide default values if the user assigns the global to 'uninit'.
    _____________________________________________________________________
    */
    public void visitGlobalDecl(GlobalDecl gd) {
        Var globalVar = gd.var();

        if(globalVar.init() == null) {
            Literal defaultValue = null;
            if (gd.type().isInt()) {
                defaultValue = new Literal(new Token(token.TokenType.INT_LIT, "0", gd.location), ConstantKind.INT);
            }
            else if(gd.type().isChar()) {
                defaultValue = new Literal(new Token(token.TokenType.CHAR_LIT, "", gd.location), ConstantKind.CHAR);
            }
            else if(gd.type().isBool()) {
                defaultValue = new Literal(new Token(token.TokenType.BOOL_LIT, "False", gd.location), ConstantKind.BOOL);
            }
            else if(gd.type().isReal()) {
                defaultValue = new Literal(new Token(token.TokenType.REAL_LIT, "0.0", gd.location), ConstantKind.REAL);
            }
            else if(gd.type().isString()) {
                defaultValue = new Literal(new Token(token.TokenType.STR_LIT, "", gd.location), ConstantKind.STR);
            }
            globalVar.setInit(defaultValue);
        }
        globalVar.init().visit(this);

        // ERROR CHECK #1: Check if the global variable's declared type
        //                 matches the type of the initial value
        if(!Type.assignmentCompatible(gd.type(),globalVar.init().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(gd)
                    .addErrorType(ErrorType.TYPE_ERROR_401)
                    .addArgs(gd.toString(),gd.type(),globalVar.init().type)
                    .error());
        }

        globalVar.setType(gd.type());
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
                    .addErrorType(ErrorType.TYPE_ERROR_406)
                    .addArgs(is.condition().type)
                    .error());
        }

        currentScope = is.symbolTableIfBlock;
        if(is.ifBlock() != null) { is.ifBlock().visit(this); }
        currentScope = currentScope.closeScope();

        if(is.elifStmts().size() > 0) { is.elifStmts().visit(this); }

        if(is.elseBlock() != null) {
            currentScope = is.symbolTableElseBlock;
            is.elseBlock().visit(this);
            currentScope = currentScope.closeScope();
        }
    }

    /*
    ____________________________ Invocations ____________________________
    In C Minor, both forms of invocations will have the same exact type
    checking done on them.

    We first need to make sure the method exists via a name check since
    we had to wait until we had the argument types to find the correct
    method for overloading. If th

    TODO: This might be broken... must think more
    _____________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        String funcSignature = in.toString() + "/";

        in.arguments().visit(this);

        for(int i = 0; i < in.arguments().size(); i++)
            funcSignature += in.arguments().get(i).type.typeSignature();
        in.setInvokeSignature(funcSignature);

        // Function Check
        if(in.target() == null) {

            // ERROR CHECK #1: Make sure the method exists in the current scope
            if(!currentScope.hasName(funcSignature)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(ErrorType.SCOPE_ERROR_319)
                        .addArgs(in.toString())
                        .error());
            }

            FuncDecl fd = currentScope.findName(funcSignature).declName().asTopLevelDecl().asFuncDecl();

            for(int i = 0; i < fd.params().size(); i++) {
                Type paramType = fd.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName())) {
//                    msg = new TypeError(in.arguments().get(i).toString(),in,paramType,argType,ErrorType.FUNC_INVALID_ARG_TYPE);
//                    try { msg.printMsg(); }
//                    catch(Exception e) { throw e; }
                }
            }

            in.type = fd.returnType();
        }
        // Method Check
        else {
            in.target().visit(this);
            Type targetType = in.target().type;
            ClassDecl cd = currentScope.findName(targetType.typeName()).declName().asTopLevelDecl().asClassDecl();

            // ERROR CHECK #1: Make sure the method exists in the current scope
            if(!cd.symbolTable.hasName(funcSignature)) {
                errors.add(new ErrorBuilder(generateScopeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(ErrorType.SCOPE_ERROR_320)
                        .addArgs(in.toString(),cd.toString())
                        .error());
            }

            MethodDecl md = cd.symbolTable.findName(funcSignature).declName().asMethodDecl();

            // Error #1
            if(in.arguments() != null && md.params().size() != in.arguments().size()) {
//                msg = new TypeError(funcSignature,in,null,null,ErrorType.FUNC_INVALID_NUM_OF_ARGS);
//                try { msg.printMsg(); }
//                catch(Exception e) { throw e; }
            }

            if(in.arguments() != null && in.arguments().size() > 0)
                in.arguments().visit(this);

            for(int i = 0; i < md.params().size(); i++) {
                Type paramType = md.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName())) {
//                    msg = new TypeError(in.arguments().get(i).toString(),in,paramType,argType,ErrorType.FUNC_INVALID_ARG_TYPE);
//                    try { msg.printMsg(); }
//                    catch(Exception e) { throw e; }
                }
            }
            in.targetType = targetType;
            in.type = md.returnType();
        }
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
    }

    /*
    ________________________ Local Declarations ________________________
    We need to ensure that if a user initializes a local variable to a
    value, the value needs to match the type of the declaration.

    Remember, C Minor does NOT support type coercion. This means the
    value MUST be the same type as the declaration. The only way around
    this is through a valid cast expression.

    Also, if the user initially assigns a local variable to store
    `uninit`, we will automatically set the default value based on the type.
    ______________________________________________________________________
    */
    public void visitLocalDecl(LocalDecl ld) {
        Var localVar = ld.var();

        if(localVar.init() == null) {
            Literal defaultValue = null;
            if (ld.type().isInt())
                defaultValue = new Literal(new Token(token.TokenType.INT_LIT, "0", ld.location), ConstantKind.INT);
            else if(ld.type().isChar())
                defaultValue = new Literal(new Token(token.TokenType.CHAR_LIT, "", ld.location), ConstantKind.CHAR);
            else if(ld.type().isBool())
                defaultValue = new Literal(new Token(token.TokenType.BOOL_LIT, "False", ld.location), ConstantKind.BOOL);
            else if(ld.type().isReal())
                defaultValue = new Literal(new Token(token.TokenType.REAL_LIT, "0.0", ld.location), ConstantKind.REAL);
            else if(ld.type().isString())
                defaultValue = new Literal(new Token(token.TokenType.STR_LIT, "", ld.location), ConstantKind.STR);
            else
                defaultValue = null;
            localVar.setInit(defaultValue);
        }

        if(localVar.init() != null) { localVar.init().visit(this); }

        // ERROR CHECK #1: Check if the local variable's declared type
        //                 matches the type of the initial value
        if(!Type.assignmentCompatible(ld.type(),localVar.init().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(ld)
                    .addErrorType(ErrorType.TYPE_ERROR_400)
                    .addArgs(ld.toString(),ld.type(),localVar.init().type)
                    .error());
        }

        localVar.setType(ld.type());
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
                    .addErrorType(ErrorType.TYPE_ERROR_417)
                    .error());
        }
        super.visitMainDecl(md);
        currentScope = currentScope.closeScope();
    }

    /*
    _________________________ Method Declarations _________________________
    There is no type checking needing to be inside of methods. We just set
    the current scope to be inside the method and visit its body.
    _______________________________________________________________________
    */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;
        super.visitMethodDecl(md);
        currentScope = currentScope.closeScope();
    }

    /*
    _________________________ Name Expressions  _________________________
    All we need to do is find the declaration associated with the name
    and set it equal to the type given in its declaration.
    _____________________________________________________________________
    */
    public void visitNameExpr(NameExpr ne) {
        NameNode decl = currentScope.findName(ne.toString());

        if(decl.declName().isStatement()) { ne.type = decl.declName().asStatement().asLocalDecl().type(); }
        else if(decl.declName().isParamDecl()) { ne.type = decl.declName().asParamDecl().getType(); }
        else if(decl.declName().isTopLevelDecl()) {
            TopLevelDecl tDecl = decl.declName().asTopLevelDecl();

            if(tDecl.isGlobalDecl()) { ne.type = tDecl.asGlobalDecl().type();}
            else if(tDecl.isEnumDecl()) { ne.type = tDecl.asEnumDecl().type(); }
            else if(tDecl.isClassDecl()) { ne.type = new ClassType(tDecl.asClassDecl().name()); }
        }
    }

    /*
    ___________________________ New Expressions ___________________________
    Since we generate a constructor automatically for the user, we only
    have to check whether an initial value given to an object matches the
    type of its corresponding field declaration.
    _______________________________________________________________________
    */
    public void visitNewExpr(NewExpr ne) {
        String className = ne.classType().getName().toString();

        // Find the ClassDecl node for the corresponding new expression
        ClassDecl cd = currentScope.findName(className).declName().asTopLevelDecl().asClassDecl();
        InitDecl currConstructor = cd.constructor();

        Vector<Var> args = ne.args();

        for(int i = 0; i < args.size(); i++) {
            Expression currArg = args.get(i).init();
            currArg.visit(this);

            String argName = args.get(i).name().toString();
            Type fieldDeclType = cd.symbolTable.findName(argName).declName().asFieldDecl().type();

            // ERROR CHECK #1: Make sure the type of argument value matches type of field declaration
            if(!Type.assignmentCompatible(currArg.type,fieldDeclType)) {
                errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                        .addLocation(ne)
                        .addErrorType(ErrorType.TYPE_ERROR_412)
                        .addArgs(argName,fieldDeclType,currArg.type)
                        .error());
            }
        }
        ne.type = ne.classType();
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

        Type declaredReturnType = null;
        if(currentContext.isMethodDecl()) {
            declaredReturnType = currentContext.asMethodDecl().returnType();
        }
        else {
            if(currentContext.asTopLevelDecl().isFuncDecl()) {
                declaredReturnType = currentContext.asTopLevelDecl().asFuncDecl().returnType();
            }
            else {
                declaredReturnType = currentContext.asTopLevelDecl().asMainDecl().returnType();

            }
        }

        // ERROR CHECK #1: If the function is declared "Void", then a return statement
        //                 can not return any expression.
        if(rs.expr() != null && declaredReturnType.isVoidType()) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(rs)
                    .addErrorType(ErrorType.TYPE_ERROR_413)
                    .addArgs(rs.expr().type,currentContext.toString())
                    .error());
        }

        // ERROR CHECK #2: If the function is declared with an explicit return type,
        //                 then we need to make sure the return statement's expression
        //                 is of the corresponding type
        if(rs.expr() != null && !Type.assignmentCompatible(declaredReturnType,rs.expr().type)) {
            errors.add(new ErrorBuilder(generateTypeError,interpretMode)
                    .addLocation(rs)
                    .addErrorType(ErrorType.TYPE_ERROR_414)
                    .addArgs(rs.expr().type,currentContext.toString(),declaredReturnType)
                    .error());
        }

        if(rs.expr() != null) { rs.type = rs.expr().type; }
        else { rs.type = null; }
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
                            .addErrorType(ErrorType.TYPE_ERROR_405)
                            .addArgs(eType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1407)
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
                            .addErrorType(ErrorType.TYPE_ERROR_405)
                            .addArgs(eType)
                            .addSuggestType(ErrorType.TYPE_SUGGEST_1408)
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
                    .addErrorType(ErrorType.TYPE_ERROR_407)
                    .addArgs(ws.condition().type)
                    .error());
        }

        if(ws.nextExpr() != null) { ws.nextExpr().visit(this); }

        currentScope = ws.symbolTable;
        ws.whileBlock().visit(this);

        currentScope = currentScope.closeScope();
    }
}
