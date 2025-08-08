package ast.types;

import ast.AST;
import ast.misc.Name;
import utilities.Visitor;

public class EnumType extends DiscreteType {

    private Name name;
    private Type constantType;

    public EnumType() { this(null,null); }
    public EnumType(String n, Discretes d) {
        super(d);
        name = new Name(n);

        if(specificType == Discretes.INT) { constantType = new DiscreteType(Discretes.INT); }
        else if(specificType == Discretes.CHAR) { constantType = new DiscreteType(Discretes.CHAR); }
        else { constantType = new DiscreteType(Discretes.BOOL); }
    }

    public Type constantType() { return constantType; }

    public void setName(Name name) {
        this.name = name;
    }

    public void setConstantType(Type constantType) {
        this.constantType = constantType;
    }

    public boolean isEnumType() { return true; }
    public EnumType asEnumType() { return this; }

    @Override
    public String typeName() { return name.toString(); }

    @Override
    public String toString() { return typeName(); }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link EnumType}
     */
    @Override
    public AST deepCopy() {
        return new EnumTypeBuilder()
                   .setMetaData(this)
                   .setName(this.name.deepCopy().asSubNode().asName())
                   .setConstantType(this.getDiscreteType())
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitEnumType(this); }

    public static class EnumTypeBuilder extends NodeBuilder {
        private final EnumType et = new EnumType();

        public EnumTypeBuilder setMetaData(AST node) {
            super.setMetaData(et,node);
            return this;
        }

        public EnumTypeBuilder setName(Name name) {
            et.setName(name);
            return this;
        }

        public EnumTypeBuilder setConstantType(Discretes d) {
            et.setConstantType(new DiscreteType(d));
            return this;
        }

        public EnumType create() {
            return et;
        }
    }
}
