package AST.ClassBody;

import AST.*;
import AST.Types.*;
import Token.*;
import Utilities.PokeVisitor;

public class DataDecl extends AST {

    private Modifier mod;
    private Var var;
    private Type type;

    public DataDecl(Token t, Modifier m, Var v, Type type) {
        super(t);
        this.mod = m;
        this.var = v;
        this.type = type;

        addChild(this.mod);
        addChild(this.var);
        addChild(this.type);
        setParent();
    }

    public Modifier getModifier() { return mod; }
    public Var getVars() { return var; }
    public Type getType() { return type; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsDataDecl(this); }
}
