package ast.misc;

import ast.AST;

/*
___________________________ NameNode ___________________________
This is a simple interface designed to keep track of each node
in the C Minor AST that can be named. Any NameNode will be able
to be inserted within a C Minor symbol table for the purpose of
semantic analysis.

Number of NameNodes: 6

    1. EnumDecl
    2. GlobalDecl
    3. ClassDecl
    4. FuncDecl
    5. LocalDecl
    6. ParamDecl
________________________________________________________________
*/
public interface NameNode { AST decl(); }
