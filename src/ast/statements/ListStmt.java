package ast.statements;

import ast.AST;
import ast.expressions.Expression;
import ast.types.ListType;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

public class ListStmt extends Statement {

    public enum Commands { APPEND, REMOVE, INSERT}
    private static final Vector<String> names = new Vector<>(new String[]{"append","remove","insert"});

    private Commands commandType;
    private Vector<Expression> args;

    public ListStmt() { this(new Token(),null,new Vector<>()); }
    public ListStmt(Token t, Commands c, Vector<Expression> args) {
        super(t);
        this.commandType = c;
        this.args = args;

        addChild(args);
    }

    public Commands getCommand() { return this.commandType; }
    public Vector<Expression> getAllArgs() { return this.args; }

    public int getExpectedNumOfArgs() { return isInsert() ? 3 : 2; }

    public Expression getList() { return this.args.get(0); }
    public ListType getListType() { return this.args.get(0).type.asListType(); }
    public Expression getSecondArg() { return this.args.get(1); }
    public Expression getThirdArg() { return this.args.get(2); }

    public boolean isAppend() { return this.commandType == Commands.APPEND; }
    public boolean isInsert() { return this.commandType == Commands.INSERT; }
    public boolean isRemove() { return this.commandType == Commands.REMOVE; }

    public boolean isListStmt() { return true; }
    public ListStmt asListStmt() { return this;}

    @Override
    public String toString() { return  names.get(commandType.ordinal()); }

    @Override
    public void update(int pos, AST node) {
        args.remove(pos);
        args.add(pos,node.asExpression());
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link ListStmt}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> args = new Vector<>();
        for(Expression expr : this.args)
            args.add(expr.deepCopy().asExpression());

        return new ListStmtBuilder()
                   .setMetaData(this)
                   .setCommand(this.commandType)
                   .setArgs(args)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitListStmt(this); }

    public static class ListStmtBuilder extends NodeBuilder {
        private final ListStmt ls = new ListStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return ListStmtBuilder
         */
        public ListStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public ListStmtBuilder setCommand(Commands ct) {
            ls.commandType = ct;
            return this;
        }

        public ListStmtBuilder setArgs(Vector<Expression> args) {
            ls.args = args;
            return this;
        }

        public ListStmt create(){
            super.saveMetaData(ls);
            ls.addChild(ls.args);
            return ls;
        }
    }
}
