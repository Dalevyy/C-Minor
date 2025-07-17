package micropasses;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
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

public class TypeValidityPass extends Visitor {

    private SymbolTable currentScope;

    private Vector<Typeifier> currentTypeParams;
    private Vector<Type> currentTypeArgs;

    private final Vector<String> instantiatedClasses;
    private final Vector<String> instantiatedFunctions;
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
        this.instantiatedFunctions = new Vector<>();
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

    /* ######################################## HELPERS ######################################## */

    /**
     * Creates a new {@link EnumType} node.
     * <p><br>
     *     This method creates an {@code EnumType} to represent an {@link EnumDecl}.
     *     An {@code EnumType} will contain the name of the enum followed by the type
     *     of the constant values the enum will represent.
     * </p>
     * @param metaData {@link ClassType} we are converting into an {@link EnumType}
     * @param ed {@link EnumDecl} we are building the {@code EnumType} around
     * @return Rewritten type now representing an {@link EnumType}
     */
    private EnumType buildEnumType(ClassType metaData, EnumDecl ed) {
        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder();

        if(ed.type().asEnumType().constantType().isInt())
            enumTypeBuilder.setConstantType(Discretes.INT);
        else
            enumTypeBuilder.setConstantType(Discretes.CHAR);

        enumTypeBuilder.setName(ed.name());
        enumTypeBuilder.setMetaData(metaData);
        return enumTypeBuilder.create();
    }

    /**
     * Checks if a variable name corresponds to a type parameter name.
     * <p><br>
     *     Similarly to how C++ handles the scope resolution of type parameters,
     *     C Minor will follow a similar approach and prevent a user from shadowing
     *     any type parameter by using the parameter's name in a different construct
     *     within a templated class or function. This method will check for us whether
     *     or not a name shadows a type parameter, so we can know if we need to
     *     generate an error message to the user.
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
     * Performs a potential rewrite on a given structured type.
     * <p>
     *     A structured type in C Minor will represent either an Array, List, or Class type.
     *     The goal of this method is to figure out what potential rewrite needs to be done
     *     for the given type. There are 2 possible scenarios:
     *     <ol>
     *         <li>
     *             Class Types
     *         <p>
     *             By default, any name used as a type in C Minor will automatically be parsed
     *             as a {@link ClassType}. This is not ideal as a type name can represent either
     *             a {@link ClassDecl}, an {@link EnumDecl}, or a type parameter. Thus, we need
     *             to make sure this type is rewritten if it does not correspond to a {@link ClassDecl}
     *             which is the job of {@link #rewriteClassType(Type)}.
     *         </p>
     *
     *         </li>
     *         <li>Array or List Types
     *         <p>
     *             When we have an Array or List type, the base type can represent a {@link ClassType}.
     *             Thus, the previous criteria above will apply, and this means we need to check if
     *             we need to perform a rewrite on the base type through {@link #rewriteClassType(Type)}.
     *         </p>
     *         </li>
     *     </ol>
     * </p>
     * @param structuredType {@link Type} we might need to perform a rewrite of
     * @return {@link Type} => This will return any type.
     */
    private Type rewriteStructuredType(Type structuredType) {
        if(structuredType.isArrayType()) {
            if(structuredType.asArrayType().baseType().isClassType()) {
                Type newBaseType = rewriteClassType(structuredType.asArrayType().baseType().asClassType());
                structuredType.asArrayType().setBaseType(newBaseType);
            }
            return structuredType;
        }

        if(structuredType.isList()) {
            if(structuredType.asListType().baseType().isClassType()) {
                Type newBaseType = rewriteClassType(structuredType.asListType().baseType().asClassType());
                structuredType.asListType().setBaseType(newBaseType);
            }
            return structuredType;
        }

        return rewriteClassType(structuredType.asClassType());
    }

