package ast.statements;

import ast.*;
import ast.expressions.BreakStmt;
import ast.expressions.ContinueStmt;
import token.*;
import utilities.PrettyPrint;

public abstract class Statement extends AST {
    public Statement() { super(); }
    public Statement (Token t) { super(t); }
    public Statement(AST node) { super(node); }

    public boolean isStatement() { return true; }
    public Statement asStatement() { return this; }

    public boolean isAssignStmt() { return false; }
    public AssignStmt asAssignStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an AssignStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isExprStmt() { return false; }
    public ExprStmt asExprStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an ExprStmt.\n");
        System.exit(1);
        return null;
    }
    public boolean isBlockStmt() { return false; }
    public BlockStmt asBlockStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a BlockStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isStopStmt() { return false; }
    public StopStmt asStopStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a StopStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isReturnStmt() { return false; }
    public ReturnStmt asReturnStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ReturnStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isIfStmt() { return false; }
    public IfStmt asIfStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an IfStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isWhileStmt() { return false; }
    public WhileStmt asWhileStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a WhileStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isDoStmt() { return false; }
    public DoStmt asDoStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a DoStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isForStmt() { return false; }
    public ForStmt asForStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ForStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isChoiceStmt() { return false; }
    public ChoiceStmt asChoiceStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ChoiceStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isCaseStmt() { return false; }
    public CaseStmt asCaseStmt() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a CaseStmt.\n");
        System.exit(1);
        return null;
    }

    public boolean isLocalDecl() { return false; }
    public LocalDecl asLocalDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a LocalDecl.\n");
        System.exit(1);
        return null;
    }
}
