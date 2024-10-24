package AST.Statements;

import AST.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

//TODO: Add a SymbolTable here
public class BlockStmt extends Statement {

    Vector<LocalDecl> varDecls;
    Vector<Statement> stmts;

    public BlockStmt(Token t, Vector<LocalDecl> vd, Vector<Statement> s) {
        super(t);
        this.varDecls = vd;
        this.stmts = s;

        addChild(this.varDecls);
        addChild(this.stmts);
        setParent();
    }

    public Vector<LocalDecl> getVarDecls() { return varDecls; }
    public Vector<Statement> getStmts() { return stmts; }

    public boolean isBlockStmt() { return true; }
    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsBlockStmt(this); }
}
