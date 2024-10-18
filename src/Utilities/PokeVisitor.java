package Utilities;

import AST.*;
import AST.ClassBody.*;
import AST.Expressions.*;
import AST.Operators.*;
import AST.Statements.*;
import AST.TopLevelDecls.*;
import AST.Types.*;

public abstract class PokeVisitor {

    public AST itsAssignOp(AssignOp ao) { return ao.visitChildren(this); }
    public AST itsAssignStmt(AssignStmt as) { return as.visitChildren(this); }
    public AST itsBinaryExpr(BinaryExpr be) { return be.visitChildren(this); }
    public AST itsBinaryOp(BinaryOp bo) { return bo.visitChildren(this); }
    public AST itsBlockStmt(BlockStmt bs) { return bs.visitChildren(this); }
    public AST itsCaseStmt(CaseStmt cs) { return cs.visitChildren(this); }
    public AST itsCastExpr(CastExpr ce) { return ce.visitChildren(this); }
    public AST itsChoiceLabel(Label cl) { return cl.visitChildren(this); }
    public AST itsChoiceStmt(ChoiceStmt chs) { return chs.visitChildren(this); }
    public AST itsClassBody(ClassBody cb) { return cb.visitChildren(this); }
    public AST itsClassDecl(ClassDecl cd) { return cd.visitChildren(this); }
    public AST itsClassType(ClassType ct) { return ct.visitChildren(this); }
    public AST itsCompilation(Compilation c) { return c.visitChildren(this); }
    public AST itsDataDecl(DataDecl dd) { return dd.visitChildren(this); }
    public AST itsDiscreteType(DiscreteType dt) { return dt.visitChildren(this); }
    public AST itsDoStmt(DoStmt ds) { return ds.visitChildren(this); }
    public AST itsEnumDecl(EnumDecl ed) { return ed.visitChildren(this); }
    public AST itsExprStmt(ExprStmt es) { return es.visitChildren(this); }
    public AST itsFieldExpr(FieldExpr fe) { return fe.visitChildren(this); }
    public AST itsForStmt(ForStmt fs) { return fs.visitChildren(this); }
    public AST itsFuncDecl(FuncDecl fd) { return fd.visitChildren(this); }
    public AST itsGlobalDecl(GlobalDecl gd) { return gd.visitChildren(this); }
    public AST itsIfStmt(IfStmt is) { return is.visitChildren(this); }
    public AST itsInStmt(InStmt ins) { return ins.visitChildren(this); }
    public AST itsInvocation(Invocation i) { return i.visitChildren(this); }
    public AST itsListLiteral(ListLiteral ll) { return ll.visitChildren(this); }
    public AST itsListType(ListType lt) { return lt.visitChildren(this); }
    public AST itsLiteral(Literal l) { return l.visitChildren(this); }
    public AST itsLocalDecl(LocalDecl ld) { return ld.visitChildren(this); }
    public AST itsMainDecl(MainDecl md) { return md.visitChildren(this); }
    public AST itsMethodDecl(MethodDecl med) { return med.visitChildren(this); }
    public AST itsModifier(Modifier m) { return m.visitChildren(this); }
    public AST itsName(Name n) { return n.visitChildren(this); }
    public AST itsNameExpr(NameExpr ne) { return ne.visitChildren(this); }
    public AST itsNewExpr(NewExpr nwe) { return nwe.visitChildren(this); }
    public AST itsOutStmt(OutStmt os) { return os.visitChildren(this); }
    public AST itsParamDecl(ParamDecl pd) { return pd.visitChildren(this); }
    public AST itsReturnStmt(ReturnStmt rs) { return rs.visitChildren(this); }
    public AST itsScalarType(ScalarType st) { return st.visitChildren(this); }
    public AST itsStopStmt(StopStmt ss) { return ss.visitChildren(this); }
    public AST itsUnaryExpr(UnaryExpr ue) { return ue.visitChildren(this); }
    public AST itsUnaryOp(UnaryOp uo) { return uo.visitChildren(this); }
    public AST itsVar(Var v) { return v.visitChildren(this); }
    public AST itsVector(Vector ve) { return ve.visitChildren(this); }
    public AST itsWhileStmt(WhileStmt ws) { return ws.visitChildren(this); }
}
