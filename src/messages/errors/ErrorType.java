package messages.errors;

import utilities.PrettyPrint;
import utilities.PropertiesLoader;

import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.io.InputStream;

import java.util.Properties;

public enum ErrorType {

    //       SCOPE ERRORS

    SCOPE_ERROR_300,
    SCOPE_ERROR_301,
    SCOPE_ERROR_302,
    SCOPE_ERROR_303,
    SCOPE_ERROR_304,
    SCOPE_ERROR_305,
    SCOPE_ERROR_306,
    SCOPE_ERROR_307,
    SCOPE_ERROR_308,
    SCOPE_ERROR_309,
    SCOPE_ERROR_310,
    SCOPE_ERROR_311,
    SCOPE_ERROR_312,
    SCOPE_ERROR_313,
    SCOPE_ERROR_314,
    SCOPE_ERROR_315,
    SCOPE_ERROR_316,
    SCOPE_ERROR_317,
    SCOPE_ERROR_318,
    

    // Type Checking Errors
    LOCAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR,
    GLOBAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR,
    FIELD_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR,
    ASSIGN_STMT_TYPE_DOES_NOT_MATCH,
    ASSIGN_STMT_INVALID_TYPES_USED,

    BIN_EXPR_NOT_ASSIGNCOMP,
    BIN_EXPR_NOT_NUMERIC,
    BIN_EXPR_SLEFT_SRIGHT_NOT_INT,
    BIN_EXPR_BITWISE_NOT_DISCRETE,
    BIN_EXPR_LOGICAL_NOT_BOOL,
    BIN_EXPR_OBJ_OPS_MISSING_OBJ,

    UNARY_EXPR_INVALID_NEGATION,
    UNARY_EXPR_INVALID_NOT,

    CAST_EXPR_INVALID_INT_CAST,
    CAST_EXPR_INVALID_CHAR_CAST,
    CAST_EXPR_INVALID_REAL_CAST,
    CAST_EXPR_INVALID_CAST,

    IF_CONDITION_NOT_BOOLEAN,
    LOOP_CONDITION_NOT_BOOLEAN,

    NEW_EXPR_INVALID_ARG_TYPE,
    FIELD_EXPR_INVALID_TARGET_TYPE,

    FUNC_INVALID_NUM_OF_ARGS,
    FUNC_INVALID_ARG_TYPE,

    MAIN_RETURN_TYPE_ERROR,



    //       MODIFIER ERRORS

    CAN_NOT_ACCESS_NON_PUBLIC_FIELD,
    CAN_NOT_ACCESS_NON_PUBLIC_METHOD,
    CAN_NOT_INHERIT_FROM_A_FINAL_CLASS,
    CAN_NOT_INSTANTIATE_AN_ABSTRACT_CLASS,
    CONCRETE_CLASS_DOES_NOT_IMPLEMENT_ABSTRACT_SUPERCLASS,
    CONSTANT_VARAIBLE_CAN_NOT_BE_REASSIGNED,
    RECURSIVE_FUNCTION_CALL_NOT_ALLOWED,
    RECURSIVE_METHOD_CALL_NOT_ALLOWED,

    SCOPE_SUGGEST_1300,
    SCOPE_SUGGEST_1301;

    private static Properties errorMessages;
    private static final String PATH = "utilities/ErrorType.properties";

    public String getMessage() {
        return errorMessages.getProperty(name());
    }

    static {
        URL url = PropertiesLoader.getURL(PATH);
        String path = PATH;
        if(url != null) { path = url.getFile(); }
        errorMessages = PropertiesLoader.loadProperties(new File(path));
    }
}
