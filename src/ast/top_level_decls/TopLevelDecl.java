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
    public ClassDecl asClassDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a ClassDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isEnumDecl() { return false; }
    public EnumDecl asEnumDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into an EnumDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isFuncDecl() { return false; }
    public FuncDecl asFuncDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a FuncDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isGlobalDecl() { return false; }
    public GlobalDecl asGlobalDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a GlobalDecl.\n");
        System.exit(1);
        return null;
    }

    public boolean isMainDecl() { return false; }
    public MainDecl asMainDecl() {
        System.out.println(PrettyPrint.RED + "Error! Expression can not be casted into a MainDecl.\n");
        System.exit(1);
        return null;
    }
}
