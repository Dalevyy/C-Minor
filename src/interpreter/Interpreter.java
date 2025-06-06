package interpreter;

import ast.*;
import ast.classbody.*;
import ast.expressions.*;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.ClassType;
import ast.types.DiscreteType;
import ast.types.Type;
import messages.*;
import messages.errors.ErrorBuilder;
import messages.errors.runtime.RuntimeErrorFactory;
import utilities.*;

import java.io.*;
import java.util.HashMap;
import java.math.BigDecimal;

public class Interpreter extends Visitor {

    private RuntimeStack stack;
    private SymbolTable currentScope;
    private Object currValue;
    private Type currTarget;
    private final RuntimeErrorFactory generateRuntimeError;
    private boolean inAssignStmt;
    private boolean returnFound;
    private boolean breakFound;
    private boolean continueFound;

    /**
     * Creates interpreter for the VM
     * @param st Symbol Table
     */
    public Interpreter(SymbolTable st) {
        stack = new RuntimeStack();
        generateRuntimeError = new RuntimeErrorFactory();
        this.interpretMode = true;
        returnFound = false;
        breakFound = false;
        continueFound = false;
        this.currentScope = st;
    }

    /**
     * Build string that will output the contents of an array or list.<br><br>
     * <p>
     *     To avoid users needing to create for loops to view individual contents
     *     of an array or list, C Minor will allow a user to output the array or
     *     list directly in order to see what they are storing. This method aids
     *     in generating the output for the user.
     * </p>
     * @param arr Current array we are building the output for
     * @param sb String builder
     */
    private void printList(Vector<Object> arr,StringBuilder sb) {
        sb.append("[");
        for(int i = 1; i < arr.size(); i++) {
            if(arr.get(i) instanceof Vector)
                printList((Vector<Object>)arr.get(i),sb);
            else
                sb.append(arr.get(i));
            if(i != arr.size()-1)
                sb.append(", ");
        }
        sb.append("]");
    }

    /*
    _________________________ Array Expressions _________________________
    For an array expression, we want to retrieve the array from the stack
    and access the specific value the user wants by using the provided
    index. Additionally, C Minor starts indexing at 1, not 0.
    _____________________________________________________________________
    */
    public void visitArrayExpr(ArrayExpr ae) {
        Vector<Object> arr;
        if(currValue instanceof HashMap)
            arr = (Vector<Object>) ((HashMap<String,Object>) currValue).get(ae.toString());
        else {
            ae.arrayTarget().visit(this);
            arr = (Vector<Object>) currValue;
        }
                   Vector<Object> curr = arr;

        for(int i = 0; i < ae.arrayIndex().size(); i++) {
            ae.arrayIndex().get(i).visit(this);
            int offset = (int) currValue;

            if(arr.get(0) instanceof ArrayLiteral) {
                ArrayLiteral al = (ArrayLiteral)arr.get(0);
                if(!al.arrayDims().isEmpty())
                    al.arrayDims().get(i).visit(this);
                else if(i == 0)
                    currValue = arr.size() -1;
                else
                    currValue = arr.size()-1;
            }
            else if(i==0)
                currValue = arr.size()-1;
            else
                currValue = arr.size()-1;

            if (offset <= 0 || offset > (int)currValue) {
                    new ErrorBuilder(generateRuntimeError, interpretMode)
                            .addLocation(ae)
                            .addErrorType(MessageType.RUNTIME_ERROR_603)
                            .error();
            }


            Object v = curr.get(offset);
            if(v instanceof Vector)
                curr = (Vector) v;
            else {
                currValue = v;
                if(inAssignStmt) { currValue = new Vector<>(new Object[]{curr,offset}); }
            }
        }
    }

