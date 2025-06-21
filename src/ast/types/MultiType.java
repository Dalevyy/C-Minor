package ast.types;

import token.Token;
import utilities.Vector;
import utilities.Visitor;

/**
 * An internal representation of all the possible types an
 * object variable could be. A {@code MultiType} is only
 * created when a user executes a retype statement, and the
 * type of the object will still not be known until runtime.
 */
public class MultiType extends Type {

    private final ClassType initialType;
    private final Vector<ClassType> allTypes;

    private ClassType runtimeType;      // Used by the interpreter

    public MultiType(ClassType it, Vector<ClassType> ct) {
        super(new Token());
        this.initialType = it;
        this.allTypes = ct;
    }

    public void addType(ClassType ct) {
        if(!allTypes.contains(ct))
            allTypes.add(ct);
    }

    public ClassType getInitialType() { return initialType; }
    public Vector<ClassType> getAllTypes() { return allTypes; }

    public void setRuntimeType(ClassType ct) { this.runtimeType = ct; }
    public ClassType getRuntimeType() { return this.runtimeType; }

    public boolean isMultiType() { return true; }
    public MultiType asMultiType() { return this; }

    public static MultiType create(ClassType base, ClassType sub) {
        return new MultiType(base,new Vector<>(new ClassType[]{base, sub}));
    }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();

        for(int i = 1; i <= allTypes.size(); i++) {
            ClassType ct = allTypes.get(i-1);
            if(i == allTypes.size())
                sb.append(ct);
            else
                sb.append(ct).append("/");
        }
      
        return sb.toString();
    }

    @Override
    public String toString() { return typeName(); }

    @Override
    public void visit(Visitor v) { v.visitMultiType(this); }
}
