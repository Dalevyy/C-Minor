package messages;

import utilities.PropertiesLoader;

import java.io.File;
import java.net.URL;

import java.util.Properties;

public enum MessageType {

    /*      SCOPE ERRORS     */
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
    SCOPE_ERROR_319,
    SCOPE_ERROR_320,

    /*      TYPE ERRORS     */
    TYPE_ERROR_400,
    TYPE_ERROR_401,
    TYPE_ERROR_402,
    TYPE_ERROR_403,
    TYPE_ERROR_404,
    TYPE_ERROR_405,
    TYPE_ERROR_406,
    TYPE_ERROR_407,
    TYPE_ERROR_408,
    TYPE_ERROR_409,
    TYPE_ERROR_410,
    TYPE_ERROR_411,
    TYPE_ERROR_412,
    TYPE_ERROR_413,
    TYPE_ERROR_414,
    TYPE_ERROR_415,
    TYPE_ERROR_416,
    TYPE_ERROR_417,
    TYPE_ERROR_418,
    TYPE_ERROR_419,
    TYPE_ERROR_420,


    /*      MOD ERRORS     */
    MOD_ERROR_500,
    MOD_ERROR_501,
    MOD_ERROR_502,
    MOD_ERROR_503,
    MOD_ERROR_504,
    MOD_ERROR_505,
    MOD_ERROR_506,
    MOD_ERROR_507,
    MOD_ERROR_508,

    CAN_NOT_ACCESS_NON_PUBLIC_FIELD,
    CAN_NOT_ACCESS_NON_PUBLIC_METHOD,
    CAN_NOT_INHERIT_FROM_A_FINAL_CLASS,
    CAN_NOT_INSTANTIATE_AN_ABSTRACT_CLASS,
    CONCRETE_CLASS_DOES_NOT_IMPLEMENT_ABSTRACT_SUPERCLASS,
    CONSTANT_VARAIBLE_CAN_NOT_BE_REASSIGNED,
    RECURSIVE_FUNCTION_CALL_NOT_ALLOWED,
    RECURSIVE_METHOD_CALL_NOT_ALLOWED,

    SCOPE_SUGGEST_1300,
    SCOPE_SUGGEST_1301,

    TYPE_SUGGEST_1400,
    TYPE_SUGGEST_1401,
    TYPE_SUGGEST_1402,
    TYPE_SUGGEST_1403,
    TYPE_SUGGEST_1404,
    TYPE_SUGGEST_1405,
    TYPE_SUGGEST_1406,
    TYPE_SUGGEST_1407,
    TYPE_SUGGEST_1408,
    TYPE_SUGGEST_1409,
    TYPE_SUGGEST_1410,
    TYPE_SUGGEST_1411,

    MOD_SUGGEST_1500,
    MOD_SUGGEST_1501,
    MOD_SUGGEST_1502,
    MOD_SUGGEST_1503,
    MOD_SUGGEST_1504,
    MOD_SUGGEST_1505,
    MOD_SUGGEST_1506,
    MOD_SUGGEST_1507;


    private static Properties errorMessages;
    private static final String PATH = "utilities/MessageType.properties";

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
