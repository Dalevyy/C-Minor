package ast.types;

import ast.Name;
import utilities.Visitor;

public class EnumType extends DiscreteType {

    private final Name name;
    private final Type constantType;

    public EnumType(String n, Discretes d) {
        super(d);
        name = new Name(n);

        if(dType == Discretes.INT) { constantType = new DiscreteType(Discretes.INT); }
        else if(dType == Discretes.CHAR) { constantType = new DiscreteType(Discretes.CHAR); }
        else { constantType = new DiscreteType(Discretes.BOOL); }
    }

    public Type constantType() { return constantType; }

    public boolean isEnumType() { return true; }
    public EnumType asEnumType() { return this; }

    public String typeName() { return "Enum"; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumType(this); }
}
