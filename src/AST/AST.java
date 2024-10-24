package AST;

import java.util.ArrayList;
import Token.*;
import Utilities.PokeVisitor;

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
    public AST parent;
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

//    public void setDebugInfo(Token t) {
//        text = t.getText();
//        location.start = t.getLocation().start;
//        location.end = t.getLocation().end;
//    }

    public void appendText(String s) { this.text += s; }
    public String getText() { return this.text; }

    // add Vector children here instead
    public void setParent() {
        for(AST n : children)
            n.parent = this;
    }

    public void addChild(AST node) {
        if(node != null)
            children.add(node);
    }


    /*
    ----------------------------------------------------------------------
                               Visitor Methods
    ----------------------------------------------------------------------
    */

    public abstract AST whosThatNode(PokeVisitor v);

    public AST visitChildren(PokeVisitor v) {
        for(AST child : children) {
            if(child != null)
                child.whosThatNode(v);
        }
        return null;
    }
}
