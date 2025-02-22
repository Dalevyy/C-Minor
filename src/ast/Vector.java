package ast;

import utilities.Visitor;

// Extend from Arraylist
public class Vector<ASTNode extends AST> extends AST {

    public Vector() { super(); }
    public Vector(AST node) {
        super(node);
        addChild(node);
    }

    public void append(ASTNode node) { addChild(node); }

    public AST pop() { return removeChild(); }

    public void merge(Vector<ASTNode> seq) {
        for(int i = 0; i < seq.size(); i++)
            this.append(seq.get(i));
    }

    public void merge(ASTNode node) { this.append(node); }

    public ASTNode get(int pos) { return (ASTNode)this.children.get(pos); }

    public int size() { return this.children.size(); }

    public boolean isVector() { return true; }
    public Vector<ASTNode> asVector() { return this; }

    @Override
    public void visit(Visitor v) { v.visitVector(this); }
}
