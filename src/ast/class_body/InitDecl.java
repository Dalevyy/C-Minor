package ast.class_body;

import ast.*;
import ast.statements.AssignStmt;
import utilities.Visitor;

/*
Created after type checking is complete
*/
public class InitDecl extends AST {

    private Vector<AssignStmt> params;
    
    public InitDecl(Vector<AssignStmt> p) {
        this.params = p;
    }

    public Vector<AssignStmt> assignStmts() { return this.params; }

    @Override
    public void visit(Visitor v) { v.visitInitDecl(this); }
}
