package ast.top_level_decls;

import ast.*;
import token.*;
import utilities.PrettyPrint;

public abstract class TopLevelDecl extends AST {
    public TopLevelDecl() { super(); }
    public TopLevelDecl(Token t) { super(t); }
    public TopLevelDecl(AST node) { super(node); }

    public boolean isTopLevelDecl() { return true; }
    public TopLevelDecl asTopLevelDecl() { return this; }

    public boolean isClassDecl() { return false; }
    public ClassDecl asClassDecl() { throw new RuntimeException("Expression can not be casted into a ClassDecl.\n"); }

    public boolean isEnumDecl() { return false; }
    public EnumDecl asEnumDecl() { throw new RuntimeException("Expression can not be casted into an EnumDecl.\n"); }

    public boolean isFuncDecl() { return false; }
    public FuncDecl asFuncDecl() { throw new RuntimeException("Expression can not be casted into a FuncDecl.\n"); }

    public boolean isGlobalDecl() { return false; }
    public GlobalDecl asGlobalDecl() { throw new RuntimeException("Expression can not be casted into a GlobalDecl.\n"); }

    public boolean isMainDecl() { return false; }
    public MainDecl asMainDecl() { throw new RuntimeException("Expression can not be casted into a MainDecl.\n"); }
}
