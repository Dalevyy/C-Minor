package micropasses;

import ast.classbody.FieldDecl;
import ast.statements.ForStmt;
import ast.statements.LocalDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.TopLevelDecl;
import ast.types.DiscreteType;
import ast.types.EnumType;
import utilities.SymbolTable;
import utilities.Visitor;

public class ClassToEnumTypeRewrite extends Visitor {

    private SymbolTable currentScope;

    public ClassToEnumTypeRewrite(SymbolTable st) { this.currentScope = st; }

    public void visitFieldDecl(FieldDecl fd) {
        if(fd.type().isClassType()) {
            TopLevelDecl customType = currentScope.findName(fd.type().typeName()).decl().asTopLevelDecl();
            if(customType.isEnumDecl()) {
                if(customType.asEnumDecl().constantType().asEnumType().constantType().isInt()
                        || customType.asEnumDecl().constantType() == null) {
                    fd.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isClassType()) {
            TopLevelDecl customType = currentScope.findName(gd.type().typeName()).decl().asTopLevelDecl();
            if(customType.isEnumDecl()) {
                if(customType.asEnumDecl().constantType().asEnumType().constantType().isInt()
                        || customType.asEnumDecl().constantType() == null) {
                    gd.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(ld.type().isClassType()) {
            TopLevelDecl customType = currentScope.findName(ld.type().typeName()).decl().asTopLevelDecl();
            if(customType.isEnumDecl()) {
                if(customType.asEnumDecl().constantType().asEnumType().constantType().isInt()
                        || customType.asEnumDecl().constantType() == null) {
                    ld.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }
}
