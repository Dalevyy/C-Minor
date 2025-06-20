package ast;

import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.Expression;
import ast.misc.Compilation;
import ast.misc.Name;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.operators.Operator;
import ast.statements.Statement;
import ast.topleveldecls.TopLevelDecl;
import ast.types.Type;
import token.Location;
import token.Position;
import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * Superclass for all C Minor parse tree nodes
 */
public abstract class AST {

    /**
     * The textual representation of the current AST node
     */
    public String text;
    /**
     * The location of the AST node in the program
     * */
    public Location location;
    /**
     * List of child nodes that the current AST is a parent of
     */
    public Vector<AST> children = new Vector<>();
    /**
     * Reference to current AST node's parent
     */
    private AST parent;

    public AST() {
        this.text = "";
        this.location = new Location();
    }

    public AST(Token t) {
        this.text = t.getText();
        this.location = new Location();
        this.location.start = t.getLocation().start;
        this.location.end = t.getLocation().end;
    }

    public AST(AST node) {
        if(node != null) {
            this.text = node.text;
            this.location = node.location;
            this.parent = node.parent;
            this.children = node.children;
        }
    }

    public void appendText(String s) { this.text += s; }
    public String getText() { return this.text; }

    public void setEndLocation(Position end) { this.location.end = end;}
    public Location getLocation() { return this.location; }
    public int startLine() { return this.location.start.line; }

    public void copy(AST n) {
        this.text = n.text;
        this.location = n.location;
        this.parent = n.parent;
    }

    public void copyAndRemove(AST n) {
        this.copy(n);
        for(AST c : n.children) {
            this.addChild(c);
            c.parent = this;
        }
        
        AST curr = n;
        while(curr.getParent() != null) {
            curr = curr.getParent();
            for(int i = 0; i < curr.children.size(); i++) {
                AST c = curr.children.get(i);
                if(c == n) {
                    c.removeChild(i);
                    c.children.add(i,this);
                }
            }
            n.parent = n.getParent();
        }
        n.parent = null;
    }

    public void copyAndRemove(AST n, AST parent) {
        this.copyAndRemove(n);
        this.parent = parent;
    }

    public void updateNode(Token t) {
        this.text = t.getText();
        this.location = new Location();
        this.location.start = t.getLocation().start;
        this.location.end = t.getLocation().end;
    }

    public void setParent() {
        for(AST n : children)
            n.parent = this;
    }
    public AST getParent() { return parent; }

    public void addChild(AST node) {
        if(node != null)
            children.add(node);
    }

    public <T extends AST> void addChild(Vector<T> nodes) {
        for(AST node : nodes) { this.addChild(node); }
    }

    public AST removeChild() {
        if(!children.isEmpty())
            return children.remove(children.size()-1);
        return null;
    }

    public AST removeChild(int pos) {
        if(!children.isEmpty())
            return children.remove(pos);
        return null;
    }

    public String getStartPosition() {
        return this.location.start.line + "." + this.location.start.column;
    }

    public void printLine() {
        System.out.println(startLine() + "| " + this.text);
    }
    public String line() { return startLine() + "| " + this.text + "\n"; }

    public boolean isCompilation() { return false; }
    public Compilation asCompilation() { throw new RuntimeException("Expression can not be casted into a Compilation Unit.\n"); }

    public boolean isExpression() { return false; }
    public Expression asExpression() { throw new RuntimeException("Expression can not be casted into an Expression.\n"); }

    public boolean isFieldDecl() { return false; }
    public FieldDecl asFieldDecl() { throw new RuntimeException("Expression can not be casted into a FieldDecl.\n"); }

    public boolean isMethodDecl() { return false; }
    public MethodDecl asMethodDecl() { throw new RuntimeException("Expression can not be casted into a MethodDecl.\n"); }

    public Name asName() { throw new RuntimeException("Expression can not be casted into a Name.\n"); }

    public boolean isOperator() { return false; }
    public Operator asOperator() { throw new RuntimeException("Expression can not be casted into an Operator.\n"); }

    public boolean isParamDecl() { return false; }
    public ParamDecl asParamDecl() { throw new RuntimeException("Expression can not be casted into a ParamDecl.\n"); }

    public boolean isStatement() { return false; }
    public Statement asStatement() { throw new RuntimeException("Expression can not be casted into a Statement.\n"); }

    public boolean isTopLevelDecl() { return false; }
    public TopLevelDecl asTopLevelDecl() { throw new RuntimeException("Expression can not be casted into a TopLevelDecl.\n"); }

    public boolean isType() { return false; }
    public Type asType() { throw new RuntimeException("Expression can not be casted into a Type.\n"); }

    public boolean isVar() { return false; }
    public Var asVar() { throw new RuntimeException("Expression can not be casted into a Var.\n"); }

    // getType: Helper method to get a node's type (if applicable)
    public Type getType() {
        if(this.isExpression()) { return this.asExpression().type; }
        else if(this.isParamDecl()) { return this.asParamDecl().type(); }
        else if(this.isStatement()) {
            if (this.asStatement().isLocalDecl()) { return this.asStatement().asLocalDecl().type();}
            else { return null; }
        }
        else if(this.isTopLevelDecl()) {
            if(this.asTopLevelDecl().isGlobalDecl()) { return this.asTopLevelDecl().asGlobalDecl().type(); }
            else if(this.asTopLevelDecl().isEnumDecl()) { return this.asTopLevelDecl().asEnumDecl().type(); }
            else { return null; }
        }
        else if(this.isFieldDecl()) { return this.asFieldDecl().type(); }
        else { return null; }

    }
    /*
    ----------------------------------------------------------------------
                               Visitor Methods
    ----------------------------------------------------------------------
    */

    public abstract void visit(Visitor v);

    public void visitChildren(Visitor v) {
        for(AST child : children) {
            if(child != null)
                child.visit(v);
        }
    }
}
