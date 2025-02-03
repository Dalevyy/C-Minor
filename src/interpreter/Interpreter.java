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

public class Interpreter extends Visitor {

    private SymbolTable stack;
    private SymbolTable currentScope;
    private Expression currExpr;
    private boolean insideLoop;
    private boolean returnFound;

    public Interpreter() {
        stack = new SymbolTable();
        insideLoop = false;
        returnFound = false;
    }

    public void visitArrayExpr(ArrayExpr ae) {
        ArrayList<Object> arr = (ArrayList<Object>) stack.getValueInRuntimeStack(ae.arrayTarget().toString());
        ae.setValue(arr.get((int)ae.arrayIndex().getValue(stack)));
        currExpr = ae;
    }

    public void visitArrayLiteral(ArrayLiteral al) {
        ArrayList<Object> arr = new ArrayList<Object>();
        for(int i = 0; i < al.arrayExprs().size(); i++) {
            al.arrayExprs().get(i).visit(this);
            arr.add(currExpr.getValue(stack));
        }
        currExpr = al;
    }

    public void visitAssignStmt(AssignStmt as) {
        as.LHS().visit(this);
        String assignID = currExpr.toString();

        as.RHS().visit(this);
        Object newValue = currExpr.getValue(stack);

        String aOp = as.assignOp().toString();

        // TODO: Operator Overloads as well

        switch(aOp) {
            case "=": {
                stack.setValueInRuntimeStack(assignID, newValue);
                break;
            }
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "**=": {
                if(currExpr.type.isInt()) {
                    int oldVal = (int) stack.getValueInRuntimeStack(assignID);
                    int val = (int) newValue;
                    switch(aOp) {
                        case "+=" -> stack.setValueInRuntimeStack(assignID,oldVal+val);
                        case "-=" -> stack.setValueInRuntimeStack(assignID,oldVal-val);
                        case "*=" -> stack.setValueInRuntimeStack(assignID,oldVal*val);
                        case "/=" -> stack.setValueInRuntimeStack(assignID,oldVal/val);
                        case "%=" -> stack.setValueInRuntimeStack(assignID,oldVal%val);
                        case "**=" -> stack.setValueInRuntimeStack(assignID,(int)Math.pow(oldVal,val));
                    }
                    break;
                }
                else if(currExpr.type.isReal()) {
                    double oldVal = (double) stack.getValueInRuntimeStack(assignID);
                    double val = (double) newValue;
                    switch(aOp) {
                        case "+=" -> stack.setValueInRuntimeStack(assignID,oldVal+val);
                        case "-=" -> stack.setValueInRuntimeStack(assignID,oldVal-val);
                        case "*=" -> stack.setValueInRuntimeStack(assignID,oldVal*val);
                        case "/=" -> stack.setValueInRuntimeStack(assignID,oldVal/val);
                        case "%=" -> stack.setValueInRuntimeStack(assignID,oldVal%val);
                        case "**=" -> stack.setValueInRuntimeStack(assignID,Math.pow(oldVal,val));
                    }
                    break;
                }
                else if(currExpr.type.isString()) {
                    String oldVal = (String) stack.getValueInRuntimeStack(assignID);
                    String val = (String) newValue;
                    stack.setValueInRuntimeStack(assignID,oldVal+val);
                    break;
                }
            }
        }
    }

