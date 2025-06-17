package ast.statements;

import ast.AST;
import ast.expressions.Expression;
import ast.expressions.NameExpr;
import ast.types.ListType;
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

    public NameExpr getListName() { return this.args.get(0).asNameExpr(); }
    public ListType getListType() { return this.args.get(0).type.asListType(); }
    public Expression getSecondArg() { return this.args.get(1); }
    public Expression getThirdArg() { return this.args.get(2); }

    public boolean isListStmt() { return true; }
    public ListStmt asListStmt() { return this;}

    @Override
    public String toString() { return  names.get(commandType.ordinal()); }

    @Override
    public void update(int pos, AST n) {
        args.remove(pos);
        args.add(pos,n.asExpression());
    }

    @Override
    public void visit(Visitor v) { v.visitListStmt(this); }
}