    /**
     * Rewrites a {@code ClassType} into an {@code EnumType} (if applicable).
     * <p>
     *     Since the parser does not distinguish the difference between class
     *     and enum types, we have to do a manual rewrite of any class types that
     *     actually represent an enum type in order to do proper type checking.
     *     This method handles the rewrite for us if it needs to be done.
     * </p>
     * @param ct Type we might need to rewrite
     * @return A type representing the original {@code ClassType} or a new {@code EnumType}.
     */
    private Type rewriteClassType(ClassType ct) {
        // ERROR CHECK #1: The name checker did not resolve the names of class types. This means we will
        //                 now check if the current class type can resolve to either a class, an enum, or
        //                 a type parameter. If no resolution can be made, we will print out a type error.
        if(!currentScope.hasNameSomewhere(ct.getClassName().toString())) {
            errors.add(
                new ErrorBuilder(generateTypeError,currentFile,interpretMode)
                    .addLocation(ct.getRootParent())
                    .addErrorType(MessageType.TYPE_ERROR_443)
                    .addArgs(ct)
                    .addSuggestType(MessageType.TYPE_SUGGEST_1440)
                    .addSuggestArgs(ct)
                    .error()
            );
        }

        // Special Case: If we are instantiating a template class, we want to replace every type parameter
        //               with the appropriate type argument. Thus, we will look through the template's type
        //               parameters and find which one corresponds to the current template type. From there,
        //               we replace the template type with the passed type argument.
        if(currentTypeArgs != null) {
            for(int i = 0; i < currentTypeParams.size(); i++) {
                Typeifier typeParam = currentTypeParams.get(i);
                if(typeParam.equals(ct.toString()))
                    return Type.instantiateType(ct, currentTypeArgs.get(i));
            }
        }

        AST classTypeDecl = currentScope.findName(ct.getClassNameAsString()).decl();

        // Case 1: The class type represents an enum. This means we need to rewrite it to represent an enum type.
        if(classTypeDecl.isTopLevelDecl() && classTypeDecl.asTopLevelDecl().isEnumDecl())
            return buildEnumType(ct, classTypeDecl.asTopLevelDecl().asEnumDecl());
        else if(classTypeDecl.isTopLevelDecl() && classTypeDecl.asTopLevelDecl().isClassDecl()) {
            // Case 2: The class type represents a template class.
            if(ct.isTemplatedType())
                checkIfTemplateTypeIsValid(ct);
        }
        else if(!classTypeDecl.isTypeifier()) {
            // ERROR CHECK #2: If the class type does not represent an enum, a class, or a type parameter, then
            //                 this means a variable name was used as a type which makes no sense whatsoever.
            errors.add(
                new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                    .addLocation(ct.getRootParent())
                    .addErrorType(MessageType.TYPE_ERROR_465)
                    .addArgs(ct)
                    .addSuggestType(MessageType.TYPE_SUGGEST_1452)
                    .error()
            );
        }


//        // Function Case
//        else if(currentFunction != null && !currentFunction.typeParams().isEmpty()) {
//            for(int i = 0; i < currentFunction.typeParams().size(); i++) {
//                Typeifier param = currentFunction.typeParams().get(i).asTypeifier();
//                if(param.toString().equals(ct.asClassType().getClassName().toString())) {
//                    ct = typeArgs.get(i);
//                    break;
//                }
//            }
//        }

        // Case 3: The class type represents a normal class. No rewrite is needed so return the class type.
        return ct;
    }

