package micropasses;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.expressions.Invocation;
import ast.expressions.NewExpr;
import ast.misc.Compilation;
import ast.misc.ParamDecl;
import ast.misc.Typeifier;
import ast.statements.LocalDecl;
import ast.topleveldecls.ClassDecl;
import ast.topleveldecls.EnumDecl;
import ast.topleveldecls.FuncDecl;
import ast.topleveldecls.GlobalDecl;
import ast.topleveldecls.ImportDecl;
import ast.types.ClassType;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType;
import ast.types.EnumType.EnumTypeBuilder;
import ast.types.Type;
import messages.MessageType;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeErrorBuilder;
import messages.errors.scope.ScopeErrorFactory;
import messages.errors.type.TypeErrorFactory;
import namechecker.NameChecker;
import utilities.SymbolTable;
import utilities.Vector;
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
public class TypeValidityPass extends Visitor {

    private SymbolTable currentScope;
    private String currentFile = "";

    private FuncDecl currentFunction;
    private ClassDecl currentTemplate;
    private Vector<Type> typeArgs;
    private Vector<Typeifier> currentTypeParams;
    private Vector<String> instantiatedClasses;
    private final ScopeErrorFactory generateScopeError;
    private final TypeErrorFactory generateTypeError;
    private final Vector<String> errors;

    /**
     * Creates type validity micropass in compilation mode.
     */
    public TypeValidityPass() {
        this.currentScope = null;
        this.generateScopeError = new ScopeErrorFactory();
        this.generateTypeError = new TypeErrorFactory();
        this.instantiatedClasses = new Vector<>();
        this.errors = new Vector<>();
    }

    /**
     * Creates type validity micropass in interpretation mode.
     * @param st Compilation Unit Symbol Table
     * @param mode Boolean to mark interpretation mode
     */
    public TypeValidityPass(SymbolTable st, boolean mode) {
        this();
        this.currentScope = st;
        this.interpretMode = mode;
    }

    /**
     * Checks if a variable name corresponds to a type parameter name.
     * <p><br>
     *     Similarly to how C++ handles the scope resolution of type parameters,
     *     C Minor will follow a similar approach and prevent a user from shadowing
     *     the type parameter by using the name in a different construct within a
     *     class or function. This method will check for us whether or not a name
     *     shadows a type parameter, so we can know if we need to generate an error.
     * </p>
     * @param name String representation of a variable name
     * @return Boolean
     */
    private boolean nameShadowsTypeParam(String name) {
        for(Typeifier tp : currentTypeParams)
            if(name.equals(tp.toString()))
                return true;

        return false;
    }

    /**
     * Rewrites a {@code ClassType} into an {@code EnumType} (if applicable).
     * <p>
     *     Since the parser does not distinguish the difference between class
     *     and enum types, we have to do a manual rewrite of any class types that
     *     actually represent an enum type in order to do proper type checking.
     *     This method handles the rewrite for us if it needs to be done.
     * </p>
     * @param t Type we might need to rewrite
     * @return A type representing the original {@code ClassType} or a new {@code EnumType}.
     */
    private Type rewriteClassType(Type t) {
        t.visit(this);

        AST typeDecl = currentScope.findName(t.asClassType().getClassName().toString()).decl();
        if(typeDecl.isTopLevelDecl() && typeDecl.asTopLevelDecl().isEnumDecl())
            return buildEnumType(typeDecl.asTopLevelDecl().asEnumDecl());

        if(t.asClassType().isTemplatedType())
            checkTemplateType(t.asClassType());

        // If we are visiting a newly created template, then we want to replace
        // all type parameters with the correct type arguments
        if(currentTemplate != null && !currentTemplate.typeParams().isEmpty()) {
            for(int i = 0; i < currentTemplate.typeParams().size(); i++) {
                Typeifier param = currentTemplate.typeParams().get(i).asTypeifier();
                if(param.toString().equals(t.asClassType().getClassName().toString())) {
                    t = typeArgs.get(i);
                    break;
                }
            }
        }

        return t;
    }

