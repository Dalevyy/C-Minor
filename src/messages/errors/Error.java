package messages.errors;

import messages.Message;
import messages.MessageType;
import utilities.PrettyPrint;

public abstract class Error extends Message {

    protected MessageType error;
    protected MessageType suggest;

    public abstract String header();

    protected String errorNumber() {
        return error.toString().substring(error.toString().lastIndexOf("_")+1);
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.header());
        if(this.location() != null)
            sb.append(this.location().line());
        sb.append(this.buildError());
        if(this.suggest != null) {
            sb.append("\n");
            sb.append(this.buildSuggestion());
        }
        System.out.println(sb);

        if(!interpretMode)
            System.exit(1);
        // If we have a redeclaration error, then throw an exception indicating
        // we had a redeclaration in order to prevent the removal of the name
        else if(this.errorType() == MessageType.SCOPE_ERROR_300
                || this.errorType() == MessageType.SCOPE_ERROR_302
                || this.errorType() == MessageType.SCOPE_ERROR_304
                || this.errorType() == MessageType.SCOPE_ERROR_305
                || this.errorType() == MessageType.SCOPE_ERROR_311
                || this.errorType() == MessageType.SCOPE_ERROR_312
                || this.errorType() == MessageType.SCOPE_ERROR_316
                || this.errorType() == MessageType.SCOPE_ERROR_329)
            throw new RuntimeException("Redeclaration");
        else
            throw new RuntimeException("Error");
        
        return sb.toString();
    }

    private String buildError() {
        String errorMsg = PrettyPrint.RED + this.error.getMessage() + PrettyPrint.RESET;
        if(this.args != null) {
            for(int i = 0; i < this.args.length; i++) {
                String arg = "<arg" + i + ">";
                errorMsg = errorMsg.replace(arg,this.args[i].toString());
            }
        }
        return errorMsg;
    }

    private String buildSuggestion() {
        String suggestMsg = "\nSuggestion:\n" + PrettyPrint.RED + this.suggest.getMessage() + PrettyPrint.RESET;
        if(this.argsForSuggestions != null) {
            for(int i = 0; i < this.argsForSuggestions.length; i++) {
                String arg = "<arg" + i + ">";
                suggestMsg = suggestMsg.replace(arg,this.argsForSuggestions[i].toString());
            }
        }
        return suggestMsg;
    }

    public void setErrorType(MessageType et) { this.error = et; }
    public MessageType errorType() { return this.error; }

    public void setSuggestType(MessageType et) { this.suggest = et; }
    public MessageType suggestType() { return this.suggest; }
}