    public void visitBinaryExpr(BinaryExpr be) {
        be.LHS().visit(this);
        Expression LHS = currExpr;

        be.RHS().visit(this);
        Expression RHS = currExpr;

        String binOp = be.binaryOp().toString();

        currExpr = be;

        // TODO: Check for operator overloads here

        switch(binOp) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "**": {
                if(be.type.isInt()) {
                    int lValue = (int) LHS.getValue(stack);
                    int rValue = (int) RHS.getValue(stack);
                    switch(binOp) {
                        case "+" -> currExpr.setValue(lValue + rValue);
                        case "-" -> currExpr.setValue(lValue - rValue);
                        case "*" -> currExpr.setValue(lValue * rValue);
                        case "/" -> currExpr.setValue(lValue / rValue);
                        case "%" -> currExpr.setValue(lValue % rValue);
                        case "**" -> currExpr.setValue((int)Math.pow(lValue,rValue));
                    }
                    break;
                }
                else if(be.type.isReal()) {
                    double lValue = (double) LHS.getValue(stack);
                    double rValue = (double) RHS.getValue(stack);
                    switch(binOp) {
                        case "+" -> currExpr.setValue(lValue + rValue);
                        case "-" -> currExpr.setValue(lValue - rValue);
                        case "*" -> currExpr.setValue(lValue * rValue);
                        case "/" -> currExpr.setValue(lValue / rValue);
                        case "%" -> currExpr.setValue(lValue % rValue);
                        case "**" -> currExpr.setValue(Math.pow(lValue,rValue));
                    }
                    break;
                }
                else if(be.type.isString()) {
                    String lValue = (String) LHS.getValue(stack);
                    String rValue = (String) RHS.getValue(stack);
                    currExpr.setValue(lValue+rValue);
                    break;
                }
            }
            case "==":
            case "!=": {
                if(LHS.type.isBool() && RHS.type.isBool()) {
                    boolean lValue = (boolean) LHS.getValue(stack);
                    boolean rValue = (boolean) RHS.getValue(stack);
                    switch(binOp) {
                        case "==" -> currExpr.setValue(lValue == rValue);
                        case "!=" -> currExpr.setValue(lValue != rValue);
                    }
                    break;
                }
                else if(LHS.type.isString() && RHS.type.isString()) {
                    String lValue = (String) LHS.getValue(stack);
                    String rValue = (String) RHS.getValue(stack);
                    switch(binOp) {
                        case "==" -> currExpr.setValue(lValue.equals(rValue));
                        case "!=" -> currExpr.setValue(!lValue.equals(rValue));
                    }
                    break;
                }
            }
            case "<":
            case "<=":
            case ">":
            case ">=": {
                if(LHS.type.isInt() && RHS.type.isInt()) {
                    int lValue = (int) LHS.getValue(stack);
                    int rValue = (int) RHS.getValue(stack);
                    switch (binOp) {
                        case "<" -> currExpr.setValue(lValue < rValue);
                        case "<=" -> currExpr.setValue(lValue <= rValue);
                        case ">" -> currExpr.setValue(lValue > rValue);
                        case ">=" -> currExpr.setValue(lValue >= rValue);
                    }
                    break;
                }
                else if(LHS.type.isReal() && RHS.type.isReal()) {
                    double lValue = (double) LHS.getValue(stack);
                    double rValue = (double) RHS.getValue(stack);
                    switch (binOp) {
                        case "<" -> currExpr.setValue(lValue < rValue);
                        case "<=" -> currExpr.setValue(lValue <= rValue);
                        case ">" -> currExpr.setValue(lValue > rValue);
                        case ">=" -> currExpr.setValue(lValue >= rValue);
                    }
                    break;
                }
            }
            case "and":
            case "or": {
                boolean lValue = (boolean) LHS.getValue(stack);
                boolean rValue = (boolean) RHS.getValue(stack);
                switch(binOp) {
                    case "and" -> currExpr.setValue(lValue && rValue);
                    case "or" -> currExpr.setValue(lValue || rValue);
                }
                break;
            }
            case "<<":
            case ">>":
            case "&":
            case "|":
            case "^": {
                if(be.type.isInt()) {
                    int lValue = (int) LHS.getValue(stack);
                    int rValue = (int) RHS.getValue(stack);
                    switch(binOp) {
                        case "<<" -> currExpr.setValue(lValue << rValue);
                        case ">>" -> currExpr.setValue(lValue >> rValue);
                        case "^" -> currExpr.setValue(lValue ^ rValue);
                    }
                    break;
                }
                else if(be.type.isBool()) {
                    boolean lValue = (boolean) LHS.getValue(stack);
                    boolean rValue = (boolean) RHS.getValue(stack);
                    switch(binOp) {
                        case "&" -> currExpr.setValue(lValue & rValue);
                        case "|" -> currExpr.setValue(lValue | rValue);
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

    public void visitBlockStmt(BlockStmt bs) {
        stack = stack.openNewScope();

        bs.decls().visit(this);

        for(int i = 0; i < bs.stmts().size(); i++) {
            if(returnFound) {
                returnFound = false;
                break;
            }
            bs.stmts().get(i).visit(this);
        }
        stack = stack.closeScope();
    }

    public void visitBreakStmt(BreakStmt bs) {
        if(!insideLoop) {
            System.out.println(PrettyPrint.RED + "Error! 'break' statement can not appear outside loop");
            System.exit(1);
        }
    }

    public void visitCastExpr(CastExpr cs) {
        cs.castExpr().visit(this);
        if(cs.castType().isInt()) { cs.setValue((int)currExpr.getValue(stack)); }
        else if(cs.castType().isReal()) { cs.setValue(Double.valueOf((int)currExpr.getValue(stack))); }
        else if(cs.castType().isChar()) { cs.setValue((Character)currExpr.getValue(stack)); }
        else if(cs.castType().isString()) { cs.setValue((String)currExpr.getValue(stack)); }
    }

    public void visitChoiceStmt(ChoiceStmt cs) {
        cs.choiceExpr().visit(this);

        for(int i = 0; i < cs.caseStmts().size(); i++) {
            CaseStmt curr = cs.caseStmts().get(i);
            if(currExpr.getValue(stack).equals(curr.choiceLabel().toString())) {
                curr.visit(this);
                break;
            }
        }
    }

    public void visitContinueStmt(ContinueStmt cs) {
        if(!insideLoop) {
            System.out.println(PrettyPrint.RED + "Error! 'continue' statement can not appear outside loop");
            System.exit(1);
        }
    }

    public void visitDoStmt(DoStmt ds) {
        do {
            insideLoop = true;
            ds.doBlock().visit(this);
            insideLoop = false;
            ds.condition().visit(this);
        } while ((boolean) currExpr.getValue(stack));
    }

    public void visitEndl(Endl e) { currExpr.setValue("\n"); }

    public void visitEnumDecl(EnumDecl ed) {
        ArrayList<Object> arr = new ArrayList<Object>();
        for(int i = 0; i < ed.enumVars().size(); i++) {
            ed.enumVars().get(i).visit(this);
            arr.add(currExpr.getValue(stack));
        }
        stack.setValueInRuntimeStack(ed.name().toString(),arr);
    }

    public void visitFieldExpr(FieldExpr fe) {
        HashMap<String,Object> instance = (HashMap<String,Object>) stack.getValueInRuntimeStack(fe.fieldTarget().toString());
        fe.setValue(instance.get(fe.name().toString()));
        currExpr = fe;
    }

    public void visitForStmt(ForStmt fs) {
        fs.forInits().visit(this);
        fs.condition().visit(this);
        while((boolean)currExpr.getValue(stack)) {
            insideLoop = true;
            fs.forBlock().visit(this);
            insideLoop = false;
            fs.nextExpr().visit(this);
            fs.condition().visit(this);
        }
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        gd.var().init().visit(this);
        stack.setValueInRuntimeStack(gd.var().toString(),currExpr.getValue(stack));
    }

    public void visitIfStmt(IfStmt is) {
        is.condition().visit(this);
        if((boolean)currExpr.getValue(stack)) { is.ifBlock().visit(this); }
        else {
            if(is.elifStmts().size() > 0) {
                for(int i = 0; i < is.elifStmts().size(); i++) {
                    IfStmt curr = is.elifStmts().get(i);
                    curr.condition().visit(this);
                    if((boolean)currExpr.getValue(stack)) {
                        curr.ifBlock().visit(this);
                        break;
                    }
                }
            }
            else if(is.elseBlock() != null) { is.elseBlock().visit(this); }
        }
    }

    public void visitInStmt(InStmt in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
            input = reader.readLine();
        } catch(Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        String[] args = input.split(" ");
        for(int i = 0; i < in.inExprs().size(); i++)
           stack.setValueInRuntimeStack(in.inExprs().get(i).toString(),args[i]);
    }

    public void visitInvocation(Invocation in) {
        ArrayList<Object> args = new ArrayList<Object>();

        for(int i = 0; i < in.arguments().size(); i++) {
            in.arguments().get(i).visit(this);
            args.add(currExpr.getValue(currentScope));
        }

        SymbolTable oldScope = currentScope;
        if(in.target() == null) {
            FuncDecl fd = currentScope.findName(in.toString()).declName().asTopLevelDecl().asFuncDecl();
            currentScope = fd.symbolTable;

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = fd.params().get(i);
                stack.setValueInRuntimeStack(currParam.toString(),args.get(i));
            }

            fd.visit(this);
        }
        else {
            ClassDecl cd = currentScope.findName(in.targetType.typeName()).declName().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.toString()).declName().asMethodDecl();
            currentScope = md.symbolTable;

            for(int i = 0; i < in.arguments().size(); i++) {
                ParamDecl currParam = md.params().get(i);
                stack.setValueInRuntimeStack(currParam.toString(),args.get(i));
            }

            md.visit(this);
        }
        currentScope = oldScope;
    }

    public void visitListLiteral(ListLiteral li) {
        ArrayList<Object> arr = new ArrayList<Object>();
        for(int i = 0; i < li.exprs().size(); i++) {
            li.exprs().get(i).visit(this);
            arr.add(currExpr.getValue(stack));
        }
        currExpr = li;
    }

    public void visitLiteral(Literal li) {
        currExpr = li;
        if(li.type.isInt()) {
            if(li.text.charAt(0) == '~')
                currExpr.setValue(-1*Integer.parseInt(li.text.substring(1)));
            else
                currExpr.setValue(Integer.parseInt(li.text));
        }
        else if(li.type.isChar()) { currExpr.setValue(li.text); }
        else if(li.type.isBool()) { currExpr.setValue(Boolean.parseBoolean(li.text)); }
        else if(li.type.isReal()) { currExpr.setValue(Double.parseDouble(li.text)); }
        else if(li.type.isString()) {
            currExpr.setValue(li.text.substring(1,li.text.length()-1));
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        ld.var().init().visit(this);
        stack.setValueInRuntimeStack(ld.var().toString(),currExpr.getValue(stack));
    }

    public void visitMainDecl(MainDecl md) {
        currentScope = md.symbolTable;
        md.mainBody().visit(this);
    }

    public void visitNameExpr(NameExpr ne) {
        currExpr = ne;
        currExpr.setValue(stack.getValueInRuntimeStack(ne.toString()));
    }

    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.classType().typeName()).declName().asTopLevelDecl().asClassDecl();

        HashMap<String,Object> instance = new HashMap<String,Object>();
        for(int i = 0; i < ne.args().size(); i++) {
            Var currArg = ne.args().get(i);
            currArg.init().visit(this);
            instance.put(currArg.toString(),currExpr.getValue(stack));
        }
        ne.setValue(instance);
        currExpr = ne;
    }

    public void visitOutStmt(OutStmt os) {
        for(int i = 0; i < os.outExprs().size(); i++) {
            os.outExprs().get(i).visit(this);
            System.out.print(currExpr.getValue(stack));
        }
    }

    public void visitReturnStmt(ReturnStmt rs) {
        if(rs.expr() != null)
            rs.expr().visit(this);

        returnFound = true;
    }

    public void visitStopStmt(StopStmt ss) {
        System.exit(1);
    }

    public void visitUnaryExpr(UnaryExpr ue) {
        ue.expr().visit(this);
        Object val = currExpr.getValue(stack);

        String uOp = ue.unaryOp().toString();

        //TODO: Operator Overload

        switch(uOp) {
            case "~": {
                if(ue.type.isInt()) {
                    int uVal = (int) val;
                    currExpr.setValue(uVal*-1);
                    break;
                }
                else if(ue.type.isReal()) {
                    double uVal = (double) val;
                    currExpr.setValue(uVal*-1);
                    break;
                }
            }
            case "not": {
                boolean uVal = (boolean) val;
                currExpr.setValue(!uVal);
                break;
            }
        }

        if(ue.expr().isNameExpr())
            stack.setValueInRuntimeStack(ue.expr().asNameExpr().toString(),currExpr.getValue(stack));
    }

    public void visitWhileStmt(WhileStmt ws) {
        ws.condition().visit(this);
        while((boolean)currExpr.getValue(stack)) {
            insideLoop = true;
            ws.whileBlock().visit(this);
            insideLoop = false;
            ws.condition().visit(this);
        }
    }
}
