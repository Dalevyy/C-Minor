package utilities;

/*
This is a class that provides ANSI escape sequences. The purpose
is to help customize the error messages.
*/
public final class PrettyPrint {
    // General
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[22m";
    public static final String ITALIC = "\u001B[23m";
    public static final String LINE = "\u001B[24m";

    // Foreground Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Background Colors
    public static final String bBLACK = "\u001B[40m";
    public static final String bRED = "\u001B[41m";
    public static final String bGREEN = "\u001B[42m";
    public static final String bYELLOW = "\u001B[43m";
    public static final String bBLUE = "\u001B[44m";
    public static final String bPURPLE = "\u001B[45m";
    public static final String bCYAN = "\u001B[46m";
    public static final String bWHITE = "\u001B[47m";

}
