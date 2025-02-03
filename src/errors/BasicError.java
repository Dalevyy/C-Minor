package errors;

import ast.AST;
import utilities.PrettyPrint;

public abstract class BasicError {

    protected static String fileName;
    protected static boolean interpret = false;
    protected String errorMessage;

    public static void setFileName(String fileName) { BasicError.fileName = fileName; }
    public static void setInterpret() { interpret = true; }

    public void printHeader(String color) { System.out.println(color + errorMessage + PrettyPrint.RESET); }
}


