package interpreter;

import ast.classbody.InitDecl;
import ast.classbody.MethodDecl;
import ast.expressions.*;
import ast.misc.CompilationUnit;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.ClassType;
import ast.types.DiscreteType;
import ast.types.ScalarType;
import ast.types.Type;
import interpreter.value.RuntimeList;
import interpreter.value.RuntimeObject;
import interpreter.value.Value;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.runtime.RuntimeError;
import utilities.RuntimeStack;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

/**
 * A {@link Visitor} class that executes a C Minor program.
 * <p>
 *     When a user is in interpretation mode, a program will be
 *     evaluated and executed by this class. Currently, compilation
 *     mode will also execute the program through this class, though
 *     this is only temporary.
 * </p>
 * @author Daniel Levy
 */
public class Interpreter extends Visitor {

    /**
     * An imitation of a {@link RuntimeStack}
     */
    private RuntimeStack stack;

    /**
     * {@link SymbolTable} to denote the current scope we are in for calling functions/methods.
     */
    private SymbolTable currentScope;

    /**
     * Stores the current value the interpreter is evaluating.
     */
    private Value currentValue;

    /**
     * Flag set when a {@code break} statement is found.
     */
    private boolean breakFound;

    /**
     * Flag set when a {@code continue} statement is found.
     */
    private boolean continueFound;

    /**
     * Flag set when the {@link Interpreter} executes an assignment statement.
     */
    private boolean insideAssignment;

    /**
     * Flag set when the {@link Interpreter} executes an output statement.
     */
    private boolean output;

    /**
     * Flag set when a {@code return} statement is found.
     */
    private boolean returnFound;

    /**
     * Creates interpreter for the VM.
     * @param st Symbol Table
     */
    public Interpreter(SymbolTable st) {
        this.stack = new RuntimeStack();
        this.currentScope = st;
        this.breakFound = false;
        this.continueFound = false;
        this.insideAssignment = false;
        this.output = false;
        this.returnFound = false;
        this.handler = new MessageHandler();
    }

    /**
     * Resets the {@link #currentValue} to prevent any unwarranted errors to occur.
     *
     */
    public void reset() {
        this.currentValue = null;
        this.insideAssignment = false;
        if(this.output) {
            this.output = false;
            System.out.println();
        }
    }

    /**
     * Evaluates an array expression.
     * <p><br>
     *     We will retrieve the array from the stack, and we will access whatever
     *     value is stored at the position specified. In C Minor, arrays and lists
     *     are indexed starting at 1, not 0, so we will factor that into our runtime
     *     error checking.
     * </p>
     * @param ae Array Expression
     */
    public void visitArrayExpr(ArrayExpr ae) {
        ae.getArrayTarget().visit(this);
        RuntimeList lst = currentValue.asList();

        for(int i = 0; i < ae.getArrayIndex().size(); i++) {
            ae.getArrayIndex().get(i).visit(this);
            int offset = currentValue.asInt();

            // Figures out the maximum size of the current array/list
            if(lst.isArray()) {
                ArrayLiteral al = lst.getMetaData().asArrayLiteral();
                if(!al.getArrayDims().isEmpty())
                    al.getArrayDims().get(i).visit(this);
                else
                    currentValue = new Value(lst.size(),new DiscreteType(DiscreteType.Discretes.INT));
            } else
                currentValue = new Value(lst.size(),new DiscreteType(DiscreteType.Discretes.INT));

            // error check yay
            if(offset <= 0 || offset > currentValue.asInt()) {
                handler.createErrorBuilder(RuntimeError.class)
                        .addLocation(ae.getFullLocation())
                        .addErrorNumber(MessageNumber.RUNTIME_ERROR_603)
                        .generateError();
            }

            currentValue = lst.get(offset);
            if(currentValue.isList())
                lst = currentValue.asList();
            else if(insideAssignment) {
                lst.setOffset(offset);
                currentValue = lst;
            }
        }
    }

    /**
     * Evaluates an array literal.
     * <p><br>
     *     Arrays are static in C Minor, so a user can not change its size
     *     once an array literal is declared. We will evaluate every expression
     *     for the current array literal and store it in a {@link RuntimeList}
     *     This will emulate an array in memory during runtime.
     * </p>
     * @param al Array Literal
     */
    public void visitArrayLiteral(ArrayLiteral al) {
        RuntimeList arr = new RuntimeList(al);

        for(Expression e : al.getArrayInits()) {
            e.visit(this);
            arr.addElement(currentValue);
        }

        currentValue = arr;
    }

