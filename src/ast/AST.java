package ast;

import java.util.ArrayList;

import ast.class_body.*;
import ast.expressions.*;
import ast.operators.*;
import ast.statements.*;
import ast.top_level_decls.*;
import ast.types.*;
import token.*;
import utilities.*;

// TOTAL NODES : 45 Nodes
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
    //TODO: Change to a Vector :)
    public ArrayList<AST> children = new ArrayList<AST>();

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
        }
    }

    public void appendText(String s) { this.text += s; }
    public Location getLocation() { return this.location; }
    public int startLine() { return this.location.start.line; }
    public void setEndLocation(Position end) { this.location.end = end;}
    public String getText() { return this.text; }

    public void setParent() {
        for(AST n : children)
            n.parent = this;
    }
    public AST getParent() { return parent; }

    public void addChild(AST node) {
        if(node != null)
            children.add(node);
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
        StringBuilder sb = new StringBuilder();
        sb.append(this.location.start.line);
        sb.append(".");
        sb.append(this.location.start.column);
        return sb.toString();
    }

    public void printLine() {
        System.out.println(startLine() + "| " + this.text);
    }

    public boolean isCompliation() { return false; }
    public Compilation asCompilation() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Compilation Unit.\n");
        System.exit(1);
        return null;
    }
    public boolean isExpression() { return false; }
    public Expression asExpression() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an Expression.\n");
        System.exit(1);
        return null;
    }

    public boolean isOperator() { return false; }
    public Operator asOperator() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an Operator.\n");
        System.exit(1);
        return null;
    }

    public boolean isStatement() { return false; }
    public Statement asStatement() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Statement.\n");
        System.exit(1);
        return null;
    }

    public boolean isTopLevelDecl() { return false; }
    public TopLevelDecl asTopLevelDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a TopLevelDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isType() { return false; }
    public Type asType() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Type.\n");
        System.exit(1);
        return null;
    }

    public boolean isFieldDecl() { return false; }
    public FieldDecl asFieldDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a FieldDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isMethodDecl() { return false; }
    public MethodDecl asMethodDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a MethodDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isParamDecl() { return false; }
    public ParamDecl asParamDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ParamDecl.\n");
        System.exit(1);
        return null;
    }

    public Vector asVector() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Vector.\n");
        System.exit(1);
        return null;
    }

    public Name asName() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a Name.\n");
        System.exit(1);
        return null;
    }

    public static boolean notNull(AST n) { return n != null;}

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
