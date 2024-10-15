package AST.Statements;

import AST.*;
import Token.*;
import Utilities.PokeVisitor;

// Leaf Node
public class StopStmt extends Statement {

    public StopStmt(Token t) { super(t); }

    public boolean isStopStmt() { return true; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsStopStmt(this); }
}
