package micropasses;

import ast.class_body.FieldDecl;
import ast.statements.LocalDecl;
import ast.top_level_decls.GlobalDecl;
import ast.top_level_decls.TopLevelDecl;
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
                if(customType.asEnumDecl().constantType().isInt()) {
                    fd.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isClassType()) {
            TopLevelDecl customType = currentScope.findName(gd.type().typeName()).decl().asTopLevelDecl();
            if(customType.isEnumDecl()) {
                if(customType.asEnumDecl().constantType().isInt()) {
                    gd.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(ld.type().isClassType()) {
            TopLevelDecl customType = currentScope.findName(ld.type().typeName()).decl().asTopLevelDecl();
            if(customType.isEnumDecl()) {
                if(customType.asEnumDecl().constantType().isInt()) {
                    ld.setType(new EnumType(customType.toString(), DiscreteType.Discretes.INT));
                }
            }
        }
    }
}
