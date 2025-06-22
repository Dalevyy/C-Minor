package ast.types;

import ast.misc.Name;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/*
___________________________ ClassType ___________________________
The first structured type is a ClassType. These types represent
any custom types that a C Minor uses defines when they create a
class. Any name that is used as a type will be parsed as a
ClassType as well.
_________________________________________________________________
*/
public class ClassType extends Type {
    private final Name name;                        // Only for single type
    private Vector<Name> inheritedTypes = new Vector<>();
    private final Vector<Type> templateTypes;

    public ClassType(String s) { this(new Token(),new Name(s),new Vector<>()); }
    public ClassType(Name n) { this(new Token(),n,new Vector<>()); }
    public ClassType(Token t, Name n) { this(t,n,new Vector<>(),new Vector<>()); }
    public ClassType(Name n, Vector<Type> ct) { this(new Token(),n,new Vector<>(),ct); }
    public ClassType(Token t, Name n, Vector<Type> tt) { this(t,n,new Vector<>(),tt); }
    public ClassType(Token t, Name n, Vector<Name> it, Vector<Type> tt) {
        super(t);
        this.name = n;
        this.templateTypes = tt;

        addChild(this.name);
        addChild(this.templateTypes);
        setParent();
    }

    public Name getName() { return name; }
    public Vector<Type> templateTypes() { return templateTypes; }

    public void setInheritedTypes(Vector<Name> it) { this.inheritedTypes = it; }
    public Vector<Name> getInheritedTypes() { return this.inheritedTypes; }

    public boolean isClassType() { return true; }
    public ClassType asClassType() { return this; }

    public static boolean classAssignmentCompatibility(Type ct1, ClassType ct2) {
        if(ct1.isMultiType())
            return ClassType.isSuperClass(ct2,ct1.asMultiType().getInitialType());
        else if(ct1.asClassType().getInheritedTypes().size() > ct2.getInheritedTypes().size())
            return ClassType.isSuperClass(ct1.asClassType(),ct2);
        else
            return ClassType.isSuperClass(ct2,ct1.asClassType());
    }

    public static boolean isSuperClass(ClassType subClass, ClassType superClass) {
        if(subClass.toString().equals(superClass.toString()))
            return true;

        for(Name n : subClass.getInheritedTypes()) {
            if(n.toString().equals(superClass.toString()))
                return true;
        }
        return false;
    }

    @Override
    public String typeName() { return name.toString(); }

    @Override
    public String toString() { return typeName(); }

    @Override
    public void visit(Visitor v) { v.visitClassType(this); }
}
