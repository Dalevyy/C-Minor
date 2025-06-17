package ast.statements;

import ast.AST;
import ast.expressions.*;
import token.*;
import utilities.Vector;
import utilities.Visitor;
import utilities.SymbolTable;

public class IfStmt extends Statement {

    public SymbolTable symbolTableIfBlock;
    public SymbolTable symbolTableElseBlock;

    private Expression cond;
    private BlockStmt ifBlock;
    private final Vector<IfStmt> elifStmts;
    private BlockStmt elseBlock;

    public IfStmt(Token t, Expression c, BlockStmt ib) { this(t,c,ib,new Vector<>(),null); }
    public IfStmt(Token t, Expression c, BlockStmt ib, Vector<IfStmt> es) { this(t,c,ib,es,null); }
    public IfStmt(Token t, Expression c, BlockStmt ib, Vector<IfStmt> es, BlockStmt eb) {
        super(t);
        this.cond = c;
        this.ifBlock = ib;
        this.elifStmts = es;
        this.elseBlock = eb;

        addChild(this.cond);
        addChild(this.ifBlock);
        addChild(this.elifStmts);
        addChild(this.elseBlock);
        setParent();
    }

    public Expression condition() { return cond; }
    public BlockStmt ifBlock() { return ifBlock; }
    public Vector<IfStmt> elifStmts() { return elifStmts; }
    public BlockStmt elseBlock() { return elseBlock; }

    public boolean isIfStmt() { return true; }
    public IfStmt asIfStmt() { return this; }

    @Override
    public void update(int pos, AST n) {
        switch(pos) {
            case 0:
                cond = n.asExpression();
                break;
            case 1:
                ifBlock = n.asStatement().asBlockStmt();
                break;
            default:
                if(elifStmts().size()-pos-2 >= 1) {
                    elifStmts.remove(pos-2);
                    elifStmts.add(pos-2,n.asStatement().asIfStmt());
                }
                else
                    elseBlock = n.asStatement().asBlockStmt();
        }
    }

    @Override
    public void visit(Visitor v) { v.visitIfStmt(this); }
}
