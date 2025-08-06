package ast.statements;

import ast.AST;
import ast.types.VoidType;
import token.*;
import utilities.Visitor;

// Leaf Node
public class StopStmt extends Statement {

    public StopStmt() { this(new Token()); }
    public StopStmt(Token t) { super(t); }

    public boolean isStopStmt() { return true; }
    public StopStmt asStopStmt() { return this; }

    @Override
    public void update(int pos, AST n) { throw new RuntimeException("A stop statement can not be updated."); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link StopStmt}
     */
    @Override
    public AST deepCopy() {
        return new StopStmtBuilder()
                   .setMetaData(this)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitStopStmt(this); }

    /**
     * Internal class that builds a {@link StopStmt} object.
     */
    public static class StopStmtBuilder extends NodeBuilder {

        /**
         * {@link StopStmt} object we are building.
         */
        private final StopStmt ss = new StopStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return StopStmtBuilder
         */
        public StopStmtBuilder setMetaData(AST node) {
            super.setMetaData(ss,node);
            return this;
        }

        /**
         * Creates a {@link StopStmt} object.
         * @return {@link StopStmt}
         */
        public StopStmt create() {
            return ss;
        }
    }
}
