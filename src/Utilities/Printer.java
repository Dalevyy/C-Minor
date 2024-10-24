package Utilities;

import AST.*;
import AST.ClassBody.*;
import AST.Expressions.*;
import AST.Operators.*;
import AST.Statements.*;
import AST.TopLevelDecls.*;
import AST.Types.*;

public class Printer extends PokeVisitor {

    private int spaces;

    public Printer() {
        System.out.println("\n");
        spaces = 0;
    }

    private String printSpaces() {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < spaces; i++) {
            s.append(" ");
            if(i%2==1)
                s.append("|");
        }
        s.append("  +--");
        return s.toString();
    }

    private String debugInfo(AST n) {
        return " <"+ n.location.start.toString() + " to " + n.location.end.toString() + "> : \'" + n.text + "\'";
    }

    private String debugLines(AST n) { return " <"+ n.location.start.toString() + " to " + n.location.end.toString() + ">"; }

    public AST itsAssignOp(AssignOp ao) {
        System.out.println(printSpaces() + "AssignOp" + debugInfo(ao));
        super.itsAssignOp(ao);
        return null;
    }

    public AST itsBinaryExpr(BinaryExpr be) {
        System.out.println(printSpaces() + "BinaryExpr" + debugInfo(be));
        spaces += 2;
        super.itsBinaryExpr(be);
        spaces -= 2;
        return null;
    }

    public AST itsBinaryOp(BinaryOp bo) {
        System.out.println(printSpaces() + "BinaryOp" + debugInfo(bo));
        super.itsBinaryOp(bo);
        return null;
    }

    public AST itsBlockStmt(BlockStmt bs) {
        System.out.println(printSpaces() + "BlockStmt" + debugInfo(bs));
        spaces += 2;
        super.itsBlockStmt(bs);
        spaces -= 2;
        return null;
    }

    public AST itsCaseStmt(CaseStmt cs) {
        System.out.println(printSpaces() + "CaseStmt");
        spaces += 2;
        super.itsCaseStmt(cs);
        spaces -= 2;
        return null;
    }

    public AST itsCastExpr(CastExpr ce) {
        System.out.println(printSpaces() + "CastExpr" + debugInfo(ce));
        spaces += 2;
        super.itsCastExpr(ce);
        spaces -= 2;
        return null;
    }

    public AST itsChoiceLabel(Label cl) {
        System.out.println(printSpaces() + "Label");
        super.itsChoiceLabel(cl);
        return null;
    }

    public AST itsChoiceStmt(ChoiceStmt cs) {
        System.out.println(printSpaces() + "ChoiceStmt");
        spaces += 2;
        super.itsChoiceStmt(cs);
        spaces -= 2;
        return null;
    }

    public AST itsClassDecl(ClassDecl cd) {
        System.out.println(printSpaces() + "ClassDecl");
        spaces += 2;
        super.itsClassDecl(cd);
        spaces -= 2;
        return null;
    }

    public AST itsClassType(ClassType ct) {
        System.out.println(printSpaces() + "ClassType");
        super.itsClassType(ct);
        return null;
    }

    public AST itsCompilation(Compilation c) {
        System.out.println("Compilation" + debugLines(c));
        spaces += 2;
        super.itsCompilation(c);
        spaces -= 2;
        return null;
    }

    public AST itsDataDecl(DataDecl fd) {
        System.out.println(printSpaces() + "FieldDecl");
        spaces += 2;
        super.itsDataDecl(fd);
        spaces -= 2;
        return null;
    }

    public AST itsDiscreteType(DiscreteType dt) {
        System.out.println(printSpaces() + "DiscreteType" + debugInfo(dt));
        return null;
    }

    public AST itsDoStmt(DoStmt ds) {
        System.out.println(printSpaces() + "DoStmt");
        spaces += 2;
        super.itsDoStmt(ds);
        spaces -= 2;
        return null;
    }

    public AST itsEnumDecl(EnumDecl ed) {
        System.out.println(printSpaces() + "EnumDecl" + debugInfo(ed));
        spaces += 2;
        super.itsEnumDecl(ed);
        spaces -= 2;
        return null;
    }

    public AST itsExprStmt(ExprStmt es) {
        //System.out.println(printSpaces() + "| ExprStmt");
       // spaces += 2;
        super.itsExprStmt(es);
       // spaces -= 2;
        return null;
    }

    public AST itsFieldExpr(FieldExpr fe) {
        System.out.println(printSpaces() + "FieldExpr");
        spaces += 2;
        super.itsFieldExpr(fe);
        spaces -= 2;
        return null;
    }

    public AST itsForStmt(ForStmt fs) {
        System.out.println(printSpaces() + "ForStmt");
        spaces += 2;
        super.itsForStmt(fs);
        spaces -= 2;
        return null;
    }

    public AST itsFuncDecl(FuncDecl fd) {
        System.out.println(printSpaces() + "FuncDecl");
        spaces += 2;
        super.itsFuncDecl(fd);
        spaces -= 2;
        return null;
    }

    public AST itsGlobalDecl(GlobalDecl gd) {
        System.out.println(printSpaces() + "GlobalDecl");
        spaces += 2;
        super.itsGlobalDecl(gd);
        spaces -= 2;
        return null;
    }

    public AST itsIfStmt(IfStmt is) {
        System.out.println(printSpaces() + "IfStmt");
        spaces += 2;
        super.itsIfStmt(is);
        spaces -= 2;
        return null;
    }

    public AST itsInStmt(InStmt ins) {
        System.out.println(printSpaces() + "InStmt");
        spaces += 2;
        super.itsInStmt(ins);
        spaces -= 2;
        return null;
    }

    public AST itsInvocation(Invocation i) {
        System.out.println(printSpaces() + "Invocation");
        spaces += 2;
        super.itsInvocation(i);
        spaces -= 2;
        return null;
    }

    public AST itsListLiteral(ListLiteral lc) {
        System.out.println(printSpaces() + "ListLiteral");
        return null;
    }

    public AST itsListType(ListType lt) {
        System.out.println(printSpaces() + "ListType");
        return null;
    }

    public AST itsLiteral(Literal l) {
        System.out.println(printSpaces() + "Literal" + debugInfo(l));
        return null;
    }

    public AST itsLocalDecl(LocalDecl ld) {
        System.out.println(printSpaces() + "LocalDecl");
        spaces += 2;
        super.itsLocalDecl(ld);
        spaces -= 2;
        return null;
    }

    public AST itsMainDecl(MainDecl md) {
        System.out.println(printSpaces() + "Main" + debugInfo(md));
        spaces += 2;
        super.itsMainDecl(md);
        spaces -= 2;
        return null;
    }

    public AST itsName(Name n) {
        System.out.println(printSpaces() + "Name" + debugInfo(n));
        return null;
    }

    public AST itsNameExpr(NameExpr ne) {
        System.out.println(printSpaces() + "NameExpr" + debugInfo(ne));
        spaces += 2;
        super.itsNameExpr(ne);
        spaces -= 2;
        return null;
    }

    public AST itsNewExpr(NewExpr nwe) {
        System.out.println(printSpaces() + "NewExpr");
        spaces += 2;
        super.itsNewExpr(nwe);
        spaces -= 2;
        return null;
    }

    public AST itsOutStmt(OutStmt os) {
        System.out.println(printSpaces() + "OutStmt" + debugInfo(os));
        spaces += 2;
        super.itsOutStmt(os);
        spaces -= 2;
        return null;
    }

    public AST itsParamDecl(ParamDecl pd) {
        System.out.println(printSpaces() + "ParamDecl");
        spaces += 2;
        super.itsParamDecl(pd);
        spaces -= 2;
        return null;
    }

    public AST itsReturnStmt(ReturnStmt rs) {
        System.out.println(printSpaces() + "ReturnStmt");
        spaces += 2;
        super.itsReturnStmt(rs);
        spaces -= 2;
        return null;
    }

    public AST itsStopStmt(StopStmt ss) {
        System.out.println(printSpaces() + "StopStmt" + debugInfo(ss));
        return null;
    }

    public AST itsScalarType(ScalarType st) {
        System.out.println(printSpaces() + "ScalarType" + debugInfo(st));
        return null;
    }

    public AST itsUnaryExpr(UnaryExpr ue) {
        System.out.println(printSpaces() + "UnaryExpr");
        spaces += 2;
        super.itsUnaryExpr(ue);
        spaces -= 2;
        return null;
    }

    public AST itsVar(Var v) {
        System.out.println(printSpaces() + "Var");
        spaces += 2;
        super.itsVar(v);
        spaces -= 2;
        return null;
    }

    public AST itsVector(Vector s) {
        if(s.children.size() != 0)
            super.itsVector(s);
        return null;
    }

    public AST itsWhileStmt(WhileStmt ws) {
        System.out.println(printSpaces() + "WhileStmt");
        spaces += 2;
        super.itsWhileStmt(ws);
        spaces -= 2;
        return null;
    }
}
