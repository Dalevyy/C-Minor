package AST.Statements;

import AST.*;
import AST.Expressions.*;
import Token.*;
import Utilities.PokeVisitor;

public class WhileStmt extends Statement {

    Expression cond;
    Expression nextExpr;
    BlockStmt whileBlock;

    public WhileStmt(Token t, Expression cond, BlockStmt whileBlock) { this(t,cond,null,whileBlock); }

    public WhileStmt(Token t, Expression cond, Expression nextExpr, BlockStmt whileBlock) {
        super(t);
        this.cond = cond;
        this.nextExpr = nextExpr;
        this.whileBlock = whileBlock;

        addChild(this.cond);
        addChild(this.nextExpr);
        addChild(this.whileBlock);
        setParent();
    }

    public Expression getCondition() { return cond; }
    public Expression getNextExpr() { return nextExpr; }
    public BlockStmt getWhileBlock() { return whileBlock; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsWhileStmt(this); }
}