    /**
     * Evaluates an array literal.
     * <p>
     *     Arrays are static in C Minor, so a user can not change its size
     *     once an array literal is declared. We will evaluate every expression
     *     for the current array literal and store it in a <code>Vector</code>.
     *     This will emulate the array during runtime.
     * </p>
     * @param al Array Literal
     */
    public void visitArrayLiteral(ArrayLiteral al) {
        Vector<Object> arr = new Vector<>();

        for(Expression e : al.arrayInits()) {
            e.visit(this);
            arr.add(currValue);
        }

        // Add the array literal to the Vector, so its
        // dimensions can be used during a visit to ArrayExpr
        arr.add(0,al);
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
                currValue = ClassType.classAssignmentCompatibility(be.LHS().type.asClassType(),
                                                                   be.RHS().type.asClassType());
                break;
            case "!instanceof":
                currValue = !ClassType.classAssignmentCompatibility(be.LHS().type.asClassType(),
                                                                    be.RHS().type.asClassType());
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

    /**
     * Terminates the current loop
     * <p>
     *     When a break statement is found, we will set the breakFound
     *     flag to be true in order to exit the current executing loop.
     * </p>
     * @param bs Break Statement
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
                cs.otherBlock().visit(this);
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
            if(breakFound || returnFound) {
                breakFound = false;
                break;
            }
            ds.condition().visit(this);
        } while ((boolean) currValue);
    }

    /**
     * When visiting an <code>EnumDecl</code>, we will evaluate each constant
     * and store the constants into the runtime stack.
     * @param ed Current enumeration
     */
    public void visitEnumDecl(EnumDecl ed) {
        for(Var constant : ed.constants()) {
            constant.init().visit(this);
            stack.addValue(constant.toString(),currValue);
        }
    }


    /**
     * We will evaluate the target for the field expression first. From there, the
     * next evaluation will be based on what the target is trying to access
     * <ul>
     *     <li>Name Expression: Evaluate access expression here.</li>
     *     <li>Everything Else: Evaluate at a different visit</li>
     * </ul>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        HashMap<String,Object> instance = (HashMap<String,Object>) currValue;

        if(fe.accessExpr().isNameExpr())
            currValue = instance.get(fe.accessExpr().toString());
        else {
            Type oldTarget = currTarget;
            if(fe.fieldTarget().type.isClassType())
                currTarget = fe.fieldTarget().type;
            else
                currTarget = fe.fieldTarget().type.asMultiType().getRuntimeType();
            fe.accessExpr().visit(this);
            currTarget = oldTarget;
        }
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
        if(fs.condLHS().type.isInt() || fs.condLHS().type.isEnumType()) {
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
        else {

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
        if(gd.var().init() == null) { currValue = null; }
        else { gd.var().init().visit(this); }

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
        HashMap<String,Object> obj = null;
        if(in.targetType.isClassType())
           obj = (HashMap<String,Object>) currValue;


        for(int i = 0; i < in.arguments().size(); i++) {
            in.arguments().get(i).visit(this);
            args.add(currValue);
        }
        if(in.toString().equals("length")) {
            Vector<Object> arr = (Vector<Object>) currValue;
            currValue = arr.size() - 1;
            return;
        }

        SymbolTable oldScope = currentScope;
        stack = stack.createCallFrame();

        // Function Invocation
        if(!in.targetType.isClassType()) {
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
            // ERROR CHECK #1: Generate an exception if the current object
            //                 does not match the expected target type
            if(!currTarget.toString().equals(in.targetType.toString())) {
                if(!currentScope.hasName(in.targetType.typeName()))
                    new ErrorBuilder(generateRuntimeError,interpretMode)
                            .addLocation(in)
                            .addErrorType(MessageType.RUNTIME_ERROR_604)
                            .addArgs(in.toString(),currTarget,in.targetType)
                            .error();
            }

            ClassDecl cd = currentScope.findName(in.targetType.typeName()).decl().asTopLevelDecl().asClassDecl();
//            String methodName = in.invokeSignature();
//
//            String searchMethod = methodName;
//            ClassDecl startClass = cd;
//            while(!cd.symbolTable.hasName(searchMethod)) {
//                startClass = currentScope.findName(startClass.superClass().toString()).decl().asTopLevelDecl().asClassDecl();
//                searchMethod = methodName + "/" + startClass.toString();
//            }
//            methodName = searchMethod;

            MethodDecl md = cd.symbolTable.findName(in.invokeSignature()).decl().asMethodDecl();
            currentScope = md.symbolTable;

            if(obj != null)
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

    public void visitListLiteral(ListLiteral ll) {
        Vector<Object> lst = new Vector<>();

        for(Expression e : ll.inits()) {
            e.visit(this);
            lst.add(currValue);
        }

        lst.add(0,ll);
        currValue = lst;
    }

    /**
     * Executes a list statement command.<br><br>
     * <p>
     *     This method will execute the current list command based on the
     *     provided arguments. If there are any issues, then we will produce
     *     an exception for the user.
     * </p>
     * @param ls The current list statement we will be executing.
     */
    public void visitListStmt(ListStmt ls) {
        // Obtain the list from the stack
        ls.getListName().visit(this);
        Vector<Object> lst = (Vector<Object>) currValue;
        int index = lst.size();

        ls.getAllArgs().get(1).visit(this);
        switch(ls.getCommand()) {
            case INSERT:
                index = (int) currValue;
                // ERROR CHECK #1: Make sure a valid position was given for an element to be inserted at
                if(index+1 <= 1 || index > lst.size()-1) {
                    new ErrorBuilder(generateRuntimeError,interpretMode)
                            .addLocation(ls)
                            .addErrorType(MessageType.RUNTIME_ERROR_605)
                            .addArgs(ls.getListName(),lst.size()-1,index)
                            .error();
                }
                ls.getAllArgs().get(2).visit(this);
            case APPEND:
                if(currValue instanceof Vector) {
                    Vector<Object> sublist = (Vector<Object>) currValue;
                    if(ls.getListType().numOfDims - ((ListLiteral)sublist.get(0)).type.asListType().numOfDims > 0)
                        lst.add(index,currValue);
                    else {
                        for(int i = 1; i < ((Vector)currValue).size();i++) {
                            lst.add(index,((Vector)currValue).get(i));
                            index += 1;
                        }
                    }
                }
                else
                    lst.add(index,currValue);
                break;
            case REMOVE:
                if(currValue instanceof Vector) {
                    Vector<Object> removeElements = (Vector) currValue;
                    if(ls.getListType().numOfDims - ((ListLiteral)removeElements.get(0)).type.asListType().numOfDims > 0)
                        lst.removeAll(currValue);
                    else
                        for(Object o : removeElements)
                            lst.removeAll(o);
                }
                else
                    lst.remove((int)currValue);
                break;
        }
    }

    /*
    ________________________ Literals ________________________
    Whenever we visit a literal, we will just evaluate it and
    set the current value to equal the evaluation result.
    __________________________________________________________
    */
    public void visitLiteral(Literal li) {
        if(li.type.isInt() || (li.type.isEnumType() && li.type.asEnumType().constantType().isInt())) {
            if(li.text.charAt(0) == '~') { currValue = (-1*Integer.parseInt(li.text.substring(1))); }
            else { currValue = Integer.parseInt(li.text); }
        }
        else if(li.type.isChar() || (li.type.isEnumType() && li.type.asEnumType().constantType().isChar())) {
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
        if(ld.var().init() == null) { currValue = null; }
        else { ld.var().init().visit(this); }

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

        HashMap<String,Object> instance = new HashMap<>();
        for(Var v : ne.args()) {
            v.init().visit(this);
            instance.put(v.toString(),currValue);
        }
        currValue = instance;

        if(cd.constructor() != null) { cd.constructor().visit(this); }
    }

    /**
     * Evaluates and prints out expressions in the VM
     * <p>
     *     We will print out every expression that appears in the current
     *     output statement during this visit.
     * </p>
     * @param os Output Statement
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.outExprs()) {
            e.visit(this);
            if(e.isEndl())
                System.out.println();
            else {
                if(e.type.isArrayType() || e.type.isListType()) {
                    Vector<Object> arr = (Vector<Object>) currValue;
                    StringBuilder sb = new StringBuilder();
                    printList(arr,sb);
                    System.out.print(sb);
                }
                else
                    System.out.print(currValue);
            }
        }
        System.out.println();
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

    public void visitRetypeStmt(RetypeStmt rs) {
        rs.getNewObject().visit(this);
        stack.addValue(rs.getName().toString(),currValue);

        AST decl = currentScope.findName(rs.getName().toString()).decl();
        if(decl.isTopLevelDecl()) {
            if (decl.asTopLevelDecl().asGlobalDecl().type().isMultiType())
                decl.asTopLevelDecl().asGlobalDecl().type().asMultiType().setRuntimeType(rs.getNewObject().type.asClassType());
        }
        else if(decl.isFieldDecl()) {
            if(decl.asFieldDecl().type().isMultiType())
                decl.asFieldDecl().type().asMultiType().setRuntimeType(rs.getNewObject().type.asClassType());
        }
        else {
            if(decl.asStatement().asLocalDecl().type().isMultiType())
                decl.asStatement().asLocalDecl().type().asMultiType().setRuntimeType(rs.getNewObject().type.asClassType());
        }
    }

    /*
    __________________________ Stop Statements  __________________________
    When we encounter a `stop`, we are going to terminate the interpreter.
    ______________________________________________________________________
    */
    public void visitStopStmt(StopStmt ss) { System.exit(1); }

    public void visitThis(This t) { currValue = stack.getValue("this"); }

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
