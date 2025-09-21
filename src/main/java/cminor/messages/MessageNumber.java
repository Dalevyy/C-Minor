package cminor.messages;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import cminor.utilities.PropertiesLoader;

import javax.swing.*;

/**
 * An enumeration that keeps track of all messages available to the compiler for error handling.
 * <p>
 *     This is an error handling system that Dr. C came up with for ProcessJ, and I decided to
 *     adapt it to use inside C Minor. Inside of utilities, there is a file called
 *     {@code MessageNumber.properties} that has a list of all available messages the compiler
 *     currently supports. This enumeration allows us to use those messages.
 * </p>
 * @author Daniel Levy
 */
public enum MessageNumber {

    /* ######################################## SYNTAX ERRORS ######################################## */
    SYNTAX_ERROR_100,
    SYNTAX_ERROR_101,
    SYNTAX_ERROR_102,

    /* ######################################## SCOPE ERRORS ######################################## */
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
    SCOPE_ERROR_321,
    SCOPE_ERROR_322,
    SCOPE_ERROR_323,
    SCOPE_ERROR_324,
    SCOPE_ERROR_325,
    SCOPE_ERROR_326,
    SCOPE_ERROR_327,
    SCOPE_ERROR_328,
    SCOPE_ERROR_329,
    SCOPE_ERROR_330,
    SCOPE_ERROR_331,
    SCOPE_ERROR_332,
    SCOPE_ERROR_333,
    SCOPE_ERROR_334,
    SCOPE_ERROR_335,
    SCOPE_ERROR_336,
    SCOPE_ERROR_337,
    SCOPE_ERROR_338,

    /* ######################################## TYPE ERRORS ######################################## */
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
    TYPE_ERROR_421,
    TYPE_ERROR_422,
    TYPE_ERROR_423,
    TYPE_ERROR_424,
    TYPE_ERROR_425,
    TYPE_ERROR_426,
    TYPE_ERROR_427,
    TYPE_ERROR_428,
    TYPE_ERROR_429,
    TYPE_ERROR_430,
    TYPE_ERROR_431,
    TYPE_ERROR_432,
    TYPE_ERROR_433,
    TYPE_ERROR_434,
    TYPE_ERROR_435,
    TYPE_ERROR_436,
    TYPE_ERROR_437,
    TYPE_ERROR_438,
    TYPE_ERROR_439,
    TYPE_ERROR_440,
    TYPE_ERROR_441,
    TYPE_ERROR_442,
    TYPE_ERROR_443,
    TYPE_ERROR_444,
    TYPE_ERROR_445,
    TYPE_ERROR_446,
    TYPE_ERROR_447,
    TYPE_ERROR_448,
    TYPE_ERROR_449,
    TYPE_ERROR_450,
    TYPE_ERROR_451,
    TYPE_ERROR_452,
    TYPE_ERROR_453,
    TYPE_ERROR_454,
    TYPE_ERROR_455,
    TYPE_ERROR_456,
    TYPE_ERROR_457,
    TYPE_ERROR_458,
    TYPE_ERROR_459,
    TYPE_ERROR_460,
    TYPE_ERROR_461,
    TYPE_ERROR_462,
    TYPE_ERROR_463,
    TYPE_ERROR_464,
    TYPE_ERROR_465,
    TYPE_ERROR_466,
    TYPE_ERROR_467,
    TYPE_ERROR_468,
    TYPE_ERROR_469,
    TYPE_ERROR_470,
    TYPE_ERROR_471,
    TYPE_ERROR_472,
    TYPE_ERROR_473,
    TYPE_ERROR_474,
    TYPE_ERROR_475,
    TYPE_ERROR_476,
    TYPE_ERROR_477,
    TYPE_ERROR_478,
    TYPE_ERROR_479,
    TYPE_ERROR_480,


    /* ######################################## MODIFIER ERRORS ######################################## */
    MOD_ERROR_500,
    MOD_ERROR_501,
    MOD_ERROR_502,
    MOD_ERROR_503,
    MOD_ERROR_504,
    MOD_ERROR_505,
    MOD_ERROR_506,
    MOD_ERROR_507,
    MOD_ERROR_508,
    MOD_ERROR_509,

    /* ######################################## RUNTIME ERRORS ######################################## */
    RUNTIME_ERROR_600,
    RUNTIME_ERROR_601,
    RUNTIME_ERROR_602,
    RUNTIME_ERROR_603,
    RUNTIME_ERROR_604,
    RUNTIME_ERROR_605,
    RUNTIME_ERROR_606,
    RUNTIME_ERROR_607,
    RUNTIME_ERROR_608,
    RUNTIME_ERROR_609,

