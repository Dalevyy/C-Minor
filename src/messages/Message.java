package messages;

import ast.AST;

public abstract class Message {

    protected String fileName;
    protected AST location;
    protected String msg;
    protected Object[] args;
    protected Object[] argsForSuggestions;

    protected boolean interpretMode;

    public abstract String createMessage();

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
}
