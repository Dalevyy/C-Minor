package ast.types;

import ast.*;
import token.*;
import utilities.Visitor;

public class ClassType extends Type {
    private Name name;
    private Vector<Type> templateTypes;

    public ClassType(Name n) { this(null,n,null); }
    public ClassType(Token t, Name n) { this(t,n,null); }

    public ClassType(Token t, Name n, Vector<Type> tt) {
        super(t);
        this.name = n;
        this.templateTypes = tt;

        addChild(this.name);
        addChild(this.templateTypes);
        setParent();
    }

    public Name getName() { return name; }
    public Vector<Type> getTemplateTypes() { return templateTypes; }

    public String typeName() { return name.toString(); }

    public boolean isClassType() { return true; }
    public ClassType asClassType() { return this; }

    public static boolean isSuperClass(ClassType current, ClassType sup) {
        if(Type.typeEqual(current,sup))
            return true;
        return false;
    }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitClassType(this); }
}
