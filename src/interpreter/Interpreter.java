package interpreter;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.*;
import ast.top_level_decls.*;
import utilities.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigDecimal;

public class Interpreter extends Visitor {

    private RuntimeStack stack;
    private SymbolTable currentScope;
    private Object currValue;
    private boolean returnFound;
    private boolean breakFound;
    private boolean continueFound;

    public Interpreter() {
        stack = new RuntimeStack();
        returnFound = false;
        breakFound = false;
        continueFound = false;
    }

    public Interpreter(SymbolTable st) {
        this();
        this.currentScope = st;
    }

//    public void visitArrayExpr(ArrayExpr ae) {
//        ArrayList<Object> arr = (ArrayList<Object>) stack.getValueInRuntimeStack(ae.arrayTarget().toString());
//        ae.setValue(arr.get((int)ae.arrayIndex().getValue(stack)));
//        currExpr = ae;
//    }
//
//    public void visitArrayLiteral(ArrayLiteral al) {
//        ArrayList<Object> arr = new ArrayList<Object>();
//        for(int i = 0; i < al.arrayExprs().size(); i++) {
//            al.arrayExprs().get(i).visit(this);
//            arr.add(currExpr.getValue(stack));
//        }
//        currExpr = al;
//    }

    /*
    _________________________ Assignment Statements _________________________
    We will evaluate the LHS of the assignment to figure out what variable we
    are updating and then the RHS to know what value we are going to assign
    to it. Then, we will update the variable in the current call frame based
    on the assignment operator we have.
    _________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {
        //as.LHS().visit(this);
        String name = as.LHS().toString();

        as.RHS().visit(this);
        Object newValue = currValue;

        String aOp = as.assignOp().toString();

        // TODO: Operator Overloads as well

        switch(aOp) {
            case "=": {
                stack.setValue(name,newValue);
                break;
            }
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=": {
                if(as.RHS().type.isInt()) {
                    int oldVal = (int) stack.getValue(name);
                    int val = (int) newValue;
                    switch(aOp) {
                        case "+=" -> stack.setValue(name,oldVal+val);
                        case "-=" -> stack.setValue(name,oldVal-val);
                        case "*=" -> stack.setValue(name,oldVal*val);
                        case "/=" -> stack.setValue(name,oldVal/val);
                        case "%=" -> stack.setValue(name,oldVal%val);
                        case "**=" -> stack.setValue(name,Math.pow(oldVal,val));
                    }
                    break;
                }
                else if(as.RHS().type.isReal()) {
                    BigDecimal oldVal = (BigDecimal) stack.getValue(name);
                    BigDecimal val = (BigDecimal) newValue;
                    switch(aOp) {
                        case "+=" -> stack.setValue(name,oldVal.add(val));
                        case "-=" -> stack.setValue(name,oldVal.subtract(val));
                        case "*=" -> stack.setValue(name,oldVal.multiply(val));
                        case "/=" -> stack.setValue(name,oldVal.divide(val));
                        case "%=" -> stack.setValue(name,oldVal.remainder(val));
                        case "**=" -> stack.setValue(name,oldVal.pow(val.toBigInteger().intValue()));
                    }
                    break;
                }
                else if(as.RHS().type.isString()) {
                    String oldVal = (String) stack.getValue(name);
                    String val = (String) newValue;
                    stack.setValue(name,oldVal+val);
                    break;
                }
            }
        }
    }

    /*
    ___________________________ Binary Expressions ___________________________
    We will first evaluate the LHS and then the RHS. From there, we will
    evaluate the binary expression based on the types of both the LHS and RHS
    and perform the correct operation from there.
    __________________________________________________________________________
    */
    public void visitBinaryExpr(BinaryExpr be) {
        be.LHS().visit(this);
        Object LHS = currValue;

        be.RHS().visit(this);
        Object RHS = currValue;

        String binOp = be.binaryOp().toString();

        // TODO: Check for operator overloads here

        switch(binOp) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "**": {
                if(be.type.isInt()) {
                    int lValue = (int) LHS;
                    int rValue = (int) RHS;
                    switch(binOp) {
                        case "+" -> currValue = lValue + rValue;
                        case "-" -> currValue = lValue - rValue;
                        case "*" -> currValue = lValue * rValue;
                        case "/" -> currValue = lValue / rValue;
                        case "%" -> currValue = lValue % rValue;
                        case "**" -> currValue = Math.pow(lValue,rValue);
                    }
                    break;
                }
                else if(be.type.isReal()) {
                    BigDecimal lValue = (BigDecimal) LHS;
                    BigDecimal rValue = (BigDecimal) RHS;
                    switch(binOp) {
                        case "+" -> currValue = lValue.add(rValue);
                        case "-" -> currValue = lValue.subtract(rValue);
                        case "*" -> currValue = lValue.multiply(rValue);
                        case "/" -> currValue = lValue.divide(rValue);
                        case "%" -> currValue = lValue.remainder(rValue);
                        case "**" -> currValue = lValue.pow(rValue.toBigInteger().intValue());
                    }
                    break;
                }
                else if(be.type.isString()) {
                    String lValue = (String) LHS;
                    String rValue = (String) RHS;
                    currValue = lValue + rValue;
                    break;
                }
            }
            case "==":
            case "!=": {
                if(be.LHS().type.isBool() && be.RHS().type.isBool()) {
                    boolean lValue = (boolean) LHS;
                    boolean rValue = (boolean) RHS;
                    switch(binOp) {
                        case "==" -> currValue = lValue == rValue;
                        case "!=" -> currValue = lValue != rValue;
                    }
                    break;
                }
                else if(be.LHS().type.isString() && be.RHS().type.isString()) {
                    String lValue = (String) LHS;
                    String rValue = (String) RHS;
                    switch(binOp) {
                        case "==" -> currValue = lValue.equals(rValue);
                        case "!=" -> currValue = !lValue.equals(rValue);
                    }
                    break;
                }
                else if(be.LHS().type.isInt() && be.RHS().type.isInt()) {
                    int lValue = (int) LHS;
                    int rValue = (int) RHS;
                    switch(binOp) {
                        case "==" -> currValue = lValue == rValue;
                        case "!=" -> currValue = lValue == rValue;
                    }
                    break;
                }
                else if(be.LHS().type.isChar() && be.RHS().type.isChar()) {
                    char lValue = (char) LHS;
                    char rValue = (char) RHS;
                    switch(binOp) {
                        case "==" -> currValue = lValue == rValue;
                        case "!=" -> currValue = lValue != rValue;
                    }
                    break;
                }
                else if(be.LHS().type.isReal() && be.RHS().type.isReal()) {
                    BigDecimal lValue = (BigDecimal) LHS;
                    BigDecimal rValue = (BigDecimal) RHS;
                    switch(binOp) {
                        case "==" -> currValue = lValue.compareTo(rValue) == 0;
                        case "!=" -> currValue = lValue.compareTo(rValue) != 0;
                    }
                    break;
                }
            }
            case "<":
            case "<=":
            case ">":
            case ">=": {
                if(be.LHS().type.isInt() && be.RHS().type.isInt()) {
                    int lValue = (int) LHS;
                    int rValue = (int) RHS;
                    switch (binOp) {
                        case "<" -> currValue = lValue < rValue;
                        case "<=" -> currValue = lValue <= rValue;
                        case ">" -> currValue = lValue > rValue;
                        case ">=" -> currValue = lValue >= rValue;
                    }
                    break;
                }
                else if(be.LHS().type.isReal() && be.RHS().type.isReal()) {
                    BigDecimal lValue = (BigDecimal) LHS;
                    BigDecimal rValue = (BigDecimal) RHS;
                    switch (binOp) {
                        case "<" -> currValue = lValue.compareTo(rValue) < 0;
                        case "<=" -> currValue = lValue.compareTo(rValue) > 0;
                        case ">" -> currValue = lValue.compareTo(rValue) >= 0;
                        case ">=" -> currValue = lValue.compareTo(rValue) <= 0;
                    }
                    break;
                }
            }
            case "and":
            case "or": {
                boolean lValue = (boolean) LHS;
                boolean rValue = (boolean) RHS;
                switch(binOp) {
                    case "and" -> currValue = lValue && rValue;
                    case "or" -> currValue = lValue || rValue;
                }
                break;
            }
            case "<<":
            case ">>":
            case "&":
            case "|":
            case "^": {
                if(be.type.isInt()) {
                    int lValue = (int) LHS;
                    int rValue = (int) RHS;
                    switch(binOp) {
                        case "<<" -> currValue = lValue << rValue;
                        case ">>" -> currValue = lValue >> rValue;
                        case "^" -> currValue = lValue ^ rValue;
                    }
                    break;
                }
                else if(be.type.isBool()) {
                    boolean lValue = (boolean) LHS;
                    boolean rValue = (boolean) RHS;
                    switch(binOp) {
                        case "&" -> currValue = lValue & rValue;
                        case "|" -> currValue = lValue | rValue;
                    }
                    break;
                }
            }
            case "instanceof":
            case "!instanceof":
            case "as?": {
                //TODO: Check class name here
            }
        }
    }

    /*
    ________________________ Block Statements ________________________
    Every time we visit a block statement, we will create a new call
    frame, visit the statements inside the block, and at the end, we
    will destroy the call frame when we reach the end of the block.
    __________________________________________________________________
    */
    public void visitBlockStmt(BlockStmt bs) {
        stack = stack.createCallFrame();

        bs.decls().visit(this);

        for(int i = 0; i < bs.stmts().size(); i++) {
            bs.stmts().get(i).visit(this);
            if(returnFound) { break; }
            else if(breakFound) { break; }
            else if(continueFound) {
                continueFound = false;
                break;
            }
        }
        stack = stack.destroyCallFrame();
    }

    /*
    _____________________ Break Statements _____________________
    We set the breakFound flag to be true, and we continue
    interpreting the C Minor program.
    ____________________________________________________________
    */
    public void visitBreakStmt(BreakStmt bs) { breakFound = true; }

    /*
    _____________________ Cast Expressions _____________________
    For cast expressions, we just evaluate the value we want to
    cast and then do the explicit type cast.
    ____________________________________________________________
    */
    public void visitCastExpr(CastExpr cs) {
        cs.castExpr().visit(this);
        if(cs.castType().isInt()) {
            if(cs.castExpr().type.isReal()) { currValue = ((BigDecimal) currValue).intValue(); }
            else if(cs.castExpr().type.isChar()) {
                if(currValue.toString().charAt(1) != '/') { currValue = (int) currValue.toString().charAt(1); }
                else { currValue = (int) currValue.toString().charAt(1) + (int) currValue.toString().charAt(2); }
            }
        }
        else if(cs.castType().isReal()) {
            if(cs.castExpr().type.isInt()) { currValue = new BigDecimal(currValue.toString()); }
        }
        else if(cs.castType().isString()) {
            if(cs.castType().isChar()) { currValue = currValue.toString(); }
        }
    }

    /*
    _________________________ Choice Statements  _________________________
    We first evaluate the value of the choice expression. Then, we check
    which case's label corresponds to the initial choice value to determine
    which case statement to execute. If none of the case statements match
    the value, then we will execute the default case statement.
    ______________________________________________________________________
    */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);

        Object choiceVal = currValue;
        for(int i = 0; i <= cs.caseStmts().size(); i++) {
            // Default Case Execution
            if(i == cs.caseStmts().size()) {
                cs.choiceBlock().visit(this);
                break;
            }
            CaseStmt currCase = cs.caseStmts().get(i);

            // Int Case
            if(cs.choiceExpr().type.isInt()) {
                int val = (int) choiceVal;

                currCase.choiceLabel().leftLabel().visit(this);
                int label = (int) currValue;

                if(currCase.choiceLabel().rightLabel() != null) {
                    currCase.choiceLabel().rightLabel().visit(this);
                    int rLabel = (int) currValue;
                    if(val >= label  && val <= rLabel) {
                        currCase.visit(this);
                        break;
                    }
                }
                else {
                    if(val == label) {
                        currCase.visit(this);
                        break;
                    }
                }
            }
            // Character Case
            else if(cs.choiceExpr().type.isChar()) {
                char val = choiceVal.toString().charAt(0);

                currCase.choiceLabel().leftLabel().visit(this);
                char label = currValue.toString().charAt(0);

                if(currCase.choiceLabel().rightLabel() != null) {
                    currCase.choiceLabel().rightLabel().visit(this);
                    char rLabel = currValue.toString().charAt(0);
                    if(val >= label  && val <= rLabel) {
                        currCase.visit(this);
                        break;
                    }
                }
                else {
                    if(val == label) {
                        currCase.visit(this);
                        break;
                    }
                }
            }
            // String Case
            else if(cs.choiceExpr().type.isString()) {
                String val = choiceVal.toString();

                currCase.choiceLabel().leftLabel().visit(this);
                String label = currValue.toString();

                if(val.equals(label)) {
                    currCase.visit(this);
                    break;
                }
            }
        }
    }

    /*
    _____________________ Continue Statements _____________________
    We set the continueFound flag to be true, and we CONTINUE
    evaluating the C Minor program. ;)
    _______________________________________________________________
    */
    public void visitContinueStmt(ContinueStmt cs) { continueFound = true; }

    /*
    ___________________________ Do Statements ___________________________
    For a do while loop, we will execute the loop body once before we
    evaluate the condition. From there, we handle this construct like we
    do with while loops.
    _____________________________________________________________________
    */
    public void visitDoStmt(DoStmt ds) {
        do {
            ds.doBlock().visit(this);
            if(breakFound) {
                breakFound = false;
                break;
            }
            ds.condition().visit(this);
        } while ((boolean) currValue);
    }

    /*
    _________________________ Enum Declarations  _________________________
    For each field inside an enumeration, we will evaluate its initial
    value and then store the constant onto the runtime stack.
    ______________________________________________________________________
    */
    public void visitEnumDecl(EnumDecl ed) {
        for(int i = 0; i < ed.enumVars().size(); i++) {
            Var enumConstant = ed.enumVars().get(i);
            enumConstant.init().visit(this);
            stack.addValue(enumConstant.toString(),currValue);
        }
    }

    /*
    ___________________________ Field Expressions ___________________________
    For a field expression, we just access the object from the stack and get
    the appropriate field's value with a lookup.
    _________________________________________________________________________
    */
    public void visitFieldExpr(FieldExpr fe) {
        String objName = fe.fieldTarget().toString();
        HashMap<String,Object> instance = (HashMap<String,Object>) stack.getValue(objName);
        currValue = instance.get(fe.name().toString());
    }

    //TODO: BROKEN AF :')
    /*
    ___________________________ For Statements ___________________________
    With a for loop, we will evaluate the loop variable declarations, and
    we will execute the loop until the loop condition becomes false.
    ______________________________________________________________________
    */
