package messages.errors;

import messages.Message;
import utilities.PrettyPrint;

public abstract class Error extends Message {

    protected ErrorType error;
    protected ErrorType suggest;

    public abstract String header();

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.header());
        sb.append(this.location().line());
        sb.append(this.buildError());
        if(this.argsForSuggestions != null) {
            sb.append("\n");
            sb.append(this.buildSuggestion());
        }
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

    private String buildSuggestion() {
        String suggestMsg = PrettyPrint.RED + this.suggest.getMessage() + PrettyPrint.RESET;
        for(int i = 0; i < this.argsForSuggestions.length; i++) {
            String arg = "<arg" + i + ">";
            suggestMsg = suggestMsg.replace(arg,this.argsForSuggestions[i].toString());
        }
        return suggestMsg;
    }

    public void setErrorType(ErrorType et) { this.error = et; }
    public ErrorType errorType() { return this.error; }

    public void setSuggestType(ErrorType et) { this.suggest = et; }
    public ErrorType suggestType() { return this.suggest; }
}
