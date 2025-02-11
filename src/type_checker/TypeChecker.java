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
import errors.type_error.*;
import messages.Message;
import messages.errors.ErrorType;
import messages.errors.TypeError;
import token.Token;
import utilities.*;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private AST currentContext;
    private Message msg;

    public TypeChecker() {
        currentScope = null;
        currentContext = null;
    }

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
            TypeErrorOLD.GenericTypeError(ae);

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
            TypeErrorOLD.GenericTypeError(ae);
    }


    /*
        _________________________Assignment Statements__________________________
        If we want to assign a new value to a variable, we need to make sure the
        value's type matches the type of the variable.

        C Minor also supports compound assignment operations such as +=, -=, *=,
        etc. which means we have to do an additional check to make sure the two
        values can perform a legal binary operation.
        ________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {

        as.LHS().visit(this);
        Type lType = as.LHS().type;

        as.RHS().visit(this);
        Type rType = as.RHS().type;

        String aOp = as.assignOp().toString();

        // Error Check #1: Make sure both the variable and value type are the same
        if(!Type.assignmentCompatible(lType,rType)) {
            msg = new TypeError(as.LHS().toString(), as, lType, rType, ErrorType.ASSIGN_STMT_TYPE_DOES_NOT_MATCH);
            msg.printMsg();
        }

        switch(aOp) {
            case "+=": {
                // Error Check #2: For a '+=' operation, the only allowed types are Int, Real, String, and Object
                if(lType.isBool() || lType.isChar()) {
                    msg = new TypeError(as.LHS().toString(), as, lType, rType, ErrorType.ASSIGN_STMT_INVALID_TYPES_USED);
                    msg.printMsg();
                }
                break;
            }
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=": {
                // Error Check #3: For all other assignment operators, the types Int, Real, and Object have to be used
                if(lType.isBool() || lType.isChar() || lType.isString()) {
                    msg = new TypeError(as.LHS().toString(), as, lType, rType, ErrorType.ASSIGN_STMT_INVALID_TYPES_USED);
                    msg.printMsg();
                }
                break;
            }
        }
    }

    /*
        ___________________________Binary Expressions___________________________
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
        ________________________________________________________________________
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
                // Error Check #1: Both LHS/RHS have to be the same type.
                if(!Type.assignmentCompatible(lType,rType)) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_ASSIGNCOMP);
                    msg.printMsg();
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
                // Error Check #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_ASSIGNCOMP);
                    msg.printMsg();
                }
                // Error Check #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_NUMERIC);
                    msg.printMsg();
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
                // Error Check #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_ASSIGNCOMP);
                    msg.printMsg();
                }
                // Error Check #2: Make sure the operands are numeric types
                if(!lType.isNumeric()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_NUMERIC);
                    msg.printMsg();
                }

                be.type = lType;
                break;
            }
            case "<<":
            case ">>": {
                // Error Check #1: Both LHS and RHS have to be an INT for shift operations
                if(!lType.isInt() || !rType.isInt()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_SLEFT_SRIGHT_NOT_INT);
                    msg.printMsg();
                }

                be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "&":
            case "|":
            case "^": {
                // Error Check #1: Make sure both types are the same
                if(!Type.assignmentCompatible(lType,rType)) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_NOT_ASSIGNCOMP);
                    msg.printMsg();
                }

                // Error Check #2: Make sure both types are discrete
                if(!lType.isDiscreteType() || !rType.isDiscreteType()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_BITWISE_NOT_DISCRETE);
                    msg.printMsg();
                }

                if(binOp.equals("^")) { be.type = new DiscreteType(Discretes.INT); }
                else { be.type = new DiscreteType(Discretes.BOOL); }
                break;
            }
            case "and":
            case "or": {
                // Error Check #1: Make sure both types are Bool
                if(!lType.isBool() || !rType.isBool()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_LOGICAL_NOT_BOOL);
                    msg.printMsg();
                }

                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                if(!lType.isClassType() && !rType.isClassType()) {
                    msg = new TypeError(be.LHS().toString(), be, lType, rType, ErrorType.BIN_EXPR_OBJ_OPS_MISSING_OBJ);
                    msg.printMsg();
                }
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
        }
    }

    /*
        _________________________ Cast Expressions  _______________________________
        In C Minor, we have 4 valid cast expressions a programmer can use:

            1. Char <--> Int
            2. Int  <--> Real
            3. Char  --> String
            4. Parent Class Object <-- Child Class Object (Runtime check)

        For mixed type expressions, this means the programmer must perform explicit
        type casts or else the compiler will generate a typing error.
        ___________________________________________________________________________
    */
    public void visitCastExpr(CastExpr ce) {
        ce.castExpr().visit(this);
        Type exprType = ce.castExpr().type;
        Type typeToCastInto = ce.castType();

        if(exprType.isInt()) {
            // Error Check #1: An Int can only be typecasted into a Char and a Real
            if(!typeToCastInto.isChar() && !typeToCastInto.isReal()) {
                msg = new TypeError(ce.toString(), ce, typeToCastInto, exprType, ErrorType.CAST_EXPR_INVALID_INT_CAST);
                msg.printMsg();
            }
        }
        else if(exprType.isChar()) {
            // Error Check #2: A Char can only be type casted into an Int and a String
            if(!typeToCastInto.isInt() && !typeToCastInto.isString()) {
                msg = new TypeError(ce.toString(), ce, typeToCastInto, exprType, ErrorType.CAST_EXPR_INVALID_CHAR_CAST);
                msg.printMsg();
            }
        }
        else if(exprType.isReal()) {
            // Error Check #3: A Real can only be type casted into an Int
            if(!typeToCastInto.isInt()) {
                msg = new TypeError(ce.toString(), ce, typeToCastInto, exprType, ErrorType.CAST_EXPR_INVALID_REAL_CAST);
                msg.printMsg();
            }
        }
        else {
            // By default, all other cast expressions will be considered invalid
            msg = new TypeError(ce.toString(), ce, typeToCastInto, exprType, ErrorType.CAST_EXPR_INVALID_CAST);
            msg.printMsg();
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
            TypeErrorOLD.GenericTypeError(cs);

        for(int i = 0; i < cs.caseStmts().size(); i++) {
            CaseStmt currCase = cs.caseStmts().get(i);
            currCase.choiceLabel().visit(this);
            Type labelType = currCase.choiceLabel().leftLabel().type;

            if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                TypeErrorOLD.GenericTypeError(cs);

            if(currCase.choiceLabel().rightLabel() != null) {
                labelType = currCase.choiceLabel().rightLabel().type;
                if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                    TypeErrorOLD.GenericTypeError(cs);
            }
            currCase.caseBlock().visit(this);
        }
        if(cs.choiceBlock() != null)
            cs.choiceBlock().visit(this);
        currentScope = currentScope.closeScope();


    }

    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    /*
        ___________________________ Do Statements ________________________________
        With all looping constructs, we only need to check if the type of the loop
        condition evaluates to be a Bool. That's all we DO here. ;)
        __________________________________________________________________________
    */
    public void visitDoStmt(DoStmt ds) {
        currentScope = ds.symbolTable;
        ds.doBlock().visit(this);
        currentScope = currentScope.closeScope();

        ds.condition().visit(this);

        // Error Check #1: Make sure Do's condition evaluates to Bool
        if(!ds.condition().type.isBool()) {
            msg = new TypeError("",ds,ds.condition().type,null,ErrorType.LOOP_CONDITION_NOT_BOOLEAN);
            msg.printMsg();
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
                    TypeErrorOLD.GenericTypeError(ed);
            }
        }
    }

    /*
        For a FieldDecl, we want to make sure the expression's type matches
        the type of the FieldDecl if a user decides to initialize the field
        to some initial value.
    */
    public void visitFieldDecl(FieldDecl fd) {
        if(fd.var().init() != null) {
            fd.var().init().visit(this);
            Type initType = fd.var().init().type;
            if(!Type.assignmentCompatible(initType,fd.type()))
                TypeErrorOLD.GenericTypeError(fd);
        }
        fd.var().setType(fd.type());
    }

    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        Type targetType = fe.fieldTarget().type;

        // Error Check #1: We want to make sure the target is indeed
        // an object, so make sure it's assigned a class type
        if(!targetType.isClassType())
            TypeErrorOLD.GenericTypeError(fe);

        ClassDecl cd = currentScope.findName(targetType.typeName()).declName().asTopLevelDecl().asClassDecl();
        FieldDecl fd = cd.symbolTable.findName(fe.name().toString()).declName().asFieldDecl();
