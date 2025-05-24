package ast.statements;

import ast.expressions.Expression;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class ListStmt extends Statement {

    public enum Commands { APPEND, REMOVE, INSERT}
    private static final Vector<String> names = new Vector<>(new String[]{"append","remove","insert"});

    private final Commands commandType;
    private final Vector<Expression> args;

    public ListStmt(Token t, Commands c, Vector<Expression> args) {
        super(t);
        this.commandType = c;
        this.args = args;

        addChild(args);
        setParent();
    }

    public Commands getCommand() { return this.commandType; }
    public Vector<Expression> getAllArgs() { return this.args; }


    public boolean isListStmt() { return true; }
    public ListStmt asListStmt() { return this;}

    @Override
    public String toString() { return  names.get(commandType.ordinal()); }

    @Override
    public void visit(Visitor v) { v.visitListStmt(this); }
}