    /* ######################################## SEMANTIC ERRORS ######################################## */
    SEMANTIC_ERROR_700,
    SEMANTIC_ERROR_701,
    SEMANTIC_ERROR_702,
    SEMANTIC_ERROR_703,
    SEMANTIC_ERROR_704,
    SEMANTIC_ERROR_705,
    SEMANTIC_ERROR_706,
    SEMANTIC_ERROR_707,
    SEMANTIC_ERROR_708,
    SEMANTIC_ERROR_709,
    SEMANTIC_ERROR_710,
    SEMANTIC_ERROR_711,
    SEMANTIC_ERROR_712,
    SEMANTIC_ERROR_713,
    SEMANTIC_ERROR_714,
    SEMANTIC_ERROR_715,
    SEMANTIC_ERROR_716,
    SEMANTIC_ERROR_717,
    SEMANTIC_ERROR_718,
    SEMANTIC_ERROR_719,
    SEMANTIC_ERROR_720,


    /* ######################################## SCOPE SUGGESTIONS ######################################## */
    SCOPE_SUGGEST_1300,
    SCOPE_SUGGEST_1301,
    SCOPE_SUGGEST_1302,
    SCOPE_SUGGEST_1303,
    SCOPE_SUGGEST_1304,
    SCOPE_SUGGEST_1305,
    SCOPE_SUGGEST_1306,
    SCOPE_SUGGEST_1307,
    SCOPE_SUGGEST_1308,

    /* ######################################## TYPE SUGGESTIONS ######################################## */
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
    TYPE_SUGGEST_1412,
    TYPE_SUGGEST_1413,
    TYPE_SUGGEST_1414,
    TYPE_SUGGEST_1415,
    TYPE_SUGGEST_1416,
    TYPE_SUGGEST_1417,
    TYPE_SUGGEST_1418,
    TYPE_SUGGEST_1419,
    TYPE_SUGGEST_1420,
    TYPE_SUGGEST_1421,
    TYPE_SUGGEST_1422,
    TYPE_SUGGEST_1423,
    TYPE_SUGGEST_1424,
    TYPE_SUGGEST_1425,
    TYPE_SUGGEST_1426,
    TYPE_SUGGEST_1427,
    TYPE_SUGGEST_1428,
    TYPE_SUGGEST_1429,
    TYPE_SUGGEST_1430,
    TYPE_SUGGEST_1431,
    TYPE_SUGGEST_1432,
    TYPE_SUGGEST_1433,
    TYPE_SUGGEST_1434,
    TYPE_SUGGEST_1435,
    TYPE_SUGGEST_1436,
    TYPE_SUGGEST_1437,
    TYPE_SUGGEST_1438,
    TYPE_SUGGEST_1439,
    TYPE_SUGGEST_1440,
    TYPE_SUGGEST_1441,
    TYPE_SUGGEST_1442,
    TYPE_SUGGEST_1443,
    TYPE_SUGGEST_1444,
    TYPE_SUGGEST_1445,
    TYPE_SUGGEST_1446,
    TYPE_SUGGEST_1447,
    TYPE_SUGGEST_1448,
    TYPE_SUGGEST_1449,
    TYPE_SUGGEST_1450,
    TYPE_SUGGEST_1451,
    TYPE_SUGGEST_1452,
    TYPE_SUGGEST_1453,
    TYPE_SUGGEST_1454,
    TYPE_SUGGEST_1455,
    TYPE_SUGGEST_1456,

    /* ######################################## MODIFIER SUGGESTIONS ######################################## */
    MOD_SUGGEST_1500,
    MOD_SUGGEST_1501,
    MOD_SUGGEST_1502,
    MOD_SUGGEST_1503,
    MOD_SUGGEST_1504,
    MOD_SUGGEST_1505,
    MOD_SUGGEST_1506,
    MOD_SUGGEST_1507,
    MOD_SUGGEST_1508,

    /* ######################################## SEMANTIC SUGGESTIONS ######################################## */
    SEMANTIC_SUGGEST_1700,
    SEMANTIC_SUGGEST_1701,
    SEMANTIC_SUGGEST_1702,
    SEMANTIC_SUGGEST_1703,
    SEMANTIC_SUGGEST_1704,
    SEMANTIC_SUGGEST_1705,

    /* ######################################## WARNINGS ######################################## */
    WARNING_1,

    /* ######################################## COMPILER SETTING ERRORS ######################################## */
    SETTING_ERROR_1,
    SETTING_ERROR_2,
    SETTING_ERROR_3,
    SETTING_ERROR_4;

    /**
     * The file location containing the compiler messages. DO NOT CHANGE UNLESS FILE CHANGES DIRECTORY.
     */
    private static final String PATH = "MessageNumber.properties";

    /**
     * List of messages that come from {@link #PATH}.
     */
    private static final Properties msgs;

    /**
     * Retrieves a message from {@link #msgs} based on the enumeration's value.
     * @return String representation of the message that will be displayed.
     */
    public String getMessage() { return msgs.getProperty(name()); }

    // This loads and stores the messages from the properties file into the enumeration file.
    static {
        InputStream p = Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH);
        msgs = new Properties();
       try { msgs.load(p);}
       catch(Exception e) { System.out.println(e); }
        // URL url = PropertiesLoader.getURL(PATH);
//String path = PATH;

//        if(url != null)
//            path = url.getFile();

    //    msgs = PropertiesLoader.loadProperties(path);
    }
}
