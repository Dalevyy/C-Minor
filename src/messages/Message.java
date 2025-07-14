package messages;

import ast.AST;
import utilities.Vector;

public abstract class Message {

    protected String fileName;
    protected AST location;
    protected String msg;
    protected Object[] args;
    protected Object[] argsForSuggestions;

    protected boolean interpretMode;

    public abstract String createMessage();
    public abstract String printMessage();

    public void setFileName(String fn) { this.fileName = fn; }
    public String fileName() { return this.fileName; }

    public void setLocation(AST n) { this.location = n; }
    public AST location() { return this.location; }

    public void setMsg(String msg) { this.msg = msg; }
    public String msg() { return this.msg; }

    public void setArgs(Object[] args) { this.args = args; }
    public Object[] args() { return this.args; }

    public void setArgsForSuggestions(Object[] args) { this.argsForSuggestions = args; }
    public Object[] suggests() { return this.argsForSuggestions; }

    public void setInterpretMode(boolean mode) { this.interpretMode = mode; }

    public boolean isError() { return false; }
    public boolean isWarning() { return false; }

    public String fileHeader() {
        if(fileName != null && !fileName.isEmpty())
            return "In " + fileName + ": ";
        return "";
    }

    public static boolean printAllMessages(Vector<Message> msgs) {
        if(msgs.isEmpty())
            return false;

        // Print out every message to the terminal. This will be done in sequential order
        // based on the order in which the messages are generated in.
        for(Message msg : msgs)
            System.out.println(msg.createMessage());

        // We will only terminate the compilation process when an error is found. This looks
        // through the messages vector to make sure we actually had an error as suppose to just
        // a bunch of errors. This might be slow... not sure. :)
        for(Message msg : msgs)
            if(msg.isError())
                return true;

        return false;
    }
}
