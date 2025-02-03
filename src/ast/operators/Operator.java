package ast.operators;

import ast.*;
import token.*;
import utilities.PrettyPrint;

public abstract class Operator extends AST {
    public Operator(Token t) { super(t); }

    public boolean isOperator() { return true; }
    public Operator asOperator() { return this; }

    public boolean isBinaryOp() { return false; }
    public BinaryOp asBinaryOp() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a BinaryOp.\n");
        System.exit(1);
        return null;
    }

    public boolean isUnaryOp() { return false; }
    public UnaryOp asUnaryOp() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a UnaryOp.\n");
        System.exit(1);
        return null;
    }

    public boolean isAssignOp() { return false; }
    public AssignOp asAssignOp() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an AssignOp.\n");
        System.exit(1);
        return null;
    }

}