//    public void visitForStmt(ForStmt fs) {
//        fs.forInits().visit(this);
//        fs.condition().visit(this);
//        while((boolean)currValue) {
//            fs.forBlock().visit(this);
//            fs.condition().visit(this);
//        }
//    }

    /*
    ________________________ Global Declarations ________________________
    We will first evaluate the initial value we assign the global
    variable to and then we will save the variable onto the current call
    frame.
    _____________________________________________________________________
    */
    public void visitGlobalDecl(GlobalDecl gd) {
        gd.var().init().visit(this);
        stack.addValue(gd.var().toString(),currValue);
    }

    /*
    ________________________ If Statements  ________________________
    First, we will evaluate the value of the condition. Then, we
    will use the condition's value to determine which branch of the
    if statement we will execute.
    ________________________________________________________________
    */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);
        if((boolean)currValue) { is.ifBlock().visit(this); }
        else {
            if(is.elifStmts().size() > 0) {
                for(int i = 0; i < is.elifStmts().size(); i++) {
                    IfStmt curr = is.elifStmts().get(i);
                    curr.condition().visit(this);
                    if((boolean)currValue) {
                        curr.ifBlock().visit(this);
                        break;
                    }
                }
            }
            else if(is.elseBlock() != null) { is.elseBlock().visit(this); }
        }
    }

    /*
    _________________________ Init Declarations _________________________
    When we visit a constructor declaration, we are concerned with
    initializing all fields that the user did not specify during object
    instantiation. We will fill these fields in for the user before we
    continue the execution of the program.
    _____________________________________________________________________
    */
    public void visitInitDecl(InitDecl id) {
        HashMap<String,Object> instance = (HashMap<String,Object>) currValue;
        for(int i = 0; i < id.assignStmts().size(); i++) {
            AssignStmt as = id.assignStmts().get(i);

            if(!instance.containsKey(as.LHS().toString())) {
                as.RHS().visit(this);
                instance.put(as.LHS().toString(),currValue);
            }
        }
        currValue = instance;
    }

