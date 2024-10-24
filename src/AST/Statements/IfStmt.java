package AST.Statements;

import AST.*;
import AST.Expressions.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

public class IfStmt extends Statement {

    private Expression cond;
    private BlockStmt ifBlock;
    private Vector<IfStmt> elifStmts;
    private BlockStmt elseBlock;

    public IfStmt(Token t, Expression c, BlockStmt ib) { this(t,c,ib,null,null); }
    public IfStmt(Token t, Expression c, BlockStmt ib, Vector<IfStmt> es) { this(t,c,ib,es,null); }
    public IfStmt(Token t, Expression c, BlockStmt ib, Vector<IfStmt> es, BlockStmt eb) {
        super(t);
        this.cond = c;
        this.ifBlock = ib;
        this.elifStmts = es;
        this.elseBlock = eb;

        addChild(this.cond);
        addChild(this.ifBlock);
        addChild(this.elseBlock);
        setParent();
    }

    public Expression getCondition() { return cond; }
    public BlockStmt getIfBlock() { return ifBlock; }
    public Vector<IfStmt> getElifStmts() { return elifStmts; }
    public BlockStmt getElseBlock() { return elseBlock; }

    public boolean isIfStmt() { return true; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsIfStmt(this); }
}
