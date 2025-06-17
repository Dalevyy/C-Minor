package ast.classbody;

import ast.*;
import ast.statements.AssignStmt;
import utilities.Vector;
import utilities.Visitor;

// Constructor Declaration is created after type checking is successful
public class InitDecl extends AST {

    private final Vector<AssignStmt> params;
    
    public InitDecl(Vector<AssignStmt> p) { this.params = p; }

    public Vector<AssignStmt> assignStmts() { return this.params; }

    @Override
    public void update(int pos, AST n) {
        params.remove(pos);
        params.add(pos,n.asStatement().asAssignStmt());
    }

    @Override
    public void visit(Visitor v) { v.visitInitDecl(this); }
}
