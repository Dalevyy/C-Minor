package utilities;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.operators.*;
import ast.statements.*;
import ast.top_level_decls.*;
import ast.types.*;

public class Printer extends Visitor {

    private int spaces;

    public Printer() {
        System.out.println();
        spaces = 0;
    }

    private String printSpaces() {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < spaces; i++) {
            if(i%2==0)
                s.append("|");
            s.append(" ");
        }
        s.append("+--");
        return s.toString();
    }

    private String debugInfo(AST n) {
        return debugLines(n) + " : " + n.text;
    }

    private String debugLines(AST n) { return " <"+ n.location.start.toString() + " to " + n.location.end.toString() + ">"; }

    public void visitArrayExpr(ArrayExpr ae) {
        System.out.println(printSpaces() + "ArrayExpr" + debugInfo(ae));
        spaces += 2;
        super.visitArrayExpr(ae);
        spaces -= 2;
    }

    public void visitArrayLiteral(ArrayLiteral al) {
        System.out.println(printSpaces() + "ArrayLiteral" + debugLines(al));
        spaces += 2;
        super.visitArrayLiteral(al);
        spaces -= 2;
    }

    public void visitArrayType(ArrayType at) {
        System.out.println(printSpaces() + "ArrayType" + debugInfo(at));
        spaces += 2;
        super.visitArrayType(at);
        spaces -= 2;
    }

    public void visitAssignOp(AssignOp ao) {
        System.out.println(printSpaces() + "AssignOp" + debugInfo(ao));
        super.visitAssignOp(ao);
    }

    public void visitAssignStmt(AssignStmt as) {
        System.out.println(printSpaces() + "AssignStmt" + debugInfo(as));
        spaces += 2;
        super.visitAssignStmt(as);
        spaces -= 2;
    }

    public void visitBinaryExpr(BinaryExpr be) {
        System.out.println(printSpaces() + "BinaryExpr" + debugInfo(be));
        spaces += 2;
        super.visitBinaryExpr(be);
        spaces -= 2;
    }

    public void visitBinaryOp(BinaryOp bo) {
        System.out.println(printSpaces() + "BinaryOp" + debugInfo(bo));
        super.visitBinaryOp(bo);
    }

    public void visitBlockStmt(BlockStmt bs) {
        System.out.println(printSpaces() + "BlockStmt" + debugLines(bs));
        spaces += 2;
        super.visitBlockStmt(bs);
        spaces -= 2;
    }

    public void visitCaseStmt(CaseStmt cs) {
        System.out.println(printSpaces() + "CaseStmt");
        spaces += 2;
        super.visitCaseStmt(cs);
        spaces -= 2;
    }

    public void visitCastExpr(CastExpr ce) {
        System.out.println(printSpaces() + "CastExpr" + debugInfo(ce));
        spaces += 2;
        super.visitCastExpr(ce);
        spaces -= 2;
    }

    public void visitChoiceLabel(Label cl) {
        System.out.println(printSpaces() + "Label");
        super.visitChoiceLabel(cl);
    }

    public void visitChoiceStmt(ChoiceStmt cs) {
        System.out.println(printSpaces() + "ChoiceStmt");
        spaces += 2;
        super.visitChoiceStmt(cs);
        spaces -= 2;
    }

    public void visitClassDecl(ClassDecl cd) {
        System.out.println(printSpaces() + "ClassDecl" + debugLines(cd));
        spaces += 2;
        super.visitClassDecl(cd);
        spaces -= 2;
    }

    public void visitClassType(ClassType ct) {
        System.out.println(printSpaces() + "ClassType");
        super.visitClassType(ct);
    }

    public void visitCompilation(Compilation c) {
        System.out.println("+--Compilation" + debugLines(c));
        spaces += 2;
        super.visitCompilation(c);
        spaces -= 2;
        System.out.println();
    }

    public void visitDiscreteType(DiscreteType dt) {
        System.out.println(printSpaces() + "DiscreteType" + debugInfo(dt));
    }

    public void visitDoStmt(DoStmt ds) {
        System.out.println(printSpaces() + "DoStmt");
        spaces += 2;
        super.visitDoStmt(ds);
        spaces -= 2;
    }

    public void visitEnumDecl(EnumDecl ed) {
        System.out.println(printSpaces() + "EnumDecl" + debugLines(ed));
        spaces += 2;
        super.visitEnumDecl(ed);
        spaces -= 2;
    }

    public void visitExprStmt(ExprStmt es) {
        super.visitExprStmt(es);
    }

    public void visitFieldDecl(FieldDecl fd) {
        System.out.println(printSpaces() + "FieldDecl" + debugInfo(fd));
        spaces += 2;
        super.visitFieldDecl(fd);
        spaces -= 2;
    }

    public void visitFieldExpr(FieldExpr fe) {
        System.out.println(printSpaces() + "FieldExpr");
        spaces += 2;
        super.visitFieldExpr(fe);
        spaces -= 2;
    }

    public void visitForStmt(ForStmt fs) {
        System.out.println(printSpaces() + "ForStmt");
        spaces += 2;
        super.visitForStmt(fs);
        spaces -= 2;
    }

    public void visitFuncDecl(FuncDecl fd) {
        System.out.println(printSpaces() + "FuncDecl" + debugInfo(fd));
        spaces += 2;
        super.visitFuncDecl(fd);
        spaces -= 2;
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        System.out.println(printSpaces() + "GlobalDecl" + debugInfo(gd));
        spaces += 2;
        super.visitGlobalDecl(gd);
        spaces -= 2;
    }

    public void visitIfStmt(IfStmt is) {
        System.out.println(printSpaces() + "IfStmt" + debugLines(is));
        spaces += 2;
        super.visitIfStmt(is);
        spaces -= 2;
    }

    public void visitInStmt(InStmt ins) {
        System.out.println(printSpaces() + "InStmt");
        spaces += 2;
        super.visitInStmt(ins);
        spaces -= 2;
    }

    public void visitInvocation(Invocation i) {
        System.out.println(printSpaces() + "Invocation");
        spaces += 2;
        super.visitInvocation(i);
        spaces -= 2;
    }

    public void visitListLiteral(ListLiteral lc) {
        System.out.println(printSpaces() + "ListLiteral");
    }

    public void visitListType(ListType lt) {
        System.out.println(printSpaces() + "ListType");
    }

    public void visitLiteral(Literal l) {
        System.out.println(printSpaces() + "Literal" + debugInfo(l));
    }

    public void visitLocalDecl(LocalDecl ld) {
        System.out.println(printSpaces() + "LocalDecl" + debugLines(ld));
        spaces += 2;
        super.visitLocalDecl(ld);
        spaces -= 2;
    }

    public void visitMainDecl(MainDecl md) {
        System.out.println(printSpaces() + "Main" + debugLines(md));
        spaces += 2;
        super.visitMainDecl(md);
        spaces -= 2;
    }

    public void visitMethodDecl(MethodDecl md) {
        System.out.println(printSpaces() + "MethodDecl" + debugInfo(md));
        spaces += 2;
        super.visitMethodDecl(md);
        spaces -= 2;
    }

    public void visitName(Name n) {
        System.out.println(printSpaces() + "Name" + debugInfo(n));
    }

    public void visitNameExpr(NameExpr ne) {
        System.out.println(printSpaces() + "NameExpr" + debugInfo(ne));
        spaces += 2;
        super.visitNameExpr(ne);
        spaces -= 2;
    }

    public void visitNewExpr(NewExpr nwe) {
        System.out.println(printSpaces() + "NewExpr");
        spaces += 2;
        super.visitNewExpr(nwe);
        spaces -= 2;
    }

    public void visitOutStmt(OutStmt os) {
        System.out.println(printSpaces() + "OutStmt" + debugInfo(os));
        spaces += 2;
        super.visitOutStmt(os);
        spaces -= 2;
    }

    public void visitParamDecl(ParamDecl pd) {
        System.out.println(printSpaces() + "ParamDecl");
        spaces += 2;
        super.visitParamDecl(pd);
        spaces -= 2;
    }

    public void visitReturnStmt(ReturnStmt rs) {
        System.out.println(printSpaces() + "ReturnStmt");
        spaces += 2;
        super.visitReturnStmt(rs);
        spaces -= 2;
    }

    public void visitStopStmt(StopStmt ss) {
        System.out.println(printSpaces() + "StopStmt" + debugInfo(ss));
    }

    public void visitScalarType(ScalarType st) {
        System.out.println(printSpaces() + "ScalarType" + debugInfo(st));
    }

    public void visitTypeifier(Typeifier t) {
        System.out.println(printSpaces() + "Typeifier" + debugInfo(t));
    }

    public void visitUnaryExpr(UnaryExpr ue) {
        System.out.println(printSpaces() + "UnaryExpr");
        spaces += 2;
        super.visitUnaryExpr(ue);
        spaces -= 2;
    }

    public void visitVar(Var v) {
        System.out.println(printSpaces() + "Var");
        spaces += 2;
        super.visitVar(v);
        spaces -= 2;
    }

    public void visitVector(Vector s) {
        if(s.children.size() > 0)
            super.visitVector(s);
    }

    public void visitWhileStmt(WhileStmt ws) {
        System.out.println(printSpaces() + "WhileStmt");
        spaces += 2;
        super.visitWhileStmt(ws);
        spaces -= 2;
    }
}
