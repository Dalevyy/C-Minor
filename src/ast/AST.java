package ast;

import ast.classbody.ClassBody;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.Expression;
import ast.misc.*;
import ast.operators.Operator;
import ast.statements.Statement;
import ast.topleveldecls.TopLevelDecl;
import ast.types.Type;
import token.Location;
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

    public Location getLocation() { return this.location; }
    public int startLine() { return this.location.start.line; }

    /**
     * Copies the metadata of an AST node into the current node.
     * @param node AST node we want to copy
     */
    public void copyMetaData(AST node) {
        this.text = node.text;
        this.location = node.location;
        this.parent = node.parent;
    }

    /**
     * Removes all references of the passed AST node and replaces it with the current node.
     * @param node AST node we want to copy and remove from the tree
     */
    public void replace(AST node) {
        this.copyMetaData(node);

        AST curr = node;
        /*
            We will start removing all references of 'node' here. We will 
            traverse the AST going upwards until we reach the root.
        */
        while(curr.getParent() != null) {
            curr = curr.getParent();
            // Look through the current node's children
            for(int i = 0; i < curr.children.size(); i++) {
                /*
                    If a child node matches the passed 'node', then we need to replace 
                    the child node with the current node ('this'). We will update both
                    the children vector alongside the individual reference in the object.
                */
                if(curr.children.get(i).equals(node)) {
                    curr.removeChild(i);
                    curr.children.add(i,this);
                    curr.update(i,this);
                }
            }
        }
        node.parent = null;
    }

    /**
     * Updates a node's metadata based on a passed token.
     * @param t Token
     */
    public void updateMetaData(Token t) {
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

    public AST getRootParent() {
        if(parent == null)
            return null;
        AST curr = parent;
        while(curr.parent != null) {
            if(curr.isStatement() && curr.asStatement().isExprStmt())
                return curr;
            curr = curr.parent;
        }
        return curr;
    }

    public AST getCompilationUnit() {
        if(parent == null)
            return this;

        AST curr = parent;
        while(curr.parent != null && !curr.isCompilation())
            curr = curr.parent;
        return curr;
    }

    public void addChild(AST node) {
        if(node != null) {
            children.add(node);
            node.parent = this;
        }
    }

    public <T extends AST> void addChild(Vector<T> nodes) {
        if(!nodes.isEmpty())
            for(AST node : nodes)
                this.addChild(node);
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

    public abstract void update(int pos, AST n);

    public abstract AST deepCopy();

    public String getStartPosition() {
        return this.location.start.line + "." + this.location.start.column;
    }

    public void printLine() {
        System.out.println(startLine() + "| " + this.text);
    }
    public String line() { return startLine() + "| " + this.text + "\n"; }

    public String header() { return line(); }

    public boolean isCompilation() { return false; }
    public Compilation asCompilation() { throw new RuntimeException("Expression can not be casted into a Compilation Unit.\n"); }

    public boolean isExpression() { return false; }
    public Expression asExpression() { throw new RuntimeException("Expression can not be casted into an Expression.\n"); }

    public boolean isFieldDecl() { return false; }
    public FieldDecl asFieldDecl() { throw new RuntimeException("Expression can not be casted into a FieldDecl.\n"); }

    public boolean isLabel() { return true; }
    public Label asLabel() { throw new RuntimeException("Expression can not be casted into a Label.\n"); }

    public boolean isMethodDecl() { return false; }
    public MethodDecl asMethodDecl() { throw new RuntimeException("Expression can not be casted into a MethodDecl.\n"); }

    public ClassBody asClassBody() { throw new RuntimeException("fix later but this isn't a class body."); }
    public boolean isName() { return false; }
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

    public boolean isTypeifier() { return false; }
    public Typeifier asTypeifier() { throw new RuntimeException("Expression can not be casted into a Typeifier.\n"); }

    public boolean isVar() { return false; }
    public Var asVar() { throw new RuntimeException("Expression can not be casted into a Var.\n"); }

    // getType: Helper method to get a node's type (if applicable) (should be removed \0_0/)
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

    public static class NodeBuilder {
        protected String text = "";
        protected Location location = new Location();
        protected AST parent = null;

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return NodeBuilder
         */
        protected NodeBuilder setMetaData(AST node) {
            this.text = node.text;
            this.location = node.location;
            this.parent = node.parent;
            return this;
        }

        // Copies the internal metadata to the newly created node
        protected AST saveMetaData(AST node) {
            if(node.text.isEmpty())
                node.text = this.text;
            node.location = this.location;
            node.parent = this.parent;
            return node;
        }
    }
}