    /**
     * Verifies the validity of a template type.
     * <p><br>
     *     If a user writes a template type in place of a class type, this method will
     *     perform the necessary error checks to ensure the template type was written
     *     correctly based on its class definition. The {@link typechecker.TypeChecker}
     *     will handle the rest of the type errors related to assignment compatibility.
     * </p>
     * @param ct {@link ClassType} representing a potential template type
     */
    private void checkIfTemplateTypeIsValid(ClassType ct) {
        ClassDecl template = currentScope.findName(ct.getClassNameAsString()).decl().asTopLevelDecl().asClassDecl();

        // ERROR CHECK #1: When a template type is written, we want to make sure the correct number of
        //                 type arguments were passed. This will be based on the number of type parameters
        //                 the template class was declared with. There are 2 possible errors here.
        if(template.typeParams().size() != ct.typeArgs().size()) {
            // Case 1: This error is generated when a user writes type arguments for a non-template class type.
            if(template.typeParams().isEmpty()) {
                errors.add(
                    new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                        .addLocation(ct.getRootParent())
                        .addErrorType(MessageType.TYPE_ERROR_444)
                        .addArgs(template)
                        .error()
                );
            }
            // Case 2: This error is generated when the wrong number of type arguments were used for a template class type.
            else {
                ErrorBuilder eb = new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                                      .addLocation(ct.getRootParent())
                                      .addErrorType(MessageType.TYPE_ERROR_445)
                                      .addArgs(template)
                                      .addSuggestArgs(template, template.typeParams().size());

                if(template.typeParams().size() == 1)
                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1441).error());
                else
                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1442).error());
            }
        }

        // We now look through each type parameter of the template class.
        for (int i = 0; i < template.typeParams().size(); i++) {
            Typeifier typeParam = template.typeParams().get(i);

            // ERROR CHECK #2: If a user prefixed the type parameter with a type annotation, then we will check if
            //                 the passed type argument can be used in the current type argument. If no type annotation
            //                 was given, this check is not needed, and we will let the type checker handle the rest.
            if(typeParam.hasTypeAnnotation() && !typeParam.isValidTypeArg(ct.typeArgs().get(i))) {
                errors.add(
                    new ErrorBuilder(generateTypeError, currentFile, interpretMode)
                        .addLocation(ct.getRootParent())
                        .addErrorType(MessageType.TYPE_ERROR_446)
                        .addArgs(ct.typeArgs().get(i), template)
                        .addSuggestType(MessageType.TYPE_SUGGEST_1443)
                        .addSuggestArgs(template, typeParam.possibleTypeToString(), i + 1)
                        .error()
                );
            }
        }
    }

//    public void checkFuncTemplateType(FuncDecl fd, Invocation in) {
//        // ERROR CHECK #1: This checks if both the function and the invocation have the same amount of type parameters.
//        if(fd.typeParams().size() != in.getTypeArgs().size()) {
//            // This error message is generated when a user tries to instantiate a non-templated function.
//            if(fd.typeParams().isEmpty()) {
//                errors.add(
//                    new ErrorBuilder(generateTypeError,currentFile,interpretMode)
//                        .addLocation(in.getRootParent())
//                        .addErrorType(MessageType.TYPE_ERROR_462)
//                        .addArgs(fd)
//                        .error()
//                );
//            }
//            // This error message is generated when a user tries to instantiate a templated function.
//            else {
//                ErrorBuilder eb = new ErrorBuilder(generateTypeError,currentFile,interpretMode)
//                                      .addLocation(in.getRootParent())
//                                      .addErrorType(MessageType.TYPE_ERROR_463)
//                                      .addArgs(in.getSignature())
//                                      .addSuggestArgs(fd,fd.typeParams().size());
//
//                if(fd.typeParams().size() == 1)
//                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1449).error());
//                else
//                    errors.add(eb.addSuggestType(MessageType.TYPE_SUGGEST_1450).error());
//            }
//        }
//
//        // We now look through each type parameter for the corresponding function.
//        for (int i = 0; i < fd.typeParams().size(); i++) {
//            Typeifier tp = fd.typeParams().get(i);
//            // ERROR CHECK #2: This checks if the correct type was passed as an argument (if applicable).
//            if(tp.hasPossibleType() && !tp.isValidType(in.getTypeArgs().get(i))) {
//                errors.add(
//                    new ErrorBuilder(generateTypeError, currentFile, interpretMode)
//                        .addLocation(in.getRootParent())
//                        .addErrorType(MessageType.TYPE_ERROR_446)
//                        .addArgs(in.getTypeArgs().get(i), fd)
//                        .addSuggestType(MessageType.TYPE_SUGGEST_1451)
//                        .addSuggestArgs(fd, tp.possibleTypeToString(), i + 1)
//                        .error()
//                );
//            }
//        }
//    }

    /**
     * Creates an instance of a template class based on the given type arguments.
     * <p><br>
     *     This method is responsible for actually generating an instance of a template
     *     class that will then be bound to a {@link NewExpr}. We will create a copy of
     *     the template class and replace all of its type parameters with the given type
     *     arguments.
     * </p>
     * @param ct Template type that we use to instantiate a template class
     * @return {@link ClassDecl} representing the instantiated class
     */
    private ClassDecl instantiatesClassTemplate(ClassType ct) {
        // If the template class was already instantiated with the given type arguments,
        // then we don't want to reinstantiate it and instead get the instantiated class.
        if(instantiatedClasses.contains(ct.toString()))
            return currentScope.findName(ct).decl().asTopLevelDecl().asClassDecl();

        ClassDecl template = currentScope.findName(ct.getClassNameAsString()).decl().asTopLevelDecl().asClassDecl();
        ClassDecl copyOfTemplate = template.deepCopy().asTopLevelDecl().asClassDecl();

        // We need to rerun the name checker, so the copy of the template can
        // have an independent symbol table from the original template class.
        copyOfTemplate.visit(new NameChecker());

        // We now visit the copy's class in order to replace all type parameters with the given type arguments.
        currentTypeArgs = ct.typeArgs();
        copyOfTemplate.visit(this);

        currentTypeArgs = null;
        // TESTING IF THIS WORKS!!!!!!!!!!!!!!!!!!!
        copyOfTemplate.removeTypeParams();

        instantiatedClasses.add(ct.toString());
        currentScope.addNameToRootTable(ct.toString(), copyOfTemplate);

        return copyOfTemplate;
    }

