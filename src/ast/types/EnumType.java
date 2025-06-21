package ast.types;

import ast.misc.Name;
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

    @Override
    public String typeName() { return name.toString(); }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitEnumType(this); }

    public static class EnumTypeBuilder {
        private String name;
        private Discretes constantType;

        public EnumTypeBuilder setName(String s) {
            this.name = s;
            return this;
        }

        public EnumTypeBuilder setConstantType(Discretes d) {
            this.constantType = d;
            return this;
        }

        public EnumType createEnumType() { return new EnumType(name,constantType); }
    }
}