    /**
     * Executes an assignment statement.
     * <p><br>
     *     We will evaluate the LHS and RHS of the {@link AssignStmt}, so we
     *     can calculate the new value we need to assign. We will then store
     *     the resulting value into the stack.
     * </p>
     * @param as Assignment Statement
     */
    public void visitAssignStmt(AssignStmt as) {
        String assignOp = as.assignOp().toString();

        insideAssignment = true;
        as.LHS().visit(this);
        Value oldValue = currentValue;
        insideAssignment = false;

        as.RHS().visit(this);
        Value newValue = currentValue;

        switch(assignOp) {
            case "=":
                break;
            default:
                if(as.RHS().type.isInt()) {
                    switch (assignOp) {
                        case "+=" -> newValue = new Value(oldValue.asInt() + newValue.asInt(), newValue.getType());
                        case "-=" -> newValue = new Value(oldValue.asInt() - newValue.asInt(), newValue.getType());
                        case "*=" -> newValue = new Value(oldValue.asInt() * newValue.asInt(), newValue.getType());
                        case "/=" -> newValue = new Value(oldValue.asInt() / newValue.asInt(), newValue.getType());
                        case "%=" -> newValue = new Value(oldValue.asInt() % newValue.asInt(), newValue.getType());
                        case "**=" -> newValue = new Value(Math.round(Math.pow(oldValue.asInt(), newValue.asInt())), newValue.getType());
                    }
                } else if(as.RHS().type.isReal()) {
                    switch(assignOp) {
                        case "+=" -> newValue = new Value(oldValue.asReal().add(newValue.asReal()), newValue.getType());
                        case "-=" -> newValue = new Value(oldValue.asReal().subtract(newValue.asReal()), newValue.getType());
                        case "*=" -> newValue = new Value(oldValue.asReal().multiply(newValue.asReal()), newValue.getType());
                        case "/=" -> newValue = new Value(oldValue.asReal().divide(newValue.asReal(), MathContext.DECIMAL128), newValue.getType());
                        case "%=" -> newValue = new Value(oldValue.asReal().remainder(newValue.asReal()), newValue.getType());
                        case "**=" -> newValue = new Value(oldValue.asReal().pow(newValue.asReal().toBigInteger().intValue(),MathContext.DECIMAL128), newValue.getType());
                    }
                } else if(as.RHS().type.isString())
                    newValue = new Value(newValue.asString() + oldValue.asString(), newValue.getType());
        }

        if(as.LHS().isNameExpr())
            stack.setValue(as.LHS(), newValue);
        else if(as.LHS().isFieldExpr()) {
            insideAssignment = true;
            as.LHS().visit(this);
            currentValue.asObject().setField(as.LHS().asFieldExpr().getFieldName(),newValue);
            insideAssignment = false;
        }
        else if(as.LHS().isArrayExpr()) {
            insideAssignment = true;
            as.LHS().visit(this);
            currentValue.asList().addElement(newValue);
            insideAssignment = false;
        }
    }

