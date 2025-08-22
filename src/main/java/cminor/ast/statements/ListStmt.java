package cminor.ast.statements;

import cminor.ast.AST;
import cminor.ast.expressions.Expression;
import cminor.ast.expressions.Invocation;
import cminor.ast.types.ListType;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * A {@link Statement} node that executes a list command.
 * <p>
 *     A list command is an action a user wishes to perform on a {@link ListType}
 *     during runtime. There are currently 3 commands: append, remove, and insert.
 * </p>
 * @author Daniel Levy
 */
public class ListStmt extends Statement {

    /**
     * An enumeration that stores all possible list commands.
     */
    public enum Commands { APPEND, REMOVE, INSERT}

    /**
     * A {@link Vector} containing the string representation of {@link Commands}.
     */
    private static final Vector<String> names = new Vector<>(new String[]{"append","remove","insert"});

    /**
     * The specific command that the list statement executes.
     */
    private Commands command;

    /**
     * A {@link Vector} containing the arguments passed to the list statement.
     */
    private Vector<Expression> args;

    /**
     * This is needed in the edge case where a list command is actually a function call when executing in the VM.
     */
    private Invocation funcCall = null;

    /**
     * Default constructor for {@link ListStmt}.
     */
    public ListStmt() { this(new Token(),null,new Vector<>()); }

    /**
     * Main constructor for {@link ListStmt}.
     * @param metaData {@link Token} containing all the metadata we will save into this node.
     * @param command {@link Commands} to store into {@link #command}.
     * @param args {@link Vector} of {@link Expression} to store into {@link #args}.
     */
    public ListStmt(Token metaData, Commands command, Vector<Expression> args) {
        super(metaData);

        this.command = command;
        this.args = args;

        addChildNode(this.args);
    }

    /**
     * Getter method for {@link #command}.
     * @return {@link Commands}
     */
    public Commands getCommand() { return this.command; }

    /**
     * Getter method for {@link #args}.
     * @return {@link Vector} of expressions
     */
    public Vector<Expression> getAllArgs() { return this.args; }

    /**
     * Checks if the current command is {@code append}.
     * @return {@code True} if the command is {@code append}, {@code False} otherwise.
     */
    public boolean isAppend() { return this.command == Commands.APPEND; }

    /**
     * Checks if the current command is {@code insert}.
     * @return {@code True} if the command is {@code insert}, {@code False} otherwise.
     */
    public boolean isInsert() { return this.command == Commands.INSERT; }

    /**
     * Checks if the current command is {@code remove}.
     * @return {@code True} if the command is {@code remove}, {@code False} otherwise.
     */
    public boolean isRemove() { return this.command == Commands.REMOVE; }

    /**
     * Returns the expected number of arguments based on the {@link #command}.
     * @return 3 if the command is {@code Insert}, 2 otherwise.
     */
    public int getExpectedNumOfArgs() { return isInsert() ? 3 : 2; }

    /**
     * Getter to return the list we want to use the command on.
     * @return {@link Expression}
     */
    public Expression getList() { return args.get(0); }

    /**
     * Getter to return the type of list.
     * @return {@link ListType}
     */
    public ListType getListType() { return args.get(0).type.asList(); }

    /**
     * Getter to return the second argument in the list statement.
     * @return {@link Expression}
     */
    public Expression getSecondArg() { return args.get(1); }

    /**
     * Getter to return the third argument in the list statement.
     * @return {@link Expression}
     */
    public Expression getThirdArg() { return args.get(2); }

    /**
     * Setter method to set {@link #funcCall}.
     * @param in {@link Invocation}
     */
    public void setInvocation(Invocation in) { funcCall = in; }

    /**
     * Getter method for {@link #funcCall}.
     * @return {@link Invocation}
     */
    public Invocation getInvocation() { return funcCall; }

    /**
     * {@inheritDoc}
     */
    public boolean isListStmt() { return true; }

    /**
     * {@inheritDoc}
     */
    public ListStmt asListStmt() { return this;}

    /**
     * Returns the {@link #command} as a string.
     * @return String representation of {@link #command}.
     */
    @Override
    public String toString() { return  names.get(command.ordinal()); }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int pos, AST node) {
        args.remove(pos);
        args.add(pos,node.asExpression());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AST deepCopy() {
        Vector<Expression> args = new Vector<>();
        for(Expression arg : this.args)
            args.add(arg.deepCopy().asExpression());

        return new ListStmtBuilder()
                   .setMetaData(this)
                   .setCommand(command)
                   .setArgs(args)
                   .create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Visitor v) { v.visitListStmt(this); }

    /**
     * Internal class that builds a {@link ListStmt} object.
     */
    public static class ListStmtBuilder extends NodeBuilder {

        /**
         * {@link ListStmt} object we are building.
         */
        private final ListStmt ls = new ListStmt();

        /**
         * @see ast.AST.NodeBuilder#setMetaData(AST, AST)
         * @return Current instance of {@link ListStmtBuilder}.
         */
        public ListStmtBuilder setMetaData(AST node) {
            super.setMetaData(ls, node);
            return this;
        }

        /**
         * Sets the list statement's {@link #command}.
         * @param command {@link Commands} representing the command the list statement executes.
         * @return Current instance of {@link ListStmtBuilder}.
         */
        public ListStmtBuilder setCommand(Commands command) {
            ls.command = command;
            return this;
        }

        /**
         * Sets the list statement's {@link #args}.
         * @param args {@link Vector} containing the arguments passed to the list statement.
         * @return Current instance of {@link ListStmtBuilder}.
         */
        public ListStmtBuilder setArgs(Vector<Expression> args) {
            ls.args = args;
            return this;
        }

        /**
         * Creates a {@link ListStmt} object.
         * @return {@link ListStmt}
         */
        public ListStmt create(){
            ls.addChildNode(ls.args);
            return ls;
        }
    }
}
