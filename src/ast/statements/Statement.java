package ast.statements;

import ast.*;
import token.*;

public abstract class Statement extends AST {
    public Statement() { super(); }
    public Statement (Token t) { super(t); }
    public Statement(AST node) { super(node); }

    public boolean isStatement() { return true; }
    public Statement asStatement() { return this; }

    public boolean isAssignStmt() { return false; }
    public AssignStmt asAssignStmt() { throw new RuntimeException("Expression can not be casted into an AssignStmt.\n"); }

    public boolean isBlockStmt() { return false; }
    public BlockStmt asBlockStmt() { throw new RuntimeException("Expression can not be casted into a BlockStmt.\n"); }

    public boolean isCaseStmt() { return false; }
    public CaseStmt asCaseStmt() { throw new RuntimeException("Expression can not be casted into a CaseStmt.\n"); }

    public boolean isChoiceStmt() { return false; }
    public ChoiceStmt asChoiceStmt() { throw new RuntimeException("Expression can not be casted into a ChoiceStmt.\n"); }

    public boolean isDoStmt() { return false; }
    public DoStmt asDoStmt() { throw new RuntimeException("Expression can not be casted into a DoStmt.\n"); }

    public boolean isExprStmt() { return false; }
    public ExprStmt asExprStmt() { throw new RuntimeException("Expression can not be casted into an ExprStmt.\n"); }

    public boolean isForStmt() { return false; }
    public ForStmt asForStmt() { throw new RuntimeException("Expression can not be casted into a ForStmt.\n"); }

    public boolean isIfStmt() { return false; }
    public IfStmt asIfStmt() { throw new RuntimeException("Expression can not be casted into an IfStmt.\n"); }

    public boolean isLocalDecl() { return false; }
    public LocalDecl asLocalDecl() { throw new RuntimeException("Expression can not be casted into a LocalDecl.\n"); }

    public boolean isReturnStmt() { return false; }
    public ReturnStmt asReturnStmt() { throw new RuntimeException("Expression can not be casted into a ReturnStmt.\n"); }

    public boolean isRetypeStmt() { return false; }
    public RetypeStmt asRetypeStmt() { throw new RuntimeException("Expression can not be casted into a RetypeStmt\n"); }

    public boolean isStopStmt() { return false; }
    public StopStmt asStopStmt() { throw new RuntimeException("Expression can not be casted into a StopStmt.\n"); }

    public boolean isWhileStmt() { return false; }
    public WhileStmt asWhileStmt() { throw new RuntimeException("Expression can not be casted into a WhileStmt.\n"); }
}
