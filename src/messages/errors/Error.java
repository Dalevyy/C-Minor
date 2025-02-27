package messages.errors;

import messages.Message;
import utilities.PrettyPrint;

public abstract class Error extends Message {

    protected ErrorType error;

    public abstract String header();
    public abstract String buildSuggestion();

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.header());
        sb.append(this.location().line());
        sb.append(this.buildError());
        // sb.append(this.buildSuggestion());
        System.out.println(sb.toString());

        if(!interpretMode) { System.exit(1); }
        else { throw new RuntimeException(); }
        return sb.toString();
    }

    private String buildError() {
        String errorMsg = PrettyPrint.RED + this.error.getMessage() + PrettyPrint.RESET;
        for(int i = 0; i < this.args.length; i++) {
            String arg = "<arg" + i + ">";
            errorMsg = errorMsg.replace(arg,this.args[i].toString());
        }
        return errorMsg;
    }

    public void setErrorType(ErrorType et) { this.error = et; }
    public ErrorType errorType() { return this.error; }
}
