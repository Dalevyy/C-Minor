package AST.TopLevelDecls;

import AST.*;
import AST.Types.*;
import Token.*;
import Utilities.PokeVisitor;

/*
 +-----------------------------------------------------------+
 +                        EnumDecl                           +
 +-----------------------------------------------------------+

An EnumDecl is composed of 3 parts:
    1. A name given to the Enum
    2. An optional type denoting what type the Enum evaluates to
    3. A vector of Vars that store the enum values. All Vars
       will have a name, but have an optional expression.

Parent Node: Compilation
*/
public class EnumDecl extends TopLevelDecl {

    private Name name;
    private Type type;
    private Vector<Var> eVars;

    public EnumDecl(Token t, Name name, Vector<Var> ef) { this(t,name, null, ef); }

    public EnumDecl(Token t, Name name, Type type, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.type = type;
        this.eVars = ef;

        addChild(this.name);
        addChild(this.type);
        addChild(this.eVars);
        setParent();
        setDebugInfo();
    }

    public Name getName() { return name; }
    public Type getType() { return type; }
    public Vector<Var> getEnumVars() { return eVars;}

    public boolean isEnumDecl() { return true; }

    public void setDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(" " + this.name.getText() + " ");
        sb.append("type ");
        if(this.type != null)
            sb.append(this.type.getText());
        sb.append("{ ");
        for(int i = 0; i < eVars.children.size(); i++) {
            if(i == eVars.children.size()-1)
                this.location.end = eVars.children.get(i).location.end;
        //    sb.append(eVars.asASTNode(i).getText() + " ");
        }
        sb.append("}");
        this.location.end.addToCol();
        this.appendText(sb.toString());
    }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsEnumDecl(this); }
}