    /**
     * Builds a new {@code EnumType} node.
     * <p>
     *     This method will create a new type for an enum declaration. This
     *     type is specifically an {@code EnumType} that will contain the
     *     name of the enum followed by the type of data stored in the enum's
     *     constants.
     * </p>
     * @param ed Enum Declaration we are building a type for
     * @return {@code EnumType}
     */
    private EnumType buildEnumType(EnumDecl ed) {
        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder();

        if(ed.type().asEnumType().constantType().isInt())
            enumTypeBuilder.setConstantType(Discretes.INT);
        else
            enumTypeBuilder.setConstantType(Discretes.CHAR);

        enumTypeBuilder.setName(ed.name());
        return enumTypeBuilder.create();
    }

    /**
     * Checks the validity of a templated type.
     * <p>
     *     If a user tries to write a templated type, then this method will
     *     be called. We will be checking if the user correctly wrote the
     *     type based on the provided type parameters from the class. At this
     *     point, we are only caring about whether the type name itself is written
     *     correctly. Other type errors such as assignment compatibility will be
     *     handled by the {@link typechecker.TypeChecker} in the next step.
     * </p>
     * @param ct Class type representing a templated type.
     */
    private void checkTemplateType(ClassType ct) {
        ClassDecl cd = currentScope.findName(ct.getClassNameAsString()).decl().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: This checks if both the class and the class type have the same number of type parameters.
        if(cd.typeParams().size() != ct.typeArgs().size()) {
            // This error message is generated when a user tries to instantiate a non-templated class.
            if(cd.typeParams().isEmpty()) {
                errors.add(
                        new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                                .addLocation(ct.getRootParent())
                                .addErrorType(MessageType.TYPE_ERROR_444)
                                .addArgs(cd)
                                .error()
                );
            }
            // This error message is generated when a user tries to instantiate a templated class.
            else {
                ErrorBuilder eb = new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                        .addLocation(ct.getRootParent())
                        .addErrorType(MessageType.TYPE_ERROR_445)
                        .addArgs(cd, cd.typeParams().size(), ct.typeArgs().size())
                        .addSuggestArgs(cd, cd.typeParams().size());

                if (cd.typeParams().size() == 1)
                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1441).error());
                else
                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1442).error());
            }
        }

        // We now look through each type parameter for the corresponding class.
        for (int i = 0; i < cd.typeParams().size(); i++) {
            Typeifier tp = cd.typeParams().get(i);
            // ERROR CHECK #2: This checks if the correct type was passed as an argument (if applicable).
            if(tp.hasPossibleType() && !tp.isValidType(ct.typeArgs().get(i))) {
                errors.add(
                    new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                        .addLocation(ct.getRootParent())
                        .addErrorType(MessageType.TYPE_ERROR_446)
                        .addArgs(ct.typeArgs().get(i), ct)
                        .addSuggestType(MessageType.TYPE_SUGGEST_1443)
                        .addSuggestArgs(ct, tp.possibleTypeToString(), i + 1)
                        .error()
                );
            }
        }
    }

    private ClassDecl instantiatesTemplate(ClassType ct) {
        if(instantiatedClasses.contains(ct.toString()))
            return currentScope.findName(ct.toString()).decl().asTopLevelDecl().asClassDecl();

        ClassDecl originalClass = currentScope.findName(ct.getClassNameAsString()).decl().asTopLevelDecl().asClassDecl();
        ClassDecl copy = originalClass.deepCopy().asTopLevelDecl().asClassDecl();
        copy.visit(new NameChecker());
        ClassDecl oldTemplate = currentTemplate;
        Vector<Type> oldArgs = typeArgs;
        SymbolTable oldScope = currentScope;

        currentTemplate = copy;
        typeArgs = ct.typeArgs();

        copy.visit(this);

        currentTemplate = oldTemplate;
        typeArgs = oldArgs;
        currentScope = oldScope;
      //  copy.removeTypeParams();

        instantiatedClasses.add(ct.toString());
        currentScope.addNameToRootTable(ct.toString(),copy);

        return copy;
    }

    /**
     * Sets the current scope to be inside of a class.
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;

        if(cd.isTemplate())
            currentTypeParams = cd.typeParams();

        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
        currentTypeParams = null;
    }

    /**
     * Checks the validity of a class type.
     * <p>
     *     Since a user can write anything as a type, this is where we will
     *     check if what the user wrote represents a valid type in their program.
     * </p>
     * @param ct Class Type
     */
    public void visitClassType(ClassType ct) {
        // ERROR CHECK #1: This checks if the type written was actually declared in the program.
        if(!currentScope.hasNameSomewhere(ct.getClassName().toString())) {
            errors.add(
                new ErrorBuilder(generateTypeError,currentFile,interpretMode)
                    .addLocation(ct)
                    .addErrorType(MessageType.TYPE_ERROR_443)
                    .addArgs(ct)
                    .addSuggestType(MessageType.TYPE_SUGGEST_1440)
                    .addSuggestArgs(ct)
                    .error()
            );
        }
    }

    /**
     * Begins the C Minor type validity micropass.
     * <p>
     *     During compilation mode, {@code visitCompilation} will be the first
     *     method executed when we start the type validity micropass.
     * </p>
     * @param c Compilation Unit
     */
    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;
        currentFile = c.getFile();
        super.visitCompilation(c);
    }

    /**
     * Validates the type of a field declaration.
     * @param fd Field Declaration
     */
    public void visitFieldDecl(FieldDecl fd) {
        if(fd.type().isClassType())
            fd.setType(rewriteClassType(fd.type()));

        if(fd.var().init() != null)
            fd.var().init().visit(this);
    }

    public void visitFuncDecl(FuncDecl fd) {
        currentFunction = fd;
        super.visitFuncDecl(fd);
    }

    /**
     * Validates the type of a global declaration.
     * @param gd Global Declaration
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isClassType())
            gd.setType(rewriteClassType(gd.type()));

        if(gd.var().init() != null)
            gd.var().init().visit(this);
    }

    /**
     * Checks the type validity of an imported file.
     * <p>
     *     If a user has any imported files, we will have to make sure
     *     all the types in that file are valid before we work on the
     *     main file of the program.
     * </p>
     * @param im Import Declaration
     */
    public void visitImportDecl(ImportDecl im) {
        SymbolTable oldScope = currentScope;
        String oldFile = currentFile;

        im.getCompilationUnit().visit(this);

        currentScope = oldScope;
        currentFile = oldFile;
    }

    public void visitInvocation(Invocation in) {
//        if(in.invokesTemplateFunction()) {
//            FuncDecl templateDecl = currentScope.findName(in.toString()).decl().asTopLevelDecl().asFuncDecl();
//
//            if(in.templatedTypes().size() != templateDecl.typeParams().size()) {
//                errors.add(
//                    new ErrorBuilder(generateTypeError,currentFile,interpretMode)
//                        .error()
//                );
//            }
//
//
//            for(Typeifier tp : templateDecl.typeParams()) {
//
//            }
//        }
    }

    /**
     * Validates the type of a local declaration.
     * @param ld Local Declaration
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: This checks if the local variable's name shadows a declared type parameter.
        if(currentTypeParams != null && nameShadowsTypeParam(ld.toString())) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(ld)
                    .addErrorType(MessageType.SCOPE_ERROR_328)
                    .addArgs(ld)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(ld).decl())
                    .error()
            );
        }

        if(ld.type().isClassType())
            ld.setType(rewriteClassType(ld.type()));
        if(ld.var().init() != null)
            ld.var().init().visit(this);
    }

    /**
     * Validates the type of a new expression.
     * <p>
     *     If a user tries to instantiate a templated class, then we
     *     will handle the error checking by calling {@link #checkTemplateType(ClassType)}.
     *     If there were no errors, we will then proceed to create a new instance
     *     of the class with the passed type parameters. From there, the rest of the
     *     type error checking will be done in the {@link typechecker.TypeChecker}.
     * </p>
     * @param ne New Expression
     */
    public void visitNewExpr(NewExpr ne) {
        if(ne.createsFromTemplate()) {
            checkTemplateType(ne.getClassType());
            ne.templatedClass = instantiatesTemplate(ne.getClassType());
        }
    }

    /**
     * Validates the type of a parameter.
     * @param pd Parameter Declaration.
     */
    public void visitParamDecl(ParamDecl pd) {
        if(pd.type().isClassType())
            pd.setType(rewriteClassType(pd.type()));
    }
}
