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
    private final Name name;        // Only for single type
    private final Vector<Type> templateTypes;

    public ClassType(String s) { this(new Token(),new Name(s),new Vector<>()); }
    public ClassType(Name n) { this(new Token(),n,new Vector<>()); }
    public ClassType(Token t, Name n) { this(t,n,new Vector<>(),new Vector<>()); }
    public ClassType(Name n, Vector<ClassType> it) { this(new Token(),n,it,new Vector<>()); }
    public ClassType(Token t, Name n, Vector<Type> tt) { this(t,n,new Vector<>(),tt); }
    public ClassType(Token t, Name n, Vector<ClassType> it, Vector<Type> tt) {
        super(t);
        this.name = n;
        this.templateTypes = tt;

        addChild(this.name);
        addChild(this.templateTypes);
        setParent();
    }

    public Name getName() { return name; }
    public Vector<Type> templateTypes() { return templateTypes; }

    public String typeName() {
        if(name.toString().contains("/")) { return name.toString().substring(0,name.toString().indexOf("/")); }
        return name.toString();
    }

    public String getClassHiearchy() { return name.toString(); }

    public boolean isClassType() { return true; }
    public ClassType asClassType() { return this; }

    public static boolean classAssignmentCompatibility(ClassType ct1, ClassType ct2) {
        if(ct1.toString().length() > ct2.toString().length())
            return ClassType.isSuperClass(ct1,ct2);
        else
            return ClassType.isSuperClass(ct2,ct1);
    }

    public static boolean isSuperClass(ClassType subClass, ClassType superClass) {
        if(subClass.toString().equals(superClass.toString())) { return true; }

        String classHierarchy = subClass.getClassHiearchy();
        String superClassName = superClass.getClassHiearchy();
        int slashLocation = classHierarchy.indexOf('/');

        while(slashLocation != -1) {
            String subClassName = classHierarchy.substring(0,slashLocation);
            if(subClassName.equals(superClassName)) { return true; }
            if(classHierarchy.length() == 1) { return false; }
            classHierarchy = classHierarchy.substring(slashLocation+1);
        }

        return false;
    }

    @Override
    public String toString() { return this.typeName(); }

    @Override
    public void visit(Visitor v) { v.visitClassType(this); }
}
