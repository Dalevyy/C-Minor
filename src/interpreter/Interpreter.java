package interpreter;

import ast.AST;
import ast.classbody.InitDecl;
import ast.classbody.MethodDecl;
import ast.expressions.ArrayExpr;
import ast.expressions.ArrayLiteral;
import ast.expressions.BinaryExpr;
import ast.expressions.BreakStmt;
import ast.expressions.CastExpr;
import ast.expressions.ContinueStmt;
import ast.expressions.Expression;
import ast.expressions.FieldExpr;
import ast.expressions.InStmt;
import ast.expressions.Invocation;
import ast.expressions.ListLiteral;
import ast.expressions.Literal;
import ast.expressions.NameExpr;
import ast.expressions.NewExpr;
import ast.expressions.OutStmt;
import ast.expressions.This;
import ast.expressions.UnaryExpr;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.statements.AssignStmt;
import ast.statements.BlockStmt;
import ast.statements.CaseStmt;
import ast.statements.ChoiceStmt;
import ast.statements.DoStmt;
import ast.statements.ForStmt;
import ast.statements.IfStmt;
import ast.statements.ListStmt;
import ast.statements.LocalDecl;
import ast.statements.ReturnStmt;
import ast.statements.RetypeStmt;
import ast.statements.Statement;
import ast.statements.StopStmt;
import ast.statements.WhileStmt;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.types.ClassType;
import ast.types.Type;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.runtime.RuntimeErrorFactory;
import utilities.RuntimeStack;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

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
     * Creates interpreter for the VM.
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
     * Builds string that will output the contents of an array or list.<br><br>
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
     * Evaluates an array literal.<br><br>
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

    /**
     * Executes an assignment statement.<br><br>
     * <p>
     *     We will first evaluate the LHS of the assignment, so we can access
     *     the correct variable in the runtime stack. Then, we will evaluate
     *     the RHS and assign its value into the current call frame.
     * </p>
     * @param as Assignment Statement
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
            case "=":
                stack.setValue(name,newValue);
                break;
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=":
                if(as.RHS().type.isInt()) {
                    int oldVal = (int) stack.getValue(name);
                    int val = (int) newValue;
                    switch(aOp) {
                        case "+=" -> stack.setValue(name,oldVal+val);
                        case "-=" -> stack.setValue(name,oldVal-val);
                        case "*=" -> stack.setValue(name,oldVal*val);
                        case "/=" -> stack.setValue(name,oldVal/val);
                        case "%=" -> stack.setValue(name,oldVal%val);
                        case "**=" -> stack.setValue(name,(int)Math.pow(oldVal,val));
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
                else {
                    String oldVal = stack.getValue(name).toString();
                    String val = newValue.toString();
                    stack.setValue(name,oldVal+val);
                }
        }

        if(as.LHS().isFieldExpr()) {
            String objName = as.LHS().asFieldExpr().fieldTarget().toString();
            HashMap<String,Object> instance = (HashMap<String,Object>) stack.getValue(objName);
            instance.put(name,stack.getValue(name));
        }
    }

    /**
     * Evaluates a binary expression.<br><br>
     * <p>
     *     We will first evaluate the LHS and the RHS of the binary expression.
     *     Then, we will evaluate the binary expression based on the LHS and RHS
     *     types.
     * </p>
     * @param be Binary Expression
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
                        case "**" -> currValue = (int) Math.pow(lValue,rValue);
                    }
                    break;
                }
                else if(be.type.isReal()) {
                    BigDecimal lValue = new BigDecimal(LHS.toString());
                    BigDecimal rValue = new BigDecimal(RHS.toString());
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
                        case "!=" -> currValue = lValue != rValue;
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
                    BigDecimal lValue = new BigDecimal(LHS.toString());
                    BigDecimal rValue = new BigDecimal(RHS.toString());
                    switch (binOp) {
                        case "<" -> currValue = lValue.compareTo(rValue) < 0;
                        case "<=" -> currValue = lValue.compareTo(rValue) <= 0;
                        case ">" -> currValue = lValue.compareTo(rValue) > 0;
                        case ">=" -> currValue = lValue.compareTo(rValue) >= 0;
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
                ClassType objType;
                if(be.LHS().type.isMultiType())
                    objType = be.LHS().type.asMultiType().getRuntimeType();
                else
                    objType = be.LHS().type.asClassType();

                currValue = ClassType.classAssignmentCompatibility(objType,be.RHS().type.asClassType());
                if(binOp.equals("!instanceof"))
                    currValue = !(boolean)currValue;
                break;
        }
    }

    /**
     * Executes a block statement.<br><br>
     * <p>
     *     For every block statement, we will create a new call frame on
     *     the runtime stack. We will then visit every declaration and
     *     statement found inside the block statement. Depending on the
     *     flags we find, we will also terminate executing the block
     *     statement early if needed.
     * </p>
     * @param bs Block Statement
     */
    public void visitBlockStmt(BlockStmt bs) {
        stack = stack.createCallFrame();

        for(AST decl : bs.decls())
            decl.visit(this);

        for(Statement s : bs.stmts()) {
            s.visit(this);
            if(returnFound || breakFound || continueFound)
                break;
        }

        stack = stack.destroyCallFrame();
    }

    /**
     * Executes a break statement.<br><br>
     * <p>
     *     When a break statement is found, we will set the {@code breakFound}
     *     flag to be true. This will allow us to terminate the loop early, and
     *     we can move on to execute a different part of the program.
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

    /**
     * Executes a continue statement.<br><br>
     * <p>
     *     When a continue is found, we will set the {@code continueFound}
     *     flag to be true. This will allow us to stop executing the current
     *     iteration of the loop and move on to the next iteration.
     * </p>
     * @param cs Continue Statement
     */
    public void visitContinueStmt(ContinueStmt cs) { continueFound = true; }

    /**
     * Executes a do while loop.<br><br>
     * <p>
     *     For a do while loop, we will execute the loop body once and then
     *     we will continue executing the body as long as the do while's
     *     condition evaluates to be true.
     * </p>
     * @param ds Do Statement
     */
    public void visitDoStmt(DoStmt ds) {
        do {
            ds.doBlock().visit(this);
            if(breakFound || returnFound) {
                breakFound = false;
                break;
            }
            else if(continueFound)
                continueFound = false;

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
     * Evaluates a field expression.<br><br>
     * <p>
     *     We will evaluate the target for the field expression first. From there,
     *     the next evaluation will be based on what the target is trying to access
     *     <ul>
     *      <li>Name Expression: Evaluate access expression here.</li>
     *      <li>Everything Else: Evaluate at a different visit</li>
     *     </ul>
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.fieldTarget().visit(this);
        HashMap<String,Object> instance = (HashMap<String,Object>) currValue;

        if(fe.accessExpr().isNameExpr()) {
            currValue = instance.get(fe.accessExpr().toString());
            if(currValue == null) {
                new ErrorBuilder(generateRuntimeError,interpretMode)
                        .addLocation(fe)
                        .addErrorType(MessageType.RUNTIME_ERROR_606)
                        .addArgs(fe.accessExpr(),fe.fieldTarget().type.asMultiType().getRuntimeType())
                        .error();
            }
        }
        else {
            Type oldTarget = currTarget;
            if(fe.fieldTarget().type.isMultiType())
                currTarget = fe.fieldTarget().type.asMultiType().getRuntimeType();
            else
                currTarget = fe.fieldTarget().type;
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
            int LHS, RHS;
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
            char LHS, RHS;
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

    /**
     * Executes a global declaration statement.<br><br>
     * <p>
     *     By executing a global declaration, we will be adding a new
     *     global variable to the runtime stack. If no initial value was
     *     given to the variable by the user, then the variable will
     *     automatically be assigned to be null.
     * </p>
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.var().init() == null)
            currValue = null;
        else
            gd.var().init().visit(this);

        stack.addValue(gd.var().toString(),currValue);
    }

    /**
     * Executes an if statement.<br><br>
     * <p>
     *     For an if statement, we will evaluate the condition first
     *     to determine which block of the if statement we will need
     *     to execute.
     * </p>
     * @param is If Statement
     */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);
        boolean condition = (boolean) currValue;
        if(condition)
            is.ifBlock().visit(this);
        else if(!is.elifStmts().isEmpty()){
            for(int i = 0; i < is.elifStmts().size(); i++) {
                IfStmt e = is.elifStmts().get(i);
                e.condition().visit(this);
                if ((boolean) currValue) {
                    e.ifBlock().visit(this);
                    break;
                }
            }
        }
        if(!condition && is.elseBlock() != null)
            is.elseBlock().visit(this);
    }

    /**
     * Executes a constructor declaration.<br><br>
     * <p>
     *     A constructor declaration is only visited during a {@code visitNewExpr}
     *     when we are trying to instantiate a new object. For all fields not
     *     specified in the new expression, we will add these fields with an initial
     *     value to the object's hash map.
     * </p>
     * @param id Init Declaration
     */
    public void visitInitDecl(InitDecl id) {
        HashMap<String,Object> instance = (HashMap<String,Object>) currValue;

        for(AssignStmt as : id.assignStmts()) {
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
        Vector<ParamDecl> params;
        HashMap<String,Object> vals = new HashMap<>();

        HashMap<String,Object> obj = null;
        if(in.targetType.isClassType()) {
            if(!(currValue instanceof HashMap))
                obj = (HashMap<String,Object>) stack.getValue("this");
            else
                obj = (HashMap<String,Object>) currValue;
        }

        for(Expression arg : in.arguments()) {
            arg.visit(this);
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
        if(in.targetType.isVoidType()) {
            FuncDecl fd = currentScope.findName(in.invokeSignature()).decl().asTopLevelDecl().asFuncDecl();
            params = fd.params();
            currentScope = fd.symbolTable;

            for(int i = 0; i < in.arguments().size(); i++)
                stack.addValue(params.get(i).toString(),args.get(i));

            fd.funcBlock().visit(this);
        }
        // Method Invocation
        else {
            // ERROR CHECK #1: Generate an exception if the current object type
            //                 is not assignment compatible with the target type
            if(!ClassType.classAssignmentCompatibility(currTarget.asClassType(),in.targetType.asClassType())) {
                new ErrorBuilder(generateRuntimeError,interpretMode)
                        .addLocation(in)
                        .addErrorType(MessageType.RUNTIME_ERROR_604)
                        .addArgs(in.toString(),currTarget,in.targetType)
                        .error();
            }

            ClassDecl cd = currentScope.findName(currTarget.toString()).decl().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.invokeSignature()).decl().asMethodDecl();
            params = md.params();
            currentScope = md.symbolTable;

            if(obj != null)
                for(Object val : obj.keySet())
                    stack.addValue(val.toString(),obj.get(val));

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = md.params().get(i);
                stack.addValue(currParam.toString(),args.get(i));
            }

            md.methodBlock().visit(this);
        }

        returnFound = false;
        for(int i = 0; i < in.arguments().size(); i++) {
            if(in.arguments().get(i).isNameExpr()) {
                String argName = in.arguments().get(i).toString();
                ParamDecl currParam = params.get(i);

                if(currParam.mod.isOut() || currParam.mod.isInOut() || currParam.mod.isRef())
                    vals.put(argName,stack.getValue(currParam.toString()));
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

    /**
     * Evaluates a literal.<br><br>
     * <p>
     *     We will interpret the value of the literal
     *     and save it into the {@code currValue}.
     * </p>
     * @param li Literal
     */
    public void visitLiteral(Literal li) {
        switch(li.getConstantKind()) {
            case INT:
                currValue = Integer.parseInt(li.text);
                break;
            case CHAR:
                currValue = li.text.charAt(1);
                break;
            case BOOL:
                currValue = Boolean.parseBoolean(li.text);
                break;
            case REAL:
                currValue = new BigDecimal(li.text);
                break;
            case STR:
                currValue = li.text.substring(1,li.text.length()-1); // Removes quotes
                break;
            default:
                if(li.type.asEnumType().constantType().isInt())
                    currValue = Integer.parseInt(li.text);
                else
                    currValue = li.text.charAt(1);
        }
    }

    /**
     * Executes a local declaration statement.<br><br>
     * <p>
     *     By executing a local declaration, we will allocate space
     *     on the runtime stack to store a new local value. If there is
     *     no value to store (i.e. if a user did not assign any value to
     *     the variable), we will just store null.
     * </p>
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        if(ld.var().init() == null)
            currValue = null;
        else
            ld.var().init().visit(this);

        stack.addValue(ld.var().toString(),currValue);
    }

    /**
     * Evaluates a name expression.<br><br>
     * <p>
     *     By evaluating a name expression, we are trying to access the
     *     value stored at its memory location in the runtime stack.
     * </p>
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        if(ne.toString().equals("this"))
            currValue = stack.getValue("this");
        else
            currValue = stack.getValue(ne.toString());
    }

    /**
     * Evaluates a new expression.<br><br>
     * <p>
     *     When we are instantiating a new object, we will use a hash map to
     *     keep track of the internal state of the object. During this visit,
     *     we will store all fields the user chose to initialize prior to
     *     calling the constructor to handle the rest of the field initializations.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.classType().typeName()).decl().asTopLevelDecl().asClassDecl();

        HashMap<String,Object> instance = new HashMap<>();
        for(Var v : ne.args()) {
            v.init().visit(this);
            instance.put(v.toString(),currValue);
        }
        currValue = instance;

        if(cd.constructor() != null)
            cd.constructor().visit(this);
    }

    /**
     * Evaluates expressions that will be printed inside the VM.<br><br>
     * <p>
     *     We will visit every expression contained in the current
     *     output statement and print each value to the terminal.
     * </p>
     * @param os Output Statement
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.outExprs()) {
            e.visit(this);
            if(e.isEndl())
                System.out.println();
            else if(e.type.isArrayType() || e.type.isListType()) {
                Vector<Object> arr = (Vector<Object>) currValue;
                StringBuilder sb = new StringBuilder();
                printList(arr,sb);
                System.out.print(sb);
            }
            else
                System.out.print(currValue);
        }
        System.out.println();
    }

    /**
     * Executes a return statement.<br><br>
     * <p>
     *     When we encounter a return statement, we will evaluate the expression
     *     that will be returned (if there is any) and set the {@code returnFound}
     *     flag to be true.
     * </p>
     * @param rs Return Statements
     */
    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.expr() != null)
            rs.expr().visit(this);
        returnFound = true;
    }

    public void visitRetypeStmt(RetypeStmt rs) {
        rs.getNewObject().visit(this);
        stack.setValue(rs.getName().toString(),currValue);

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

    /**
     * Executes a stop statement.<br><br>
     * <p>
     *     If a stop statement is written by the user, we
     *     will simply terminate the C Minor interpreter.
     * </p>
     * @param ss Stop Statement
     */
    public void visitStopStmt(StopStmt ss) { System.exit(1); }

    /**
     * Executes a {@code This} statement.<br><br>
     * <p>
     *     When we are inside of a class, we want to make sure we access the
     *     correct fields and methods for the current object. This will be done
     *     by
     * </p>
     * @param t This Statement
     */
    public void visitThis(This t) { currValue = stack.getValue("this"); }

    /**
     * Evaluates a unary expression.<br><br>
     * <p>
     *     We will evaluate the unary expression and store
     *     the result into the {@code currValue} variable.
     * </p>
     * @param ue Unary Expression
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.expr().visit(this);

        //TODO: Operator Overload

        switch(ue.unaryOp().toString()) {
            case "~":
                if(ue.type.isInt())
                    currValue = ~((int) currValue);
                else
                    currValue = ~((char) currValue);
                break;
            case "not":
                currValue = !((boolean) currValue);
                break;
        }
    }

    /**
     * Executes a while loop.<br><br>
     * <p>
     *     For a while loop, we will evaluates its condition and executes its
     *     body as long as its condition remains true.
     * </p>
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        ws.condition().visit(this);
        while((boolean)currValue) {
            ws.whileBlock().visit(this);
            if(breakFound || returnFound) {
                breakFound = false;
                break;
            }
            else if(continueFound)
                continueFound = false;
            ws.condition().visit(this);
        }
    }
}
