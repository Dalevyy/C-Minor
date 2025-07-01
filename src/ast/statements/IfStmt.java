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
    private Vector<IfStmt> elifStmts;
    private BlockStmt elseBlock;

    public IfStmt(){ this(new Token(),null,null,null,null); }
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

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link IfStmt}
     */
    @Override
    public AST deepCopy() {
        IfStmtBuilder isb = new IfStmtBuilder();

        Vector<IfStmt> elifStmts = new Vector<>();
        for(IfStmt is : this.elifStmts)
            elifStmts.add(is.deepCopy().asStatement().asIfStmt());

        if(this.elseBlock != null)
            isb.setElseBlock(this.elseBlock.deepCopy().asStatement().asBlockStmt());

        return isb.setMetaData(this)
                  .setCondition(this.cond.deepCopy().asExpression())
                  .setIfBlock(this.ifBlock.deepCopy().asStatement().asBlockStmt())
                  .setElifStmts(elifStmts)
                  .create();
    }

    @Override
    public void visit(Visitor v) { v.visitIfStmt(this); }

    public static class IfStmtBuilder extends NodeBuilder {
        private final IfStmt is = new IfStmt();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return IfStmtBuilder
         */
        public IfStmtBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public IfStmtBuilder setCondition(Expression cond) {
            is.cond = cond;
            return this;
        }

        public IfStmtBuilder setIfBlock(BlockStmt ifBlock) {
            is.ifBlock = ifBlock;
            return this;
        }

        public IfStmtBuilder setElifStmts(Vector<IfStmt> elifStmts) {
            is.elifStmts = elifStmts;
            return this;
        }

        public IfStmtBuilder setElseBlock(BlockStmt elseBlock) {
            is.elseBlock = elseBlock;
            return this;
        }

        public IfStmt create(){
            super.saveMetaData(is);
            is.addChild(is.cond);
            is.addChild(is.ifBlock);
            is.addChild(is.elifStmts);
            is.addChild(is.elseBlock);
            return is;
        }
    }
}

