package AST;

import Utilities.PokeVisitor;

// Extend from Arraylist
public class Vector<ASTNode extends AST> extends AST {

    public Vector() { super(); }
    public Vector(AST node) {
        super(node);
        addChild(node);
    }

    public void append(ASTNode node) { addChild(node); }

    public void merge(Vector<ASTNode> seq) {
        for(int i = 0; i < seq.size(); i++)
            this.append(seq.get(i));
    }

    public void merge(ASTNode node) { this.append(node); }

    public ASTNode get(int pos) { return (ASTNode)this.children.get(pos); }

    public int size() { return this.children.size(); }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsVector(this); }
}
