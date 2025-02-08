package messages.errors;

import ast.AST;
import messages.Message;
import utilities.PrettyPrint;

public class Error extends Message {

    protected ErrorType error;

    public Error(AST node) { super(node); }

    protected String printStartLocation() { return super.printStartLocation(); }

    public void printMsg() { super.printMsgLine(); }
    public void setMsg() { msg = "An unspecified error has occurred! Please try again."; }
}
