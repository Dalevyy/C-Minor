package micropasses;

import ast.classbody.FieldDecl;
import ast.statements.LocalDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.TopLevelDecl;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType;
import utilities.SymbolTable;
import utilities.Visitor;

public class ClassToEnumTypeRewrite extends Visitor {

    private final SymbolTable currentScope;

    public ClassToEnumTypeRewrite(SymbolTable st) { this.currentScope = st; }

    private Discretes findConstantType(EnumDecl ed) {
        if(ed.type().asEnumType().constantType().isInt()) { return Discretes.INT; }
        else if(ed.type().asEnumType().constantType().isChar()) { return Discretes.CHAR; }
        else {
            if(ed.constants().get(0).type().isInt()) { return Discretes.INT; }
            else { return Discretes.CHAR; }
        }
    }

    public void visitFieldDecl(FieldDecl fd) {
        if(fd.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(fd.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { fd.setType(new EnumType(decl.toString(),findConstantType(decl.asEnumDecl()))); }
        }
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(gd.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { gd.setType(new EnumType(decl.toString(),findConstantType(decl.asEnumDecl()))); }
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(ld.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(ld.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { ld.setType(new EnumType(decl.toString(),findConstantType(decl.asEnumDecl()))); }
        }
    }
}
