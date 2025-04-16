package ast.types;

import ast.*;
import token.*;
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
    private final Name name;
    private final Vector<Type> templateTypes;

    public ClassType(Name n) { this(new Token(),n,null); }
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
    public Vector<Type> templateTypes() { return templateTypes; }

    public String typeName() {
        if(name.toString().contains("/")) { return name.toString().substring(0,name.toString().indexOf("/")); }
        return name.toString();
    }

    public boolean isClassType() { return true; }
    public ClassType asClassType() { return this; }

    public static boolean isSuperClass(ClassType subClass, ClassType superClass) {
        if(subClass.toString().equals(superClass.toString())) { return true; }

        String classHierarchy = subClass.toString();
        String superClassName = superClass.toString();
        int slashLocation = classHierarchy.indexOf('/');

        while(slashLocation != -1) {
            String subClassName = classHierarchy.substring(0,slashLocation);
            if(subClassName.equals(superClassName)) { return true; }
            classHierarchy = classHierarchy.substring(slashLocation+1,classHierarchy.length());
        }

        return false;
    }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public void visit(Visitor v) { v.visitClassType(this); }
}
