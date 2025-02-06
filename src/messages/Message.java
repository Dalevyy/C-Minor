package messages;

import ast.AST;
import java.util.ArrayList;

public abstract class Message {

    private ArrayList<Message> allMsgs;

    protected String msg;     // Stores the message we will print to user
    protected AST location;   // Stores which node in the AST the message is printed for

    public Message(AST location) { this.location = location; }

    protected void printMsgLine() { this.location.printLine(); }
    protected String printStartLocation() { return location.getStartPosition(); }

    public abstract void printMsg();
    public abstract void setMsg();

}