//    // Goal : Create the dang function.
//    public FuncDecl instantiatesFunction(Invocation in) {
//        // If a function was already instantiated with the given type arguments, we do not want to
//        // waste time instantiating it again. We just want to return it
//        if(instantiatedFunctions.contains(in.templateSignature()))
//            return currentScope.findName(in.templateSignature()).decl().asTopLevelDecl().asFuncDecl();
//
//        FuncDecl originalFunc = currentScope.findName(in.getSignature()).decl().asTopLevelDecl().asFuncDecl();
//        FuncDecl copy = originalFunc.deepCopy().asTopLevelDecl().asFuncDecl();
//        copy.visit(new NameChecker());
//
//        SymbolTable oldScope = currentScope;
//        currentFunction = copy;
//        typeArgs = in.getTypeArgs();
//
//        copy.visit(this);
//
//        currentScope = oldScope;
//        instantiatedFunctions.add(in.templateSignature());
//        // I DO NOT KNOW WHY THIS IS BROKEN AT ALL BECAUSE ITS NOT BROKEN INTERNALLY YAY !!!!!!!!!!!!!!!!!
//        copy.resetTypeParams();
//        currentScope.addNameToRootTable(in.templateSignature(),copy);
//
//        return copy;
//    }

    /* ######################################## VISITS ######################################## */

    /**
     * Sets the current scope to be inside of a class.
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        SymbolTable oldScope = currentScope;
        currentScope = cd.symbolTable;

        if(cd.isTemplate())
            currentTypeParams = cd.typeParams();

        super.visitClassDecl(cd);

        currentScope = oldScope;
        currentTypeParams = null;
    }

    /**
     * Begins the C Minor type validity micropass.
     * <p><br>
     *     During compilation mode, {@code visitCompilation} will be the first
     *     method executed when we start the type validity micropass.
     * </p>
     * @param c {@link Compilation}
     */
    public void visitCompilation(Compilation c) {
        currentScope = c.globalTable;
        currentFile = c.getFile();
        super.visitCompilation(c);
    }

    /**
     * Validates the type of a field.
     * <p><br>
     *     This visit will rewrite the type of the {@link FieldDecl} if needed. There
     *     is no other checks that need to be performed at this stage.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        if(fd.type().isStructuredType()) {
            Type updatedType = rewriteStructuredType(fd.type());
            fd.setType(updatedType);
        }

        if(fd.var().init() != null)
            fd.var().init().visit(this);
    }

    /**
     * Validates the type of a global variable.
     * <p><br>
     *     This visit will rewrite the type of the {@link GlobalDecl} if needed. There
     *     is no other checks that need to be performed at this stage.
     * </p>
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        if(gd.type().isStructuredType()) {
            Type updatedType = rewriteStructuredType(gd.type());
            gd.setType(updatedType);
        }

        if(gd.var().init() != null)
            gd.var().init().visit(this);
    }

    /**
     * Checks the type validity of an imported file.
     * <p><br>
     *     If a user has any imported files, we will have to make sure all types
     *     in that file are valid before we work on the main file of the program.
     *     This visit will handle the type validity check for each imported file.
     * </p>
     * @param im {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl im) {
        SymbolTable oldScope = currentScope;
        String oldFile = currentFile;

        im.getCompilationUnit().visit(this);

        currentScope = oldScope;
        currentFile = oldFile;
    }

    /**
     * Validates the type of a local variable.
     * <p><br>
     *     This visit will rewrite the type of the {@link LocalDecl} if needed. Additionally,
     *     we will check if the local's name shadows a previously defined type parameter
     *     when we are visiting either a template function or class.
     * </p>
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        // ERROR CHECK #1: If we are visiting a templated class or function, then we will check if
        //                 the current local variable's name will shadow a type parameter name. The
        //                 name checker will not have caught this error when we have nested scopes
        //                 which is why we have to do this check now.
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

        if(ld.type().isStructuredType()) {
            Type updatedType = rewriteStructuredType(ld.type());
            ld.setType(updatedType);
        }

        if(ld.var().init() != null)
            ld.var().init().visit(this);
    }

    /**
     * Validates the type of a method.
     * <p><br>
     *     During this visit, we want to make sure the method's parameters do not conflict
     *     with any previously declared type parameters, and we also want to see if we need
     *     to rewrite its return type. From there, we will just validate the types within
     *     its {@link ast.statements.BlockStmt}.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        for(ParamDecl pd : md.params())
            pd.visit(this);

        if(md.returnType().isStructuredType()) {
            Type updatedType = rewriteStructuredType(md.returnType());
            md.setReturnType(updatedType);
        }

        md.methodBlock().visit(this);
    }

    /**
     * Validates the type of a new expression.
     * <p><br>
     *     When a user tries to instantiate a template class, we will check to make sure
     *     the template type is valid and then proceed to instantiate the class for the user.
     *     We will bind the instantiated class to the {@link NewExpr} instead of a variable.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        if(ne.createsFromTemplate()) {
            checkIfTemplateTypeIsValid(ne.getClassType());
            ne.setInstantiatedClass(instantiatesClassTemplate(ne.getClassType()));
        }
    }

    /**
     * Validates the type of a parameter.
     * <p><br>
     *     This visit will rewrite the type of the {@link ParamDecl} if needed. Additionally,
     *     we will check if the parameter's name shadows a previously defined type parameter
     *     when we are visiting methods inside of a template class.
     * </p>
     * @param pd {@link ParamDecl}
     */
    public void visitParamDecl(ParamDecl pd) {
        // ERROR CHECK #1: If we are visiting a templated class, then we will check if the parameter's
        //                 name shadows a type parameter name. Due to how the name checker resolves methods,
        //                 this particular check was not completed, so we will do it now.
        if(currentTypeParams != null && nameShadowsTypeParam(pd.toString())) {
            errors.add(
                new ScopeErrorBuilder(generateScopeError,currentFile,interpretMode)
                    .addLocation(pd)
                    .addErrorType(MessageType.SCOPE_ERROR_328)
                    .addArgs(pd)
                    .asScopeErrorBuilder()
                    .addRedeclaration(currentScope.findName(pd).decl())
                    .error()
            );
        }

        if(pd.type().isStructuredType()) {
            Type updatedType = rewriteStructuredType(pd.type());
            pd.setType(updatedType);
        }
    }
}
