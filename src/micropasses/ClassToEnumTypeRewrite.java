package micropasses;

import ast.classbody.FieldDecl;
import ast.misc.Compilation;
import ast.misc.ParamDecl;
import ast.statements.LocalDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.TopLevelDecl;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType;
import ast.types.EnumType.EnumTypeBuilder;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Micropass #5
 * <br><br>
 * The parser generates a <code>ClassType</code> node when a name is used for a <i>type</i> regardless
 * if the name represents a <i>Class</i> or <i>Enum</i>. This means we have to do a pass to change all
 * <code>ClassType</code> nodes to be <code>EnumType</code> nodes if the name represents an <i>Enum</i>.
 * This pass needs to be completed before typechecking or else we can't properly run the assignment
 * compatibility method.
 * <br><br>
 * The following is a list of declarations this micropass will run on.
 * <ol>
 *     <li><code>FieldDecl</code></li>
 *     <li><code>GlobalDecl</code></li>
 *     <li><code>LocalDecl</code></li>
 * </ol>
 * @author Daniel Levy
 */
public class ClassToEnumTypeRewrite extends Visitor {

    private SymbolTable currentScope;

    public ClassToEnumTypeRewrite() { currentScope = null; }
    public ClassToEnumTypeRewrite(SymbolTable st) { this.currentScope = st; }

    private EnumType buildEnumType(EnumDecl ed) {
        EnumTypeBuilder typeBuilder = new EnumTypeBuilder();
        if(ed.type().asEnumType().constantType().isInt()) { typeBuilder.setConstantType(Discretes.INT); }
        else { typeBuilder.setConstantType(Discretes.CHAR); }

        return typeBuilder
                .setName(ed.toString())
                .createEnumType();
    }

    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;
        super.visitCompilation(c);
    }

    public void visitFieldDecl(FieldDecl fd) {
        if(fd.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(fd.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { fd.setType(buildEnumType(decl.asEnumDecl())); }
        }
    }

    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(gd.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { gd.setType(buildEnumType(decl.asEnumDecl()));}
        }
    }

    public void visitLocalDecl(LocalDecl ld) {
        if(ld.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(ld.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { ld.setType(buildEnumType(decl.asEnumDecl())); }
        }
    }

    public void visitParamDecl(ParamDecl pd) {
        if(pd.type().isClassType()) {
            TopLevelDecl decl = currentScope.findName(pd.type().typeName()).decl().asTopLevelDecl();
            if(decl.isEnumDecl()) { pd.setType(buildEnumType(decl.asEnumDecl())); }
        }
    }
}
