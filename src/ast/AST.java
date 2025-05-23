package ast;

import ast.classbody.*;
import ast.expressions.*;
import ast.misc.Compilation;
import ast.misc.Name;
import ast.misc.ParamDecl;
import ast.misc.Var;
import ast.operators.*;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.*;
import token.*;
import utilities.*;

/*
____________________________ AST ____________________________
This is the superclass for all C Minor parse tree nodes.

Total Unique C Minor AST Nodes: 49
_____________________________________________________________
*/
public abstract class AST {

    /*
        Each node will contain its textual representation
        alongside its location in the program. This info
        is copied from the tokens in the Parser whenever we
        create a new AST node.
    */
    public String text;
    public Location location;

    /*
        AST Structure:
            Each node in the AST will have a reference to its
            parent alongside references to all of its children.
    */
    private AST parent;

    public Vector<AST> children = new Vector<>();

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
    public Location getLocation() { return this.location; }
    public int startLine() { return this.location.start.line; }
    public void setEndLocation(Position end) { this.location.end = end;}
    public String getText() { return this.text; }

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
        n.parent = null;
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

    public <T extends AST> void addChild(Vector<T> nodes) { for(AST node : nodes) { this.addChild(node); } }

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
