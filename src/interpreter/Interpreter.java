package interpreter;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.statements.*;
import ast.top_level_decls.*;
import ast.types.Type;
import messages.*;
import messages.errors.ErrorBuilder;
import messages.errors.runtime_error.RuntimeErrorFactory;
import utilities.*;

import java.io.*;
import java.util.HashMap;
import java.math.BigDecimal;

public class Interpreter extends Visitor {

    private RuntimeStack stack;
    private SymbolTable currentScope;
    private Object currValue;
    private RuntimeErrorFactory generateRuntimeError;
    private boolean inAssignStmt;
    private boolean returnFound;
    private boolean breakFound;
    private boolean continueFound;

    public Interpreter() {
        stack = new RuntimeStack();
        generateRuntimeError = new RuntimeErrorFactory();
        this.interpretMode = true;
        returnFound = false;
        breakFound = false;
        continueFound = false;
    }

    public Interpreter(SymbolTable st) {
        this();
        this.currentScope = st;
    }

    /*
    _________________________ Array Expressions _________________________
    For an array expression, we want to retrieve the array from the stack
    and access the specific value the user wants by using the provided
    index. Additionally, C Minor starts indexing at 1, not 0.
    _____________________________________________________________________
    */
    public void visitArrayExpr(ArrayExpr ae) {
        LocalDecl arrayDecl = currentScope.findName(ae.arrayTarget().toString()).decl().asStatement().asLocalDecl();
        Vector<Object> arr = (Vector<Object>) stack.getValue(ae.arrayTarget().toString());

        int index = 0;
        Vector<Expression> dims = arrayDecl.var().init().asArrayLiteral().arrayDims();
        for(int i = 0; i < ae.arrayIndex().size(); i++) {
            ae.arrayIndex().get(i).visit(this);
            int currOffset = (int) currValue-1;
            for(int j = i+1; j < dims.size(); j++) {
                dims.get(j).visit(this);
                currOffset *= (int) currValue;
            }
            index += currOffset;
        }

        if(index < 0 || index >= arr.size()) {
            new ErrorBuilder(generateRuntimeError,interpretMode)
                    .addLocation(ae)
                    .addErrorType(MessageType.RUNTIME_ERROR_602)
                    .error();
        }

        if(inAssignStmt) { currValue = new Vector<>(new Object[]{arr,index}); }
        else { currValue = arr.get(index); }
    }

    /*
    _________________________ Array Literals _________________________
    Arrays are static in C Minor which means we will have to create an
    array for the user and then evaluate/store each initial expression
    into the array.
    __________________________________________________________________
    */
    public void visitArrayLiteral(ArrayLiteral al) {
        Vector<Object> arr = new Vector<>();

        for(Expression e : al.arrayInits()) {
            e.visit(this);
            if(currValue instanceof Vector) { arr.addAll((Vector<Object>)currValue); }
            else { arr.add(currValue); }
        }

        currValue = arr;
    }

