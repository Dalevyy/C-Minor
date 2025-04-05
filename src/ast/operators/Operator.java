package ast.operators;

import ast.*;
import token.*;
import utilities.PrettyPrint;

public abstract class Operator extends AST {
    public Operator(Token t) { super(t); }

    public boolean isOperator() { return true; }
    public Operator asOperator() { return this; }

    public boolean isBinaryOp() { return false; }
    public BinaryOp asBinaryOp() { throw new RuntimeException("Expression can not be casted into a BinaryOp.\n"); }

    public boolean isUnaryOp() { return false; }
    public UnaryOp asUnaryOp() { throw new RuntimeException("Expression can not be casted into a UnaryOp.\n"); }

    public boolean isAssignOp() { return false; }
    public AssignOp asAssignOp() { throw new RuntimeException("Expression can not be casted into an AssignOp.\n"); }
}
