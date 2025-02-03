package ast.class_body;

import ast.*;
import utilities.Visitor;

// Created after NameChecker
public class InitDecl extends AST {

    private Vector<FieldDecl> params;
    
    public InitDecl(Vector<FieldDecl> p) {
        this.params = p;
    }

    public Vector<FieldDecl> params() { return this.params; }

    @Override
    public void visit(Visitor v) { v.visitInitDecl(this); }
}
