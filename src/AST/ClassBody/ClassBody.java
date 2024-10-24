package AST.ClassBody;

import AST.*;
import AST.Vector;
import Token.*;
import Utilities.PokeVisitor;

public class ClassBody extends AST {

    Vector<DataDecl> dataDecls;
    Vector<MethodDecl> methodDecls;

    public ClassBody(Token t, Vector<DataDecl> dd, Vector<MethodDecl> md) {
        super(t);
        this.dataDecls = dd;
        this.methodDecls = md;

        addChild(this.dataDecls);
        addChild(this.methodDecls);
        setParent();
    }

    public Vector<DataDecl> getDataDecls() { return dataDecls; }
    public Vector<MethodDecl> getMethodDecls() { return methodDecls; }

    @Override
    public AST whosThatNode(PokeVisitor v) { return v.itsClassBody(this); }
}
