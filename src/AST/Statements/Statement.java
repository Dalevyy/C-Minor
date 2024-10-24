package AST.Statements;

import AST.*;
import Token.*;

public abstract class Statement extends AST {
    public Statement() { super(); }
    public Statement (Token t) { super(t); }
    public Statement(AST node) { super(node); }

    public boolean isAssignStmt() { return false; }
    public boolean isExprStmt() { return false; }
    public boolean isBlockStmt() { return false; }

    public boolean isStopStmt() { return false; }
    public StopStmt asStopStmt() {
        throw new IllegalStateException("Error! Can not convert to Stop");
    }

    public boolean isReturnStmt() { return false; }
    public boolean isIfStmt() { return false; }
    public boolean isWhileStmt() { return false; }
    public boolean isDoStmt() { return false; }
    public boolean isForStmt() { return false; }
    public boolean isChoiceStmt() { return false; }
    public boolean isCaseStmt() { return false; }
    public boolean isLocalDecl() { return false; }

    // TODO: Is/AS Methods here :)
}