    /**
     * Evaluates a binary expression.
     * <p><br>
     *     We first need to evaluate the values of the LHS and RHS of the current
     *     binary expression. Then, we will perform the correct binary operation
     *     based on the type the binary expression evaluates to.
     * </p>
     * @param be Binary Expression
     */
    public void visitBinaryExpr(BinaryExpr be) {
        String binOp = be.getBinaryOp().toString();

        be.getLHS().visit(this);
        Value LHS = currentValue;

        if(!binOp.equals("instanceof") && !binOp.equals("!instanceof"))
            be.getRHS().visit(this);
        Value RHS = currentValue;

        switch(binOp) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "**":
                if(be.type.isInt()) {
                    switch(binOp) {
                        case "+" -> currentValue = new Value(LHS.asInt() + RHS.asInt(),be.type);
                        case "-" -> currentValue = new Value(LHS.asInt() - RHS.asInt(),be.type);
                        case "*" -> currentValue = new Value(LHS.asInt() * RHS.asInt(),be.type);
                        case "/" -> currentValue = new Value(LHS.asInt() / RHS.asInt(),be.type);
                        case "%" -> currentValue = new Value(LHS.asInt() % RHS.asInt(),be.type);
                        case "**" -> currentValue = new Value(Math.round(Math.pow(LHS.asInt(),RHS.asInt())),be.type);
                    }
                    break;
                }
                else if(be.type.isReal()) {
                    switch(binOp) {
                        case "+" -> currentValue = new Value(LHS.asReal().add(RHS.asReal()),be.type);
                        case "-" -> currentValue = new Value(LHS.asReal().subtract(RHS.asReal()),be.type);
                        case "*" -> currentValue = new Value(LHS.asReal().multiply(RHS.asReal()),be.type);
                        case "/" -> currentValue = new Value(LHS.asReal().divide(RHS.asReal(),MathContext.DECIMAL128),be.type);
                        case "%" -> currentValue = new Value(LHS.asReal().remainder(RHS.asReal()),be.type);
                        case "**" -> currentValue = new Value(LHS.asReal().pow(RHS.asReal().toBigInteger().intValue(),MathContext.DECIMAL128),be.type);
                    }
                    break;
                }
                else if(be.type.isString()) {
                    currentValue = new Value(LHS.asString() + RHS.asString(),be.type);
                    break;
                }
            case "==":
            case "!=":
                switch(binOp) {
                    case "==" -> currentValue = new Value(LHS.equals(RHS),be.type);
                    case "!=" -> currentValue = new Value(!LHS.equals(RHS),be.type);
                }
                break;
            case "<":
            case "<=":
            case ">":
            case ">=":
                if(be.getRHS().type.isInt()) {
                    switch (binOp) {
                        case "<" -> currentValue = new Value(LHS.asInt() < RHS.asInt(),be.type);
                        case "<=" -> currentValue = new Value(LHS.asInt() <= RHS.asInt(),be.type);
                        case ">" -> currentValue = new Value(LHS.asInt() > RHS.asInt(),be.type);
                        case ">=" -> currentValue = new Value(LHS.asInt() >= RHS.asInt(),be.type);
                    }
                    break;
                }
                else if(be.getRHS().type.isReal()) {
                    switch (binOp) {
                        case "<" -> currentValue = new Value(LHS.asReal().compareTo(RHS.asReal()) < 0, be.type);
                        case "<=" -> currentValue = new Value(LHS.asReal().compareTo(RHS.asReal()) <= 0, be.type);
                        case ">" -> currentValue = new Value(LHS.asReal().compareTo(RHS.asReal()) > 0, be.type);
                        case ">=" -> currentValue = new Value(LHS.asReal().compareTo(RHS.asReal()) >= 0, be.type);
                    }
                    break;
                }
            case "and":
            case "or":
                switch(binOp) {
                    case "and" -> currentValue = new Value(LHS.asBool() && RHS.asBool(),be.type);
                    case "or" -> currentValue = new Value(LHS.asBool() || RHS.asBool(),be.type);
                }
                break;
            case "<<":
            case ">>":
                switch(binOp) {
                    case "<<" -> currentValue = new Value(LHS.asInt() << RHS.asInt(), be.type);
                    case ">>" -> currentValue = new Value(LHS.asInt() >> RHS.asInt(), be.type);
                }
                break;
            case "&":
            case "|":
            case "^":
                if(be.getRHS().type.isInt()) {
                    switch(binOp) {
                        case "&" -> currentValue = new Value(LHS.asInt() & RHS.asInt(),be.type);
                        case "|" -> currentValue = new Value(LHS.asInt() | RHS.asInt(),be.type);
                        case "^" -> currentValue = new Value(LHS.asInt() ^ RHS.asInt(),be.type);
                    }
                    break;
                }
                else if(be.getRHS().type.isInt()) {
                    switch(binOp) {
                        case "&" -> currentValue = new Value(LHS.asChar() & RHS.asChar(),be.type);
                        case "|" -> currentValue = new Value(LHS.asChar() | RHS.asChar(),be.type);
                        case "^" -> currentValue = new Value(LHS.asChar() ^ RHS.asChar(),be.type);
                    }
                    break;
                }
                else if(be.getRHS().type.isBool()) {
                    switch (binOp) {
                        case "&" -> currentValue = new Value(LHS.asBool() & RHS.asBool(),be.type);
                        case "|" -> currentValue = new Value(LHS.asBool() | RHS.asBool(),be.type);
                        case "^" -> currentValue = new Value(LHS.asBool() ^ RHS.asBool(),be.type);
                    }
                    break;
                }
            case "instanceof":
            case "!instanceof":
                ClassType objType = LHS.asObject().getCurrentType();
                ClassType classType = be.getRHS().type.asClassType();

                switch(binOp) {
                    case "instanceof" -> currentValue = new Value(ClassType.classAssignmentCompatibility(objType,classType),be.type);
                    case "!instanceof" -> currentValue = new Value(!ClassType.classAssignmentCompatibility(objType,classType),be.type);
                }
                break;
        }
    }

    /**
     * Executes a block statement.
     * <p><br>
     *     We will create a new call frame on the {@link #stack} every time
     *     we visit a block statement. We then individually visit each statement
     *     found in the block statement. First we execute all local declaration
     *     statements followed by the remaining statements in the block. Once each
     *     statement has been visited (or if we encounter a statement that requires
     *     us to terminate the execution of the block), we will destroy the call frame.
     * </p>
     * @param bs Block Statement
     */
    public void visitBlockStmt(BlockStmt bs) {
        stack = stack.createCallFrame();

        for(LocalDecl decl : bs.decls())
            decl.visit(this);

        for(Statement s : bs.stmts()) {
            s.visit(this);
            /*
                The `break`, `continue`, and `return` keywords will terminate
                the execution of the current block statement we are in.
            */
            if(returnFound || breakFound || continueFound)
                break;
        }

        stack = stack.destroyCallFrame();
    }

    /**
     * Executes a break statement.
     * <p><br>
     *     When a break statement is executed, we will set the {@link #breakFound}
     *     flag to be true. This means we will need to terminate the current loop
     *     statement that is executing.
     * @param bs Break Statement
     */
    public void visitBreakStmt(BreakStmt bs) { breakFound = true; }

    /**
     * Evaluates a cast expression.
     * <p><br>
     *     We will evaluate the cast expression's value and
     *     typecast it to the appropriate type.
     * </p>
     * @param cs Cast Expression
     */
    public void visitCastExpr(CastExpr cs) {
        cs.getCastExpr().visit(this);
        if(cs.getCastType().isInt()) {
            if(cs.getCastExpr().type.isReal())
                currentValue = new Value(currentValue.asReal().intValue(),cs.type);
            else
                currentValue = new Value((int) currentValue.asChar(),cs.type);
        }
        else if(cs.getCastType().isReal()) {
            if(cs.getCastExpr().type.isInt())
                currentValue = new Value(new BigDecimal(currentValue.asInt()),cs.type);
        }
        else if(cs.getCastType().isString()) {
            if(cs.getCastType().isChar())
                currentValue = new Value(currentValue.asChar(),cs.type);
        }
    }

    /**
     * Executes a choice statement.
     * <p><br>
     *     We will first evaluate the choice value. Then, we will go through
     *     each case statement and find which label the choice value belongs to
     *     in order to determine which case statement to execute. If the value
     *     does not belong to any label, then we will execute the default statement.
     * </p>
     * @param cs Choice Statement
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);

        Value choice = currentValue;

        for(int i = 0; i <= cs.caseStmts().size(); i++) {
            Value label, rightLabel;
            // Default Case Execution
            if(i == cs.caseStmts().size()) {
                cs.otherBlock().visit(this);
                break;
            }

            CaseStmt currCase = cs.caseStmts().get(i);

            currCase.choiceLabel().getLeftConstant().visit(this);
            label = currentValue;

            if(currCase.choiceLabel().getRightConstant() != null) {
                currCase.choiceLabel().getRightConstant().visit(this);
                rightLabel = currentValue;
                if((cs.choiceExpr().type.isInt() && (choice.asInt() >= label.asInt() && choice.asInt() <= rightLabel.asInt()))
                || (cs.choiceExpr().type.isChar() && (choice.asChar() >= label.asChar() && choice.asChar() <= rightLabel.asChar()))) {
                    currCase.visit(this);
                    break;
                }
            } else {
                if(cs.choiceExpr().type.isInt() && (choice.asInt() == label.asInt())
                || cs.choiceExpr().type.isChar() && (choice.asChar() == label.asChar())
                || cs.choiceExpr().type.isString() && (choice.asString().equals(label.asString()))) {
                    currCase.visit(this);
                    break;
                }

            }
        }
    }

    /**
     * Begins the execution of a program in compilation mode.
     * <p><br>
     *     For the time being, we are going to execute programs via
     *     the interpreter if a user is in compilation mode instead of
     *     generating bytecode. When we do this, we want to make sure to
     *     visit every {@code EnumDecl} and {@code GlobalDecl}, so they can
     *     be saved into the runtime stack before visiting the main function.
     * </p>
     * @param c Compilation unit representing the program we are executing
     */
    public void visitCompilationUnit(CompilationUnit c) {
        for(ImportDecl im : c.getImports()) {
            im.visit(this);
            c.addClassDecl(im.getCompilationUnit().getClasses());
            c.addFuncDecl(im.getCompilationUnit().getFunctions());
        }

        for(EnumDecl ed : c.getEnums())
            ed.visit(this);

        for(GlobalDecl gd : c.getGlobals())
            gd.visit(this);

        if(c.getMain() != null)
            c.getMain().getBody().visit(this);
    }

    /**
     * Executes a continue statement.
     * <p><br>
     *     When a continue statement is executed, we will set the {@link #continueFound}
     *     flag to be true. This means we will need to end the current loop iteration early
     *     and move on to the next iteration.
     * @param cs Continue Statement
     */
    public void visitContinueStmt(ContinueStmt cs) { continueFound = true; }

    /**
     * Executes a do while loop.
     * <p><br>
     *     A do while loop requires the loop to execute at least once and
     *     then the loop will continue to be executed until the condition
     *     evaluates to be false. Internally, we will be using a Java do
     *     while loop to help us execute the code.
     * </p>
     * @param ds Do Statement
     */
    public void visitDoStmt(DoStmt ds) {
        do {
            ds.doBlock().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
            ds.condition().visit(this);
        } while(currentValue.asBool());

        breakFound = false;
    }

    /**
     * Executes an enum declaration.
     * <p>
     *     We will evaluate each constant in the enumeration and store
     *     each constant into the runtime stack.
     * </p>
     * @param ed Enum Declaration
     */
    public void visitEnumDecl(EnumDecl ed) {
        for(Var constant : ed.getConstants()) {
            constant.getInitialValue().visit(this);
            stack.addValue(constant, currentValue);
        }
    }

    /**
     * Evaluates a field expression.
     * <p><br>
     *     We will evaluate the target expression first to get the
     *     object we need and then we will evaluates its access expression.
     *     If we need to save any value into the object itself, then we
     *     will make sure to set {@link #currentValue} to be the object.
     * </p>
     * @param fe Field Expression
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.getTarget().visit(this);
        RuntimeObject obj = currentValue.asObject();

        // If the field expression starts with parent, we have to change the object's type
        // to be the parent type in order for us to call the correct method
        if(fe.getTarget().isNameExpr() && fe.getTarget().asNameExpr().isParentKeyword()) {
            Type oldType = obj.getCurrentType();
            obj.setType(fe.getTarget().type.asClassType());
            fe.getAccessExpr().visit(this);
            obj.setType(oldType.asClassType());
        } else
            fe.getAccessExpr().visit(this);

        // If we're executing an assignment statement and a field expression appears on the LHS,
        // we have to make sure we can save the value into the object instead of just getting a field value.
        if(insideAssignment) {
            currentValue = obj;
            insideAssignment = false;
        }
    }

    /**
     * Executes a for statement.
     * <p><br>
     *     Since for loops are static in C Minor, we will evaluate the LHS and
     *     the RHS expressions to determine how many iterations are needed. We
     *     then simply execute the for loop's body until either the for loop
     *     terminates early or once all iterations are completed.
     * </p>
     * @param fs For Statement
     */
    public void visitForStmt(ForStmt fs) {
        String loopOp = fs.loopOp().toString();

        fs.condLHS().visit(this);
        Value LHS = currentValue;

        fs.condRHS().visit(this);
        Value RHS = currentValue;

        // Handles both iterating over Ints and Chars
        switch(loopOp) {
            case "<.." -> LHS = new Value(LHS.asInt() + 1, fs.condLHS().type);
            case "..<" -> RHS = new Value(RHS.asInt() - 1, fs.condRHS().type);
            case "<..<" -> {
                LHS = new Value(LHS.asInt() + 1, fs.condLHS().type);
                RHS = new Value(RHS.asInt() - 1, fs.condRHS().type);
            }
        }

        stack.createCallFrame();
        stack.addValue(fs.loopVar(),LHS);

        for(int i = LHS.asInt(); i <= RHS.asInt(); i++) {
            stack.setValue(fs.loopVar(),new Value(i,fs.condLHS().type));
            fs.forBlock().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
        }

        stack.destroyCallFrame();
        breakFound = false;
    }

    /**
     * Executes a global declaration statement.
     * <p><br>
     *     By executing a global declaration, we will allocate
     *     space on the runtime stack to store a new global value.
     * </p>
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.getInitialValue() != null)
            gd.getInitialValue().visit(this);
        stack.addValue(gd.getVariableName(), currentValue);
    }

    /**
     * Executes an if statement.
     * <p><br>
     *     An if statement is only executed if its condition evaluates to be true.
     *     We will check each condition for every if statement contained in {@link IfStmt}
     *     and if none of them evaluates to be true, then we will simply execute the
     *     else statement (if applicable).
     * </p>
     * @param is If Statement
     */
    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);

        if(currentValue.asBool())
            is.ifBlock().visit(this);
        else {
            if(!is.elifStmts().isEmpty()) {
                for(int i = 0; i < is.elifStmts().size(); i++) {
                    IfStmt elif = is.elifStmts().get(i);
                    elif.condition().visit(this);
                    if(currentValue.asBool()) {
                        elif.ifBlock().visit(this);
                        return;
                    }
                }
            }
            if(is.elseBlock() != null)
                is.elseBlock().visit(this);
        }
    }

    /**
     * Executes a constructor declaration.
     * <p><br>
     *     A constructor declaration is only visited after a {@code visitNewExpr}
     *     call is completed. This visit will initialize the remaining fields that
     *     the user didn't initialize for the newly created object.
     * </p>
     * @param id Init Declaration
     */
    public void visitInitDecl(InitDecl id) {
        RuntimeObject obj = currentValue.asObject();

        for(AssignStmt as : id.getInitStmts()) {
            if(!obj.hasField(as.LHS())) {
                as.RHS().visit(this);
                obj.setField(as.LHS(), currentValue);
            }
        }

        currentValue = obj;
    }

    /**
     * Executes an input statement.
     * <p><br>
     *     In C Minor, the interpreter will handle all runtime errors for the
     *     programmer. This means that if a user incorrectly writes a value that
     *     needs to be stored, we will automatically generate an error and terminate
     *     the program.
     * </p>
     * @param in Input Statement
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

        // ERROR CHECK #1: This checks if the user inputted the expected number of input values.
        if(vals.size() != in.getInExprs().size()) {
            handler.createErrorBuilder(RuntimeError.class)
                    .addLocation(in)
                    .addErrorNumber(MessageNumber.RUNTIME_ERROR_600)
                    .generateError();
        }

        for(int i = 0; i < vals.size(); i++) {
            String currVal = vals.get(i);
            Expression currExpr = in.getInExprs().get(i);
            try {
                if(currExpr.type.isInt())
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(DiscreteType.Discretes.INT)));
                else if(currExpr.type.isReal())
                    stack.setValue(currExpr,new Value(new BigDecimal(currVal),new ScalarType(ScalarType.Scalars.REAL)));
                else if(currExpr.type.isChar())
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(DiscreteType.Discretes.CHAR)));
                else if(currExpr.type.isString())
                    stack.setValue(currExpr,new Value(currVal,new ScalarType(ScalarType.Scalars.STR)));
                else
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(DiscreteType.Discretes.BOOL)));
            } catch(Exception e) {
                // ERROR CHECK #2: Make sure user input matches the type of the input variable
                handler.createErrorBuilder(RuntimeError.class)
                    .addLocation(in)
                    .addErrorNumber(MessageNumber.RUNTIME_ERROR_601)
                    .addErrorArgs(currExpr.type)
                    .generateError();
            }
        }
    }

    /**
     * Executes an import declaration.
     * <p><br>
     *     For an import declaration, we simply want to execute its compilation unit
     *     and add all of its top level declarations into the interpreter.
     * </p>
     * @param im Import Declaration
     */
    public void visitImportDecl(ImportDecl im) { im.getCompilationUnit().visit(this); }

    /**
     * Evaluates an invocation.
     * <p>
     *     An invocation will result in a call to a function or a method. In
     *     either case, we will evaluate all arguments and save each argument
     *     as their respective parameter in the stack. This means we will create
     *     and destroy a call frame manually instead of visiting {@link BlockStmt}
     *     to handle it for us.
     * </p>
     * @param in Invocation
     */
    public void visitInvocation(Invocation in) {
        RuntimeObject obj = null;
        if(currentValue != null && currentValue.isObject())
            obj = currentValue.asObject();
        else if(stack.getValue("this") != null)
            obj = stack.getValue("this").asObject();

        Vector<ParamDecl> params;
        Vector<Value> args = new Vector<>();
        SymbolTable oldScope = currentScope;

        stack = stack.createCallFrame();

        // Evaluates all arguments
        for(Expression arg : in.getArgs()) {
            arg.visit(this);
            args.add(currentValue);
        }

        if(in.isLengthInvocation()) {
            currentValue = new Value(currentValue.asList().size(),in.type);
            return;
        }

        // Function Invocation
        if(in.targetType.isVoidType()) {
            FuncDecl fd = (in.templatedFunction != null)
                    ? in.templatedFunction : currentScope.findName(in.getSignature()).getDecl().asTopLevelDecl().asFuncDecl();
            params = fd.getParams();
            currentScope = fd.getScope();

            // Save arguments into respective parameters and add to the stack.
            for(int i = 0; i < in.getArgs().size(); i++)
                stack.addValue(params.get(i),args.get(i));


            fd.getBody().visit(this);
        }
        // Method Invocation
        else {
            // ERROR CHECK #1: This checks if the object's type is assignment compatible with the expected object type.
            if(!ClassType.classAssignmentCompatibility(obj.getCurrentType(),in.targetType.asClassType())) {
                handler.createErrorBuilder(RuntimeError.class)
                        .addLocation(in.getFullLocation())
                        .addErrorNumber(MessageNumber.RUNTIME_ERROR_604)
                        .addErrorArgs(in.toString(),obj.getCurrentType(),in.targetType)
                        .generateError();
            }

            // Find the class that contains the specific method we want to call
            ClassDecl cd = currentScope.findName(obj.getCurrentType().asClassType()).getDecl().asTopLevelDecl().asClassDecl();
            while(!cd.getScope().hasName(in.getSignature()))
                cd = currentScope.findName(cd.getSuperClass()).getDecl().asTopLevelDecl().asClassDecl();

            MethodDecl md = cd.getScope().findName(in.getSignature()).getDecl().asClassNode().asMethodDecl();
            params = md.getParams();
            currentScope = md.getScope();

            // Create a 'this' pointer when we invoke an object's method for the first time
            stack.addValue("this",obj);

            // Save arguments into respective parameters and add to the stack.
            for(int i = 0; i < in.getArgs().size(); i++)
                stack.addValue(params.get(i),args.get(i));

            md.getBody().visit(this);
        }

        // Figure out which variables need to be updated.
        HashMap<String,Value> varsToUpdate = new HashMap<>();
        for(int i = 0; i < in.getArgs().size(); i++) {
            if(in.getArgs().get(i).isNameExpr()) {
                String argName = in.getArgs().get(i).toString();
                ParamDecl currParam = params.get(i);

                if(currParam.mod.isOutMode() || currParam.mod.isInOutMode() || currParam.mod.isRefMode())
                    varsToUpdate.put(argName,stack.getValue(currParam));
            }
        }

        currentScope = oldScope;
        returnFound = false;
        stack = stack.destroyCallFrame();

        // Update variables with new values before finishing visit.
        for(String varName : varsToUpdate.keySet())
            stack.setValue(varName,varsToUpdate.get(varName));
    }

    /**
     * Evaluates a list literal.
     * <p><br>
     *     Lists are dynamic in C Minor, so a user may change the size of
     *     the list during the runtime execution of their program. Similarly
     *     to {@link #visitArrayLiteral(ArrayLiteral)}, we will evaluate every
     *     expression in the current list literal and store it into a
     *     {@link RuntimeList} to emulate a list in memory during runtime.
     * </p>
     * @param ll List Literal
     */
    public void visitListLiteral(ListLiteral ll) {
        RuntimeList lst = new RuntimeList(ll);

        for(Expression e : ll.getInits()) {
            e.visit(this);
            lst.addElement(currentValue);
        }

        currentValue = lst;
    }

    /**
     * Executes a list statement command.
     * <p><br>
     *     This method will execute the current list command based on the
     *     provided arguments. If there are any issues, then we will produce
     *     an exception for the user.
     * </p>
     * @param ls The current list statement we will be executing.
     */
    public void visitListStmt(ListStmt ls) {
        if(ls.getInvocation() != null) {
            ls.getInvocation().visit(this);
            return;
        }

        // Obtain list from the stack
        ls.getList().visit(this);
        RuntimeList lst = currentValue.asList();

        ls.getAllArgs().get(1).visit(this);
        switch(ls.getCommand()) {
            case APPEND:
                lst.add(currentValue);
                break;
            case INSERT:
                Value index = currentValue;

                // ERROR CHECK #1: This makes sure the passed index is in the list's memory range.
                if(index.asInt() < 1 || index.asInt() > lst.size()) {
                    handler.createErrorBuilder(RuntimeError.class)
                        .addLocation(ls)
                        .addErrorNumber(MessageNumber.RUNTIME_ERROR_605)
                        .addErrorArgs(ls.getList(),lst.size(),index.asInt())
                        .generateError();
                }

                ls.getAllArgs().get(2).visit(this);
                lst.insertElement(index.asInt(),currentValue);
                break;
            case REMOVE:
                boolean successfulRemoval = true;

                // ERROR CHECK #2: This also makes sure the passed index is in the list's memory range
                if(currentValue.getType().isInt()) {
                    if(currentValue.asInt() < 1 || currentValue.asInt() > lst.size()) {
                        handler.createErrorBuilder(RuntimeError.class)
                            .addLocation(ls)
                            .addErrorNumber(MessageNumber.RUNTIME_ERROR_608)
                            .generateError();
                    }

                    lst.remove(currentValue.asInt());
                }
                else
                    successfulRemoval = lst.remove(currentValue);

                // ERROR CHECK #3: This will throw an exception to the user if an element couldn't be removed.
                if(!successfulRemoval) {
                    handler.createErrorBuilder(RuntimeError.class)
                        .addLocation(ls)
                        .addErrorNumber(MessageNumber.RUNTIME_ERROR_609)
                        .addErrorArgs(ls.getAllArgs().get(1),ls.getList())
                        .generateError();
                }

        }
    }

    /**
     * Evaluates a literal.
     * <p><br>
     *     We create a new value to represent the literal
     *     and store the result into {@link #currentValue}.
     * </p>
     * @param li Literal
     */
    public void visitLiteral(Literal li) { currentValue = new Value(li); }

    /**
     * Executes a local declaration statement.
     * <p><br>
     *     By executing a local declaration, we will allocate
     *     space on the runtime stack to store a new local value.
     * </p>
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        if(ld.hasInitialValue())
            ld.getInitialValue().visit(this);
        stack.addValue(ld.getVariableName(), currentValue);
    }

    /**
     * Evaluates a name expression.
     * <p><br>
     *     Any time the interpreter encounters a name, it will access
     *     the value the name refers to in the runtime stack. If we have
     *     an object, then we want to access the name within the object
     *     unless it doesn't exist.
     * </p>
     * @param ne Name Expression
     */
    public void visitNameExpr(NameExpr ne) {
        // Ignore any names that refer to 'parent' keyword.
        if(ne.isParentKeyword())
            return;

        // Special Case: If we are evaluating a complex field expression, execute this branch
        if(currentValue != null) {
//            && currentValue.isObject() && ne.getParent().isExpression()
//            && (ne.getParent().asExpression().isFieldExpr() || ne.getParent().asExpression().isArrayExpr())) {
//            // ERROR CHECK #1: This checks if the field exists for the current object.
//            if(!currentValue.asObject().hasField(ne)) {
//                handler.createErrorBuilder(RuntimeError.class)
//                    .addLocation(ne.getRootParent())
//                    .addErrorNumber(MessageNumber.RUNTIME_ERROR_606)
//                    .addErrorArgs(ne, currentValue.asObject().getCurrentType())
//                    .generateError();
//            }
            currentValue = currentValue.asObject().getField(ne);
        }
        else {
            currentValue = stack.getValue(ne);
            // ERROR CHECK #2: This makes sure any uninitialized objects are not accessed by the user.
            if(currentValue == null && !insideAssignment) {
                handler.createErrorBuilder(RuntimeError.class)
                    .addLocation(ne.getFullLocation())
                    .addErrorNumber(MessageNumber.RUNTIME_ERROR_607)
                    .addErrorArgs(ne)
                    .generateError();
            }
        }
    }

    /**
     * Evaluates a new expression.
     * <p><br>
     *     We will create a {@link RuntimeObject} whenever we visit a {@link NewExpr}.
     *     During this visit, we will initialize the fields the user explicitly initialized
     *     for the object before we visit an {@link InitDecl} to handle the initialization
     *     of the rest of the fields.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        RuntimeObject obj = new RuntimeObject(ne.type);
        ClassDecl cd = currentScope.findName(ne.type.asClassType().getClassNameAsString()).getDecl().asTopLevelDecl().asClassDecl();

        for(Var field : ne.getInitialFields()) {
            field.getInitialValue().visit(this);
            obj.setField(field, currentValue);
        }

        currentValue = obj;
        cd.getConstructor().visit(this);
    }

    /**
     * Executes an output statement.
     * <p><br>
     *     We will visit every expression  in the current output
     *     statement and print each value to the terminal.
     * </p>
     * @param os Output Statement
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.getOutExprs()) {
            e.visit(this);
            if(e.isEndl()) {
                System.out.println();
                output = false;
            }
            else if(currentValue.isList()) {
                StringBuilder sb = new StringBuilder();
                RuntimeList.buildList(currentValue.asList(),sb);
                System.out.print(sb);
                output = true;
            }
            else {
                System.out.print(currentValue);
                output = true;
            }
        }
    }

    /**
     * Executes a return statement.
     * <p><br>
     *     When we encounter a return statement, we will set the
     *     {@link #returnFound} flag to be true, so we know to stop
     *     the current function or method execution. Additionally, we
     *     will evaluate the value that needs to be returned (if applicable).
     * </p>
     * @param rs Return Statement
     */
    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.expr() != null)
            rs.expr().visit(this);
        returnFound = true;
    }

    /**
     * Executes a retype statement.
     * <p><br>
     *     By executing a retype statement, we are creating a new instance
     *     of the object and saving the object into the stack.
     * </p>
     * @param rt Retype Statement
     */
    public void visitRetypeStmt(RetypeStmt rt) {
        rt.getNewObject().visit(this);
        stack.setValue(rt.getName(), currentValue);
        currentValue = null;
    }

    /**
     * Executes a stop statement.
     * <p><br>
     *     If a stop statement is written by the user, we
     *     will simply terminate the C Minor interpreter.
     * </p>
     * @param ss Stop Statement
     */
    public void visitStopStmt(StopStmt ss) { System.exit(1); }

    /**
     * Executes a {@link ThisStmt}.
     * <p><br>
     *     When we are executing code related to an object, we want to make sure we
     *     are accessing the correct fields and methods for the current {@link RuntimeObject}.
     *     This will be done by internally keeping track of a "this" pointer whenever
     *     we are inside of a class.
     * </p>
     * @param ts ThisStmt
     */
    public void visitThis(ThisStmt ts) { currentValue = stack.getValue("this"); }

    /**
     * Evaluates a unary expression.
     * <p><br>
     *     We will evaluate the unary expression and save its value.
     * </p>
     * @param ue Unary Expression
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.getExpr().visit(this);
        
        switch(ue.getUnaryOp().toString()) {
            case "~":
                currentValue = ue.getExpr().type.isInt() ? new Value(~(currentValue.asInt()),ue.type) : new Value(~currentValue.asChar(),ue.type);
                break;
            case "not":
                currentValue = new Value(!currentValue.asBool(),ue.type);
                break;
        }
    }

    /**
     * Executes a while loop.
     * <p><br>
     *     A while loop will be executed as long as its condition remains true.
     *     We will be using a Java while loop internally to help us execute the code.
     * </p>
     * @param ws While Statement
     */
    public void visitWhileStmt(WhileStmt ws) {
        ws.condition().visit(this);

        while(currentValue.asBool()) {
            ws.whileBlock().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
            ws.condition().visit(this);
        }
        breakFound = false;
    }
}