//    public void visitInStmt(InStmt in) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        String input = "";
//        try {
//            input = reader.readLine();
//        } catch(Exception e) {
//            System.out.println(e);
//            System.exit(1);
//        }
//
//        String[] args = input.split(" ");
//        for(int i = 0; i < in.inExprs().size(); i++)
//           stack.setValueInRuntimeStack(in.inExprs().get(i).toString(),args[i]);
//    }

    /*
    ____________________________ Invocations ____________________________
    For invocations, we will first evaluate each argument we are passing
    and store each value into a separate list.

    Then, we determine whether the invocation is for a function or a
    method. In either case, we will create a new call frame to store the
    values for each parameter and then execute the function/method body.

    At the end, we will save the parameter results if the parameters have
    the appropriate modifiers.
    _____________________________________________________________________
    */
    public void visitInvocation(Invocation in) {
        ArrayList<Object> args = new ArrayList<Object>();
        HashMap<String,Object> vals = new HashMap<String,Object>();

        for(int i = 0; i < in.arguments().size(); i++) {
            in.arguments().get(i).visit(this);
            args.add(currValue);
        }

        SymbolTable oldScope = currentScope;
        stack = stack.createCallFrame();

        // Function Invocation
        if(in.target() == null) {
            FuncDecl fd = currentScope.findName(in.invokeSignature()).declName().asTopLevelDecl().asFuncDecl();
            currentScope = fd.symbolTable;

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = fd.params().get(i);
                stack.addValue(currParam.toString(),args.get(i));
            }

            fd.funcBlock().visit(this);
            returnFound = false;

            for(int i = 0; i < in.arguments().size(); i++) {
                if(in.arguments().get(i) instanceof NameExpr) {
                    String argName = in.arguments().get(i).toString();
                    ParamDecl currParam = fd.params().get(i);

                    if(currParam.mod.isOut() || currParam.mod.isInOut() || currParam.mod.isRef())
                        vals.put(argName,stack.getValue(currParam.toString()));
                }
            }
        }
        // Method Invocation
        else {
            in.target().visit(this);
            HashMap<String,Object> obj = (HashMap<String,Object>) currValue;

            ClassDecl cd = currentScope.findName(in.targetType.typeName()).declName().asTopLevelDecl().asClassDecl();
            String methodName = in.invokeSignature();

            if(!cd.symbolTable.hasName(methodName)) {  methodName += "_" + cd.toString(); }

            System.out.println(in.targetType.toString());

//            if(cd.superClass() != null && in.type.) {
//                methodName += "_" + cd.toString();
//            }
            MethodDecl md = cd.symbolTable.findName(methodName).declName().asMethodDecl();
            currentScope = md.symbolTable;

            for(String s : obj.keySet()) { stack.addValue(s,obj.get(s)); }

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = md.params().get(i);
                stack.addValue(currParam.toString(),args.get(i));
            }

            md.methodBlock().visit(this);
            returnFound = false;

            for(int i = 0; i < in.arguments().size(); i++) {
                if(in.arguments().get(i) instanceof NameExpr) {
                    String argName = in.arguments().get(i).toString();
                    ParamDecl currParam = md.params().get(i);

                    if(currParam.mod.isOut() || currParam.mod.isInOut() || currParam.mod.isRef())
                        vals.put(argName,stack.getValue(currParam.toString()));
                }
            }
        }

        currentScope = oldScope;
        stack = stack.destroyCallFrame();

        for(String s : vals.keySet()) { stack.setValue(s,vals.get(s)); }
    }

