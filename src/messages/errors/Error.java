package messages.errors;

import messages.Message;
import messages.MessageType;
import utilities.PrettyPrint;

public abstract class Error extends Message {

    protected MessageType error;
    protected MessageType suggest;

    public abstract String header();

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.header());
        sb.append(this.location().line());
        sb.append(this.buildError());
        if(this.suggest != null) {
            sb.append("\n");
            sb.append(this.buildSuggestion());
        }
        System.out.println(sb);

        if(!interpretMode) { System.exit(1); }
        // If we are running the interpreter and have an error with a statement, then we want to throw an exception
        // with the statement's name. If the error is **NOT** related to redeclaration, then we want to remove the
        // statement from our symbol table
        if(this.location().isParamDecl() && this.errorType() != MessageType.SCOPE_ERROR_304) {
            throw new RuntimeException(this.location().toString());
        }
        else if(this.location().isTopLevelDecl()) {
            if(this.location.asTopLevelDecl().isEnumDecl()
                    && this.errorType() != MessageType.SCOPE_ERROR_305
                    && this.errorType() != MessageType.SCOPE_ERROR_306) {
                throw new RuntimeException(this.location().toString());
            }
            else if(this.location.asTopLevelDecl().isClassDecl() && this.errorType() != MessageType.SCOPE_ERROR_316) {
                throw new RuntimeException(this.location().toString());
            }
            else if(this.location.asTopLevelDecl().isGlobalDecl() && this.errorType() != MessageType.SCOPE_ERROR_302) {
                throw new RuntimeException(this.location().asTopLevelDecl().asGlobalDecl().var().toString());
            }
            else if(this.location.asTopLevelDecl().isFuncDecl()
                    && this.errorType() != MessageType.SCOPE_ERROR_311
                    && this.errorType() != MessageType.SCOPE_ERROR_312) {
                throw new RuntimeException(this.location().toString());
            }
        }
        else if(this.location().isStatement()) {
            if(this.location().asStatement().isLocalDecl() && this.errorType() != MessageType.SCOPE_ERROR_300) {
                throw new RuntimeException(this.location().asStatement().asLocalDecl().var().toString());
            }
        }
        
        throw new RuntimeException();
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
        String suggestMsg = PrettyPrint.RED + this.suggest.getMessage() + PrettyPrint.RESET;
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
