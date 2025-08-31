package cminor.interpreter;

import cminor.ast.classbody.InitDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.ParamDecl;
import cminor.ast.misc.Var;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.ast.types.ClassType;
import cminor.ast.types.DiscreteType;
import cminor.ast.types.ScalarType;
import cminor.ast.types.Type;
import cminor.interpreter.value.RuntimeList;
import cminor.interpreter.value.RuntimeObject;
import cminor.interpreter.value.Value;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.runtime.RuntimeError;
import cminor.utilities.RuntimeStack;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;
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
     * Flag set when a {@code return} statement is found.
     */
    private boolean returnFound;

    /**
     * Creates the interpreter for the {@link VM}.
     * @param globalScope The {@link SymbolTable} representing the VM's global scope.
     */
    public Interpreter(SymbolTable globalScope) {
        this.stack = new RuntimeStack();
        this.currentValue = null;
        this.currentScope = globalScope;
        this.breakFound = false;
        this.continueFound = false;
        this.insideAssignment = false;
        this.returnFound = false;
        this.handler = new MessageHandler();
    }

    /**
     * Evaluates an array expression.
     * <p>
     *     We will retrieve the array from the stack, and we will access whatever
     *     value is stored at the position specified. In C Minor, arrays and lists
     *     are indexed starting at 1, not 0, so we will factor that into our runtime
     *     error checking.
     * </p>
     * @param ae {@link ArrayExpr}
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
                    currentValue = new Value(lst.size(),new DiscreteType(ScalarType.Scalars.INT));
            } else
                currentValue = new Value(lst.size(),new DiscreteType(ScalarType.Scalars.INT));

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
     * <p>
     *     Arrays are static in C Minor, so a user can not change its size
     *     once an array literal is declared. We will evaluate every expression
     *     for the current array literal and store it in a {@link RuntimeList}
     *     This will emulate an array in memory during runtime.
     * </p>
     * @param al {@link ArrayLiteral}
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
     * <p>
     *     We will evaluate the LHS and RHS of the {@link AssignStmt}, so we
     *     can calculate the new value we need to assign. We will then store
     *     the resulting value into the stack.
     * </p>
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        String assignOp = as.getOperator().toString();

        insideAssignment = true;
        as.getLHS().visit(this);
        Value oldValue = currentValue;
        insideAssignment = false;

        as.getRHS().visit(this);
        Value newValue = currentValue;

        switch(assignOp) {
            case "=":
                break;
            default:
                if(as.getRHS().type.isInt()) {
                    switch (assignOp) {
                        case "+=" -> newValue = new Value(oldValue.asInt() + newValue.asInt(), newValue.getType());
                        case "-=" -> newValue = new Value(oldValue.asInt() - newValue.asInt(), newValue.getType());
                        case "*=" -> newValue = new Value(oldValue.asInt() * newValue.asInt(), newValue.getType());
                        case "/=" -> newValue = new Value(oldValue.asInt() / newValue.asInt(), newValue.getType());
                        case "%=" -> newValue = new Value(oldValue.asInt() % newValue.asInt(), newValue.getType());
                        case "**=" -> newValue = new Value(Math.round(Math.pow(oldValue.asInt(), newValue.asInt())), newValue.getType());
                    }
                } else if(as.getRHS().type.isReal()) {
                    switch(assignOp) {
                        case "+=" -> newValue = new Value(oldValue.asReal().add(newValue.asReal()), newValue.getType());
                        case "-=" -> newValue = new Value(oldValue.asReal().subtract(newValue.asReal()), newValue.getType());
                        case "*=" -> newValue = new Value(oldValue.asReal().multiply(newValue.asReal()), newValue.getType());
                        case "/=" -> newValue = new Value(oldValue.asReal().divide(newValue.asReal(), MathContext.DECIMAL128), newValue.getType());
                        case "%=" -> newValue = new Value(oldValue.asReal().remainder(newValue.asReal()), newValue.getType());
                        case "**=" -> newValue = new Value(oldValue.asReal().pow(newValue.asReal().toBigInteger().intValue(),MathContext.DECIMAL128), newValue.getType());
                    }
                } else if(as.getRHS().type.isString())
                    newValue = new Value(oldValue.asString() + newValue.asString(), newValue.getType());
        }

        if(as.getLHS().isNameExpr())
            stack.setValue(as.getLHS(), newValue);
        else if(as.getLHS().isFieldExpr()) {
            insideAssignment = true;
            as.getLHS().visit(this);
            currentValue.asObject().setField(as.getLHS().asFieldExpr().getFieldName(),newValue);
            insideAssignment = false;
        }
        else if(as.getLHS().isArrayExpr()) {
            insideAssignment = true;
            as.getLHS().visit(this);
            currentValue.asList().addElement(newValue);
            insideAssignment = false;
        }
    }

    /**
     * Evaluates a binary expression.
     * <p>
     *     We first need to evaluate the values of the LHS and RHS of the current
     *     binary expression. Then, we will perform the correct binary operation
     *     based on the type the binary expression evaluates to.
     * </p>
     * @param be {@link BinaryExpr}
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
                else if(be.getRHS().type.isChar()) {
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
                ClassType classType = be.getRHS().type.asClass();
                switch(binOp) {
                    case "instanceof" -> currentValue = new Value(ClassType.temporaryName(currentScope.getGlobalScope(), objType,classType),be.type);
                    case "!instanceof" -> currentValue = new Value(!ClassType.temporaryName(currentScope.getGlobalScope(), objType,classType),be.type);
                }
                break;
        }
    }

    /**
     * Executes a block statement.
     * <p>
     *     We will create a new call frame on the {@link #stack} every time
     *     we visit a block statement. We then individually visit each statement
     *     found in the block statement. First we execute all local declaration
     *     statements followed by the remaining statements in the block. Once each
     *     statement has been visited (or if we encounter a statement that requires
     *     us to terminate the execution of the block), we will destroy the call frame.
     * </p>
     * @param bs {@link BlockStmt}
     */
    public void visitBlockStmt(BlockStmt bs) {
        stack = stack.createCallFrame();

        for(LocalDecl decl : bs.getLocalDecls())
            decl.visit(this);

        for(Statement s : bs.getStatements()) {
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
     * <p>
     *     When a break statement is executed, we will set the {@link #breakFound}
     *     flag to be true. This means we will need to terminate the current loop
     *     statement that is executing.
     * @param bs {@link BreakStmt}
     */
    public void visitBreakStmt(BreakStmt bs) { breakFound = true; }

    /**
     * Evaluates a cast expression.
     * <p>
     *     We will evaluate the cast expression's value and
     *     typecast it to the appropriate type.
     * </p>
     * @param cs {@link CastExpr}
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
     * <p>
     *     We will first evaluate the choice value. Then, we will go through
     *     each case statement and find which label the choice value belongs to
     *     in order to determine which case statement to execute. If the value
     *     does not belong to any label, then we will execute the default statement.
     * </p>
     * @param cs {@link ChoiceStmt}
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.getChoiceValue().visit(this);

        Value choice = currentValue;

        for(int i = 0; i <= cs.getCases().size(); i++) {
            Value label, rightLabel;
            // Default Case Execution
            if(i == cs.getCases().size()) {
                cs.getDefaultBody().visit(this);
                break;
            }

            CaseStmt currCase = cs.getCases().get(i);

            currCase.getLabel().getLeftConstant().visit(this);
            label = currentValue;

            if(currCase.getLabel().getRightConstant() != null) {
                currCase.getLabel().getRightConstant().visit(this);
                rightLabel = currentValue;
                if((cs.getChoiceValue().type.isInt()
                        && (choice.asInt() >= label.asInt() && choice.asInt() <= rightLabel.asInt()))
                || (cs.getChoiceValue().type.isChar()
                        && (choice.asChar() >= label.asChar() && choice.asChar() <= rightLabel.asChar()))) {
                    currCase.visit(this);
                    break;
                }
            } else {
                if(cs.getChoiceValue().type.isInt() && (choice.asInt() == label.asInt())
                || cs.getChoiceValue().type.isChar() && (choice.asChar() == label.asChar())
                || cs.getChoiceValue().type.isString() && (choice.asString().equals(label.asString()))) {
                    currCase.visit(this);
                    break;
                }

            }
        }
    }

    public void visitClassDecl(ClassDecl cd) { /* Do Nothing. */  }

    /**
     * Begins the execution of a program in compilation mode.
     * <p>
     *     For the time being, we are going to execute programs via
     *     the interpreter if a user is in compilation mode instead of
     *     generating bytecode. When we do this, we want to make sure to
     *     visit every {@code EnumDecl} and {@code GlobalDecl}, so they can
     *     be saved into the runtime stack before visiting the main function.
     * </p>
     * @param c {@link CompilationUnit}
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
     * <p>
     *     When a continue statement is executed, we will set the {@link #continueFound}
     *     flag to be true. This means we will need to end the current loop iteration early
     *     and move on to the next iteration.
     * @param cs {@link ContinueStmt}
     */
    public void visitContinueStmt(ContinueStmt cs) { continueFound = true; }

    /**
     * Executes a do while loop.
     * <p>
     *     A do while loop requires the loop to execute at least once and
     *     then the loop will continue to be executed until the condition
     *     evaluates to be false. Internally, we will be using a Java do
     *     while loop to help us execute the code.
     * </p>
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        do {
            ds.getBody().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
            ds.getCondition().visit(this);
        } while(currentValue.asBool());

        breakFound = false;
    }

    /**
     * Executes an enum declaration.
     * <p>
     *     We will evaluate each constant in the enumeration and store
     *     each constant into the runtime stack.
     * </p>
     * @param ed {@link EnumDecl}
     */
    public void visitEnumDecl(EnumDecl ed) {
        for(Var constant : ed.getConstants()) {
            constant.getInitialValue().visit(this);
            stack.addValue(constant, currentValue);
        }
    }

    /**
     * Evaluates a field expression.
     * <p>
     *     We will evaluate the target expression first to get the
     *     object we need, and we will evaluate its access expression.
     *     If we need to save any value into the object itself, then we
     *     will make sure to set {@link #currentValue} to be the object.
     * </p>
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) {
        fe.getTarget().visit(this);
        RuntimeObject obj = currentValue.asObject();

        // If the field expression starts with parent, we have to change the object's type
        // to be the parent type in order for us to call the correct method
        if(fe.getTarget().isParentStmt()) {
            Type oldType = obj.getCurrentType();
            obj.setType(fe.getTarget().type.asClass());
            fe.getAccessExpr().visit(this);
            obj.setType(oldType.asClass());
        } else if(fe.getAccessExpr().isNameExpr())
            currentValue = obj.getField(fe.getAccessExpr());
        else
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
     * <p>
     *     Since for loops are static in C Minor, we will evaluate the LHS and
     *     the RHS expressions to determine how many iterations are needed. We
     *     then simply execute the for loop's body until either the for loop
     *     terminates early or once all iterations are completed.
     * </p>
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        String loopOp = fs.getLoopOperator().toString();

        fs.getStartValue().visit(this);
        Value LHS = currentValue;

        fs.getEndValue().visit(this);
        Value RHS = currentValue;

        // Handles both iterating over Ints and Chars
        switch(loopOp) {
            case "<.." -> LHS = new Value(LHS.asInt() + 1, fs.getStartValue().type);
            case "..<" -> RHS = new Value(RHS.asInt() - 1, fs.getEndValue().type);
            case "<..<" -> {
                LHS = new Value(LHS.asInt() + 1, fs.getStartValue().type);
                RHS = new Value(RHS.asInt() - 1, fs.getEndValue().type);
            }
        }

        stack.createCallFrame();
        stack.addValue(fs.getControlVariable(),LHS);

        for(int i = LHS.asInt(); i <= RHS.asInt(); i++) {
            stack.setValue(fs.getControlVariable(),new Value(i,fs.getStartValue().type));
            fs.getBody().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
        }

        stack.destroyCallFrame();
        breakFound = false;
    }

    public void visitFuncDecl(FuncDecl fd) {  /* Do nothing. */ }

    /**
     * Executes a global declaration statement.
     * <p>
     *     By executing a global declaration, we will allocate
     *     space on the runtime stack to store a new global value.
     * </p>
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.getInitialValue() != null)
            gd.getInitialValue().visit(this);
        stack.addValue(gd.getVariableName(), currentValue);
    }

    /**
     * Executes an if statement.
     * <p>
     *     An if statement is only executed if its condition evaluates to be true.
     *     We will check each condition for every if statement contained in {@link IfStmt}
     *     and if none of them evaluates to be true, then we will simply execute the
     *     else statement (if applicable).
     * </p>
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        is.getCondition().visit(this);

        if(currentValue.asBool())
            is.getIfBody().visit(this);
        else {
            if(!is.getElifs().isEmpty()) {
                for(int i = 0; i < is.getElifs().size(); i++) {
                    IfStmt elif = is.getElifs().get(i);
                    elif.getCondition().visit(this);
                    if(currentValue.asBool()) {
                        elif.getIfBody().visit(this);
                        return;
                    }
                }
            }
            if(is.getElseBody() != null)
                is.getElseBody().visit(this);
        }
    }

    /**
     * Executes a constructor declaration.
     * <p>
     *     A constructor declaration is only visited after a {@code visitNewExpr}
     *     call is completed. This visit will initialize the remaining fields that
     *     the user didn't initialize for the newly created object.
     * </p>
     * @param id {@link InitDecl}
     */
    public void visitInitDecl(InitDecl id) {
        RuntimeObject obj = currentValue.asObject();

        for(AssignStmt as : id.getInitStmts()) {
            if(!obj.hasField(as.getLHS().asFieldExpr().getAccessExpr())) {
                if(as.getRHS() == null)
                    currentValue = null;
                else
                    as.getRHS().visit(this);
                obj.setField(as.getLHS(), currentValue);
            }
        }

        currentValue = obj;
    }

    /**
     * Executes an input statement.
     * <p>
     *     In C Minor, the interpreter will handle all runtime errors for the
     *     programmer. This means that if a user incorrectly writes a value that
     *     needs to be stored, we will automatically generate an error and terminate
     *     the program.
     * </p>
     * @param in {@link InStmt}
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
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(ScalarType.Scalars.INT)));
                else if(currExpr.type.isReal())
                    stack.setValue(currExpr,new Value(new BigDecimal(currVal),new ScalarType(ScalarType.Scalars.REAL)));
                else if(currExpr.type.isChar())
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(ScalarType.Scalars.CHAR)));
                else if(currExpr.type.isString())
                    stack.setValue(currExpr,new Value(currVal,new ScalarType(ScalarType.Scalars.STR)));
                else
                    stack.setValue(currExpr,new Value(currVal,new DiscreteType(ScalarType.Scalars.BOOL)));
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
     * <p>
     *     For an import declaration, we simply want to execute its compilation unit
     *     and add all of its top level declarations into the interpreter.
     * </p>
     * @param im {@link ImportDecl}
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
     * @param in {@link Invocation}
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
        if(!in.isMethodInvocation()) {
            FuncDecl fd = (in.templatedFunction != null)
                    ? in.templatedFunction : currentScope.findMethod(in.getName().toString(),in.getSignature()).asTopLevelDecl().asFuncDecl();
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
//            if(!ClassType.classAssignmentCompatibility(obj.getCurrentType(),in.targetType.asClass())) {
//                handler.createErrorBuilder(RuntimeError.class)
//                        .addLocation(in.getFullLocation())
//                        .addErrorNumber(MessageNumber.RUNTIME_ERROR_604)
//                        .addErrorArgs(in.toString(),obj.getCurrentType(),in.targetType)
//                        .generateError();
//            }

            // Find the class that contains the specific method we want to call
            ClassDecl cd = currentScope.findName(obj.getCurrentType().asClass()).asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.getScope().findMethod(in.getName().toString(), in.getSignature()).asClassNode().asMethodDecl();
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
     * @param ll {@link ListLiteral}
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
     * <p>
     *     This method will execute the current list command based on the
     *     provided arguments. If there are any issues, then we will produce
     *     an exception for the user.
     * </p>
     * @param ls {@link ListStmt}
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
     * <p>
     *     We create a new value to represent the literal
     *     and store the result into {@link #currentValue}.
     * </p>
     * @param li {@link Literal}
     */
    public void visitLiteral(Literal li) { currentValue = new Value(li); }

    /**
     * Executes a local declaration statement.
     * <p>
     *     By executing a local declaration, we will allocate
     *     space on the runtime stack to store a new local value.
     * </p>
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        if(ld.getInitialValue() != null)
            ld.getInitialValue().visit(this);
        stack.addValue(ld.getVariableName(), currentValue);
    }

    /**
     * Evaluates a name expression.
     * <p>
     *     Any time the interpreter encounters a name, it will access
     *     the value the name refers to in the runtime stack. If we have
     *     an object, then we want to access the name within the object
     *     unless it doesn't exist.
     * </p>
     * @param ne {@link NameExpr}
     */
    public void visitNameExpr(NameExpr ne) {
        // Special Case: If we are evaluating a complex field expression, execute this branch
        if(currentValue != null && currentValue.isObject() && ne.inComplexFieldExpr()) {
            // ERROR CHECK #1: This checks if the field exists for the current object.
//            if(!currentValue.asObject().hasField(ne)) {
//                handler.createErrorBuilder(RuntimeError.class)
//                    .addLocation(ne.getFullLocation())
//                    .addErrorNumber(MessageNumber.RUNTIME_ERROR_606)
//                    .addErrorArgs(ne, currentValue.asObject().getCurrentType())
//                    .generateError();
//            }
            currentValue = currentValue.asObject().getField(ne);
        }
//        else if(currentValue != null && currentValue.isObject() && ne.inFieldExpr())
//            currentValue = currentValue.asObject().getField(ne);
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
     * <p>
     *     We will create a {@link RuntimeObject} whenever we visit a {@link NewExpr}.
     *     During this visit, we will initialize the fields the user explicitly initialized
     *     for the object before we visit an {@link InitDecl} to handle the initialization
     *     of the rest of the fields.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        RuntimeObject obj = new RuntimeObject(ne.type);
        ClassDecl cd = currentScope.findName(ne.getClassName()).asTopLevelDecl().asClassDecl();

        for(Var field : ne.getInitialFields()) {
            field.getInitialValue().visit(this);
            obj.setField(field, currentValue);
        }

        currentValue = obj;
        cd.getConstructor().visit(this);
    }

    /**
     * Executes an output statement.
     * <p>
     *     We will visit every expression  in the current output
     *     statement and print each value to the terminal.
     * </p>
     * @param os {@link OutStmt}
     */
    public void visitOutStmt(OutStmt os) {
        for(Expression e : os.getOutExprs()) {
            e.visit(this);
            if(e.isEndl())
                System.out.println();
            else if(currentValue.isList()) {
                StringBuilder sb = new StringBuilder();
                RuntimeList.buildList(currentValue.asList(),sb);
                System.out.print(sb);
            }
            else
                System.out.print(currentValue);
        }
    }

    /**
     * Executes a return statement.
     * <p>
     *     When we encounter a return statement, we will set the
     *     {@link #returnFound} flag to be true, so we know to stop
     *     the current function or method execution. Additionally, we
     *     will evaluate the value that needs to be returned (if applicable).
     * </p>
     * @param rs {@link ReturnStmt}

     */
    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.getReturnValue() != null)
            rs.getReturnValue().visit(this);
        returnFound = true;
    }

    /**
     * Executes a retype statement.
     * <p>
     *     By executing a retype statement, we are creating a new instance
     *     of the object and saving the object into the stack.
     * </p>
     * @param rt {@link RetypeStmt}

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
     * <p>
     *     When we are executing code related to an object, we want to make sure we
     *     are accessing the correct fields and methods for the current {@link RuntimeObject}.
     *     This will be done by internally keeping track of a "this" pointer whenever
     *     we are inside a class.
     * </p>
     * @param ts {@link ThisStmt}
     */
    public void visitThis(ThisStmt ts) { currentValue = stack.getValue("this"); }

    /**
     * Evaluates a unary expression.
     * <p>
     *     We will evaluate the unary expression and save its value.
     * </p>
     * @param ue {@link UnaryExpr}
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        ue.getExpr().visit(this);

        switch(ue.getUnaryOp().toString()) {
            case "~":
                currentValue = ue.getExpr().type.isInt() ?
                        new Value(~(currentValue.asInt()),ue.type) : new Value(~currentValue.asChar(),ue.type);
                break;
            case "not":
                currentValue = new Value(!currentValue.asBool(),ue.type);
                break;
        }
    }

    /**
     * Executes a while loop.
     * <p>
     *     A while loop will be executed as long as its condition remains true.
     *     We will be using a Java while loop internally to help us execute the code.
     * </p>
     * @param ws {@link WhileStmt}

     */
    public void visitWhileStmt(WhileStmt ws) {
        ws.getCondition().visit(this);

        while(currentValue.asBool()) {
            ws.getBody().visit(this);

            if(breakFound || returnFound)
                break;

            continueFound = false;
            ws.getCondition().visit(this);
        }
        breakFound = false;
    }
}