//
//        //Error Check #2
//        if(!Type.assignmentCompatible(fType,fd.type()))
//            TypeErrorOLD.GenericTypeError(fe);
//
         fe.type = fd.type();
    }

    /*
        ___________________________ For Statements ________________________________
        Just like with do statements, we only check if the for loop's condition
        evaluates to be a Bool. There's nothing else FOR us to type check here. ;)
        ___________________________________________________________________________
    */
    public void visitForStmt(ForStmt fs) {
        currentScope = fs.symbolTable;

        fs.condition().visit(this);
        if(!fs.condition().type.isBool()) {
            msg = new TypeError("",fs,fs.condition().type,null,ErrorType.LOOP_CONDITION_NOT_BOOLEAN);
            msg.printMsg();
        }
            TypeErrorOLD.GenericTypeError(fs);

        if(fs.nextExpr() != null) { fs.nextExpr().visit(this); }
        if(fs.forBlock() != null) { fs.forBlock().visit(this); }

        currentScope = currentScope.closeScope();
    }

    public void visitFuncDecl(FuncDecl fd) {
        currentScope = fd.symbolTable;
        currentContext = fd;
        super.visitFuncDecl(fd);
        currentScope = currentScope.closeScope();
    }

    /*
    ________________________Global Declarations___________________________
    Global declarations are handled in the exact same way that local
    declarations are.

    We are checking if the global variable's declared type matches the type
    of the initial value it is assigned to. Additionally, we will provide
    default values if the user assigns the global to 'uninit'.
    _______________________________________________________________________
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

        // Error Check #1: Check if the global variable's declared type
        //                 matches the type of the initial value
        if(!Type.assignmentCompatible(gd.type(),globalVar.init().type)) {
            msg = new TypeError(gd.toString(),gd, gd.type(), globalVar.init().type, ErrorType.GLOBAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR);
            msg.printMsg();
        }

        globalVar.setType(gd.type());
    }


    /*
    _____________________________  If Statements ______________________________
    Similarly to the loop constructs, we only have to check if an if statement's
    condition evaluates into a Bool. IF this is a true, then we are good to go
    visit other nodes. :)
    ___________________________________________________________________________
    */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);

        if(!is.condition().type.isBool()) {
            msg = new TypeError("",is,is.condition().type,null,ErrorType.IF_CONDITION_NOT_BOOLEAN);
            msg.printMsg();
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

    public void visitInvocation(Invocation in) {
        String funcName = in.toString();

        // Function Check
        if(in.target() == null) {
            FuncDecl fd = currentScope.findName(funcName).declName().asTopLevelDecl().asFuncDecl();

            // Error #1
            if(fd.params().size() != in.arguments().size())
                TypeErrorOLD.GenericTypeError(in);

            in.arguments().visit(this);

            for(int i = 0; i < fd.params().size(); i++) {
                Type paramType = fd.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName()))
                    TypeErrorOLD.GenericTypeError(in);
            }

            in.type = fd.returnType();
        }
        // Method Check
        else {
            in.target().visit(this);
            Type tt = in.target().type;
            ClassDecl cd = currentScope.findName(tt.typeName()).declName().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(funcName).declName().asMethodDecl();

            // Error #1
            if(in.arguments() != null && md.params().size() != in.arguments().size())
                TypeErrorOLD.GenericTypeError(in);

            if(in.arguments() != null && in.arguments().size() > 0)
                in.arguments().visit(this);

            for(int i = 0; i < md.params().size(); i++) {
                Type paramType = md.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName()))
                    TypeErrorOLD.GenericTypeError(in);
            }
            in.targetType = tt;
            in.type = md.returnType();
        }
    }

    /*
        ________________________Literals_________________________
        We set the type based on the type of literal that
        was parsed. This only includes Scalar and Discrete
        types. All other literals (Arrays, Lists, and Objects)
        will be handled in separate AST visits.
        _________________________________________________________
    */
    public void visitLiteral(Literal li) {
        if(li.getConstantKind() == ConstantKind.BOOL) { li.type = new DiscreteType(Discretes.BOOL); }
        else if(li.getConstantKind() == ConstantKind.INT) { li.type = new DiscreteType(Discretes.INT); }
        else if(li.getConstantKind() == ConstantKind.CHAR) { li.type = new DiscreteType(Discretes.CHAR); }
        else if(li.getConstantKind() == ConstantKind.STR) { li.type = new ScalarType(Scalars.STR); }
        else if(li.getConstantKind() == ConstantKind.REAL) { li.type = new ScalarType(Scalars.REAL); }
    }

    /*
        _________________________Local Declarations___________________________
        We need to ensure that if a user initializes a local variable
        to a value, the value needs to match the type of the declaration.

        Remember, C Minor does NOT support type coercion. This means the
        value MUST be the same type as the declaration. The only way around
        this is through a valid cast expression.

        Also, if the user initially assigns a local variable to store `uninit`,
        we will automatically set the default value based on the type.
        ______________________________________________________________________
    */
    public void visitLocalDecl(LocalDecl ld) {
        Var localVar = ld.var();

        if(localVar.init() == null) {
            Literal defaultValue = null;
            if (ld.type().isInt()) {
                defaultValue = new Literal(new Token(token.TokenType.INT_LIT, "0", ld.location), ConstantKind.INT);
            }
            else if(ld.type().isChar()) {
                defaultValue = new Literal(new Token(token.TokenType.CHAR_LIT, "", ld.location), ConstantKind.CHAR);
            }
            else if(ld.type().isBool()) {
                defaultValue = new Literal(new Token(token.TokenType.BOOL_LIT, "False", ld.location), ConstantKind.BOOL);
            }
            else if(ld.type().isReal()) {
                defaultValue = new Literal(new Token(token.TokenType.REAL_LIT, "0.0", ld.location), ConstantKind.REAL);
            }
            else if(ld.type().isString()) {
                defaultValue = new Literal(new Token(token.TokenType.STR_LIT, "", ld.location), ConstantKind.STR);
            }
            localVar.setInit(defaultValue);
        }
        localVar.init().visit(this);

        // Error Check #1: Check if the local variable's declared type
        //                 matches the type of the initial value
        if(!Type.assignmentCompatible(ld.type(),localVar.init().type)) {
            msg = new TypeError(ld.toString(),ld, ld.type(), localVar.init().type, ErrorType.LOCAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR);
            msg.printMsg();
        }

        localVar.setType(ld.type());
    }

    /*
        We do not need to do any type checking when we visit the main
        function of the program. All we have to do here is set the
        currentScope to be in main.
    */
    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;
        super.visitMainDecl(md);
        currentScope = currentScope.closeScope();
    }

    /*
        For a MethodDecl, there is no type checking that needs to
        be done. We set the current scope to be the method and then
        visit its children.
    */
    public void visitMethodDecl(MethodDecl md) {
        currentScope = md.symbolTable;
        currentContext = md;
        super.visitMethodDecl(md);
        currentScope = currentScope.closeScope();
    }

    /*
        ____________________________ Name Expressions  ____________________________
        All we need to do is find the declaration associated with the name and set
        it equal to the type given to the declaration.
        ___________________________________________________________________________
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
        In C Minor, a default constructor will automatically be generated
        whenever a user declares a class in their program. This means we
        only need to check if the types of the fields corresponds to the
        actual types of the expressions we are instantiating an object with.
    */
    public void visitNewExpr(NewExpr ne) {
        String className = ne.classType().getName().toString();

        // First, find the ClassDecl the new expression is trying
        // to instantiate and get its constructor
        ClassDecl cd = currentScope.findName(className).declName().asTopLevelDecl().asClassDecl();
        InitDecl currConstructor = cd.getConstructor();

        Vector<Var> args = ne.args();
        Vector<FieldDecl> dataFields = currConstructor.params();

        for(int i = 0; i < args.size(); i++) {
            Expression e = args.get(i).init();
            e.visit(this);
            Type eType = e.type;

            String argName = args.get(i).name().toString();
            Type declType = cd.symbolTable.findName(argName).declName().asFieldDecl().type();

            if(!Type.assignmentCompatible(eType,declType))
                TypeErrorOLD.GenericTypeError(ne);
        }
        ne.type = ne.classType();
    }

    /*
        A return statement will always be inside either a function,
        a method, or the main function of the program.
    */
    public void visitReturnStmt(ReturnStmt rs) {

        // First, get the type of the expression we are returning (if any)
        if(rs.expr() != null)
            rs.expr().visit(this);

        // Then, we figure out the return type of the current function we are in
        Type rType = null;
        if(currentContext.isMethodDecl())
            rType = currentContext.asMethodDecl().returnType();
        else {
            if(currentContext.asTopLevelDecl().isFuncDecl())
                rType = currentContext.asTopLevelDecl().asFuncDecl().returnType();
            else
                rType = currentContext.asTopLevelDecl().asMainDecl().returnType();
        }

        /*
          ERROR CHECK #1:
              If the function is declared "Void", then a return statement can not
              return any expression.
        */
        if(rs.expr() != null && rType == null)
            StmtError.VoidReturnError(rs);

        /*
          ERROR CHECK #2:
              If the function is declared with an explicit return type, then we need
              to make sure the return statement's expression is of the corresponding type
        */
        if(rs.expr() != null && !Type.assignmentCompatible(rType,rs.expr().type))
            StmtError.InvalidReturnError(rs,rType);

        if(rs.expr() != null)
            rs.type = rs.expr().type;
        else
            rs.type = null;
    }

    /*
        __________________________ Unary Expressions  _____________________________
        We only have 2 unary operators in C Minor, so there isn't much to check.
        Here is each operator:

            1. '~'
                - Operand Type: Int or Real
                - Unary Expression Type: Type of both operands

            2. 'not'
                - Operand Type: Bool
                - Unary Expression Type: Bool

        Both unary operators may also be overloaded, so we also will check if the
        overload was defined by the user.
        ___________________________________________________________________________
    */
    public void visitUnaryExpr(UnaryExpr ue) {

        ue.expr().visit(this);
        Type eType = ue.expr().type;
        String uOp = ue.unaryOp().toString();

        // TODO: OPERATOR OVERLOAD CHECK HERE
        if(eType.isClassType()) {
            ClassDecl cd = currentScope.findName(eType.typeName()).declName().asTopLevelDecl().asClassDecl();
            if(!cd.symbolTable.hasName(uOp)) {
                // TODO: Error Message HERE
                ;
            }
            ue.type = eType;
            return;
        }

        switch(uOp) {
            case "~":
                // Error Check #2: Make sure we are negating an Int or Real
                if(eType.isInt()) { ue.type = new DiscreteType(Discretes.INT); }
                else if(eType.isReal()) { ue.type = new ScalarType(Scalars.REAL); }
                else {
                    msg = new TypeError(ue.toString(), ue, eType, eType, ErrorType.UNARY_EXPR_INVALID_NEGATION);
                    msg.printMsg();
                }
                break;
            case "not":
                // Error Check #3: Make sure 'not' is performed on a Bool
                if(eType.isBool()) { ue.type = new DiscreteType(Discretes.BOOL); }
                else {
                    msg = new TypeError(ue.toString(), ue, eType, eType, ErrorType.UNARY_EXPR_INVALID_NOT);
                    msg.printMsg();
                }
                break;
        }
    }

    /*
        ___________________________While Statements_____________________________
        Similarly to the other loop constructs, we only need to check whether or
        not the while's loop condition evaluates to a boolean. All other type
        checks related to the while loop will be handled by other visits.
        ________________________________________________________________________
    */
    public void visitWhileStmt(WhileStmt ws) {

        ws.condition().visit(this);

        // Error Check #1: While's condition must be a Boolean
        if(!ws.condition().type.isBool()) {
            msg = new TypeError("",ws,ws.condition().type,null,ErrorType.LOOP_CONDITION_NOT_BOOLEAN);
            msg.printMsg();
        }

        if(ws.nextExpr() != null) { ws.nextExpr().visit(this); }

        currentScope = ws.symbolTable;
        ws.whileBlock().visit(this);

        currentScope = currentScope.closeScope();
    }
}
