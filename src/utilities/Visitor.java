package utilities;

import ast.classbody.*;
import ast.expressions.*;
import ast.misc.*;
import ast.operators.*;
import ast.statements.*;
import ast.topleveldecls.*;
import ast.types.*;

public abstract class Visitor {

    protected boolean interpretMode;

    public void visitArrayExpr(ArrayExpr ae) { ae.visitChildren(this); }
    public void visitArrayLiteral(ArrayLiteral al) { al.visitChildren(this); }
    public void visitArrayType(ArrayType at) { at.visitChildren(this); }
    public void visitAssignOp(AssignOp ao) { ao.visitChildren(this); }
    public void visitAssignStmt(AssignStmt as) { as.visitChildren(this); }
    public void visitBinaryExpr(BinaryExpr be) { be.visitChildren(this); }
    public void visitBinaryOp(BinaryOp bo) { bo.visitChildren(this); }
    public void visitBlockStmt(BlockStmt bs) { bs.visitChildren(this); }
    public void visitBreakStmt(BreakStmt bs) { bs.visitChildren(this); }
    public void visitCaseStmt(CaseStmt cs) { cs.visitChildren(this); }
    public void visitCastExpr(CastExpr ce) { ce.visitChildren(this); }
    public void visitChoiceLabel(Label cl) { cl.visitChildren(this); }
    public void visitChoiceStmt(ChoiceStmt chs) { chs.visitChildren(this); }
    public void visitClassBody(ClassBody cb) { cb.visitChildren(this); }
    public void visitClassDecl(ClassDecl cd) { cd.visitChildren(this); }
    public void visitClassType(ClassType ct) { ct.visitChildren(this); }
    public void visitCompilation(Compilation c) { c.visitChildren(this); }
    public void visitContinueStmt(ContinueStmt cs) { cs.visitChildren(this); }
    public void visitDiscreteType(DiscreteType dt) { dt.visitChildren(this); }
    public void visitDoStmt(DoStmt ds) { ds.visitChildren(this); }
    public void visitEndl(Endl e) { e.visitChildren(this); }
    public void visitEnumDecl(EnumDecl ed) { ed.visitChildren(this); }
    public void visitEnumType(EnumType et) { et.visitChildren(this); }
    public void visitExprStmt(ExprStmt es) { es.visitChildren(this); }
    public void visitFieldDecl(FieldDecl dd) { dd.visitChildren(this); }
    public void visitFieldExpr(FieldExpr fe) { fe.visitChildren(this); }
    public void visitForStmt(ForStmt fs) { fs.visitChildren(this); }
    public void visitFuncDecl(FuncDecl fd) { fd.visitChildren(this); }
    public void visitGlobalDecl(GlobalDecl gd) { gd.visitChildren(this); }
    public void visitIfStmt(IfStmt is) { is.visitChildren(this); }
    public void visitInitDecl(InitDecl id) { id.visitChildren(this); }
    public void visitInStmt(InStmt ins) { ins.visitChildren(this); }
    public void visitInvocation(Invocation i) { i.visitChildren(this); }
    public void visitListLiteral(ListLiteral ll) { ll.visitChildren(this); }
    public void visitListStmt(ListStmt ls) { ls.visitChildren(this); }
    public void visitListType(ListType lt) { lt.visitChildren(this); }
    public void visitLiteral(Literal l) { l.visitChildren(this); }
    public void visitLocalDecl(LocalDecl ld) { ld.visitChildren(this); }
    public void visitLoopOp(LoopOp lo) { lo.visitChildren(this); }
    public void visitMainDecl(MainDecl md) { md.visitChildren(this); }
    public void visitMethodDecl(MethodDecl med) { med.visitChildren(this); }
    public void visitModifier(Modifier m) { m.visitChildren(this); }
    public void visitMultiType(MultiType mt) { mt.visitChildren(this); }
    public void visitName(Name n) { n.visitChildren(this); }
    public void visitNameExpr(NameExpr ne) { ne.visitChildren(this); }
    public void visitNewExpr(NewExpr nwe) { nwe.visitChildren(this); }
    public void visitOutStmt(OutStmt os) { os.visitChildren(this); }
    public void visitParamDecl(ParamDecl pd) { pd.visitChildren(this); }
    public void visitReturnStmt(ReturnStmt rs) { rs.visitChildren(this); }
    public void visitRetypeStmt(RetypeStmt rs) { rs.visitChildren(this); }
    public void visitScalarType(ScalarType st) { st.visitChildren(this); }
    public void visitStopStmt(StopStmt ss) { ss.visitChildren(this); }
    public void visitThis(This t) { t.visitChildren(this); }
    public void visitTypeifier(Typeifier t) { t.visitChildren(this); }
    public void visitUnaryExpr(UnaryExpr ue) { ue.visitChildren(this); }
    public void visitUnaryOp(UnaryOp uo) { uo.visitChildren(this); }
    public void visitVar(Var v) { v.visitChildren(this); }
    public void visitVoidType(VoidType vt) { vt.visitChildren(this); }
    public void visitWhileStmt(WhileStmt ws) { ws.visitChildren(this); }
}