    /*
    _________________________ Assignment Statements _________________________
    We will evaluate the LHS of the assignment to figure out what variable we
    are updating and then the RHS to know what value we are going to assign
    to it. Then, we will update the variable in the current call frame based
    on the assignment operator we have.
    _________________________________________________________________________
    */
    public void visitAssignStmt(AssignStmt as) {
        String name = as.LHS().toString();

        as.RHS().visit(this);
        Object newValue = currValue;

        String aOp = as.assignOp().toString();

        if(as.LHS().isArrayExpr()) {
            inAssignStmt = true;
            as.LHS().visit(this);
            Vector<Object> arr = (Vector) ((Vector)currValue).get(0);
            int index = (int) ((Vector)currValue).get(1);
            switch(aOp) {
                case "=": {
                    arr.set(index, newValue);
                    break;
                }
                case "+=":
                case "-=":
                case "*=":
                case "/=":
                case "%=":
                case "**=": {
                    if (as.RHS().type.isInt()) {
                        int oldVal = (int) arr.get(index);
                        int val = (int) newValue;
                        switch (aOp) {
                            case "+=" -> arr.set(index, oldVal + val);
                            case "-=" -> arr.set(index, oldVal - val);
                            case "*=" -> arr.set(index, oldVal * val);
                            case "/=" -> arr.set(index, oldVal / val);
                            case "%=" -> arr.set(index, oldVal % val);
                            case "**=" -> arr.set(index, Math.pow(oldVal, val));
                        }
                        break;
                    } else if (as.RHS().type.isReal()) {
                        BigDecimal oldVal = (BigDecimal) arr.get(index);
                        BigDecimal val = (BigDecimal) newValue;
                        switch (aOp) {
                            case "+=" -> arr.set(index, oldVal.add(val));
                            case "-=" -> arr.set(index, oldVal.subtract(val));
                            case "*=" -> arr.set(index, oldVal.multiply(val));
                            case "/=" -> arr.set(index, oldVal.divide(val));
                            case "%=" -> arr.set(index, oldVal.remainder(val));
                            case "**=" -> arr.set(index, oldVal.pow(val.toBigInteger().intValue()));
                        }
                        break;
                    } else if (as.RHS().type.isString()) {
                        String oldVal = (String) arr.get(index);
                        String val = (String) newValue;
                        arr.set(index, oldVal + val);
                        break;
                    }
                }
            }
            inAssignStmt = false;
            return;
        }

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

        if(as.LHS().isFieldExpr()) {
            String objName = as.LHS().asFieldExpr().fieldTarget().toString();
            HashMap<String,Object> instance = (HashMap<String,Object>) stack.getValue(objName);
            instance.put(name,stack.getValue(name));
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
                currValue = Type.assignmentCompatible(be.LHS().type,be.RHS().type);
                break;
            case "!instanceof":
                currValue = !Type.assignmentCompatible(be.LHS().type,be.RHS().type);
                break;
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

        for(AST decl : bs.decls()) { decl.visit(this); }

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

    /*
    ___________________________ For Statements ___________________________
    Since for loops are static, we will evaluate the LHS/RHS of the
    conditional statement to determine the number of iterations we need to
    do. From there, we will just execute the loop body and update our
    iteration counter in the stack.
    ______________________________________________________________________
    */
    public void visitForStmt(ForStmt fs) {
        if(fs.condLHS().type.isInt()) {
            int LHS = 0, RHS = 0;
            fs.condLHS().visit(this);
            LHS = (int) currValue;

            fs.condRHS().visit(this);
            RHS = (int) currValue;

            switch(fs.loopOp().toString()) {
                case "<..":
                    LHS += 1;
                    break;
                case "..<":
                    RHS -= 1;
                    break;
                case "<..<":
                    LHS += 1;
                    RHS -= 1;
                    break;
            }
            stack = stack.createCallFrame();
            stack.addValue(fs.loopVar().toString(),LHS);
            for(int i = LHS; i <= RHS; i++) {
                stack.setValue(fs.loopVar().toString(),i);
                fs.forBlock().visit(this);
            }
            stack = stack.destroyCallFrame();
        }
        else if(fs.condRHS().type.isChar()) {
            char LHS = 0, RHS = 0;
            fs.condLHS().visit(this);
            LHS = (char) currValue;

            fs.condRHS().visit(this);
            RHS = (char) currValue;

            switch(fs.loopOp().toString()) {
                case "<..":
                    LHS += 1;
                    break;
                case "..<":
                    RHS -= 1;
                    break;
                case "<..<":
                    LHS += 1;
                    RHS -= 1;
                    break;
            }

            stack = stack.createCallFrame();
            stack.addValue(fs.loopVar().toString(),LHS);
            for(char i = LHS; i <= RHS; i++) {
                stack.setValue(fs.loopVar().toString(),i);
                fs.forBlock().visit(this);
            }
            stack = stack.destroyCallFrame();
        }
    }

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
        else if(is.elifStmts().size() > 0){
            for(int i = 0; i < is.elifStmts().size(); i++) {
                IfStmt e = is.elifStmts().get(i);
                e.condition().visit(this);
                if ((boolean) currValue) {
                    e.ifBlock().visit(this);
                    break;
                }
                else if(i == is.elifStmts().size()-1 && is.elseBlock() != null) {
                    is.elseBlock().visit(this);
                }
            }
        }
        else if(is.elseBlock() != null) { is.elseBlock().visit(this); }
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

    /*
    ___________________________ In Statements ___________________________
    _____________________________________________________________________
    */
    public void visitInStmt(InStmt in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try { input = br.readLine(); }
        catch(IOException e) { System.out.println("Error! Input was not recognized"); }

        Vector<String> vals = new Vector<>();
        while(input.indexOf(' ') != -1) {
            vals.add(input.substring(0,input.indexOf(' ')));
            input = input.substring(input.indexOf(' ')+1);
        }
        vals.add(input);

        // ERROR CHECK #1: Make sure the number of input arguments matches the number
        //                 of expected input values that should be written
        if(vals.size() != in.inExprs().size()) {
            new ErrorBuilder(generateRuntimeError,interpretMode)
                    .addLocation(in)
                    .addErrorType(MessageType.RUNTIME_ERROR_600)
                    .error();
        }
        for(int i = 0; i < vals.size(); i++) {
            String currVal = vals.get(i);
            Expression currExpr = in.inExprs().get(i);
            try {
                if(currExpr.type.isInt()) {
                    if(currVal.charAt(0) == '~') { stack.setValue(currExpr.toString(),-1*Integer.parseInt(currVal.substring(1))); }
                    else { stack.setValue(currExpr.toString(),Integer.parseInt(currVal)); }
                }
                else if(currExpr.type.isReal()) {
                    if(currVal.charAt(0) == '~') { stack.setValue(currExpr.toString(),new BigDecimal(currVal.substring(1)).multiply(new BigDecimal(-1))); }
                    else { stack.setValue(currExpr.toString(), new BigDecimal(currVal)); }
                }
                else if(currExpr.type.isChar() || currExpr.type.isString()) { stack.setValue(currExpr.toString(),currVal); }
                else { stack.setValue(currExpr.toString(), currVal); }
            } catch(Exception e) {
                // ERROR CHECK #2: Make sure user input matches the type of the input variable
                new ErrorBuilder(generateRuntimeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.RUNTIME_ERROR_601)
                        .addArgs(currExpr.type)
                        .error();
            }
        }
    }

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
        Vector<Object> args = new Vector<>();
        HashMap<String,Object> vals = new HashMap<>();

        for(int i = 0; i < in.arguments().size(); i++) {
            in.arguments().get(i).visit(this);
            args.add(currValue);
        }

        SymbolTable oldScope = currentScope;
        stack = stack.createCallFrame();

        // Function Invocation
        if(in.target() == null && !in.targetType.isClassType()) {
            FuncDecl fd = currentScope.findName(in.invokeSignature()).decl().asTopLevelDecl().asFuncDecl();
            currentScope = fd.symbolTable;

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = fd.params().get(i);
                stack.addValue(currParam.toString(),args.get(i));
            }

            fd.funcBlock().visit(this);
            returnFound = false;

            for(int i = 0; i < in.arguments().size(); i++) {
                if(in.arguments().get(i).isNameExpr()) {
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

            if(!in.target().asNameExpr().getName().toString().equals("this")) { stack.addValue("this",obj); }

            ClassDecl cd = currentScope.findName(in.targetType.typeName()).decl().asTopLevelDecl().asClassDecl();
            String methodName = in.invokeSignature();

            String searchMethod = methodName;
            ClassDecl startClass = cd;
            while(!cd.symbolTable.hasName(searchMethod)) {
                startClass = currentScope.findName(startClass.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
                searchMethod = methodName + "/" + startClass.toString();
            }
            methodName = searchMethod;

            MethodDecl md = cd.symbolTable.findName(methodName).decl().asMethodDecl();
            currentScope = md.symbolTable;

            for(String s : obj.keySet()) { stack.addValue(s,obj.get(s)); }

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = md.params().get(i);
                stack.addValue(currParam.toString(),args.get(i));
            }

            md.methodBlock().visit(this);
            returnFound = false;

            for(int i = 0; i < in.arguments().size(); i++) {
                if(in.arguments().get(i).isNameExpr()) {
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

    public void visitListLiteral(ListLiteral li) {

        Vector<Object> lst = new Vector<>();

        for(int i = 0; i < li.inits().size(); i++) {
            li.inits().get(i).visit(this);
            lst.add(currValue);
        }

        currValue = lst;
    }

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
        else if(li.type.isChar()) {
            if(li.text.charAt(1) == '\\') { currValue = li.text.substring(1,3); }
            else { currValue = li.text.charAt(1); }
        }
        else if(li.type.isBool()) { currValue = Boolean.parseBoolean(li.text); }
        else if(li.type.isReal()) {
            if(li.text.charAt(0) == '~') { currValue = (new BigDecimal(li.text.substring(1)).multiply(new BigDecimal(-1))); }
            else { currValue = new BigDecimal(li.text); }
        }
        else if(li.type.isString()) { currValue = li.text.substring(1,li.text.length()-1); } // Removes single quotes
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
    public void visitNameExpr(NameExpr ne) {
        if (ne.getName().toString().equals("this")) { currValue = stack.getValue("this"); }
        else {
            currValue = stack.getValue(ne.toString());
        }
    }

    /*
    ___________________________ New Expressions ___________________________
    When we are instantiating a new object, we will create a hash map that
    stores each object's initial field value (if specified by the user) and
    then we will call the constructor we made for the user.
    _______________________________________________________________________
    */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.classType().typeName()).decl().asTopLevelDecl().asClassDecl();

        HashMap<String,Object> instance = new HashMap<String,Object>();
        for(int i = 0; i < ne.args().size(); i++) {
            Var currArg = ne.args().get(i);
            currArg.init().visit(this);
            instance.put(currArg.toString(),currValue);
        }

        currValue = instance;

        if(cd.constructor() != null) { cd.constructor().visit(this); }
    }

    /*
    ___________________________ Out Statements ___________________________
    ______________________________________________________________________
    */
    public void visitOutStmt(OutStmt os) {
        boolean endlFound = false;
        for(int i = 0; i < os.outExprs().size(); i++) {
            Expression e = os.outExprs().get(i);
            if(e.isEndl()) { System.out.println(); endlFound = true; }
            else {
                e.visit(this);
                System.out.print(currValue);
            }
        }
        if(!endlFound) System.out.println();
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
    expressions by first evaluating the individual expression. From there,
    we will perform the operation that is needed.
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
            if(breakFound || returnFound) {
                breakFound = false;
                break;
            }
            ws.condition().visit(this);
        }
    }
}