//    public void visitListLiteral(ListLiteral li) {
//        ArrayList<Object> arr = new ArrayList<Object>();
//        for(int i = 0; i < li.exprs().size(); i++) {
//            li.exprs().get(i).visit(this);
//            arr.add(currExpr.getValue(stack));
//        }
//        currExpr = li;
//    }

    /*
    ________________________ Literals ________________________
    Whenever we visit a literal, we will just evaluate it and
    set the current value to equal the evaluation result.
    __________________________________________________________
    */
    public void visitLiteral(Literal li) {
        if(li.type.isInt()) {
            if(li.text.charAt(0) == '~') { currValue = (-1*Integer.parseInt(li.text.substring(1))); }
            else { currValue = Integer.parseInt(li.text); }
        }
        else if(li.type.isChar()) { currValue = li.text; }
        else if(li.type.isBool()) { currValue = Boolean.parseBoolean(li.text); }
        else if(li.type.isReal()) {
            if(li.text.charAt(0) == '~') { currValue = (new BigDecimal(li.text.substring(1)).multiply(new BigDecimal(-1))); }
            else { currValue = new BigDecimal(li.text); }
        }
        else if(li.type.isString()) { currValue = li.text.substring(1,li.text.length()-1); } // Removes quotes
    }

    /*
    ________________________ Local Declarations ________________________
    We first visit the initial value that is set when we are making a
    local declaration (at this point, the value will be either given or
    we will manually set one for the user). We will then add the local
    variable onto the current call frame.
    ____________________________________________________________________
    */
    public void visitLocalDecl(LocalDecl ld) {
        ld.var().init().visit(this);
        stack.addValue(ld.var().toString(),currValue);
    }

    /*
    _________________________ Name Expressions  _________________________
    For name expressions, we just need to retrieve the value associated
    with the name from the stack and set the current value equal to it.
    _____________________________________________________________________
    */
    public void visitNameExpr(NameExpr ne) { currValue = stack.getValue(ne.toString()); }

    /*
    ___________________________ New Expressions ___________________________
    When we are instantiating a new object, we will create a hash map that
    stores each object's initial field value (if specified by the user) and
    then we will call the constructor we made for the user.
    _______________________________________________________________________
    */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.classType().typeName()).declName().asTopLevelDecl().asClassDecl();

        HashMap<String,Object> instance = new HashMap<String,Object>();
        for(int i = 0; i < ne.args().size(); i++) {
            Var currArg = ne.args().get(i);
            currArg.init().visit(this);
            instance.put(currArg.toString(),currValue);
        }

        currValue = instance;

        cd.constructor().visit(this);
    }

    /*
    ___________________________ Out Statements ___________________________
    ______________________________________________________________________
    */
    public void visitOutStmt(OutStmt os) {
        for(int i = 0; i < os.outExprs().size(); i++) {
            Expression e = os.outExprs().get(i);
            if(e.isEndl()) { System.out.println(); }
            else {
                e.visit(this);
                if(currValue instanceof String && currValue.equals("' '")) // Guess I need this here?
                    System.out.print(" ");
                else
                    System.out.print(currValue);
            }
        }
    }

    /*
    ___________________________ Return Statements ___________________________
    If a return statement has an expression, we will evaluate the expression.
    Then, we will just set the return found flag to be true, so we can stop
    evaluating statements in the block statement.
    _________________________________________________________________________
    */
    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.expr() != null) { rs.expr().visit(this); }
        returnFound = true;
    }

    /*
    __________________________ Stop Statements  __________________________
    When we encounter a `stop`, we are going to terminate the interpreter.
    ______________________________________________________________________
    */
    public void visitStopStmt(StopStmt ss) { System.exit(1); }

    /*
    __________________________ Unary Expressions  __________________________
    We will evaluate unary expressions just like we did with binary
    expressions by first evaluating the individual expression and then we
    will perform the operation that is needed.
    ________________________________________________________________________
    */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.expr().visit(this);
        Object val = currValue;

        String uOp = ue.unaryOp().toString();

        //TODO: Operator Overload

        switch(uOp) {
            case "~": {
                if(ue.type.isInt()) {
                    int uVal = (int) val;
                    currValue = uVal * -1;
                    break;
                }
                else if(ue.type.isReal()) {
                    BigDecimal uVal = (BigDecimal) val;
                    currValue = uVal.multiply(new BigDecimal(-1));
                    break;
                }
            }
            case "not": {
                boolean uVal = (boolean) val;
                currValue = !uVal;
                break;
            }
        }
    }

    /*
    ___________________________ While Statements ___________________________
    For a while loop, we evaluate the condition and if its true, then we
    will continue to execute the body of the loop until the condition
    evaluates to be false.
    ________________________________________________________________________
    */
    public void visitWhileStmt(WhileStmt ws) {
        ws.condition().visit(this);
        while((boolean)currValue) {
            ws.whileBlock().visit(this);
            if(breakFound) {
                breakFound = false;
                break;
            }
            ws.condition().visit(this);
        }
    }
}
