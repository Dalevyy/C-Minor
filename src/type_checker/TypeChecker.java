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
import errors.TypeError.*;
import utilities.*;

public class TypeChecker extends Visitor {

    private SymbolTable currentScope;
    private AST currentContext;

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
            TypeError.GenericTypeError(ae);

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
            TypeError.GenericTypeError(ae);
    }

    /*
        For an assignment statement, we can only assign expressions that are the SAME
        type as the variable we are trying to assign to.
    */
    public void visitAssignStmt(AssignStmt as) {

        // First, get the type of the LHS
        as.LHS().visit(this);
        Type lType = as.LHS().type;

        // Then, get the type of the RHS
        as.RHS().visit(this);
        Type rType = as.RHS().type;

        String aOp = as.assignOp().toString();

        switch(aOp) {
            case "+=":
                if(lType.isString() || rType.isString()) {
                    if(!(lType.isString() && rType.isString()))
                        TypeError.GenericTypeError(as);
                    break;
                }
            default:
                if(!Type.assignmentCompatible(lType,rType))
                    TypeError.GenericTypeError(as);
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
        String binOp = be.BinaryOp().toString();

        // TODO: Operator overloads. Just check if it's been defined or not :)
        // ERROR CHECK #1: If an object is in a unary expression, we check
        // if the unary operator was overloaded in the class the object represents
//        if(lType.isClassType() && rType.isClassType()) {
//            if(!Type.typeEqual(lType,rType))
//                TypeError.GenericTypeError(be);
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
//                TypeError.GenericTypeError(be);
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
                    TypeError.GenericTypeError(be);
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
            TypeError.GenericTypeError(ce);

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
            TypeError.GenericTypeError(cs);

        for(int i = 0; i < cs.caseStmts().size(); i++) {
            CaseStmt currCase = cs.caseStmts().get(i);
            currCase.choiceLabel().visit(this);
            Type labelType = currCase.choiceLabel().leftLabel().type;

            if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                TypeError.GenericTypeError(cs);

            if(currCase.choiceLabel().rightLabel() != null) {
                labelType = currCase.choiceLabel().rightLabel().type;
                if(!(labelType.isInt() || labelType.isChar() || labelType.isString()))
                    TypeError.GenericTypeError(cs);
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
            TypeError.GenericTypeError(ds);

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
                    TypeError.GenericTypeError(ed);
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
                TypeError.GenericTypeError(fd);
        }
        fd.var().setType(fd.type());
    }

    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        Type targetType = fe.fieldTarget().type;

        // Error Check #1: We want to make sure the target is indeed
        // an object, so make sure it's assigned a class type
        if(!targetType.isClassType())
            TypeError.GenericTypeError(fe);

        ClassDecl cd = currentScope.findName(targetType.typeName()).declName().asTopLevelDecl().asClassDecl();
        FieldDecl fd = cd.symbolTable.findName(fe.name().toString()).declName().asFieldDecl();
//
//        //Error Check #2
//        if(!Type.assignmentCompatible(fType,fd.type()))
//            TypeError.GenericTypeError(fe);
//
         fe.type = fd.type();
    }

    public void visitForStmt(ForStmt fs) {
        currentScope = fs.symbolTable;

        fs.condition().visit(this);
        Type condType = fs.condition().type;
        if(!condType.isBool())
            TypeError.GenericTypeError(fs);

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

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.var().init() != null) {
            gd.var().init().visit(this);
            Type initType = gd.var().init().type;
            if(!Type.assignmentCompatible(gd.type(),initType))
                TypeError.GenericTypeError(gd);
        }
        gd.var().setType(gd.type());
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
                TypeError.GenericTypeError(in);

            in.arguments().visit(this);

            for(int i = 0; i < fd.params().size(); i++) {
                Type paramType = fd.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName()))
                    TypeError.GenericTypeError(in);
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
                TypeError.GenericTypeError(in);

            if(in.arguments() != null && in.arguments().size() > 0)
                in.arguments().visit(this);

            for(int i = 0; i < md.params().size(); i++) {
                Type paramType = md.params().get(i).getType();
                Type argType = in.arguments().get(i).type;
                if(!paramType.typeName().equals(argType.typeName()))
                    TypeError.GenericTypeError(in);
            }
            in.targetType = tt;
            in.type = md.returnType();
        }
    }

    /*
        When we are visiting a literal expression, we set the type
        to correspond with the ConstantKind field which was set when
        we parsed the literal.
    */
    public void visitLiteral(Literal li) {
        if(li.getConstantKind() == ConstantKind.BOOL)
            li.type = new DiscreteType(Discretes.BOOL);
        else if(li.getConstantKind() == ConstantKind.INT)
            li.type = new DiscreteType(Discretes.INT);
        else if(li.getConstantKind() == ConstantKind.CHAR)
            li.type = new DiscreteType(Discretes.CHAR);
        else if(li.getConstantKind() == ConstantKind.STR)
            li.type = new ScalarType(Scalars.STR);
        else if(li.getConstantKind() == ConstantKind.REAL)
            li.type = new ScalarType(Scalars.REAL);

    }

    /*
        For a local declaration, we check to make sure the type of
        the initialization expression matches the explicit type
        given to the local. This will be done with an assignment
        compatibility test since if we instantiate objects, we can
        store a child class object directly into a parent class object.

        If a user wrote the 'uninit' keyword in place of an initialization
        expression, then we can simply set the variable's type to be the
        explicit type of the local without needing additional type checking.
    */
    public void visitLocalDecl(LocalDecl ld) {
        if(ld.var().init() != null) {
            ld.var().init().visit(this);
            Type initType = ld.var().init().type;
            if(!Type.assignmentCompatible(initType,ld.type()))
                TypeError.GenericTypeError(ld);
        }

        ld.var().setType(ld.type());
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
                TypeError.GenericTypeError(ne);
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
                TypeError.GenericTypeError(ne);
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
                TypeError.GenericTypeError(ue);
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
                    TypeError.GenericTypeError(ue);
                break;
            case "not":
                if(eType.isBool())
                    ue.type = new DiscreteType(Discretes.BOOL);
                else
                    TypeError.GenericTypeError(ue);
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
