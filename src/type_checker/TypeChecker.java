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
        For an assignment statement, we can only assign expressions that are the SAME
        type as the variable we are trying to assign to.
    */
        /*
    ________________________Assignment Statements___________________________
    If we want to assign a new value to a variable, we need to make sure the
    value's type matches the type of the variable. There's nothing else we
    need to do, and all assignment operators can be handled in the same way
    ________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {

        as.LHS().visit(this);
        Type lType = as.LHS().type;

        as.RHS().visit(this);
        Type rType = as.RHS().type;

        String aOp = as.assignOp().toString();

        if(!Type.assignmentCompatible(lType,rType)) {
            msg = new TypeError(as.LHS().toString(), as, lType, rType, ErrorType.ASSIGN_STMT_TYPE_DOES_NOT_MATCH);
            msg.printMsg();
        }

        switch(aOp) {
            case "+=": {
                if(lType.isBool() || lType.isChar())
                    ;
            }
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=": {
                if(lType.isBool() || lType.isChar() || lType.isString())
                    ;
            }
        }

    }

    /*

        For a BinaryExpr, these are all the possible binary operators we can
        use. Each binary operator has to type check if its operands correspond
        to the allowed types for each operator before setting the type of the BinaryExpr.

        Binary Operators:
            1) ==, !=
                - Operand Type: Both operands need to be the same type
                - BinaryExpr Type: Bool
            2) >, >=, <, <=, <>, <=>
                - Operand Type: Numeric (Int, Real, Char)
                - BinaryExpr Type: Bool
            3) +, -, *, /, %, **
                - Operand Type: Numeric (Int, Real, Char) or String (ONLY for +)
                - BinaryExpr Type: Type of both operands
            4) <<, >>
                - Operand Type: Int
                - BinaryExpr Type: Int
            5) &, |
                - Operand Type: Discrete
                - BinaryExpr Type: Bool
            6) ^
                - Operand Type: Discrete
                - BinaryExpr Type: Int
            7) instanceof, !instanceof, as?
                - Operand Type: Class
                - BinaryExpr Type: Bool
            8) and, or
                - Operand Type: Bool
                - BinaryExpr Type: Bool
    */
    public void visitBinaryExpr(BinaryExpr be) {

        // First, we get the type of the LHS
        be.LHS().visit(this);
        Type lType = be.LHS().type;

        // Then, we get the type of the RHS
        be.RHS().visit(this);
        Type rType = be.RHS().type;
        String binOp = be.binaryOp().toString();

        // TODO: Operator overloads. Just check if it's been defined or not :)
        // ERROR CHECK #1: If an object is in a unary expression, we check
        // if the unary operator was overloaded in the class the object represents
//        if(lType.isClassType() && rType.isClassType()) {
//            if(!Type.typeEqual(lType,rType))
//                TypeErrorOLD.GenericTypeError(be);
//
//            ClassDecl cd = currentScope.getVarName(lType.typeName()).getID().asTopLevelDecl().asClassDecl();
//
//            if(!cd.symbolTable.hasMethod(binOp)) {
//                if(binOp.equals("instanceof") || binOp.equals("!instanceof") || binOp.equals("as?"))
//
//
//            }
//            if(!(binOp.equals("instanceof") || binOp.equals("!instanceof") || binOp.equals("as?")))
//            else if(!currentScope.isNameUsedAnywhere(binOp))
//                TypeErrorOLD.GenericTypeError(be);
//            else {
//                be.type = lType;
//                return;
//            }
//        }

        switch(binOp) {
            case "==":
            case "!=": {
                if(!Type.assignmentCompatible(lType,rType))
                    BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "<>":
            case "<=>": {
                if(!lType.isNumeric())
                    BinaryExprError.BinaryOpInvalidTypeError(be,lType,true);
                else if(!rType.isNumeric())
                    BinaryExprError.BinaryOpInvalidTypeError(be,rType,false);
                else if(!Type.assignmentCompatible(lType,rType))
                    BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "**": {
                if(lType.isString() || rType.isString()) {
                    if(!lType.isString() || !rType.isString())
                        BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                    if(!binOp.equals("+"))
                        BinaryExprError.BinaryOpInvalidOpError(be);
                }
                else if(!Type.assignmentCompatible(lType,rType))
                    BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                be.type = lType;
                break;
            }
            case "<<":
            case ">>": {
                if(!lType.isInt())
                    BinaryExprError.BinaryOpInvalidTypeError(be,rType,true);
                else if(!rType.isInt())
                    BinaryExprError.BinaryOpInvalidTypeError(be,rType,false);
                else
                    be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "&":
            case "|": {
                if(!lType.isDiscreteType() || !rType.isDiscreteType())
                    BinaryExprError.BinaryOpInvalidOpError(be);
                else if(!lType.typeName().equals(rType.typeName()))
                    BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                break;
            }
            case "^": {
                if(lType.isDiscreteType() && rType.isDiscreteType())
                    BinaryExprError.BinaryOpInvalidOpError(be);
                else if(!Type.assignmentCompatible(lType,rType))
                    BinaryExprError.BinaryOpsNonMatchingError(be,lType,rType);
                be.type = new DiscreteType(Discretes.INT);
                break;
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                if(!lType.isClassType() || !rType.isClassType())
                    TypeErrorOLD.GenericTypeError(be);
                be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
            case "and":
            case "or": {
                if(!lType.isBool())
                    BinaryExprError.BinaryOpInvalidTypeError(be,rType,true);
                else if(!rType.isBool())
                    BinaryExprError.BinaryOpInvalidTypeError(be,rType,false);
                else
                    be.type = new DiscreteType(Discretes.BOOL);
                break;
            }
        }
    }

    /*
        In a C Minor program, we do not employ any form of
        type coercion. It is expected the user will explicitly
        type cast all values they want to use when working with
        expressions containing multiple types.

        List of Valid Type Casts:
            1) Char <=> Int
            2) Int <=> Real
            3) Char => String
            4) Parent Object <= Child Object
    */
    public void visitCastExpr(CastExpr ce) {
        ce.castExpr().visit(this);
        Type cType = ce.castExpr().type;
        Type targetType = ce.castType();

        if(cType.isInt()) {
            if (!(targetType.isChar() || targetType.isReal()))
                CastError.InvalidIntCastError(ce);
        }
        else if(cType.isChar()) {
            if(!(targetType.isInt() || targetType.isString()))
                CastError.InvalidIntCastError(ce);
        }
        else if(cType.isReal()) {
            if(!targetType.isInt())
                CastError.InvalidIntCastError(ce);
        }
        else if(cType.isEnum() || targetType.isEnum())
            CastError.InvalidIntCastError(ce);
        else if(cType.isClassType() && targetType.isClassType())
            ;
        else
            TypeErrorOLD.GenericTypeError(ce);

        ce.type = targetType;
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

    public void visitDoStmt(DoStmt ds) {
        ds.condition().visit(this);
        Type condType = ds.condition().type;

        if(!condType.isBool())
            TypeErrorOLD.GenericTypeError(ds);

        currentScope = ds.symbolTable;
        ds.doBlock().visit(this);
        currentScope = currentScope.closeScope();
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

    public void visitForStmt(ForStmt fs) {
        currentScope = fs.symbolTable;

        fs.condition().visit(this);
        Type condType = fs.condition().type;
        if(!condType.isBool())
            TypeErrorOLD.GenericTypeError(fs);

        if(fs.forBlock() != null)
            fs.forBlock().visit(this);
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

    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);
        Type condType = is.condition().type;

        if(!condType.isBool())
            StmtError.ifConditionError(is);

        currentScope = is.symbolTableIfBlock;
        if(is.ifBlock() != null)
            is.ifBlock().visit(this);
        currentScope = currentScope.closeScope();

        if(is.elifStmts().size() > 0)
            is.elifStmts().visit(this);

        if(is.elseBlock() != null)
            is.elseBlock().visit(this);
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
        For a NameExpr, all we have to do is find its declaration
        and set its type equal to its declaration type.
    */
    public void visitNameExpr(NameExpr ne) {
        NameNode decl = currentScope.findName(ne.toString());

        if(decl.declName().isStatement())
            ne.type = decl.declName().asStatement().asLocalDecl().type();
        else if(decl.declName().isParamDecl())
            ne.type = decl.declName().asParamDecl().getType();
        else if(decl.declName().isTopLevelDecl()) {
            TopLevelDecl tDecl = decl.declName().asTopLevelDecl();
            if(tDecl.isGlobalDecl())
                ne.type = tDecl.asGlobalDecl().type();
            else if(tDecl.isEnumDecl())
                ne.type = tDecl.asEnumDecl().type();
            else if(tDecl.isClassDecl())
                ne.type = new ClassType(tDecl.asClassDecl().name());
            else
                TypeErrorOLD.GenericTypeError(ne);
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
        In C Minor, there are only two unary operators, so type
        checking is straightforward.

        Unary Operators:
            1) ~
                - Operand Type: Int or Real
                - UnaryExpr Type: Type of operand (Int or Real)
            2) not
                - Operand Type: Bool
                - UnaryExpr Type: Bool

        A user is also allowed to overload both of these operators.
        This means if the expression the unary operator operates on
        is a class type, we need to make sure the user overloaded the
        operator somewhere in the class definition.
    */
    public void visitUnaryExpr(UnaryExpr ue) {

        ue.expr().visit(this);
        Type eType = ue.expr().type;
        String uOp = ue.unaryOp().toString();

        // ERROR CHECK #1: If an object is in a unary expression, we check
        // if the unary operator was overloaded in the class the object represents
        if(eType.isClassType()) {
            ClassDecl cd = currentScope.getVarName(eType.typeName()).declName().asTopLevelDecl().asClassDecl();
            if(!cd.symbolTable.hasMethod(uOp))
                TypeErrorOLD.GenericTypeError(ue);
            ue.type = eType;
            return;
        }

        switch(uOp) {
            case "~":
                if(eType.isInt())
                    ue.type = new DiscreteType(Discretes.INT);
                else if(eType.isReal())
                    ue.type = new ScalarType(Scalars.REAL);
                else
                    TypeErrorOLD.GenericTypeError(ue);
                break;
            case "not":
                if(eType.isBool())
                    ue.type = new DiscreteType(Discretes.BOOL);
                else
                    TypeErrorOLD.GenericTypeError(ue);
                break;
        }
    }

    /*
        When we visit a WhileStmt, we only perform one type check.
            1. The condition expression of a while loop must
               evaluate to be a Boolean type.
    */
    public void visitWhileStmt(WhileStmt ws) {

        ws.condition().visit(this);
        Type condType = ws.condition().type;

        /*
          ERROR CHECK #1:
              The condition expression for a while loop must evaluate to Bool.
        */
        if(!condType.isBool())
            StmtError.whileConditionError(ws);

        currentScope = ws.symbolTable;
        ws.whileBlock().visit(this);
        currentScope = currentScope.closeScope();
    }

}
