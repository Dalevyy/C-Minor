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
    private final Vector<ClassType> types;

    private ClassType runtimeType;

    public MultiType(ClassType it, Vector<ClassType> ct) {
        super(new Token());
        this.initialType = it;
        this.types = ct;
    }

    public void addType(ClassType ct) {
        if(!types.contains(ct))
            types.add(ct);
    }

    public ClassType getInitialType() { return initialType; }
    public Vector<ClassType> getAllTypes() { return types; }

    public void setRuntimeType(ClassType ct) { this.runtimeType = ct; }
    public ClassType getRuntimeType() { return this.runtimeType; }

    public boolean isMultiType() { return true; }
    public MultiType asMultiType() { return this; }

    @Override
    public String typeName() {
        StringBuilder sb = new StringBuilder();
        for(ClassType ct : types)
            sb.append(ct.toString());
        return sb.toString();
    }

    @Override
    public String toString() { return typeName(); }

    @Override
    public void visit(Visitor v) { v.visitMultiType(this); }
}
