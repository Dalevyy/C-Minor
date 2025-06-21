package ast.types;

import token.Token;
import utilities.Vector;
import utilities.Visitor;

/*
___________________________ DiscreteType ___________________________
The second level of C Minor's primitive types will be discrete types
denoted by the DiscreteType node. These types will include Int, Char,
Bool, and also Enum.
____________________________________________________________________
*/
public class DiscreteType extends Type {

    public enum Discretes { INT, CHAR, BOOL, ENUM }
    private static final Vector<String> names = new Vector<>(new String[]{ "Int", "Char", "Bool", "Enum" });

    protected Discretes dType;

    public DiscreteType(Discretes d) { this(new Token(),d); }
    public DiscreteType(Token t, Discretes d) {
        super(t);
        this.dType = d;
    }

    public boolean isDiscreteType() { return true; }
    public DiscreteType asDiscreteType() { return this; }
    public Discretes getDiscreteType() { return dType; }

    @Override
    public String typeName() { return names.get(dType.ordinal()); }

    @Override
    public String toString() { return typeName(); }

    @Override
    public void visit(Visitor v) { v.visitDiscreteType(this); }
}
