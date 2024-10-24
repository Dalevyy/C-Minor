package AST;

import AST.Types.*;

public interface VarDecl {
    public Type getType();
    public String toString();

    public boolean isClassType();
    public boolean isConstant();
}
